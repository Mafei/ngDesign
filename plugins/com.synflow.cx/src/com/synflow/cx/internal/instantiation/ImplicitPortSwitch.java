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

import static com.synflow.cx.CflowConstants.PROP_AVAILABLE;
import static com.synflow.cx.CflowConstants.PROP_READ;
import static com.synflow.models.util.SwitchUtil.DONE;
import static com.synflow.models.util.SwitchUtil.visit;

import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.synflow.cx.cx.ExpressionVariable;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.StatementWrite;
import com.synflow.cx.cx.Task;
import com.synflow.cx.cx.VarRef;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.internal.services.VoidCflowSwitch;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Endpoint;
import com.synflow.models.dpn.Instance;
import com.synflow.models.dpn.Port;
import com.synflow.models.util.Void;

/**
 * This class visits and replaces references to implicit ports by actual ports.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ImplicitPortSwitch extends VoidCflowSwitch {

	private Instance instance;

	private InstantiatorImpl instantiator;

	private ConnectionMaker maker;

	public ImplicitPortSwitch(InstantiatorImpl instantiator, ConnectionMaker maker) {
		this.instantiator = instantiator;
		this.maker = maker;
	}

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
	public Void caseStatementWrite(StatementWrite stmt) {
		// visit value first
		super.caseStatementWrite(stmt);

		visitPort(stmt.getPort());
		return DONE;
	}

	@Override
	public Void caseTask(Task task) {
		// must implement caseTask because it is not in VoidCflowSwitch
		return visit(this, task.getDecls());
	}

	/**
	 * Returns the endpoint associated with the port associated with the given reference.
	 * 
	 * @param ref
	 *            reference to a port in another instance or in the containing network
	 * @return an endpoint
	 */
	private Endpoint getEndpoint(VarRef ref) {
		Variable cxPort = ref.getVariable();
		Instance otherInst = maker.getInstance(ref);
		if (otherInst == null) {
			DPN dpn = instance.getDPN();
			Port otherPort = instantiator.getMapping(dpn, cxPort);
			return new Endpoint(dpn, otherPort);
		} else {
			Port otherPort = instantiator.getMapping(otherInst.getEntity(), cxPort);
			return new Endpoint(otherInst, otherPort);
		}
	}

	public void visitInst(Inst inst, Instance instance) {
		this.instance = instance;
		visit(this, inst.getTask());
	}

	private void visitPort(VarRef ref) {
		Variable cxPort = ref.getVariable();
		Port port = instantiator.getMapping(instance.getEntity(), cxPort);
		if (port == null) {
			// reference is to another instance's port
			INode node = NodeModelUtils.getNode(ref);
			final String link = NodeModelUtils.getTokenText(node);

			Endpoint otherEndpoint = getEndpoint(ref);
			Port otherPort = otherEndpoint.getPort();
			port = instantiator.getMapping(instance.getEntity(), otherPort);
			if (port == null) {
				// we add a port to this entity and connect it to the other instance
				port = maker.getConnectedPort(link, instance, otherEndpoint);
				instantiator.putMapping(instance.getEntity(), otherPort, port);
			}
		}

		instantiator.putMapping(instance.getEntity(), ref, port);
	}

}
