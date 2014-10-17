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
package com.synflow.ngDesign.ui.internal.wizards;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * This class defines a wizard for creating a new package.
 * 
 * @author Matthieu Wipliez
 *
 */
public class NewPackageWizard extends Wizard implements INewWizard {

	public static final String WIZARD_ID = "com.synflow.ngDesign.ui.wizards.newPackage";

	private IStructuredSelection selection;

	private IWorkbench workbench;

	public NewPackageWizard() {
		setWindowTitle("New package");
	}

	@Override
	public void addPages() {
		NewPackagePage page = new NewPackagePage(selection);
		page.setWizard(this);
		addPage(page);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}

	@Override
	public boolean performFinish() {
		NewPackagePage page = (NewPackagePage) getPages()[0];
		IFolder folder = page.createNewFolder();
		if (folder == null) {
			return false;
		}

		BasicNewResourceWizard.selectAndReveal(folder, workbench.getActiveWorkbenchWindow());

		return true;
	}

}
