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

import static com.synflow.cx.CxConstants.TYPE_READS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.synflow.cx.cx.Connect;
import com.synflow.cx.cx.CxPackage.Literals;
import com.synflow.cx.cx.Network;
import com.synflow.cx.cx.VarRef;
import com.synflow.cx.instantiation.IInstantiator;
import com.synflow.cx.internal.ErrorMarker;
import com.synflow.models.dpn.Connection;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.DpnFactory;
import com.synflow.models.dpn.Endpoint;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Instance;
import com.synflow.models.dpn.Port;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.util.TypePrinter;
import com.synflow.models.ir.util.TypeUtil;

/**
 * This class defines an helper class that creates Connections.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ExplicitConnector {

	private DPN dpn;

	@Inject
	private ConnectorHelper helper;

	@Inject
	private IInstantiator instantiator;

	/**
	 * A map whose keys are Instance or DPN, and whose values are ports that can be written to
	 * (input ports for instance, output ports for DPN).
	 */
	private Multimap<EObject, Port> portMap;

	private void addError(ErrorMarker marker) {
		Network network = (Network) marker.getSource().eContainer();
		network.getErrors().add(marker);
	}

	/**
	 * Checks the assignment from <code>typeSrc</code> to <code>typeTgt</code>.
	 * 
	 * @param sourceText
	 * @param typeSrc
	 * @param typeTgt
	 * @param source
	 * @param feature
	 * @param index
	 */
	private void checkAssign(String sourceText, Type typeSrc, Type typeTgt, Connect source,
			EStructuralFeature feature, int index) {
		if (typeSrc == null || typeTgt == null) {
			return;
		}

		if (!TypeUtil.canAssign(typeSrc, typeTgt)) {
			addError(new ErrorMarker("Type mismatch: cannot convert " + sourceText + " from "
					+ new TypePrinter().toString(typeSrc) + " to "
					+ new TypePrinter().toString(typeTgt), source, feature, index));
		}
	}

	private void checkPortAssociation(Connect connect, int index, VarRef ref, Port targetPort) {
		// get port types
		Port port = instantiator.getMapping(dpn, ref.getVariable());
		Type srcType = port.getType();
		Type tgtType = targetPort.getType();

		// check assign
		INode node = NodeModelUtils.getNode(ref);
		String srcName = "'" + NodeModelUtils.getTokenText(node) + "'";
		checkAssign(srcName, srcType, tgtType, connect, Literals.CONNECT__PORTS, index);

		// check ports have the same interface type
		Port sourcePort = instantiator.getPort(dpn, ref);
		if (sourcePort.getInterface() != targetPort.getInterface()) {
			addError(new ErrorMarker("Port mismatch: incompatible interface type between "
					+ srcName + " and '" + targetPort.getName() + "'", connect,
					Literals.CONNECT__PORTS, index));
		}
	}

	private void connect(Connect connect, Instance instance, VarRef ref, Port sourcePort,
			Port targetPort) {
		Endpoint otherEndPoint;
		if (sourcePort.eContainer() == dpn) {
			otherEndPoint = new Endpoint(dpn, sourcePort);
		} else {
			otherEndPoint = new Endpoint(helper.getInstance(dpn, ref), sourcePort);
		}

		Endpoint thisEndPoint;
		if (connect.isThis()) {
			thisEndPoint = new Endpoint(dpn, targetPort);
		} else {
			thisEndPoint = new Endpoint(instance, targetPort);
		}

		Connection conn;
		if (TYPE_READS.equals(connect.getType())) {
			conn = DpnFactory.eINSTANCE.createConnection(otherEndPoint, thisEndPoint);
		} else {
			conn = DpnFactory.eINSTANCE.createConnection(thisEndPoint, otherEndPoint);
		}

		dpn.getGraph().add(conn);
	}

	public void connect(Multimap<EObject, Port> portMap, Network network, DPN dpn) {
		this.portMap = portMap;
		this.dpn = dpn;

		for (Connect connect : network.getConnects()) {
			makeConnection(connect);
		}

		this.dpn = null;
		this.portMap = null;
	}

	private void makeConnection(Connect connect) {
		Instance instance = null;
		String name;

		Collection<Port> ports;
		if (connect.isThis()) {
			name = "this";
			if (TYPE_READS.equals(connect.getType())) {
				ports = portMap.get(dpn);
			} else { // TYPE_WRITES
				ports = dpn.getInputs();
			}
		} else {
			instance = instantiator.getMapping(dpn, connect.getInstance());
			name = instance.getName();
			Entity entity = instance.getEntity();
			if (TYPE_READS.equals(connect.getType())) {
				ports = portMap.get(instance);
			} else { // TYPE_WRITES
				ports = entity.getOutputs();
			}
		}

		Iterator<Port> it = ports.iterator();
		List<Port> targetPorts = new ArrayList<>();
		int index = 0;
		for (VarRef ref : connect.getPorts()) {
			if (index >= ports.size()) {
				if (TYPE_READS.equals(connect.getType())) {
					String kind = connect.isThis() ? "output" : "input";
					addError(new ErrorMarker("Connectivity: no more ports available, all " + kind
							+ " ports of '" + name + "' are already connected", connect));
				} else {
					addError(new ErrorMarker("Connectivity: too many ports given to '" + name
							+ ".writes', expected at most " + ports.size() + ", got "
							+ connect.getPorts().size(), connect));
				}
				break;
			}

			Port sourcePort = instantiator.getMapping(dpn, ref.getVariable());

			// removes sourcePort from portMap
			// this is done for any combination of this/instance and reads/writes
			// for ports other than (instance, input) and (dpn, output) this is a no-op
			Instance sourceInst = helper.getInstance(dpn, ref);
			if (sourceInst == null) {
				portMap.remove(dpn, sourcePort);
			} else {
				portMap.remove(sourceInst, sourcePort);
			}

			Port targetPort = it.next();
			targetPorts.add(targetPort);

			checkPortAssociation(connect, index, ref, targetPort);
			index++;

			connect(connect, instance, ref, sourcePort, targetPort);
		}

		// removes target ports accessed by "reads"
		if (TYPE_READS.equals(connect.getType())) {
			EObject key = connect.isThis() ? dpn : instance;
			for (Port port : targetPorts) {
				portMap.remove(key, port);
			}
		}
	}

}
