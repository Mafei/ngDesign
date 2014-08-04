/*******************************************************************************
 * Copyright (c) 2013 Synflow SAS.
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
 * This class provides a wizard to create a new network design.
 * 
 * @author Matthieu Wipliez
 */
public class NewNetworkWizard extends NewFileWizard {

	public static final String WIZARD_ID = "com.synflow.cx.ui.wizards.newNetwork";

	/**
	 * Creates a new wizard.
	 */
	public NewNetworkWizard() {
		setWindowTitle("New C~ network");
	}

	@Override
	public void addPages() {
		NewFilePage page = new NewNetworkPage("NewNetwork", selection);
		page.setFileName("NewNetwork");
		page.setDescription("Creates a new C~ network.");
		addPage(page);
	}

}
