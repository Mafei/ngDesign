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

import static com.synflow.models.ir.util.IrUtil.getDirection;
import static org.eclipse.gef.EditPolicy.LAYOUT_ROLE;

import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import com.google.common.base.Function;
import com.synflow.models.dpn.Connection;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Endpoint;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Instance;
import com.synflow.models.dpn.Port;
import com.synflow.models.graph.Edge;
import com.synflow.models.ir.util.IrUtil;

/**
 * This class defines the edit part for an instance.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class InstancePart extends AbstractGraphicalEditPart implements NodeEditPart {

	private Map<Port, IFigure> ports;

	@Override
	protected void createEditPolicies() {
		installEditPolicy(LAYOUT_ROLE, new SimpleLayoutEditPolicy());
	}

	@Override
	protected IFigure createFigure() {
		InstanceFigure figure = new InstanceFigure();
		final Instance instance = getInstance();
		final DPN dpn = instance.getDPN();
		figure.setLabel(instance.getName());

		Entity entity = instance.getEntity();
		ports = figure.setPorts(new Function<Port, Boolean>() {
			@Override
			public Boolean apply(Port port) {
				if (IrUtil.isInput(port)) {
					Endpoint incoming = dpn.getIncoming(instance, port);
					return incoming != null;
				} else {
					List<Endpoint> endpoints = dpn.getOutgoing(new Endpoint(instance, port));
					return !endpoints.isEmpty();
				}
			}
		}, entity.getInputs(), entity.getOutputs());
		return figure;
	}

	private ConnectionAnchor getAnchor(Port port) {
		IFigure fig = ports.get(port);
		return new PortAnchor(fig, getDirection(port));
	}

	private Instance getInstance() {
		return (Instance) getModel();
	}

	@Override
	protected List<Edge> getModelSourceConnections() {
		return getInstance().getOutgoing();
	}

	@Override
	protected List<Edge> getModelTargetConnections() {
		return getInstance().getIncoming();
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connPart) {
		Connection connection = (Connection) connPart.getModel();
		return getAnchor(connection.getSourcePort());
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return null;
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connPart) {
		Connection connection = (Connection) connPart.getModel();
		return getAnchor(connection.getTargetPort());
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return null;
	}

}
