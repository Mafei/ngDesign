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

import static com.synflow.ui.internal.launching.ILaunchConfigurationConstants.CHECK_PORTS;
import static com.synflow.ui.internal.launching.ILaunchConfigurationConstants.CREATE_VCD;
import static com.synflow.ui.internal.launching.ILaunchConfigurationConstants.DEFAULT_ASSERT;
import static com.synflow.ui.internal.launching.ILaunchConfigurationConstants.DEFAULT_CHECK_PORTS;
import static com.synflow.ui.internal.launching.ILaunchConfigurationConstants.DEFAULT_CREATE_VCD;
import static com.synflow.ui.internal.launching.ILaunchConfigurationConstants.DEFAULT_NUM_CYCLES;
import static com.synflow.ui.internal.launching.ILaunchConfigurationConstants.DEFAULT_PRINT_CYCLES;
import static com.synflow.ui.internal.launching.ILaunchConfigurationConstants.ENABLE_ASSERTIONS;
import static com.synflow.ui.internal.launching.ILaunchConfigurationConstants.NUM_CYCLES;
import static com.synflow.ui.internal.launching.ILaunchConfigurationConstants.PRINT_CYCLES;
import static java.lang.Integer.MAX_VALUE;

import java.math.BigInteger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.synflow.ui.internal.SynflowUi;

/**
 * This class defines the main tab for simulation configuration type.
 * 
 * @author Matthieu Wipliez
 */
public class ConfigurationTab extends AbstractLaunchConfigurationTab {

	/**
	 * A listener which handles widget change events for the controls in this
	 * tab.
	 */
	private class WidgetListener extends SelectionAdapter implements
			ModifyListener, VerifyListener {

		@Override
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		@Override
		public void verifyText(VerifyEvent e) {
			if (e.character != 0 && e.character != SWT.DEL
					&& e.character != SWT.BS) {
				e.doit = e.character >= '0' && e.character <= '9';
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			updateLaunchConfigurationDialog();
		}

	}

	private Button checkPorts;

	private Button createVcd;

	private Button enableAssertions;

	private WidgetListener fListener = new WidgetListener();

	private Text numCycles;

	private Button printCycles;

	@Override
	public boolean canSave() {
		return isValid(null);
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

		createMain(composite);
	}

	private void createMain(Composite composite) {
		final Group group = new Group(composite, SWT.NONE);
		group.setFont(getFont());
		group.setText("&Runtime options:");
		group.setLayout(new GridLayout(2, false));
		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
		group.setLayoutData(data);

		Label label = new Label(group, SWT.NONE);
		label.setFont(getFont());
		label.setText("Number of cycles:");

		numCycles = new Text(group, SWT.BORDER | SWT.SINGLE);
		numCycles.setFont(getFont());
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		numCycles.setLayoutData(data);
		numCycles.setText("Number of cycles");
		numCycles
				.setToolTipText("Number of cycles during which the design is simulated");
		numCycles.addModifyListener(fListener);
		numCycles.addVerifyListener(fListener);

		checkPorts = new Button(group, SWT.CHECK);
		checkPorts.setFont(getFont());
		data = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		checkPorts.setLayoutData(data);
		checkPorts
				.setText("Check ports: verifies that 'write' statements do not overridde data on ports");
		checkPorts.addSelectionListener(fListener);

		createVcd = new Button(group, SWT.CHECK);
		createVcd.setFont(getFont());
		data = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		createVcd.setLayoutData(data);
		createVcd.setText("Create VCD file");
		createVcd.addSelectionListener(fListener);

		enableAssertions = new Button(group, SWT.CHECK);
		enableAssertions.setFont(getFont());
		data = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		enableAssertions.setLayoutData(data);
		enableAssertions
				.setText("Enable assertions: evaluate expressions of 'assert' statements, and fail if they are false");
		enableAssertions.addSelectionListener(fListener);

		printCycles = new Button(group, SWT.CHECK);
		printCycles.setFont(getFont());
		data = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		printCycles.setLayoutData(data);
		printCycles.setText("Print the current cycle number");
		printCycles.addSelectionListener(fListener);
	}

	private Font getFont() {
		return getControl().getFont();
	}

	@Override
	public Image getImage() {
		return SynflowUi.getImageDescriptor("icons/settings.gif").createImage();
	}

	@Override
	public String getName() {
		return "Configuration";
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		int i = 0;
		try {
			i = config.getAttribute(NUM_CYCLES, DEFAULT_NUM_CYCLES);
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getMessage());
		}
		numCycles.setText(Integer.toString(i));

		boolean bool = false;

		try {
			bool = config.getAttribute(CHECK_PORTS, DEFAULT_CHECK_PORTS);
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getMessage());
		}
		checkPorts.setSelection(bool);

		try {
			bool = config.getAttribute(CREATE_VCD, DEFAULT_CREATE_VCD);
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getMessage());
		}
		createVcd.setSelection(bool);

		try {
			bool = config.getAttribute(ENABLE_ASSERTIONS, DEFAULT_ASSERT);
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getMessage());
		}
		enableAssertions.setSelection(bool);

		try {
			bool = config.getAttribute(PRINT_CYCLES, DEFAULT_PRINT_CYCLES);
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getMessage());
		}
		printCycles.setSelection(bool);
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);

		String text = numCycles.getText();
		if (text.isEmpty()) {
			setErrorMessage("Number of cycles is empty");
			return false;
		}
		if (new BigInteger(text).compareTo(BigInteger.valueOf(MAX_VALUE)) > 0) {
			setErrorMessage("Number of cycles must be less than " + MAX_VALUE);
			return false;
		}

		return true;
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy copy) {
		try {
			copy.setAttribute(NUM_CYCLES, Integer.parseInt(numCycles.getText()));
		} catch (NumberFormatException e) {
		}

		copy.setAttribute(CHECK_PORTS, checkPorts.getSelection());
		copy.setAttribute(CREATE_VCD, createVcd.getSelection());
		copy.setAttribute(ENABLE_ASSERTIONS, enableAssertions.getSelection());
		copy.setAttribute(PRINT_CYCLES, printCycles.getSelection());
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(NUM_CYCLES, DEFAULT_NUM_CYCLES);

		configuration.setAttribute(CHECK_PORTS, DEFAULT_CHECK_PORTS);
		configuration.setAttribute(CREATE_VCD, DEFAULT_CREATE_VCD);
		configuration.setAttribute(ENABLE_ASSERTIONS, DEFAULT_ASSERT);
		configuration.setAttribute(PRINT_CYCLES, DEFAULT_PRINT_CYCLES);
	}

}
