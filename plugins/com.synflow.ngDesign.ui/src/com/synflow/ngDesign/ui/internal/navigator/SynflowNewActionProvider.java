/*******************************************************************************
 * Copyright (c) 2005, 2008, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Matthieu Wipliez (Synflow SAS) - modified
 *******************************************************************************/
package com.synflow.ngDesign.ui.internal.navigator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.NewExampleAction;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.ui.navigator.WizardActionGroup;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardRegistry;

import com.synflow.ngDesign.ui.internal.navigator.actions.NewSynflowProjectAction;

/**
 * This class defines an action provider for new items based on
 * org.eclipse.ui.internal.navigator.resources.actions.NewActionProvider. The differences are:
 * <ul>
 * <li>no generic project, replaced by Synflow Project.</li>
 * <li>no "Other" new wizard.</li>
 * </ul>
 * 
 * @author Matthieu Wipliez
 * 
 */
public class SynflowNewActionProvider extends CommonActionProvider {

	private static final String FULL_EXAMPLES_WIZARD_CATEGORY = "org.eclipse.ui.Examples"; //$NON-NLS-1$

	private static final String NEW_MENU_NAME = "common.new.menu";//$NON-NLS-1$

	private boolean contribute = false;

	private IAction newProjectAction;

	private IAction newExampleAction;

	private WizardActionGroup newWizardActionGroup;

	private ActionFactory.IWorkbenchAction showDlgAction;

	@Override
	public void dispose() {
		if (showDlgAction != null) {
			showDlgAction.dispose();
			showDlgAction = null;
		}
		super.dispose();
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		IMenuManager submenu = new MenuManager("&New", NEW_MENU_NAME);
		if (!contribute) {
			return;
		}
		// Add new project wizard shortcut
		submenu.add(newProjectAction);
		submenu.add(new Separator());

		// fill the menu from the commonWizard contributions
		newWizardActionGroup.setContext(getContext());
		newWizardActionGroup.fillContextMenu(submenu);

		submenu.add(new Separator(ICommonMenuConstants.GROUP_ADDITIONS));

		// if there are examples, then add them to the end of the menu
		if (hasExamples()) {
			submenu.add(new Separator());
			submenu.add(newExampleAction);
		}

		// append the submenu after the GROUP_NEW group.
		menu.insertAfter(ICommonMenuConstants.GROUP_NEW, submenu);
	}

	/**
	 * Return whether or not any examples are in the current install.
	 * 
	 * @return True if there exists a full examples wizard category.
	 */
	private boolean hasExamples() {
		IWizardRegistry newRegistry = PlatformUI.getWorkbench().getNewWizardRegistry();
		IWizardCategory category = newRegistry.findCategory(FULL_EXAMPLES_WIZARD_CATEGORY);
		return category != null;

	}

	@Override
	public void init(ICommonActionExtensionSite anExtensionSite) {
		if (anExtensionSite.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			IWorkbenchWindow window = ((ICommonViewerWorkbenchSite) anExtensionSite.getViewSite())
					.getWorkbenchWindow();
			showDlgAction = ActionFactory.NEW.create(window);
			newExampleAction = new NewExampleAction(window);
			newProjectAction = new NewSynflowProjectAction(window);

			newWizardActionGroup = new WizardActionGroup(window, PlatformUI.getWorkbench()
					.getNewWizardRegistry(), WizardActionGroup.TYPE_NEW,
					anExtensionSite.getContentService());

			contribute = true;
		}
	}

}
