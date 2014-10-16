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
package com.synflow.ngDesign.ui.internal.wizards;

import static com.synflow.core.ISynflowConstants.FILE_EXT_CX;
import static com.synflow.core.ISynflowConstants.FOLDER_IR;
import static com.synflow.core.ISynflowConstants.FOLDER_SIM;
import static com.synflow.core.ISynflowConstants.FOLDER_TESTBENCH;
import static org.eclipse.jdt.core.JavaCore.newSourceEntry;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.xtext.ui.XtextProjectHelper;

import com.synflow.core.SynflowNature;

/**
 * This class allows the creation of a Synflow project.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ProjectCreator {

	private static final IPath[] CP_PATTERNS = { new Path("**/*." + FILE_EXT_CX) };

	private static final String[] IGNORES = new String[] { FOLDER_IR, FOLDER_SIM, FOLDER_TESTBENCH,
			"verilog-gen", "vhdl-gen" };

	/**
	 * Synflow must be first for icon to appear correctly
	 */
	private static final String[] NATURES = { SynflowNature.NATURE_ID, JavaCore.NATURE_ID,
			XtextProjectHelper.NATURE_ID };

	/**
	 * Creates the .gitignore file.
	 * 
	 * @param project
	 *            the project being created
	 * @throws CoreException
	 */
	private void addGitIgnore(IProject project) throws CoreException {
		IFile file = project.getFile(".gitignore");
		if (file.exists()) {
			return;
		}

		StringBuilder builder = new StringBuilder();
		for (String dir : IGNORES) {
			builder.append('/');
			builder.append(dir);
			builder.append('\n');
		}

		byte[] bytes = builder.toString().getBytes();
		InputStream source = new ByteArrayInputStream(bytes);
		file.create(source, true, null);
	}

	/**
	 * Creates the project, adds natures and builders.
	 * 
	 * @param project
	 *            project
	 * @param description
	 *            project description
	 * @throws CoreException
	 */
	private void configureDescription(IProject project, IProjectDescription description)
			throws CoreException {
		// create and open project
		project.create(description, null);
		project.open(null);

		// retrieve existing description (if any)
		description = project.getDescription();

		// set natures, and updates project description
		description.setNatureIds(NATURES);
		project.setDescription(description, null);

		// create "src" folder if it does not exist
		IFolder src = project.getFolder("src");
		if (!src.exists()) {
			src.create(true, true, null);
		}

		// add "src" source entry
		// default JRE
		IClasspathEntry[] jre = { JavaRuntime.getDefaultJREContainerEntry() };
		int n = jre.length;
		IClasspathEntry[] entries = Arrays.copyOf(jre, n + 1);
		entries[n] = newSourceEntry(src.getFullPath(), CP_PATTERNS);

		// create Java project and set entries
		IJavaProject javaProject = JavaCore.create(project);
		javaProject.setRawClasspath(entries, null);
	}

	/**
	 * Creates a new project at the given location from the given project handle.
	 * 
	 * @param handle
	 *            a project handle
	 * @param location
	 *            an URI (may be <code>null</code>)
	 * @throws CoreException
	 *             if something goes wrong
	 */
	public void createProject(IProject handle, URI location) throws CoreException {
		// create description
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProjectDescription description = workspace.newProjectDescription(handle.getName());
		description.setLocationURI(location);

		configureDescription(handle, description);
		addGitIgnore(handle);
	}

}
