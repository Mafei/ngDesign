/*******************************************************************************
 * Copyright (c) 2012-2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.core.util;

import static com.synflow.core.IProperties.IMPL_EXTERNAL;
import static com.synflow.core.IProperties.PROP_DEPENDENCIES;
import static com.synflow.core.IProperties.PROP_FILE;
import static com.synflow.core.IProperties.PROP_IMPLEMENTATION;
import static com.synflow.core.IProperties.PROP_TYPE;
import static com.synflow.core.ISynflowConstants.FILE_EXT_IR;
import static com.synflow.core.ISynflowConstants.FOLDER_IR;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synflow.core.SynflowCore;
import com.synflow.models.dpn.Entity;
import com.synflow.models.ir.util.IrUtil;

/**
 * This class defines utility methods to find .cf/.ir files from qualified class names (as well as
 * the other way around).
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CoreUtil {

	/**
	 * Returns the file with the given attributes in the given project, and if no such file is
	 * found, look into its dependencies.
	 * 
	 * @param project
	 *            a project
	 * @param folder
	 *            folder in which to look for
	 * @param className
	 *            class name of the file this method will look for
	 * @param fileExt
	 *            file extension
	 * @return an {@link IFile}
	 */
	private static IFile findFile(IProject project, String folder, String className, String fileExt) {
		// list of projects, starting with this project
		Collection<IProject> projects = getBuildPath(project);

		// go through list of projects
		IFile file = null;
		for (IProject prj : projects) {
			file = getFile(prj, folder, className, fileExt);
			if (file.exists()) {
				return file;
			}
		}

		return file;
	}

	/**
	 * Finds the .ir file in the given project (or its required projects) with the given class name.
	 * 
	 * @param project
	 *            a project
	 * @param className
	 *            class name of the .ir file this method will look for
	 * @return an {@link IFile}
	 */
	public static IFile findIrFile(IProject project, String className) {
		return findFile(project, FOLDER_IR, className, FILE_EXT_IR);
	}

	/**
	 * Returns the list of projects that are in the build path of this project (includes this
	 * project).
	 * 
	 * @param project
	 *            a project
	 * @return list of projects
	 */
	public static Collection<IProject> getBuildPath(IProject project) {
		Set<IProject> projects = new LinkedHashSet<>();
		getBuildPath(projects, project);
		return projects;
	}

	private static void getBuildPath(Set<IProject> projects, IProject project) {
		projects.add(project);

		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject.exists()) {
			try {
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				String[] projectNames = javaProject.getRequiredProjectNames();
				for (int i = 0; i < projectNames.length; i++) {
					IProject requiredProject = root.getProject(projectNames[i]);
					getBuildPath(projects, requiredProject);
				}
			} catch (JavaModelException e) {
				SynflowCore.log(e);
			}
		}
	}

	/**
	 * Returns the dependencies of the given entity.
	 * 
	 * @param entity
	 *            an entity
	 * @return a JSON array, empty if the entity has no dependencies
	 */
	private static JsonArray getDependencies(JsonObject implementation) {
		if (implementation != null) {
			JsonElement dependencies = implementation.get(PROP_DEPENDENCIES);
			if (dependencies != null && dependencies.isJsonArray()) {
				return dependencies.getAsJsonArray();
			}
		}
		return new JsonArray();
	}

	/**
	 * Returns the file in the given project, folder, with the given class name and file extension.
	 * Does not check for existence of the given file.
	 * 
	 * @param project
	 *            a project
	 * @param folder
	 *            folder in which to look for
	 * @param className
	 *            class name of the file this method will look for
	 * @param fileExt
	 *            file extension
	 * @return an {@link IFile}
	 */
	private static IFile getFile(IProject project, String folder, String className, String fileExt) {
		String name = IrUtil.getFile(className);
		String fileName = folder + "/" + name + "." + fileExt;
		return project.getFile(fileName);
	}

	/**
	 * Returns a list of file list for the given entity. If the entity has a built-in or external
	 * implementation, the list begins with dependencies.
	 * 
	 * @param entity
	 * @return
	 */
	public static Iterable<String> getFileList(Entity entity) {
		JsonObject implementation = CoreUtil.getImplementation(entity);
		Iterable<String> deps = Iterables.transform(getDependencies(implementation),
				new Function<JsonElement, String>() {
					@Override
					public String apply(JsonElement dependency) {
						return dependency.getAsString();
					}
				});

		if (implementation != null && IMPL_EXTERNAL.equals(implementation.get(PROP_TYPE))) {
			// external implementation
			String file = implementation.get(PROP_FILE).getAsString();
			return Iterables.concat(deps, Arrays.asList(file));
		}

		String name = entity.getName();
		return Iterables.concat(deps, ImmutableSet.of(name));
	}

	/**
	 * Returns the value of the 'implementation' property of the given entity as a JSON object.
	 * 
	 * @param entity
	 *            an entity
	 * @return a JSON object, or <code>null</code> if the entity has no implementation property
	 */
	public static JsonObject getImplementation(Entity entity) {
		JsonElement implementation = entity.getProperties().get(PROP_IMPLEMENTATION);
		if (implementation != null && implementation.isJsonObject()) {
			return implementation.getAsJsonObject();
		}
		return null;
	}

	/**
	 * Returns the .ir file in the given project with the given class name. Does not check for
	 * existence of the given file.
	 * 
	 * @param project
	 *            a project
	 * @param className
	 *            class name of the .ir file this method will look for
	 * @return an {@link IFile}
	 */
	public static IFile getIrFile(IProject project, String className) {
		return getFile(project, FOLDER_IR, className, FILE_EXT_IR);
	}

	/**
	 * Returns a new path that is built from <code>path</code>, except that it is relative to
	 * <code>root</code>. The <code>root</code> path must be a subset of <code>path</code>.
	 * 
	 * @param root
	 *            root path
	 * @param path
	 *            file path
	 * @return a new path
	 */
	public static IPath getRelative(IPath root, IPath path) {
		int count = path.matchingFirstSegments(root);
		return path.removeFirstSegments(count);
	}

	/**
	 * Returns the relative path from <code>source</code> to <code>target</code> .
	 * 
	 * @param source
	 *            source path
	 * @param target
	 *            target path
	 * @return a string that represents the relative path from source to target
	 */
	public static String getRelative(IResource source, IResource target) {
		Path from = Paths.get(source.getLocation().toString());
		Path to = Paths.get(target.getLocation().toString());
		String path = from.relativize(to).toString();
		// make path portable so it can be used anywhere
		return new org.eclipse.core.runtime.Path(path).toString();
	}

	public static boolean isExternal(Entity entity) {
		JsonObject implementation = getImplementation(entity);
		return implementation != null && IMPL_EXTERNAL.equals(implementation.get(PROP_TYPE));
	}

	/**
	 * Loads the entity with the given class name, using the base entity's resource to resolve URI.
	 * 
	 * @param base
	 *            a base entity
	 * @param className
	 *            class name of the entity to load
	 * @return an entity
	 */
	public static Entity loadEntity(Entity base, String className) {
		Resource resource = base.eResource();
		URI uri = resource.getURI();

		int count = base.getName().split("\\.").length;
		URI derivedUri = uri.trimSegments(count).appendSegments(className.split("\\."));
		URI entityUri = derivedUri.appendFileExtension(FILE_EXT_IR).appendFragment("/");

		return (Entity) resource.getResourceSet().getEObject(entityUri, true);
	}

	/**
	 * Serializes the EObject in the given file.
	 * 
	 * @param set
	 *            resource set
	 * @param file
	 *            an IFile
	 * @param eObject
	 *            EMF object to serialize
	 * @return the EMF resource in which the object was added
	 */
	public static Resource serialize(ResourceSet set, IFile file, EObject eObject) {
		// computes URI
		String pathName = file.getFullPath().toString();
		URI uri = URI.createPlatformResourceURI(pathName, true);

		// serializes the EMF object
		Resource resource = set.createResource(uri);
		resource.getContents().add(eObject);
		try {
			resource.save(null);
		} catch (IOException e) {
		}

		// register resource in resource map
		if (set instanceof ResourceSetImpl) {
			((ResourceSetImpl) set).getURIResourceMap().put(uri, resource);
		}

		return resource;
	}

}
