/*******************************************************************************
 * Copyright (c) 2012 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.ngDesign.ui.internal;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.common.collect.Lists;
import com.synflow.core.SynflowCore;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class NgDesignUi extends AbstractUIPlugin {

	public static final String COMMAND_TYPE_EXPORT_TYPE = "com.synflow.ngDesign.ui.exportType";

	// The shared instance
	private static NgDesignUi plugin;

	// The plug-in WIZARD_ID
	public static final String PLUGIN_ID = "com.synflow.ngDesign.ui"; //$NON-NLS-1$

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static NgDesignUi getDefault() {
		return plugin;
	}

	/**
	 * Creates and returns a new image descriptor for an image file located within this plug-in.
	 * 
	 * @param path
	 *            the relative path of the image file, relative to the root of the plug-in; the path
	 *            must be legal
	 * @return an image descriptor, or <code>null</code> if no image could be found
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * The constructor
	 */
	public NgDesignUi() {
	}

	/**
	 * Removes Java builder manually, because if JDT is not available then the nature is not
	 * deconfigured.
	 * 
	 * @param description
	 */
	private void removeJavaBuilder(IProjectDescription description) {
		List<ICommand> commands = Lists.newArrayList(description.getBuildSpec());
		Iterator<ICommand> it = commands.iterator();
		while (it.hasNext()) {
			String name = it.next().getBuilderName();
			if ("org.eclipse.jdt.core.javabuilder".equals(name)) {
				it.remove();
			}
		}

		description.setBuildSpec(commands.toArray(new ICommand[commands.size()]));
	}

	/**
	 * Renames all files with the .cf extension to .cx.
	 * 
	 * @param container
	 */
	private void renameCfFiles(IContainer container) {
		try {
			for (IResource member : container.members()) {
				if (member.getType() == IResource.FILE) {
					if ("cf".equals(member.getFileExtension())) {
						IPath path = member.getFullPath();
						IPath destination = path.removeFileExtension().addFileExtension("cx");
						member.move(destination, true, null);
					}
				} else if (member.getType() == IResource.FOLDER) {
					renameCfFiles((IFolder) member);
				}
			}
		} catch (CoreException e) {
			SynflowCore.log(e);
		}
	}

	private void setDefaultTheme(BundleContext context) {
		// set default theme for Windows 8 (otherwise the product looks ugly)
		ServiceReference<IThemeManager> reference = null;
		try {
			reference = context.getServiceReference(IThemeManager.class);
			if (reference == null) {
				return;
			}

			IThemeManager manager = context.getService(reference);
			if (manager == null) {
				return;
			}

			Display display = PlatformUI.getWorkbench().getDisplay();
			IThemeEngine engine = manager.getEngineForDisplay(display);
			ITheme themeActive = engine.getActiveTheme();
			if (themeActive == null) {
				if (Platform.OS_WIN32.equals(Platform.getOS())) {
					final String versionWin7 = "6.1";
					String osVersion = System.getProperty("os.version");
					if (osVersion != null && osVersion.compareTo(versionWin7) > 0) {
						// if OS is more recent than Windows 7, make theme API use Windows 7 theme
						System.setProperty("os.version", versionWin7);
					}

					// set default theme
					engine.setTheme("org.eclipse.e4.ui.css.theme.e4_default", true);
				}
			}
		} catch (RuntimeException e) {
			// ignore
		} finally {
			if (reference != null) {
				context.ungetService(reference);
			}
		}
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		updateProjects();
		setDefaultTheme(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	private void updateProjects() {
		for (IProject project : SynflowCore.getProjects()) {
			try {
				IProjectDescription description = project.getDescription();
				final String javaNature = "org.eclipse.jdt.core.javanature";
				if (description.hasNature(javaNature)) {
					renameCfFiles(project);
					removeJavaBuilder(description);

					// remove Java nature
					List<String> natures = Lists.newArrayList(description.getNatureIds());
					natures.remove(javaNature);
					description.setNatureIds(natures.toArray(new String[natures.size()]));

					// set description
					project.setDescription(description, null);

					// remove .classpath
					project.getFile(".classpath").delete(true, null);
				}
			} catch (CoreException e) {
				SynflowCore.log(e);
			}
		}
	}

}
