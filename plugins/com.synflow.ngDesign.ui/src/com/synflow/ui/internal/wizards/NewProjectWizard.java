/*******************************************************************************
 * Copyright (c) 2012-2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.ui.internal.wizards;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

/**
 * This class defines a new project wizard.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class NewProjectWizard extends Wizard implements IExecutableExtension,
		INewWizard {

	public static final String WIZARD_ID = "com.synflow.ui.wizards.newProject";

	private IConfigurationElement fConfigElement;

	private WizardNewProjectCreationPage mainPage;

	@SuppressWarnings("unused")
	private IStructuredSelection selection;

	@SuppressWarnings("unused")
	private IWorkbench workbench;

	@Override
	public void addPages() {
		mainPage = new WizardNewProjectCreationPage("new project");
		mainPage.setDescription("Creates a new project");
		mainPage.setTitle("New Project");
		addPage(mainPage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}

	@Override
	public boolean performFinish() {
		// get a project descriptor
		IProject project = mainPage.getProjectHandle();

		// location of project
		URI uri = mainPage.useDefaults() ? null : mainPage.getLocationURI();

		try {
			new ProjectCreator().createProject(project, uri);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		BasicNewProjectResourceWizard.updatePerspective(fConfigElement);

		return true;
	}

	/**
	 * Stores the configuration element for the wizard. The config element will
	 * be used in <code>performFinish</code> to set the result perspective.
	 */
	public void setInitializationData(IConfigurationElement cfig,
			String propertyName, Object data) {
		fConfigElement = cfig;
	}

}
