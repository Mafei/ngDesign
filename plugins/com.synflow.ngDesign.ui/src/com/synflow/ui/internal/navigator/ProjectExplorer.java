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
package com.synflow.ui.internal.navigator;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.navigator.CommonNavigator;

import com.synflow.core.SynflowCore;

/**
 * This class defines the Project Explorer view. We define our own view so it is not polluted with
 * all JDT actions.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ProjectExplorer extends CommonNavigator {

	public static final String VIEW_ID = "com.synflow.ui.projectExplorer";

	@Override
	protected void handleDoubleClick(DoubleClickEvent anEvent) {
		ICommandService commandService = (ICommandService) getViewSite().getService(
				ICommandService.class);
		Command openProjectCommand = commandService
				.getCommand(IWorkbenchCommandConstants.PROJECT_OPEN_PROJECT);
		if (openProjectCommand != null && openProjectCommand.isHandled()) {
			IStructuredSelection selection = (IStructuredSelection) anEvent.getSelection();
			Object element = selection.getFirstElement();
			if (element instanceof IProject && !((IProject) element).isOpen()) {
				try {
					openProjectCommand.executeWithChecks(new ExecutionEvent());
				} catch (CommandException e) {
					SynflowCore.log(e);
				}
				return;
			}
		}
		super.handleDoubleClick(anEvent);
	}

}
