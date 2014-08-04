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

import static org.eclipse.gef.EditPolicy.CONNECTION_ENDPOINTS_ROLE;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;

/**
 * This class defines the edit part for a connection.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ConnectionPart extends AbstractConnectionEditPart {

	@Override
	protected void createEditPolicies() {
		installEditPolicy(CONNECTION_ENDPOINTS_ROLE, new ConnectionEndPointPolicy());
	}

	@Override
	protected IFigure createFigure() {
		return new ConnectionFigure();
	}

}
