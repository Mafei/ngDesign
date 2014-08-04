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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.handles.HandleBounds;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import com.google.common.base.Function;
import com.synflow.models.dpn.Port;

/**
 * This class defines the figure for an instance.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class InstanceFigure extends Figure implements HandleBounds {

	private List<InstancePortFigure> inputs;

	private final Label label;

	private List<InstancePortFigure> outputs;

	private Figure visibleRect;

	public InstanceFigure() {
		setLayoutManager(new XYLayout());

		RoundedRectangle visibleRect = new RoundedRectangle();
		visibleRect.setAntialias(SWT.ON);
		visibleRect.setFill(true);
		visibleRect.setForegroundColor(ColorConstants.black);
		visibleRect.setBackgroundColor(new Color(null, 255, 255, 216));
		visibleRect.setLayoutManager(new XYLayout());
		add(visibleRect);
		this.visibleRect = visibleRect;

		label = new Label();
		visibleRect.add(label);
	}

	/**
	 * Computes the preferred size of this instance figure.
	 * 
	 * @return the size
	 */
	private Dimension computePreferredSize() {
		Dimension sizeInputs = getSize(inputs);
		Dimension sizeOutputs = getSize(outputs);

		int w = Math.max(150, sizeInputs.width + 20 + sizeOutputs.width);
		int h = 30 + Math.max(sizeInputs.height, sizeOutputs.height);

		Dimension inner = new Dimension(w, h);
		return inner.expand(20, 20);
	}

	@Override
	public boolean containsPoint(int x, int y) {
		return visibleRect.containsPoint(x, y);
	}

	/**
	 * Creates a list of labels from the given list of ports, and adds them to the insideRect
	 * figure.
	 * 
	 * @param ports
	 *            a list of ports
	 * @return a list of labels
	 */
	private List<InstancePortFigure> createLabels(Function<Port, Boolean> isConnected,
			List<Port> ports, Map<Port, IFigure> map) {
		List<InstancePortFigure> labels = new ArrayList<>(ports.size());
		for (Port port : ports) {
			InstancePortFigure fig = new InstancePortFigure(port, isConnected.apply(port));
			add(fig);
			labels.add(fig);
			map.put(port, fig);
		}
		return labels;
	}

	@Override
	public Rectangle getBounds() {
		// dirty hack because the marquee tool doesn't take handle bounds into account
		if (DirtyMarqueeTool.duringMarquee) {
			return getHandleBounds();
		}
		return super.getBounds();
	}

	@Override
	public Rectangle getHandleBounds() {
		return visibleRect.getBounds();
	}

	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		if (prefSize == null) {
			prefSize = computePreferredSize();
		}
		return prefSize;
	}

	/**
	 * Returns the size of the smallest rectangle that can contain all given labels.
	 * 
	 * @param ports
	 *            a list of labels
	 * @return a size
	 */
	private Dimension getSize(Collection<InstancePortFigure> ports) {
		Dimension size = new Dimension();
		for (InstancePortFigure port : ports) {
			Dimension portSize = port.getPreferredSize();
			size.height += portSize.height;
			size.width = Math.max(size.width, portSize.width);
		}
		return size;
	}

	/**
	 * Sets this instance's label.
	 * 
	 * @param text
	 *            a label
	 */
	public void setLabel(String text) {
		label.setText(text);
	}

	/**
	 * Sets this instance's input and output ports.
	 * 
	 * @param inputs
	 *            list of input ports
	 * @param outputs
	 *            list of output ports
	 * @return a map from port to figure
	 */
	public Map<Port, IFigure> setPorts(Function<Port, Boolean> isConnected, List<Port> inputs,
			List<Port> outputs) {
		Map<Port, IFigure> map = new HashMap<>(inputs.size() + outputs.size());
		this.inputs = createLabels(isConnected, inputs, map);
		this.outputs = createLabels(isConnected, outputs, map);
		return map;
	}

	@Override
	public void validate() {
		if (isValid()) {
			return;
		}

		Dimension dim = getSize();
		Dimension dimInside = dim.getShrinked(20, 20);
		setConstraint(visibleRect, new Rectangle(10, 10, dimInside.width, dimInside.height));
		visibleRect.setConstraint(label, new Rectangle(0, 0, dimInside.width, -1));

		// set constraints on input ports
		int x = 0;
		int y = 30;
		for (InstancePortFigure fig : inputs) {
			int h = fig.getPreferredSize().height;
			setConstraint(fig, new Rectangle(x, y, -1, -1));
			y += h;
		}

		// set constraints on output ports
		x = dim.width;
		y = 30;
		for (InstancePortFigure fig : outputs) {
			int w = fig.getPreferredSize().width;
			int h = fig.getPreferredSize().height;
			setConstraint(fig, new Rectangle(x - w, y, -1, -1));
			y += h;
		}

		super.validate();
	}

}
