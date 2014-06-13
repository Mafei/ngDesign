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
package com.synflow.core.internal.builder;

import static com.synflow.core.ISynflowConstants.FOLDER_IR;
import static com.synflow.core.ISynflowConstants.FOLDER_JAVA_GEN;
import static com.synflow.core.ISynflowConstants.FOLDER_TESTBENCH;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import com.synflow.core.BuildJob;
import com.synflow.core.ICodeGenerator;
import com.synflow.core.IFileWriter;
import com.synflow.core.SynflowCore;
import com.synflow.core.internal.EclipseFileWriter;
import com.synflow.models.dpn.Entity;
import com.synflow.models.util.EcoreHelper;

/**
 * This class defines the Synflow builder.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class SynflowBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "com.synflow.core.builder";

	private ResourceSet set;

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) {
		IProject project = getProject();

		// check or create folders
		boolean needRebuild = makeSureFolderExists(project, FOLDER_JAVA_GEN);
		if (needRebuild) {
			// schedule a build job
			Job buildJob = new BuildJob(project);
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			buildJob.setRule(workspace.getRuleFactory().buildRule());
			buildJob.schedule();
		}

		// get generator for the project
		ICodeGenerator generator = SynflowCore.getGenerator(project);
		if (generator == null) {
			return null;
		}

		// initialize
		FileResourceVisitor visitor = new FileResourceVisitor();
		set = new ResourceSetImpl();
		((ResourceSetImpl) set).setURIResourceMap(new HashMap<URI, Resource>());

		// visit project to collect resources
		collectResources(kind, visitor);

		// creates writer and initialize generator with it
		IFileWriter writer = new EclipseFileWriter();
		writer.setOutputFolder(project.getName());
		generator.initialize(writer);

		// load all derived files (actors and networks)
		List<Entity> entities = new ArrayList<>();
		List<IFile> derived = visitor.getDerived();
		for (IFile file : derived) {
			Entity entity = EcoreHelper.getEObject(set, file);
			if (entity != null) {
				entities.add(entity);
			}
		}

		// either Verilog or VHDL
		List<IFile> removed = visitor.getRemoved();
		generateCode(generator, entities, removed, monitor);

		return null;
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		// clean up build state
		final IProject project = getProject();

		// delete files in .ir folder
		SubMonitor subMonitor = SubMonitor.convert(monitor, 6);
		deleteFiles(project, FOLDER_IR, subMonitor.newChild(1));

		// clean up "-gen" folders
		deleteFiles(project, FOLDER_JAVA_GEN, subMonitor.newChild(1));
		deleteFiles(project, "verilog-gen", subMonitor.newChild(1));
		deleteFiles(project, "vhdl-gen", subMonitor.newChild(1));
		deleteFiles(project, FOLDER_TESTBENCH, subMonitor.newChild(1));
	}

	/**
	 * Visits the current project or delta with the given visitor to collect resources to build.
	 * 
	 * @param kind
	 * @param visitor
	 */
	private void collectResources(int kind, FileResourceVisitor visitor) {
		IProject project = getProject();
		try {
			switch (kind) {
			case FULL_BUILD:
				project.accept(visitor);
				break;

			case AUTO_BUILD:
			case INCREMENTAL_BUILD:
				IResourceDelta delta = getDelta(getProject());
				if (delta != null) {
					delta.accept(visitor);
				}
				break;
			}
		} catch (CoreException e) {
			SynflowCore.log(e);
		}
	}

	/**
	 * Removes all the files in the folder with the given name in the given project.
	 * 
	 * @param project
	 *            a project
	 * @param name
	 *            name of the folder to clean
	 */
	private void deleteFiles(IProject project, String name, IProgressMonitor monitor) {
		IFolder folder = project.getFolder(new Path(name));
		if (!folder.exists()) {
			return;
		}

		try {
			// first refresh so that everything can be removed by delete
			folder.refreshLocal(IResource.DEPTH_INFINITE, null);

			// find members and delete them
			IResource[] members = folder.members();
			SubMonitor subMonitor = SubMonitor.convert(monitor, members.length);
			for (IResource member : members) {
				member.delete(true, subMonitor);
			}
		} catch (CoreException e) {
			SynflowCore.log(e);
		}
	}

	/**
	 * Uses the given generator to generate code for each file in the given list.
	 * 
	 * @param generator
	 *            a generator
	 * @param entities
	 *            a list of entities
	 * @param monitor
	 *            a monitor
	 */
	private void generateCode(ICodeGenerator generator, List<Entity> entities, List<IFile> removed,
			IProgressMonitor monitor) {
		// removes previous files
		for (@SuppressWarnings("unused")
		IFile file : removed) {
			String qName = "TODO"; // TODO
			if (qName != null) {
				generator.remove(qName);
			}
		}

		final String taskName = "Generating " + generator.getName() + " ";
		final int size = entities.size();
		SubMonitor subMonitor = SubMonitor.convert(monitor, taskName, size);

		int i = 1;
		for (Entity entity : entities) {
			if (subMonitor.isCanceled()) {
				break;
			}

			String name = entity.getName();
			subMonitor.subTask(taskName + " for " + name + " (" + i + " of " + size + ")");
			subMonitor.newChild(1);
			try {
				generator.transform(entity);
				generator.print(entity);
			} catch (Exception e) {
				SynflowCore.log(e);
			}
			i++;
		}

		subMonitor.done();
	}

	/**
	 * Checks if the folder with the given name exists. If it does not, creates it.
	 * 
	 * @param project
	 *            project
	 * @param name
	 *            folder name
	 * @return true if the folder did not exist and the project needs to be rebuilt
	 */
	private boolean makeSureFolderExists(IProject project, String name) {
		IFolder folder = project.getFolder(name);
		if (!folder.exists()) {
			try {
				folder.create(true, true, null);
				return true;
			} catch (CoreException e) {
				SynflowCore.log(e);
			}
		}
		return false;
	}

}
