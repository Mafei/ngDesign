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
package com.synflow.cx.internal.instantiation;

import static com.synflow.cx.CxConstants.PROP_AVAILABLE;
import static com.synflow.cx.CxConstants.PROP_READ;
import static com.synflow.models.util.SwitchUtil.DONE;
import static com.synflow.models.util.SwitchUtil.visit;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.synflow.cx.cx.ExpressionVariable;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Network;
import com.synflow.cx.cx.StatementWrite;
import com.synflow.cx.cx.Task;
import com.synflow.cx.cx.VarRef;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.instantiation.IInstantiator;
import com.synflow.cx.internal.services.VoidCxSwitch;
import com.synflow.models.dpn.Connection;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.DpnFactory;
import com.synflow.models.dpn.Endpoint;
import com.synflow.models.dpn.Instance;
import com.synflow.models.dpn.Port;
import com.synflow.models.ir.util.IrUtil;
import com.synflow.models.util.Void;

/**
 * This class visits and replaces references to implicit ports by actual ports.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ImplicitConnector extends VoidCxSwitch {

	private DPN dpn;

	@Inject
	private ConnectorHelper helper;

	private Instance instance;

	@Inject
	private IInstantiator instantiator;

	/**
	 * A map whose keys are Instance or DPN, and whose values are ports that can be written to
	 * (input ports for instance, output ports for DPN).
	 */
	private Multimap<EObject, Port> portMap;

	@Override
	public Void caseExpressionVariable(ExpressionVariable expr) {
		VarRef ref = expr.getSource();
		String property = expr.getProperty();
		if (PROP_READ.equals(property) || PROP_AVAILABLE.equals(property)) {
			visitPort(ref);
		}

		return super.caseExpressionVariable(expr);
	}

	@Override
	public Void caseInst(Inst inst) {
		instance = instantiator.getMapping(dpn, inst);
		visit(this, inst.getTask());
		instance = null;
		return DONE;
	}

	@Override
	public Void caseStatementWrite(StatementWrite stmt) {
		// visit value first
		super.caseStatementWrite(stmt);

		visitPort(stmt.getPort());
		return DONE;
	}

	@Override
	public Void caseTask(Task task) {
		// must implement caseTask because it is not in VoidCxSwitch
		return visit(this, task.getDecls());
	}

	/**
	 * Connects the given network.
	 * 
	 * @param portMap
	 * @param network
	 * @param dpn
	 */
	public void connect(Multimap<EObject, Port> portMap, Network network, DPN dpn) {
		this.portMap = portMap;
		this.dpn = dpn;

		visit(this, network.getInstances());

		this.dpn = null;
		this.portMap = null;
	}

	/**
	 * Creates a new port from the given parameters, and adds a connection to the DPN associated
	 * with the network containing the instance.
	 * 
	 * @param link
	 *            link
	 * @param instance
	 *            an instance
	 * @param otherPort
	 *            IR port
	 * @param ref
	 *            reference to the port
	 * @return a new IR port
	 */
	private Port getConnectedPort(String link, Instance instance, Endpoint otherEndPoint) {
		boolean isDpnPort = !otherEndPoint.hasInstance();

		// create connection
		DPN dpn = instance.getDPN();
		Port otherPort = otherEndPoint.getPort();
		boolean isInput = IrUtil.isInput(otherPort);

		// compute otherEndPoint and portName
		Port thisPort = IrUtil.copy(otherPort);
		if (isDpnPort) {
			// this port is defined in this instance or containing netwok
			String portName = dpn.getSimpleName() + "_" + otherPort.getName();
			thisPort.setName(portName);
		} else {
			// this port is defined by another instance
			String portName = otherEndPoint.getInstance().getName() + "_" + otherPort.getName();
			thisPort.setName(portName);

			// if this is an input port, remove it from the port map
			if (isInput) {
				portMap.remove(otherEndPoint.getInstance(), otherPort);
			}
		}

		// create connection, add port to entity
		Connection conn;
		Endpoint thisEndPoint = new Endpoint(instance, thisPort);
		if (!(isDpnPort ^ isInput)) { // both 0 or both 1
			instance.getEntity().getInputs().add(thisPort);
			conn = DpnFactory.eINSTANCE.createConnection(otherEndPoint, thisEndPoint);
		} else {
			instance.getEntity().getOutputs().add(thisPort);
			conn = DpnFactory.eINSTANCE.createConnection(thisEndPoint, otherEndPoint);
		}

		// add connection to graph
		dpn.getGraph().add(conn);

		return thisPort;
	}

	private void visitPort(VarRef ref) {
		Variable cxPort = ref.getVariable();
		Port port = instantiator.getMapping(instance.getEntity(), cxPort);
		if (port == null) {
			// reference is to another instance's port
			INode node = NodeModelUtils.getNode(ref);
			final String link = NodeModelUtils.getTokenText(node);

			Endpoint otherEndpoint = helper.getEndpoint(dpn, ref);
			port = instantiator.getMapping(instance.getEntity(), otherEndpoint);
			if (port == null) {
				// we add a port to this entity and connect it to the other instance
				port = getConnectedPort(link, instance, otherEndpoint);
				instantiator.putMapping(instance.getEntity(), otherEndpoint, port);
			}
		}

		instantiator.putMapping(instance.getEntity(), ref, port);
	}

}
