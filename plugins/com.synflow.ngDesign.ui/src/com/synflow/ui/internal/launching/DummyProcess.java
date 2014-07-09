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
package com.synflow.ui.internal.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

/**
 * This class defines a dummy implementation of a process.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class DummyProcess extends PlatformObject implements IProcess {

	private ILaunchConfiguration configuration;

	private ILaunch launch;

	private IProgressMonitor monitor;

	private IStreamsProxy proxy;

	private boolean terminated;

	public DummyProcess(ILaunch launch, ILaunchConfiguration configuration,
			IProgressMonitor monitor) throws CoreException {
		this.configuration = configuration;
		this.launch = launch;
		this.monitor = monitor;
	}

	@Override
	public boolean canTerminate() {
		return !terminated;
	}

	/**
	 * Fires the given debug event.
	 * 
	 * @param event
	 *            debug event to fire
	 */
	protected void fireEvent(DebugEvent event) {
		DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[] { event });
		}
	}

	protected void fireTerminateEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
	}

	@Override
	public String getAttribute(String key) {
		return null;
	}

	@Override
	public int getExitValue() throws DebugException {
		return 0;
	}

	@Override
	public String getLabel() {
		return configuration.getName();
	}

	@Override
	public ILaunch getLaunch() {
		return launch;
	}

	public IProgressMonitor getProgressMonitor() {
		return monitor;
	}

	@Override
	public IStreamsProxy getStreamsProxy() {
		return proxy;
	}

	@Override
	public boolean isTerminated() {
		return terminated;
	}

	@Override
	public void setAttribute(String key, String value) {
	}

	@Override
	public void terminate() throws DebugException {
		if (!isTerminated() && canTerminate()) {
			terminated = true;

			fireTerminateEvent();

			if (monitor != null) {
				monitor.done();
			}
		}
	}

}
