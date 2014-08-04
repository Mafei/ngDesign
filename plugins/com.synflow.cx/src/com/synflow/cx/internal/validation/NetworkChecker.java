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
package com.synflow.cx.internal.validation;

import static com.synflow.core.IProperties.PROP_CLOCKS;
import static com.synflow.cx.CxConstants.DIR_IN;
import static com.synflow.cx.CxConstants.DIR_OUT;
import static org.eclipse.xtext.validation.ValidationMessageAcceptor.INSIGNIFICANT_INDEX;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

import com.google.common.base.Objects;
import com.google.gson.JsonArray;
import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Instantiable;
import com.synflow.cx.cx.Network;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.cx.CxPackage.Literals;
import com.synflow.cx.internal.instantiation.ClockDomainComputer;
import com.synflow.cx.internal.instantiation.IInstantiator;
import com.synflow.models.dpn.Connection;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Endpoint;
import com.synflow.models.dpn.Instance;
import com.synflow.models.dpn.Port;
import com.synflow.models.graph.Edge;

/**
 * This class defines a checker for networks.
 * 
 * @author Matthieu Wipliez
 * 
 */

public class NetworkChecker extends Checker {

	private final IInstantiator instantiator;

	public NetworkChecker(ValidationMessageAcceptor acceptor, IInstantiator instantiator) {
		super(acceptor);
		this.instantiator = instantiator;
	}

	/**
	 * Checks the clock domains of the given DPN.
	 * 
	 * @param network
	 *            source network. Used for error reporting.
	 * @param dpn
	 *            DPN
	 */
	private void checkClockDomains(Network network, DPN dpn) {
		JsonArray clocks = dpn.getProperties().getAsJsonArray(PROP_CLOCKS);
		if (clocks.size() < 2) {
			return;
		}

		ClockDomainComputer cdc = new ClockDomainComputer();
		for (Edge edge : dpn.getGraph().getEdges()) {
			Connection connection = (Connection) edge;

			Endpoint source = connection.getSourceEndpoint();
			Endpoint target = connection.getTargetEndpoint();
			if (source.hasInstance() && target.hasInstance()) {
				Instance srcInst = source.getInstance();
				Instance tgtInst = target.getInstance();
				if (cdc.isCombinational(srcInst) || cdc.isCombinational(tgtInst)) {
					continue;
				}

				String sourceClock = cdc.getClockDomain(source);
				String srcClkName;
				if (sourceClock == null) {
					srcClkName = "(unknown source)";
				} else {
					srcClkName = "(clock '" + sourceClock + "')";
				}

				String targetClock = cdc.getClockDomain(target);
				String tgtClkName;
				if (targetClock == null) {
					tgtClkName = "(unknown target)";
				} else {
					tgtClkName = "(clock '" + targetClock + "')";
				}

				if (!Objects.equal(sourceClock, targetClock)) {
					error("Clock domain: illegal crossing from '" + srcInst.getName() + "."
							+ source.getPort().getName() + "' " + srcClkName + " to '"
							+ tgtInst.getName() + "." + target.getPort().getName() + "' "
							+ tgtClkName, network, Literals.CX_ENTITY__NAME, INSIGNIFICANT_INDEX);
				}
			}
		}
	}

	/**
	 * Checks the connectivity of the given DPN.
	 * 
	 * @param network
	 *            source network. Used for error reporting.
	 * @param dpn
	 *            DPN
	 */
	private void checkConnectivity(Network network, DPN dpn) {
		for (Variable variable : CxUtil.getPorts(network.getPortDecls(), DIR_OUT)) {
			Port port = instantiator.getMapping(dpn, variable);
			int num = dpn.getNumIncoming(port);
			if (num == 0) {
				error("Connectivity: unconnected output port '" + port.getName() + "'", variable,
						Literals.VARIABLE__NAME, INSIGNIFICANT_INDEX);
			} else if (num > 1) {
				error("Connectivity: output port '" + port.getName()
						+ "' is connected too many times (expected 1, actual " + num + ")",
						variable, Literals.VARIABLE__NAME, INSIGNIFICANT_INDEX);
			}
		}

		for (Inst inst : network.getInstances()) {
			Instance instance = instantiator.getMapping(dpn, inst);
			Instantiable entity = inst.getEntity();
			if (entity == null) {
				entity = inst.getTask();
			}

			for (Variable variable : CxUtil.getPorts(entity.getPortDecls(), DIR_IN)) {
				Port port = instantiator.getMapping(instance.getEntity(), variable);
				int num = dpn.getNumIncoming(instance, port);

				EObject source;
				EStructuralFeature feature;
				if (inst.getTask() == null) {
					source = inst;
					feature = Literals.INST__NAME;
				} else {
					source = variable;
					feature = Literals.VARIABLE__NAME;
				}

				if (num == 0) {
					error("Connectivity: unconnected input port '" + port.getName() + "'", source,
							feature, INSIGNIFICANT_INDEX);
				} else if (num > 1) {
					error("Connectivity: input port '" + port.getName()
							+ "' is connected too many times (expected 1, actual " + num + ")",
							source, feature, INSIGNIFICANT_INDEX);
				}
			}
		}
	}

	public void checkDPN(Network network, DPN dpn) {
		checkConnectivity(network, dpn);
		checkClockDomains(network, dpn);
	}

}
