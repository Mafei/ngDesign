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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Shape;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.swt.graphics.Color;

/**
 * This class defines a connection endpoint edit policy that shows selected
 * connections in a nice color.
 * 
 * @author Matthieu Wipliez
 */
public class TransitionEndPointPolicy extends ConnectionEndpointEditPolicy {

	private static final Color color = new Color(null, 255, 0, 246);

	@Override
	protected void addSelectionHandles() {
		Shape connection = (Shape) getConnection();
		connection.setLineWidth(3);
		connection.setForegroundColor(color);
	}

	@Override
	protected void removeSelectionHandles() {
		Shape connection = (Shape) getConnection();
		connection.setLineWidth(1);
		connection.setForegroundColor(ColorConstants.black);
	}

}