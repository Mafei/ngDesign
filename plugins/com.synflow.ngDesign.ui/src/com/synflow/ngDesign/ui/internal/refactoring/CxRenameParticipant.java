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
package com.synflow.ngDesign.ui.internal.refactoring;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

import com.google.common.collect.ImmutableSet;
import com.synflow.core.SynflowCore;

/**
 * This class defines a RenameParticipant for Cx tasks.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CxRenameParticipant extends RenameParticipant {

	private static final Set<String> keywords = ImmutableSet.of("bundle", "network", "task");

	public static IStatus validateIdentifier(String id) {
		if (keywords.contains(id)) {
			String message = "'" + id + "' is not a valid identifier";
			return new Status(IStatus.ERROR, SynflowCore.PLUGIN_ID, message);
		}
		return Status.OK_STATUS;
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {
		// check name
		RenameArguments args = getArguments();
		String newName = args.getNewName();
		IPath path = new Path(newName).removeFileExtension();
		IStatus status = validateIdentifier(path.lastSegment());
		return RefactoringStatus.create(status);
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		return null;
	}

	@Override
	public String getName() {
		return "Cx rename participant";
	}

	@Override
	protected boolean initialize(Object element) {
		return true;
	}

}
