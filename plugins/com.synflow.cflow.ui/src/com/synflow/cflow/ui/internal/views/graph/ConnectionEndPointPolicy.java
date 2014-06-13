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
package com.synflow.cflow.ui.internal.views.graph;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;

/**
 * This class defines a connection endpoint edit policy that shows selected connections in color.
 * 
 * @author Matthieu Wipliez
 */
public class ConnectionEndPointPolicy extends ConnectionEndpointEditPolicy {

	@Override
	protected void addSelectionHandles() {
		Connection connection = getConnection();
		setActive(connection.getSourceAnchor().getOwner(), true);
		setActive(connection.getTargetAnchor().getOwner(), true);
		setActive(connection, true);
	}

	@Override
	protected void removeSelectionHandles() {
		Connection connection = getConnection();
		setActive(connection.getSourceAnchor().getOwner(), false);
		setActive(connection.getTargetAnchor().getOwner(), false);
		setActive(connection, false);
	}

	/**
	 * If active is true, set the figure's foreground color to red; otherwise set it to black.
	 * 
	 * @param figure
	 *            figure whose color should be changed
	 * @param active
	 *            active or not flag
	 */
	private void setActive(IFigure figure, boolean active) {
		figure.setForegroundColor(active ? ColorConstants.red : ColorConstants.black);
	}

}
