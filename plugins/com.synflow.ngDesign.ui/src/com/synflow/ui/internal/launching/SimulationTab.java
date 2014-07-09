/*******************************************************************************
 * Copyright (c) 2012 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.ui.internal.launching;

import static com.synflow.ui.internal.launching.ILaunchConfigurationConstants.CLASS;
import static com.synflow.ui.internal.launching.ILaunchConfigurationConstants.PROJECT;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.synflow.core.SynflowCore;
import com.synflow.ui.internal.FilteredRefinementDialog;
import com.synflow.ui.internal.SynflowUi;

/**
 * This class defines the main tab for simulation configuration type.
 * 
 * @author Matthieu Wipliez
 */
public class SimulationTab extends AbstractLaunchConfigurationTab {

	private static class ProjectLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			ImageDescriptor desc;
			desc = SynflowUi.getImageDescriptor("icons/sfprj.gif");
			return desc.createImage();
		}

		@Override
		public String getText(Object element) {
			return ((IProject) element).getName();
		}

	}

	/**
	 * A listener which handles widget change events for the controls in this
	 * tab.
	 */
	private class WidgetListener implements ModifyListener, SelectionListener {

		@Override
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// do nothing
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == fProjButton) {
				handleProjectButtonSelected();
			} else if (source == fSearchButton) {
				handleSearchButtonSelected();
			} else {
				updateLaunchConfigurationDialog();
			}
		}
	}

	private WidgetListener fListener = new WidgetListener();

	private Text fMainText;

	private Button fProjButton;

	private Text fProjText;

	private Button fSearchButton;

	/**
	 * Chooses a Synflow project.
	 * 
	 * @return
	 */
	private IProject chooseSynflowProject() {
		ILabelProvider labelProvider = new ProjectLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getShell(), labelProvider);
		dialog.setTitle("Project selection");
		dialog.setMessage("Choose an existing project");

		dialog.setElements(SynflowCore.getProjects());

		IProject project = getProjectFromText();
		if (project != null) {
			dialog.setInitialSelections(new Object[] { project });
		}
		if (dialog.open() == Window.OK) {
			return (IProject) dialog.getFirstResult();
		}
		return null;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		setControl(composite);

		GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 0;
		composite.setLayout(layout);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(data);

		createProjectControl(composite);
		createSpacer(composite);
		createDesignControl(composite);
	}

	/**
	 * Creates the widgets for specifying a main type.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	protected void createDesignControl(Composite composite) {
		final Group group = new Group(composite, SWT.NONE);
		group.setFont(getFont());
		group.setText("&Design:");
		group.setLayout(new GridLayout(2, false));
		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
		group.setLayoutData(data);

		fMainText = new Text(group, SWT.BORDER | SWT.SINGLE);
		fMainText.setFont(getFont());
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		fMainText.setLayoutData(data);
		fMainText.addModifyListener(fListener);

		fSearchButton = createPushButton(group, "Search...", null);
		fSearchButton.addSelectionListener(fListener);
	}

	private void createProjectControl(Composite composite) {
		final Group group = new Group(composite, SWT.NONE);
		group.setFont(getFont());
		group.setText("&Project:");
		group.setLayout(new GridLayout(2, false));
		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
		group.setLayoutData(data);

		fProjText = new Text(group, SWT.BORDER | SWT.SINGLE);
		fProjText.setFont(getFont());
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		fProjText.setLayoutData(data);
		fProjText.addModifyListener(fListener);

		fProjButton = createPushButton(group, "Browse...", null);
		fProjButton.addSelectionListener(fListener);
	}

	private void createSpacer(Composite composite) {
		Label lbl = new Label(composite, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 1;
		lbl.setLayoutData(gd);
	}

	private Font getFont() {
		return getControl().getFont();
	}

	@Override
	public Image getImage() {
		return SynflowUi.getImageDescriptor("icons/cflow_app.gif")
				.createImage();
	}

	@Override
	public String getName() {
		return "Main";
	}

	private IProject getProjectFromText() {
		String projectName = fProjText.getText().trim();
		if (projectName.isEmpty()) {
			return null;
		}

		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}

	/**
	 * Show a dialog that lets the user select a project. This in turn provides
	 * context for the main type, allowing the user to key a main type name, or
	 * constraining the search for main types to the specified project.
	 */
	protected void handleProjectButtonSelected() {
		IProject project = chooseSynflowProject();
		if (project == null) {
			return;
		}
		String projectName = project.getName();
		fProjText.setText(projectName);
	}

	protected void handleSearchButtonSelected() {
		IProject project = getProjectFromText();

		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		Shell shell = window.getShell();

		FilteredRefinementDialog dialog = new FilteredRefinementDialog(project,
				shell);
		dialog.setTitle("Select entity");
		dialog.setMessage("&Select existing entity:");
		int result = dialog.open();
		if (result == Window.OK) {
			String name = (String) dialog.getFirstResult();
			fMainText.setText(name);
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		String projectName = "";
		try {
			projectName = configuration.getAttribute(PROJECT, "");
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getMessage());
		}
		fProjText.setText(projectName);

		String className = "";
		try {
			className = configuration.getAttribute(CLASS, "");
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getMessage());
		}
		fMainText.setText(className);
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);

		String name = fProjText.getText().trim();
		if (name.length() > 0) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IStatus status = workspace.validateName(name, IResource.PROJECT);
			if (status.isOK()) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot()
						.getProject(name);
				if (!project.exists()) {
					setErrorMessage("Project \"" + name + "\" does not exist");
					return false;
				}
				if (!project.isOpen()) {
					setErrorMessage("Project \"" + name + "\" is closed");
					return false;
				}
			} else {
				setErrorMessage("Illegal project name: " + status.getMessage());
				return false;
			}
		} else {
			setErrorMessage("Project not specified");
			return false;
		}

		name = fMainText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage("Design not specified");
			return false;
		}
		return true;
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(PROJECT, fProjText.getText().trim());
		configuration.setAttribute(CLASS, fMainText.getText().trim());
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(PROJECT, "");
		configuration.setAttribute(CLASS, "");
	}

}
