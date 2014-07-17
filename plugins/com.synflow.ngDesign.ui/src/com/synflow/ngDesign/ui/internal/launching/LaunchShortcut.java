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

import static com.synflow.ngDesign.ui.internal.launching.ILaunchConfigurationConstants.CLASS;
import static com.synflow.ngDesign.ui.internal.launching.ILaunchConfigurationConstants.PROJECT;
import static com.synflow.ngDesign.ui.internal.launching.ILaunchConfigurationConstants.TYPE_SIMULATION;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import com.synflow.core.SynflowCore;
import com.synflow.models.ir.util.IrUtil;

/**
 * This class defines a launch shortcut for C~ Simulation.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class LaunchShortcut implements ILaunchShortcut2 {

	private void chooseAndLaunch(IFile file, ILaunchConfiguration[] configs,
			String mode) {
		ILaunchConfiguration config = null;
		if (configs.length == 0) {
			config = createConfiguration(file);
		} else if (configs.length == 1) {
			config = configs[0];
		} else {
			return;
		}

		try {
			config.launch(mode, null);
		} catch (CoreException e) {
			SynflowCore.log(e);
		}
	}

	private ILaunchConfiguration createConfiguration(IFile file) {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		String id = TYPE_SIMULATION;
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(id);

		try {
			// generate configuration name
			String className = "TODO"; //TODO
			String name = IrUtil.getSimpleName(className) + " - Simulation";
			name = manager.generateLaunchConfigurationName(name);

			// create configuration
			ILaunchConfigurationWorkingCopy wc = type.newInstance(null, name);
			wc.setAttribute(PROJECT, file.getProject().getName());

			// qualified name of object
			wc.setAttribute(CLASS, className);

			return wc.doSave();
		} catch (CoreException e) {
			SynflowCore.log(e);
			return null;
		}
	}

	private ILaunchConfiguration[] getConfigurations(IFile file) {
		IProject project = file.getProject();

		String id = TYPE_SIMULATION;
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(id);
		try {
			// configurations that match the given resource
			List<ILaunchConfiguration> configs = new ArrayList<ILaunchConfiguration>();

			// candidates
			ILaunchConfiguration[] candidates = manager
					.getLaunchConfigurations(type);
			String qualifiedName = "TODO"; // TODO
			for (ILaunchConfiguration config : candidates) {
				String projectName = config.getAttribute(PROJECT, "");
				String className = config.getAttribute(CLASS, "");
				if (projectName.equals(project.getName())
						&& className.equals(qualifiedName)) {
					configs.add(config);
				}
			}

			return configs.toArray(new ILaunchConfiguration[] {});
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public IResource getLaunchableResource(IEditorPart editorpart) {
		IEditorInput input = editorpart.getEditorInput();
		if (input instanceof IFileEditorInput) {
			return ((IFileEditorInput) input).getFile();
		}

		return null;
	}

	@Override
	public IResource getLaunchableResource(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IFile) {
				return (IFile) obj;
			}
		}

		return null;
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		IResource resource = getLaunchableResource(editorpart);
		if (resource instanceof IFile) {
			return getConfigurations((IFile) resource);
		}

		return null;
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		IResource resource = getLaunchableResource(selection);
		if (resource instanceof IFile) {
			return getConfigurations((IFile) resource);
		}

		return null;
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		IResource resource = getLaunchableResource(editor);
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			chooseAndLaunch(file, getConfigurations(file), mode);
		}
	}

	@Override
	public void launch(ISelection selection, String mode) {
		IResource resource = getLaunchableResource(selection);
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			chooseAndLaunch(file, getConfigurations(file), mode);
		}
	}

}
