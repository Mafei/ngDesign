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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

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

	/**
	 * If it does not exist, creates the given folder. If the parent folders do not exist either,
	 * create them.
	 * 
	 * @param folder
	 *            a folder
	 * @throws CoreException
	 */
	public static void createFolder(IFolder folder) throws CoreException {
		IPath path = folder.getFullPath();
		if (folder.exists()) {
			return;
		}

		int n = path.segmentCount();
		if (n < 2) {
			throw new IllegalArgumentException("the path of the given folder "
					+ "must have at least two segments");
		}

		// check project
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(path.segment(0));
		if (!project.exists()) {
			project.create(null);
		}

		// open project
		if (!project.isOpen()) {
			project.open(null);
		}

		// check folder
		folder = root.getFolder(path.uptoSegment(2));
		if (!folder.exists()) {
			folder.create(true, true, null);
		}

		// and then check all the descendants
		for (int i = 2; i < n; i++) {
			folder = folder.getFolder(new Path(path.segment(i)));
			if (!folder.exists()) {
				folder.create(true, true, null);
			}
		}
	}

	private IProject project;

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
		try {
			IFile file = project.getFile(fileName);
			if (file.exists()) {
				file.setContents(source, true, true, null);
			} else {
				if (!file.getParent().exists()) {
					createFolder((IFolder) file.getParent());
				}
				file.create(source, true, null);
			}
		} catch (CoreException e) {
			SynflowCore.log(e);
		}
	}

}
