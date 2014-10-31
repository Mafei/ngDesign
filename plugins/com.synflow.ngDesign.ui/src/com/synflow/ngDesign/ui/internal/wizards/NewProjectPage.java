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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import com.synflow.core.SynflowCore;

/**
 * This class defines a new project wizard.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class NewProjectPage extends WizardNewProjectCreationPage {

	private String generator;

	public NewProjectPage(String pageName) {
		super(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		List<String> generators = SynflowCore.getGenerators();
		if (generators.size() == 1) {
			generator = generators.get(0);
			return;
		}

		Composite composite = (Composite) getControl();
		Group group = new Group(composite, NONE);
		group.setText("Hardware code generation");

		// create a button for each code generator
		for (String generator : generators) {
			Button button = new Button(group, SWT.RADIO);
			button.setText(generator);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Button button = (Button) e.widget;
					if (button.getSelection()) {
						NewProjectPage.this.generator = button.getText();
					}
				}
			});
		}

		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// select last button
		Control[] children = group.getChildren();
		Button last = ((Button) children[children.length - 1]);
		last.setSelection(true);
		generator = last.getText();
	}

	/**
	 * Returns the generator to use for this new project.
	 * 
	 * @return a generator name
	 */
	public String getGenerator() {
		return generator;
	}

}
