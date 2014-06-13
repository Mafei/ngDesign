/*******************************************************************************
 * Copyright (c) 2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - removed unused code
 *******************************************************************************/
/*
 * Copyright (c) 2010, IETR/INSA of Rennes
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the IETR/INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.synflow.models.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

/**
 * This class contains utility methods for dealing with resources.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class OrccUtil {

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

	/**
	 * Returns the list of ALL source folders of the required projects as well as of the given
	 * project as a list of absolute workspace paths.
	 * 
	 * @param project
	 *            a project
	 * @return a list of absolute workspace paths
	 * @throws CoreException
	 */
	public static List<IFolder> getAllSourceFolders(IProject project) {
		List<IFolder> srcFolders = new ArrayList<IFolder>();

		IJavaProject javaProject = JavaCore.create(project);
		if (!javaProject.exists()) {
			return srcFolders;
		}

		// add source folders of this project
		srcFolders.addAll(getSourceFolders(project));

		// add source folders of required projects
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		try {
			for (String name : javaProject.getRequiredProjectNames()) {
				IProject refProject = root.getProject(name);
				srcFolders.addAll(getAllSourceFolders(refProject));
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return srcFolders;
	}

	/**
	 * Returns the list of source folders of the given project as a list of absolute workspace
	 * paths.
	 * 
	 * @param project
	 *            a project
	 * @return a list of absolute workspace paths
	 */
	public static List<IFolder> getSourceFolders(IProject project) {
		List<IFolder> srcFolders = new ArrayList<IFolder>();

		IJavaProject javaProject = JavaCore.create(project);
		if (!javaProject.exists()) {
			return srcFolders;
		}

		// iterate over package roots
		try {
			for (IPackageFragmentRoot root : javaProject.getPackageFragmentRoots()) {
				IResource resource = root.getCorrespondingResource();
				if (resource != null && resource.getType() == IResource.FOLDER) {
					srcFolders.add((IFolder) resource);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return srcFolders;
	}

	/**
	 * Returns a new string that is an unescaped version of the given string. Unespaced means that
	 * "\\\\", "\\n", "\\r", "\\t" are replaced by '\\', '\n', '\r', '\t' respectively.
	 * 
	 * @param string
	 *            a string
	 * @return a new string that is an unescaped version of the given string
	 */
	public static String getUnescapedString(String string) {
		StringBuilder builder = new StringBuilder(string.length());
		boolean escape = false;
		for (int i = 0; i < string.length(); i++) {
			char chr = string.charAt(i);
			if (escape) {
				switch (chr) {
				case '\\':
					builder.append('\\');
					break;
				case 'n':
					builder.append('\n');
					break;
				case 'r':
					builder.append('\r');
					break;
				case 't':
					builder.append('\t');
					break;
				default:
					// we could throw an exception here
					builder.append(chr);
					break;
				}
				escape = false;
			} else {
				if (chr == '\\') {
					escape = true;
				} else {
					builder.append(chr);
				}
			}
		}

		return builder.toString();
	}

	/**
	 * Sets the contents of the given file, creating it if it does not exist.
	 * 
	 * @param file
	 *            a file
	 * @param source
	 *            an input stream
	 * @throws CoreException
	 */
	public static void setFileContents(IFile file, InputStream source) throws CoreException {
		if (file.exists()) {
			file.setContents(source, true, false, null);
		} else {
			IContainer container = file.getParent();
			if (container.getType() == IResource.FOLDER) {
				createFolder((IFolder) container);
			}
			file.create(source, true, null);
		}
	}

}
