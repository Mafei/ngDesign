/*******************************************************************************
 * Copyright (c) 2012 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.ngDesign.ui.internal.launching;

import static com.synflow.core.ISynflowConstants.FILE_EXT_IR;
import static com.synflow.core.ISynflowConstants.FOLDER_IR;
import static com.synflow.core.ISynflowConstants.FOLDER_JAVA_GEN;
import static com.synflow.core.ISynflowConstants.FOLDER_SIM;
import static com.synflow.ngDesign.ui.internal.launching.ILaunchConfigurationConstants.CHECK_PORTS;
import static com.synflow.ngDesign.ui.internal.launching.ILaunchConfigurationConstants.CLASS;
import static com.synflow.ngDesign.ui.internal.launching.ILaunchConfigurationConstants.CREATE_VCD;
import static com.synflow.ngDesign.ui.internal.launching.ILaunchConfigurationConstants.DEFAULT_ASSERT;
import static com.synflow.ngDesign.ui.internal.launching.ILaunchConfigurationConstants.DEFAULT_CHECK_PORTS;
import static com.synflow.ngDesign.ui.internal.launching.ILaunchConfigurationConstants.DEFAULT_CREATE_VCD;
import static com.synflow.ngDesign.ui.internal.launching.ILaunchConfigurationConstants.DEFAULT_NUM_CYCLES;
import static com.synflow.ngDesign.ui.internal.launching.ILaunchConfigurationConstants.DEFAULT_PRINT_CYCLES;
import static com.synflow.ngDesign.ui.internal.launching.ILaunchConfigurationConstants.ENABLE_ASSERTIONS;
import static com.synflow.ngDesign.ui.internal.launching.ILaunchConfigurationConstants.NUM_CYCLES;
import static com.synflow.ngDesign.ui.internal.launching.ILaunchConfigurationConstants.PRINT_CYCLES;
import static com.synflow.ngDesign.ui.internal.launching.ILaunchConfigurationConstants.PROJECT;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.ui.PlatformUI;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.synflow.core.ICodeGenerator;
import com.synflow.core.IFileWriter;
import com.synflow.core.SynflowCore;
import com.synflow.core.util.CoreUtil;
import com.synflow.models.dpn.Entity;
import com.synflow.models.ir.util.IrUtil;

/**
 * This class defines a delegate for simulation.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class LaunchDelegateSimulation extends LaunchConfigurationDelegate {

	private static class CachedResourceVisitor extends ModelResourceVisitor {

		public CachedResourceVisitor(ResourceSet set) {
			super(set, FILE_EXT_IR);
		}

		@Override
		protected boolean shouldSkipFile(IFile irFile) {
			// timestamp of .ir file
			long tsIr = irFile.getLocalTimeStamp();

			// timestamp of .java file
			String name = "TODO"; //TODO
			String fileName = IrUtil.getFile(name) + ".java";
			IPath path = new Path(FOLDER_JAVA_GEN).append(fileName);
			IFile javaFile = irFile.getProject().getFile(path);
			long tsJava = javaFile.getLocalTimeStamp();

			// skips if .java is more recent than .ir/.xdf
			return tsIr <= tsJava;
		}

	}

	/**
	 * This class handles debug events, and when the JDT launch terminates, it terminates the dummy
	 * process and removes the JDT configuration.
	 * 
	 * @author Matthieu Wipliez
	 * 
	 */
	private static class Cleaner implements IDebugEventSetListener {

		private ILaunchConfiguration configuration;

		private IProcess process;

		public Cleaner(IProcess process, ILaunchConfiguration configuration) {
			this.process = process;
			this.configuration = configuration;
		}

		@Override
		public void handleDebugEvents(DebugEvent[] events) {
			for (DebugEvent event : events) {
				if (event.getKind() == DebugEvent.TERMINATE) {
					if (!process.isTerminated()) {
						try {
							process.terminate();
							configuration.delete();
						} catch (CoreException e) {
							SynflowCore.log(e);
						}

						// remove ourselves as a listener
						DebugPlugin.getDefault().removeDebugEventListener(this);
					}
				}
			}
		}

	}

	@Inject
	@Named("Java")
	private ICodeGenerator generator;

	@Inject
	@Named("Eclipse")
	private IFileWriter writer;

	/**
	 * Generate code for the given objects.
	 * 
	 * @param eObjects
	 * @param monitor
	 */
	private void generateCode(Map<IProject, List<Entity>> map, IProgressMonitor monitor) {
		for (Entry<IProject, List<Entity>> entry : map.entrySet()) {
			IProject project = entry.getKey();
			writer.setOutputFolder(project.getName());

			List<Entity> entities = entry.getValue();
			for (Entity entity : entities) {
				if (monitor.isCanceled()) {
					break;
				}

				monitor.subTask("Building " + entity.getName());
				try {
					generator.transform(entity);
					generator.print(entity);
				} catch (Exception e) {
					SynflowCore.log(e);
				}
				monitor.worked(1);
			}
		}
	}

	/**
	 * Returns the Java configuration for the given Cx simulation launch configuration.
	 * 
	 * @param config
	 *            launch configuration for a Cx simulation
	 * @return a Java launch configuration
	 * @throws CoreException
	 */
	private ILaunchConfiguration getConfiguration(ILaunchConfiguration config) throws CoreException {
		String projectName = config.getAttribute(PROJECT, "");
		String className = config.getAttribute(CLASS, "");

		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(ID_JAVA_APPLICATION);

		// create working copy
		ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null,
				IrUtil.getSimpleName(className));

		// set mandatory attributes
		workingCopy.setAttribute(ATTR_PROJECT_NAME, projectName);
		workingCopy.setAttribute(ATTR_MAIN_TYPE_NAME, className);

		// set working directory
		String dir = "${workspace_loc:" + projectName + "/sim}";
		workingCopy.setAttribute(ATTR_WORKING_DIRECTORY, dir);

		// enable assertions
		if (config.getAttribute(ENABLE_ASSERTIONS, DEFAULT_ASSERT)) {
			workingCopy.setAttribute(ATTR_VM_ARGUMENTS, "-ea");
		}

		// set main arguments
		StringBuilder builder = new StringBuilder("-n ");
		builder.append(config.getAttribute(NUM_CYCLES, DEFAULT_NUM_CYCLES));
		if (config.getAttribute(CHECK_PORTS, DEFAULT_CHECK_PORTS)) {
			builder.append(" -check-ports");
		}
		if (config.getAttribute(CREATE_VCD, DEFAULT_CREATE_VCD)) {
			builder.append(" -create-vcd");
		}
		if (config.getAttribute(PRINT_CYCLES, DEFAULT_PRINT_CYCLES)) {
			builder.append(" -print-cycles");
		}
		workingCopy.setAttribute(ATTR_PROGRAM_ARGUMENTS, builder.toString());

		return workingCopy.doSave();
	}

	@Override
	public void launch(ILaunchConfiguration configuration, final String mode, ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		// attach process just to show when launch is terminated
		DummyProcess process = new DummyProcess(launch, configuration, monitor);
		launch.addProcess(process);

		// initializes writer
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		String projectName = configuration.getAttribute(PROJECT, "");

		// find files
		IProject project = root.getProject(projectName);
		IFolder folder = project.getFolder(FOLDER_SIM);
		if (!folder.exists()) {
			folder.create(true, true, null);
		}

		// initializes generator, load files and generate code
		generator.initialize(writer);
		Map<IProject, List<Entity>> eObjects = loadObjects(project);
		generateCode(eObjects, monitor);

		// retrieve configuration
		final ILaunchConfiguration config = getConfiguration(configuration);

		// add listener to automatically clean up when launch terminates
		DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.addDebugEventListener(new Cleaner(process, config));
		}

		// launches Java
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				DebugUITools.launch(config, mode);
			}

		});
	}

	/**
	 * Loads all .ir and .xdf files in the given project.
	 * 
	 * @param project
	 *            a project
	 * @return a list of EObject
	 * @throws CoreException
	 */
	private Map<IProject, List<Entity>> loadObjects(IProject project) throws CoreException {
		Map<IProject, List<Entity>> map = new HashMap<>();
		ResourceSetImpl set = new ResourceSetImpl();
		set.setURIResourceMap(new HashMap<URI, Resource>());

		for (IProject proj : CoreUtil.getBuildPath(project)) {
			CachedResourceVisitor visitor = new CachedResourceVisitor(set);
			proj.getFolder(FOLDER_IR).accept(visitor);
			map.put(proj, visitor.getEObjects());
		}

		return map;
	}

}
