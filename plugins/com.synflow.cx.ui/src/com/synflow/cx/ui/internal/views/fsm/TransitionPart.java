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
package com.synflow.cx.ui.internal.views.fsm;

import static org.eclipse.gef.EditPolicy.CONNECTION_ENDPOINTS_ROLE;
import static org.eclipse.gef.EditPolicy.LAYOUT_ROLE;

import java.util.List;

import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;

import com.synflow.models.dpn.Transition;

/**
 * This class defines the edit part for a transition.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class TransitionPart extends AbstractConnectionEditPart {

	@Override
	protected void createEditPolicies() {
		installEditPolicy(LAYOUT_ROLE, new LayoutEditPolicy());
		installEditPolicy(CONNECTION_ENDPOINTS_ROLE,
				new TransitionEndPointPolicy());
	}

	@Override
	protected IFigure createFigure() {
		Transition transition = (Transition) getModel();
		if (transition.getSource() == transition.getTarget()) {
			return new TransitionLoopFigure();
		} else {
			TransitionFigure figure = new TransitionFigure();

			List<Bendpoint> bendpoints;
			bendpoints = transition.get("bendpoints");
			if (bendpoints != null) {
				ConnectionLayer connLayer = (ConnectionLayer) getLayer(CONNECTION_LAYER);
				ConnectionRouter router = connLayer.getConnectionRouter();
				router.setConstraint(figure, bendpoints);
				figure.setConnectionRouter(router);
			}

			return figure;
		}
	}

}
