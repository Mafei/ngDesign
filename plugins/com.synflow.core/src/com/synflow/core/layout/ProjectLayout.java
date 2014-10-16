/*******************************************************************************
 * Copyright (c) 2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.core.layout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.synflow.core.SynflowCore;
import com.synflow.core.SynflowNature;

/**
 * This class defines helper methods to get the layout of a project.
 * 
 * @author Matthieu Wipliez
 *
 */
public class ProjectLayout {

	public static final String FOLDER_SRC = "src";

	/**
	 * Returns the direct children of the given project as an array containing either source folder
	 * or resources.
	 * 
	 * @param project
	 * @return an array of objects
	 */
	public static Object[] getChildren(IProject project) {
		List<Object> children = new ArrayList<>();
		try {
			for (IResource resource : project.members()) {
				ITreeElement element = getTreeElement(resource);
				if (element == null) {
					children.add(resource);
				} else {
					children.add(element);
				}
			}
		} catch (CoreException e) {
			SynflowCore.log(e);
		}
		return children.toArray();
	}

	/**
	 * Returns the source folder of the given project, or <code>null</code>.
	 * 
	 * @param project
	 *            a project
	 * @return a source folder, may be <code>null</code> if not accessible or does not exist
	 */
	public static SourceFolder getSourceFolder(IProject project) {
		try {
			if (project.isAccessible() && project.hasNature(SynflowNature.NATURE_ID)) {
				IFolder folder = project.getFolder(FOLDER_SRC);
				if (folder.exists()) {
					return new SourceFolder(folder);
				}
			}
		} catch (CoreException e) {
			SynflowCore.log(e);
		}

		return null;
	}

	/**
	 * Returns a tree element for the given resource.
	 * 
	 * @param resource
	 * @return a tree element; may be <code>null</code>
	 */
	public static ITreeElement getTreeElement(IResource resource) {
		if (resource.getType() != IResource.FOLDER) {
			return null;
		}

		IContainer folder = (IContainer) resource;
		IContainer container = folder.getParent();
		while (container != null && container.getType() != IResource.PROJECT) {
			folder = container;
			container = folder.getParent();
		}

		IProject project = (IProject) container;
		try {
			// Synflow project
			if (project.isAccessible() && project.hasNature(SynflowNature.NATURE_ID)) {
				// "src" folder
				if (folder.getName().equals(FOLDER_SRC)) {
					if (folder == resource) {
						return new SourceFolder(resource);
					} else {
						return new Package(resource);
					}
				}
			}
		} catch (CoreException e) {
			SynflowCore.log(e);
		}

		return null;
	}

	/**
	 * Returns <code>true</code> if the given resource is a package located inside a source folder.
	 * 
	 * @param resource
	 *            a resource
	 * @return a boolean indicating if the resource is a package
	 */
	public static boolean isPackage(IResource resource) {
		ITreeElement element = getTreeElement(resource);
		return element != null && element.isPackage();
	}

	/**
	 * Returns <code>true</code> if the given resource is a source folder.
	 * 
	 * @param resource
	 *            a resource
	 * @return a boolean indicating if the resource is a source folder
	 */
	public static boolean isSourceFolder(IResource resource) {
		ITreeElement element = getTreeElement(resource);
		return element != null && element.isSourceFolder();
	}

}
