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
package com.synflow.core.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.synflow.core.IFileWriter;
import com.synflow.core.SynflowCore;

/**
 * This class defines an implementation of a IFileWriter based on the Eclipse IFile class. The name
 * of the file must be relative to a project.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class EclipseFileWriter implements IFileWriter {

	private IProject project;

	/**
	 * Taken from org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2, (c) 2011 itemis AG
	 * 
	 * @param container
	 * @throws CoreException
	 */
	private void ensureExists(IContainer container) throws CoreException {
		if (container.exists()) {
			return;
		} else if (container instanceof IFolder) {
			ensureExists(container.getParent());
			((IFolder) container).create(true, true, null);
		}
	}

	@Override
	public boolean exists(String fileName) {
		IFile file = project.getFile(fileName);
		return file.exists();
	}

	@Override
	public void remove(String fileName) {
		IFile file = project.getFile(fileName);
		try {
			file.delete(true, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setOutputFolder(String projectName) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		project = root.getProject(projectName);
	}

	@Override
	public void write(String fileName, CharSequence sequence) {
		String contents = sequence.toString();
		InputStream source = new ByteArrayInputStream(contents.getBytes());
		write(fileName, source);
	}

	@Override
	public void write(String fileName, InputStream source) {
		IFile file = project.getFile(fileName);
		try {
			IFileStore store = EFS.getStore(file.getLocationURI());
			String localName = store.fetchInfo().getName();
			if (!file.getName().equals(localName)) {
				// case difference!
				file = project.getFile(localName);
			}

			if (file.exists()) {
				file.setContents(source, true, true, null);
			} else {
				ensureExists(file.getParent());
				file.create(source, true, null);
			}
		} catch (CoreException e) {
			SynflowCore.log(e);
		}
	}

}
