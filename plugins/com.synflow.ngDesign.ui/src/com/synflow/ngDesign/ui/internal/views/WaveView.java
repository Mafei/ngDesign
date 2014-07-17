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
package com.synflow.ngDesign.ui.internal.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 * This class defines the Wave view.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class WaveView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.synflow.ngDesign.ui.views.WaveView";

	@Override
	public void createPartControl(Composite parent) {
		WaveWidget widget = new WaveWidget(parent, SWT.NONE);
		widget.addSignal("toto");
		widget.addSignal("tata");

		getSite().getPage().addSelectionListener(new ISelectionListener() {

			@Override
			public void selectionChanged(IWorkbenchPart part,
					ISelection selection) {
				System.out.println(part + " " + selection);
			}

		});
	}

	@Override
	public void setFocus() {
	}

}
