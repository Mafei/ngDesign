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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFolderMainPage;

/**
 * This class defines a page for creating a new package.
 * 
 * @author Matthieu Wipliez
 *
 */
public class NewPackagePage extends WizardNewFolderMainPage {

	public NewPackagePage(IStructuredSelection selection) {
		super("NewPackage", selection);

		setTitle("New package");
		setDescription("Create a new package");
	}

}
