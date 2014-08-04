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
package com.synflow.cflow.ui.internal.views.fsm;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

/**
 * This class defines the figure for a state.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class StateFigure extends Figure {

	private final Label label;

	private XYLayout layout;

	public StateFigure() {
		layout = new XYLayout();
		setLayoutManager(layout);

		setForegroundColor(ColorConstants.black);

		Ellipse ellipse = new Ellipse();
		ellipse.setAntialias(SWT.ON);
		ellipse.setBounds(new Rectangle(0, 0, 30, 30));
		ellipse.setBackgroundColor(ColorConstants.white);
		ellipse.setForegroundColor(ColorConstants.black);
		add(ellipse);

		label = new Label();
		label.setForegroundColor(ColorConstants.black);

		ellipse.setLayoutManager(new XYLayout());
		ellipse.add(label);
		ellipse.setConstraint(label, new Rectangle(0, 0, 30, 30));

		setConstraint(ellipse, new Rectangle(0, 0, 30, 30));
	}

	public void setLabel(String label) {
		int index = label.lastIndexOf('_');
		String text = label.substring(index + 1);
		this.label.setText(text);
	}

	public void setLayout(Rectangle rect) {
		getParent().setConstraint(this, rect);
	}

}
