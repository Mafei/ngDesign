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
package com.synflow.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

/**
 * This class defines a build job.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class BuildJob extends Job {

	private final IProject project;

	public BuildJob(IProject project) {
		super("Build " + project.getName());
		this.project = project;
	}

	@Override
	public boolean belongsTo(Object family) {
		return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}

		try {
			monitor.beginTask("Building " + project.getName(), 2);
			project.build(IncrementalProjectBuilder.CLEAN_BUILD,
					new SubProgressMonitor(monitor, 1));
		} catch (CoreException e) {
			return e.getStatus();
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

}
