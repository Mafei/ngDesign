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
package com.synflow.ngDesign.ui.intro;

import java.util.Properties;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.osgi.framework.Bundle;

/**
 * This class defines a check out intro action.
 * 
 * @author Matthieu Wipliez
 *
 */
public class CheckoutIntroAction implements IIntroAction {

	boolean executeUpdateCommand(IIntroSite site, String id) {
		ICommandService commandService = (ICommandService) site.getService(ICommandService.class);
		IHandlerService handlerService = (IHandlerService) site.getService(IHandlerService.class);

		Command command = commandService.getCommand(id);
		ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, null);
		try {
			command.getParameters();
			command.executeWithChecks(executionEvent);
		} catch (ExecutionException e) {
			String bundleId = "org.eclipse.platform";
			Bundle bundle = Platform.getBundle(bundleId);
			if (bundle != null) {
				String msg = "Exception executing command: " + id;
				Platform.getLog(bundle).log(new Status(IStatus.ERROR, bundleId, msg, e));
			}
		} catch (NotDefinedException e) {
			return false;
		} catch (NotEnabledException e) {
			return false;
		} catch (NotHandledException e) {
			return false;
		}
		return true;
	}

	@Override
	public void run(final IIntroSite site, Properties params) {
		Runnable r = new Runnable() {
			public void run() {
				executeUpdateCommand(site, "org.eclipse.egit.ui.RepositoriesViewClone");
			}
		};

		Shell currentShell = site.getWorkbenchWindow().getShell();
		currentShell.getDisplay().asyncExec(r);
	}

}
