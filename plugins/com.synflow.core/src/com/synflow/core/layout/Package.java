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

import static com.synflow.core.ISynflowConstants.FILE_EXT_CX;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.google.common.base.Joiner;
import com.synflow.core.SynflowCore;

/**
 * This class defines a package in the project tree.
 * 
 * @author Matthieu Wipliez
 *
 */
public class Package extends AbstractTreeElement {

	private String name;

	public Package(IResource resource) {
		super(resource);
	}

	public Object[] getFiles() {
		IFolder folder = (IFolder) getResource();
		List<IFile> files = new ArrayList<>();
		try {
			for (IResource member : folder.members()) {
				if (member.getType() == IResource.FILE) {
					files.add((IFile) member);
				}
			}
		} catch (CoreException e) {
			SynflowCore.log(e);
		}
		return files.toArray();
	}

	@Override
	public String getName() {
		if (name == null) {
			IPath path = getResource().getFullPath();
			String[] segments = path.removeFirstSegments(2).segments();
			name = Joiner.on('.').join(segments);
		}
		return name;
	}

	public SourceFolder getSourceFolder() {
		return ProjectLayout.getSourceFolder(getResource().getProject());
	}

	public boolean isEmpty() {
		for (Object obj : getFiles()) {
			IFile file = (IFile) obj;
			if (FILE_EXT_CX.equals(file.getFileExtension())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isPackage() {
		return true;
	}

	@Override
	public String toString() {
		return "package " + getName();
	}

}
