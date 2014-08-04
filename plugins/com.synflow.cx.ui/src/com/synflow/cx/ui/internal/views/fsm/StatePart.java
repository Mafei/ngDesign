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
package com.synflow.cx.ui.internal.views.fsm;

import static org.eclipse.gef.EditPolicy.LAYOUT_ROLE;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import com.synflow.models.dpn.State;
import com.synflow.models.graph.Edge;

/**
 * This class defines the edit part for a state.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class StatePart extends AbstractGraphicalEditPart {

	@Override
	protected void createEditPolicies() {
		installEditPolicy(LAYOUT_ROLE, new LayoutEditPolicy());
	}

	@Override
	protected IFigure createFigure() {
		return new StateFigure();
	}

	@Override
	protected List<Edge> getModelSourceConnections() {
		State state = (State) getModel();
		return state.getOutgoing();
	}

	@Override
	protected List<Edge> getModelTargetConnections() {
		State state = (State) getModel();
		return state.getIncoming();
	}

	@Override
	protected void refreshVisuals() {
		StateFigure figure = (StateFigure) getFigure();
		State state = (State) getModel();
		Point location = state.get("location");

		figure.setLabel(state.getName());

		figure.setLayout(new Rectangle(location.x, location.y, -1, -1));
	}

}
