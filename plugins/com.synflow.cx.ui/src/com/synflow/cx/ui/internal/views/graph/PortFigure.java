/*******************************************************************************
 * Copyright (c) 2013-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.ui.internal.views.graph;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

/**
 * This class defines the figure for a port.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class PortFigure extends Figure {

	private final Label label;

	private Polygon polygon;

	public PortFigure() {
		setLayoutManager(new StackLayout());

		setBackgroundColor(ColorConstants.white);
		setForegroundColor(ColorConstants.black);

		// adds polygon first (placed on bottom)
		polygon = new Polygon();
		polygon.setFill(true);
		polygon.setPoints(new PointList());
		polygon.setAntialias(SWT.ON);
		add(polygon);

		// adds label second (placed on top)
		label = new Label();
		label.setLabelAlignment(PositionConstants.LEFT);
		add(label);
	}

	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		if (label != null) {
			Dimension size = label.getPreferredSize();
			return size.getCopy().expand(9, 0).union(new Dimension(0, 16));
		}
		return super.getPreferredSize(wHint, hHint);
	}

	@Override
	protected void layout() {
		super.layout();

		Rectangle bounds = getBounds();
		PointList points = new PointList();
		points.addPoint(0, 0);

		final int w = bounds.width;
		final int h = bounds.height;

		points.addPoint(w - (h + 1) / 2, 0);
		points.addPoint(w - 1, h / 2);
		points.addPoint(w - (h + 1) / 2, h - 1);
		points.addPoint(0, h - 1);

		points.translate(getLocation());
		polygon.setPoints(points);
	}

	public void setLabel(String text) {
		label.setText(text);
	}

}
