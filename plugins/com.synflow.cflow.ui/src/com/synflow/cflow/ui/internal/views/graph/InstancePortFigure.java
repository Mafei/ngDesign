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
package com.synflow.cflow.ui.internal.views.graph;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import com.synflow.models.dpn.Port;
import com.synflow.models.ir.util.IrUtil;

/**
 * This class defines the figure for a port that is part of an instance figure.
 * 
 * @author Matthieu Wipliez
 *
 */
public class InstancePortFigure extends Figure {

	private boolean isInput;

	private Label label;

	private Polyline line;

	private RectangleFigure rectangle;

	public InstancePortFigure(Port port, boolean isConnected) {
		setLayoutManager(new XYLayout());

		setForegroundColor(ColorConstants.black);

		label = new Label(port.getName());
		label.setBackgroundColor(ColorConstants.red);
		label.setLabelAlignment(PositionConstants.LEFT);
		add(label);

		if (isConnected) {
			line = new Polyline();
			if (!port.getType().isBool()) {
				line.setLineWidth(3);
			}
			add(line);

			rectangle = new RectangleFigure();
			rectangle.setFill(true);
			rectangle.setBackgroundColor(ColorConstants.black);
			add(rectangle);
		}

		isInput = IrUtil.isInput(port);
		if (isInput) {
			setConstraint(label, new Rectangle(15, 0, -1, -1));
		} else {
			setConstraint(label, new Rectangle(0, 0, -1, -1));
		}
	}

	/**
	 * Computes the preferred size of this instance figure.
	 * 
	 * @return the size
	 */
	private Dimension computePreferredSize() {
		Font font = getFont();
		FontData[] fontData = font.getFontData();
		for (FontData data : fontData) {
			data.setHeight(7);
		}

		Font smallFont = new Font(font.getDevice(), fontData);
		label.setFont(smallFont);

		return label.getPreferredSize();
	}

	public Label getLabel() {
		return label;
	}

	public Dimension getLabelPreferredSize() {
		return computePreferredSize();
	}

	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		if (prefSize == null) {
			Dimension labelPrefSize = computePreferredSize();
			prefSize = new Dimension(labelPrefSize).expand(16, 0);
		}
		return prefSize;
	}

	@Override
	protected void layout() {
		super.layout();

		Rectangle bounds = getBounds();
		PointList points = new PointList();

		final int w = bounds.width;
		final int h = bounds.height;

		if (isInput) {
			points.addPoint(0, h / 2);
			points.addPoint(10, h / 2);
		} else {
			points.addPoint(w - 11, h / 2);
			points.addPoint(w, h / 2);
		}

		points.translate(getLocation());
		if (line != null) {
			line.setPoints(points);
		}
	}

	@Override
	public void validate() {
		if (isValid()) {
			return;
		}

		if (rectangle != null) {
			final int w = bounds.width, h = bounds.height;
			if (isInput) {
				setConstraint(rectangle, new Rectangle(0, h / 2 - 1, 3, 3));
			} else {
				setConstraint(rectangle, new Rectangle(w - 3, h / 2 - 1, 3, 3));
			}
		}

		super.validate();
	}

}
