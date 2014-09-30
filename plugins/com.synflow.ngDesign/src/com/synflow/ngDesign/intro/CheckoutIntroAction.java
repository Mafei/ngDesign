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
package com.synflow.ngDesign.intro;

import java.util.Properties;

import org.eclipse.egit.ui.Activator;
import org.eclipse.egit.ui.UIPreferences;
import org.eclipse.egit.ui.internal.clone.GitCloneWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;

/**
 * This class defines a check out intro action.
 * 
 * @author Matthieu Wipliez
 *
 */
@SuppressWarnings("restriction")
public class CheckoutIntroAction implements IIntroAction {

	private void executeUpdateCommand(Shell shell, String url) {
		Activator.getDefault().getPreferenceStore()
				.setValue(UIPreferences.CLONE_WIZARD_IMPORT_PROJECTS, true);

		GitCloneWizard wizard = new GitCloneWizard(url);
		wizard.setShowProjectImport(true);

		WizardDialog dlg = new WizardDialog(shell, wizard);
		dlg.setHelpAvailable(true);
		dlg.open();
	}

	@Override
	public void run(final IIntroSite site, Properties params) {
		final String url = params.getProperty("url");
		final Shell shell = site.getWorkbenchWindow().getShell();

		Runnable r = new Runnable() {
			public void run() {
				executeUpdateCommand(shell, url);
			}
		};

		shell.getDisplay().asyncExec(r);
	}

}
