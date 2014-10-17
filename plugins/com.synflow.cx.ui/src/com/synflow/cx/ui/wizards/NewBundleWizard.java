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
package com.synflow.cx.ui.wizards;

/**
 * This class provides a wizard to create a new Cx bundle.
 * 
 * @author Matthieu Wipliez
 */
public class NewBundleWizard extends NewFileWizard {

	public static final String WIZARD_ID = "com.synflow.cx.ui.wizards.newBundle";

	public NewBundleWizard() {
		setWindowTitle("New Cx bundle");
	}

	@Override
	public void addPages() {
		NewFilePage page = new NewBundlePage("NewBundle", selection);
		page.setFileName("NewBundle");
		page.setDescription("Creates a new Cx bundle.");
		addPage(page);
	}

}
