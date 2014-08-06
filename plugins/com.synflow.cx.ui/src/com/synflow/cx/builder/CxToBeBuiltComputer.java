/*******************************************************************************
 * Copyright (c) 2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.builder;

import static com.synflow.cx.ui.internal.CxActivator.COM_SYNFLOW_CX_CX;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.xtext.builder.impl.IToBeBuiltComputerContribution;
import org.eclipse.xtext.builder.impl.ToBeBuilt;

import com.google.inject.Injector;
import com.synflow.cx.instantiation.IInstantiator;
import com.synflow.cx.ui.internal.CxActivator;

/**
 * This class defines a contributing ToBeBuiltComputer. For now just clears instantiation data on
 * full builds and when projects are added/removed.
 * 
 * @author Matthieu Wipliez
 *
 */
@SuppressWarnings("restriction")
public class CxToBeBuiltComputer implements IToBeBuiltComputerContribution {

	private IInstantiator instantiator;

	private void ensureInstantiatorInjected() {
		if (instantiator == null) {
			Injector injector = CxActivator.getInstance().getInjector(COM_SYNFLOW_CX_CX);
			instantiator = injector.getInstance(IInstantiator.class);
		}
	}

	@Override
	public boolean isPossiblyHandled(IStorage storage) {
		return true;
	}

	@Override
	public boolean isRejected(IFolder folder) {
		return false;
	}

	@Override
	public void removeProject(ToBeBuilt toBeBuilt, IProject project, IProgressMonitor monitor) {
		// this is called when a clean build starts
		// we just clear the instantiator's data

		ensureInstantiatorInjected();
		instantiator.clearData();
	}

	@Override
	public boolean removeStorage(ToBeBuilt toBeBuilt, IStorage storage, IProgressMonitor monitor) {
		return false;
	}

	@Override
	public void updateProject(ToBeBuilt toBeBuilt, IProject project, IProgressMonitor monitor)
			throws CoreException {
		// this is called when a full build starts
		// (if caused by a clean, removeProject has already run)

		// this is where we should update the instantiator, except that at this point we have
		// no build data and no resource description data.
	}

	@Override
	public boolean updateStorage(ToBeBuilt toBeBuilt, IStorage storage, IProgressMonitor monitor) {
		return false;
	}

}
