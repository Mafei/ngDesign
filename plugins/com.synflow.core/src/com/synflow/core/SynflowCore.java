/*******************************************************************************
 * Copyright (c) 2012-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.core;

import static org.eclipse.core.runtime.Platform.getPreferencesService;
import static org.eclipse.jdt.core.JavaCore.VERSION_1_7;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * This class defines the Synflow core plug-in, as well as various constants.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class SynflowCore implements BundleActivator {

	private static BundleContext context;

	// The shared instance
	private static SynflowCore plugin;

	// The plug-in ID
	public static final String PLUGIN_ID = "com.synflow.core";

	public static final String PROP_GENERATOR = PLUGIN_ID + ".generator";

	/**
	 * Returns the bundle associated with this plug-in.
	 * 
	 * @return the bundle associated with this plug-in
	 */
	public static Bundle getBundle() {
		return context.getBundle();
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static SynflowCore getDefault() {
		return plugin;
	}

	/**
	 * Returns the code generator used by the given project.
	 * 
	 * @param project
	 *            a project
	 * @return a code generator
	 */
	public static ICodeGenerator getGenerator(IProject project) {
		String name = getProjectPreferences(project).get(PROP_GENERATOR, null);
		if (name == null) {
			// no generator associated with the project
			try {
				IMarker marker = project.createMarker(IMarker.PROBLEM);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				String msg = "No generator associated with project '" + project.getName()
						+ "'. Please edit project properties.";
				marker.setAttribute(IMarker.MESSAGE, msg);
			} catch (CoreException e) {
				SynflowCore.log(e);
			}
			return null;
		}

		ICodeGenerator generator;
		try {
			generator = getDefault().getInstance(ICodeGenerator.class, name);
		} catch (ConfigurationException e) {
			generator = null;
			try {
				IMarker marker = project.createMarker(IMarker.PROBLEM);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				String msg = name + " code generator required to compile project '"
						+ project.getName() + "' is not available. "
						+ "Please edit project properties.";
				marker.setAttribute(IMarker.MESSAGE, msg);
			} catch (CoreException ex) {
				SynflowCore.log(ex);
			}
		}
		return generator;
	}

	/**
	 * Returns the list of declared generators.
	 * 
	 * @return a list of names of generators
	 */
	public static List<String> getGenerators() {
		// extensions
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = reg
				.getConfigurationElementsFor("com.synflow.core.injection");
		List<String> generators = new ArrayList<>();
		for (IConfigurationElement element : elements) {
			if ("generator".equals(element.getName())) {
				generators.add(element.getAttribute("name"));
			}
		}
		return generators;
	}

	/**
	 * Returns a package from the path of a folder.
	 * 
	 * @param path
	 *            a path
	 * @return a package
	 */
	public static String getPackage(IPath path) {
		if (path.segmentCount() > 2) {
			// remove /project/folder segments
			path = path.removeFirstSegments(2);
		}
		return path.toString().replace(IPath.SEPARATOR, '.');
	}

	/**
	 * Return the value stored in the preference store for the given key. If the key is not defined
	 * then return the specified default value.
	 * 
	 * @param key
	 *            the name of the preference
	 * @param defaultValue
	 *            the value to use if the preference is not defined
	 * @return the value of the preference or the given default value
	 */
	public static String getPreference(String key, String defaultValue) {
		return getPreferencesService().getString(PLUGIN_ID, key, defaultValue, null);
	}

	/**
	 * Returns the preferences node for the given project.
	 * 
	 * @param project
	 *            a project
	 * @return a preference node
	 */
	public static IEclipsePreferences getProjectPreferences(IProject project) {
		return new ProjectScope(project).getNode(SynflowCore.PLUGIN_ID);
	}

	/**
	 * Returns the list of projects with the Synflow nature.
	 * 
	 * @return the list of projects with the Synflow nature
	 */
	public static IProject[] getProjects() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		List<IProject> projects = new ArrayList<>();
		for (IProject project : root.getProjects()) {
			try {
				if (project.isOpen() && project.hasNature(SynflowNature.NATURE_ID)) {
					projects.add(project);
				}
			} catch (CoreException e) {
				log(e);
			}
		}

		return projects.toArray(new IProject[projects.size()]);
	}

	public static boolean isEmpty(IPackageFragment fragment) {
		try {
			return !fragment.hasChildren() && fragment.getNonJavaResources().length == 0;
		} catch (JavaModelException e) {
			SynflowCore.log(e);
			return true;
		}
	}

	/**
	 * Returns true if this plug-in is loaded.
	 * 
	 * @return true if this plug-in is loaded
	 */
	public static boolean isLoaded() {
		return plugin != null;
	}

	/**
	 * Logs an error status based on the given throwable.
	 * 
	 * @param t
	 *            a throwable
	 */
	public static void log(Throwable t) {
		if (!isLoaded()) {
			t.printStackTrace();
			return;
		}

		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, t.getMessage(), t);
		Platform.getLog(getBundle()).log(status);
	}

	/**
	 * Opens an input stream on the given file.
	 * 
	 * @param file
	 *            path of a file contained in this bundle
	 * @return an input stream
	 * @throws IOException
	 */
	public static InputStream openStream(IPath file) throws IOException {
		return FileLocator.openStream(getBundle(), file, false);
	}

	/**
	 * Sets the value of the given key in the preference store.
	 * 
	 * @param key
	 *            key with which the specified value is to be associated
	 * @param value
	 *            value to be associated with the specified key
	 */
	public static void setPreference(String key, String value) {
		getPreferencesService().getRootNode().node(PLUGIN_ID).put(key, value);
	}

	public static IStatus validateIdentifier(String id) {
		IStatus status = JavaConventions.validateIdentifier(id, VERSION_1_7, VERSION_1_7);
		if (!status.isOK()) {
			String message = "'" + id + "' is not a valid identifier";
			status = new Status(IStatus.ERROR, PLUGIN_ID, message);
		}
		return status;
	}

	private Injector injector;

	/**
	 * Returns the appropriate instance for the given injection type.
	 * 
	 * @param type
	 *            injection type
	 * @return an instance of type T
	 */
	public <T> T getInstance(Class<T> type) {
		return injector.getInstance(type);
	}

	/**
	 * Returns the appropriate instance for the given injection type, with a Named annotation (name
	 * is given).
	 * 
	 * @param type
	 *            injection type
	 * @param name
	 *            annotated name
	 * @return an instance of type T
	 */
	public <T> T getInstance(Class<T> type, String name) {
		Named annotation = Names.named(name);
		Key<T> key = Key.get(type, annotation);
		return injector.getInstance(key);
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		SynflowCore.context = bundleContext;
		SynflowCore.plugin = this;

		injector = Guice.createInjector(new SynflowModule());
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		injector = null;
		SynflowCore.context = null;
		SynflowCore.plugin = null;
	}

}
