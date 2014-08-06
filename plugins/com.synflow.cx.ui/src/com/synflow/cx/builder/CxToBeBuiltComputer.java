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
import com.synflow.cx.internal.instantiation.IInstantiator;
import com.synflow.cx.ui.internal.CxActivator;

@SuppressWarnings("restriction")
public class CxToBeBuiltComputer implements IToBeBuiltComputerContribution {

	private IInstantiator instantiator;

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
	}

	@Override
	public boolean removeStorage(ToBeBuilt toBeBuilt, IStorage storage, IProgressMonitor monitor) {
		return false;
	}

	@Override
	public void updateProject(ToBeBuilt toBeBuilt, IProject project, IProgressMonitor monitor)
			throws CoreException {
		if (instantiator == null) {
			Injector injector = CxActivator.getInstance().getInjector(COM_SYNFLOW_CX_CX);
			instantiator = injector.getInstance(IInstantiator.class);
		}

		instantiator.clearData();
	}

	@Override
	public boolean updateStorage(ToBeBuilt toBeBuilt, IStorage storage, IProgressMonitor monitor) {
		return false;
	}

}
