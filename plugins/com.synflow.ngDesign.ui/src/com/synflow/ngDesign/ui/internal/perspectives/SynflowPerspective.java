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
package com.synflow.ngDesign.ui.internal.perspectives;

import static org.eclipse.debug.ui.IDebugUIConstants.ID_DEBUG_PERSPECTIVE;
import static org.eclipse.egit.ui.UIPreferences.SHOW_GIT_PREFIX_WARNING;
import static org.eclipse.egit.ui.UIPreferences.SHOW_HOME_DIR_WARNING;
import static org.eclipse.egit.ui.UIPreferences.SHOW_INITIAL_CONFIG_DIALOG;
import static org.eclipse.ui.IPageLayout.ID_OUTLINE;
import static org.eclipse.ui.IPageLayout.ID_PROBLEM_VIEW;
import static org.eclipse.ui.IPageLayout.ID_PROP_SHEET;
import static org.eclipse.ui.console.IConsoleConstants.ID_CONSOLE_VIEW;
import static org.eclipse.ui.progress.IProgressConstants.PROGRESS_VIEW_ID;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.egit.ui.Activator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.synflow.cflow.ui.views.FsmView;
import com.synflow.cflow.ui.views.GraphView;
import com.synflow.ngDesign.ui.internal.navigator.ProjectExplorer;
import com.synflow.ngDesign.ui.internal.wizards.NewProjectWizard;

/**
 * This class is meant to serve as an example for how various contributions are made to a
 * perspective. Note that some of the extension point id's are referred to as API constants while
 * others are hardcoded and may be subject to change.
 */
@SuppressWarnings("restriction")
public class SynflowPerspective implements IPerspectiveFactory {

	private static final String LOG_VIEW = "org.eclipse.pde.runtime.LogView";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		resetBuiltinPreferences();

		// package explorer
		String editorArea = layout.getEditorArea();
		IFolderLayout folder = layout.createFolder("left", IPageLayout.LEFT, 0.25f, editorArea); //$NON-NLS-1$
		folder.addView(ProjectExplorer.VIEW_ID);

		// git repositories
		layout.addView("org.eclipse.egit.ui.RepositoriesView", IPageLayout.BOTTOM, 0.7f,
				ProjectExplorer.VIEW_ID);

		// problems view
		IFolderLayout outputfolder = layout.createFolder(
				"bottom", IPageLayout.BOTTOM, 0.75f, editorArea); //$NON-NLS-1$
		outputfolder.addView(ID_PROBLEM_VIEW);
		outputfolder.addView(ID_CONSOLE_VIEW);
		outputfolder.addView(ID_PROP_SHEET);
		outputfolder.addPlaceholder(PROGRESS_VIEW_ID);

		// outline
		IFolderLayout outlineFolder = layout.createFolder(
				"right", IPageLayout.RIGHT, 0.75f, editorArea); //$NON-NLS-1$
		outlineFolder.addView(ID_OUTLINE);

		// views
		IFolderLayout views = layout.createFolder("views", IPageLayout.BOTTOM, 0.5f, "right");

		// FSM and graph
		views.addView(FsmView.ID);
		views.addView(GraphView.ID);

		// action sets
		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);

		// view shortcut - Synflow-specific
		layout.addShowViewShortcut(ProjectExplorer.VIEW_ID);
		layout.addShowViewShortcut(FsmView.ID);
		layout.addShowViewShortcut(GraphView.ID);

		// view shortcut - debugging
		layout.addShowViewShortcut(ID_CONSOLE_VIEW);

		// view shortcuts - standard workbench
		layout.addShowViewShortcut(ID_OUTLINE);
		layout.addShowViewShortcut(ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(LOG_VIEW);

		// new actions - new project creation wizard
		layout.addNewWizardShortcut(NewProjectWizard.WIZARD_ID);

		// 'Window' > 'Open Perspective' contributions
		layout.addPerspectiveShortcut(ID_DEBUG_PERSPECTIVE);
	}

	private void resetBuiltinPreferences() {
		// hide warning from Git before initializing the perspective
		// so the user don't see that popup
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setValue(SHOW_HOME_DIR_WARNING, false);
		store.setValue(SHOW_INITIAL_CONFIG_DIALOG, false);
		store.setValue(SHOW_GIT_PREFIX_WARNING, false);

		// show hexadecimal and constants
		// store = JavaDebugUtils.getPreferenceStore();
		// store.setValue("org.eclipse.jdt.debug.ui.show_hex", true);
		// store.setValue("org.eclipse.jdt.debug.ui.show_constants", true);
	}

}
