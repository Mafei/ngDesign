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

public class SourceFolder extends AbstractTreeElement {

	public SourceFolder(IResource resource) {
		super(resource);
	}

	private void fillPackages(List<Package> packages, IFolder resource) {
		// TODO Auto-generated method stub

	}

	public Object[] getPackages() {
		List<Package> packages = new ArrayList<>();
		fillPackages(packages, getResource());
		return packages.toArray();
	}

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

}
