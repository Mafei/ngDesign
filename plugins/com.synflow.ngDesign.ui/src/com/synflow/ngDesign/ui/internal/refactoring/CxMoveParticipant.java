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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;

/**
 * This class defines a RenameParticipant for Cx tasks.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CxMoveParticipant extends MoveParticipant {

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {
		MoveArguments args = getArguments();
		Object target = args.getDestination();
		if (!(target instanceof IContainer)) {
			String msg = "Target must be a valid folder or project";
			return RefactoringStatus.createFatalErrorStatus(msg);
		}

		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		return null;
	}

	@Override
	public String getName() {
		return "Cx move participant";
	}

	@Override
	protected boolean initialize(Object element) {
		return true;
	}

}
