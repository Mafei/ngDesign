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
package com.synflow.cx.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.synflow.core.layout.ITreeElement;

/**
 * This class provides a page to create a new text file.
 * 
 * @author Matthieu Wipliez
 */
public abstract class NewFilePage extends WizardPage implements ModifyListener {

	private static final int SIZING_TEXT_FIELD_WIDTH = 250;

	private String containingPackage;

	private String entityName;

	private Pattern id = Pattern.compile("[A-Z][a-zA-Z0-9_]*");

	private Label labelPackage;

	private Text resourceNameField;

	private IStructuredSelection selection;

	public NewFilePage(String pageName, IStructuredSelection selection) {
		super(pageName);
		this.selection = selection;

		setDescription("Create a new package (for example: com.acme.anvil)");
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		Label label1 = new Label(composite, SWT.NONE);
		label1.setText("Package:");
		label1.setFont(this.getFont());

		labelPackage = new Label(composite, SWT.NONE);
		labelPackage.setFont(this.getFont());

		Label label2 = new Label(composite, SWT.WRAP);
		label2.setText("Name:");
		label2.setFont(this.getFont());

		// resource name entry field
		resourceNameField = new Text(composite, SWT.BORDER);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		resourceNameField.setLayoutData(data);

		initializePage();

		// add modify listener *after* page is initialized
		// so page is not complete, but no error message is shown initially
		resourceNameField.addModifyListener(this);

		// Show description on opening
		setControl(composite);
	}

	public IFile createNewFile() {
		// TODO Auto-generated method stub
		return null;
	}

	protected final String getEntityName() {
		return entityName;
	}

	public InputStream getInitialContents() {
		final String author = System.getProperty("user.name");
		final int year = Calendar.getInstance().get(Calendar.YEAR);

		return new ByteArrayInputStream(getStringContents(author, year).toString().getBytes());
	}

	protected final String getPackage() {
		return containingPackage;
	}

	protected abstract CharSequence getStringContents(String author, int year);

	/**
	 * Initializes this page's controls.
	 */
	protected void initializePage() {
		Iterator<?> it = selection.iterator();
		if (it.hasNext()) {
			Object next = it.next();
			if (next instanceof ITreeElement) {
				ITreeElement element = (ITreeElement) next;
				containingPackage = element.getName();
				labelPackage.setText(containingPackage);
			}
		}
	}

	@Override
	public void modifyText(ModifyEvent e) {
		setPageComplete(validatePage());
	}

	/**
	 * Returns whether this page's controls currently all contain valid values.
	 * 
	 * @return <code>true</code> if all controls are valid, and <code>false</code> if at least one
	 *         is invalid
	 */
	protected boolean validatePage() {
		String text = resourceNameField.getText();
		if (text.isEmpty()) {
			setErrorMessage("Enter a name.");
			return false;
		}

		if (!id.matcher(text).matches()) {
			setErrorMessage("Invalid name: '" + text + "' is not a valid identifier.");
			return false;
		}

		setErrorMessage(null);

		return true;
	}

}
