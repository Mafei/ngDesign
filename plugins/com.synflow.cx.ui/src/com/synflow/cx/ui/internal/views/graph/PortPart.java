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

import static org.eclipse.gef.EditPolicy.LAYOUT_ROLE;

import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import com.synflow.models.dpn.Connection;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Direction;
import com.synflow.models.dpn.Port;

/**
 * This class defines the edit part for a port.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class PortPart extends AbstractGraphicalEditPart implements NodeEditPart {

	@Override
	protected void createEditPolicies() {
		installEditPolicy(LAYOUT_ROLE, new SimpleLayoutEditPolicy());
	}

	@Override
	protected IFigure createFigure() {
		PortFigure figure = new PortFigure();
		Port port = getModel();
		figure.setLabel(port.getName());
		return figure;
	}

	@Override
	public final Port getModel() {
		return (Port) super.getModel();
	}

	@Override
	protected List<Connection> getModelSourceConnections() {
		Port port = getModel();
		DPN dpn = (DPN) port.eContainer();
		return dpn.getOutgoing(port);
	}

	@Override
	protected List<Connection> getModelTargetConnections() {
		Port port = getModel();
		DPN dpn = (DPN) port.eContainer();
		return dpn.getIncomingConnections(port);
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new PortAnchor(getFigure(), Direction.OUTGOING);
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new PortAnchor(getFigure(), Direction.OUTGOING);
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new PortAnchor(getFigure(), Direction.INCOMING);
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new PortAnchor(getFigure(), Direction.INCOMING);
	}

}
