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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.synflow.core.SynflowCore;

/**
 * 
 * This class defines a source folder in the project tree.
 * 
 * @author Matthieu Wipliez
 *
 */
public class SourceFolder extends AbstractTreeElement {

	public SourceFolder(IResource resource) {
		super(resource);
	}

	/**
	 * Fills the given packages list from children of the given folder.
	 * 
	 * @param packages
	 *            a list of packages
	 * @param folder
	 *            a folder
	 */
	private void fillPackages(List<Package> packages, IFolder folder) {
		try {
			for (IResource member : folder.members()) {
				if (member.getType() == IResource.FOLDER) {
					packages.add(new Package(member));
					fillPackages(packages, (IFolder) member);
				}
			}
		} catch (CoreException e) {
			SynflowCore.log(e);
		}
	}

	@Override
	public String getName() {
		return getResource().getName();
	}

	/**
	 * Computes and returns an array with all packages contained in this source folder.
	 * 
	 * @return an array of Packages
	 */
	public Object[] getPackages() {
		List<Package> packages = new ArrayList<>();
		fillPackages(packages, getResource());
		return packages.toArray();
	}

	/**
	 * Returns the project in which this source folder is contained.
	 * 
	 * @return a project
	 */
	public IProject getProject() {
		return getResource().getProject();
	}

	@Override
	public IFolder getResource() {
		return (IFolder) super.getResource();
	}

	@Override
	public boolean isSourceFolder() {
		return true;
	}

	@Override
	public String toString() {
		return "source folder \"" + getName() + "\"";
	}

}
