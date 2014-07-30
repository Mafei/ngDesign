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
package com.synflow.cflow.internal.instantiation.v2;

import static com.synflow.cflow.CflowConstants.PROP_AVAILABLE;
import static com.synflow.cflow.CflowConstants.PROP_READ;
import static com.synflow.models.util.SwitchUtil.DONE;
import static com.synflow.models.util.SwitchUtil.visit;

import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.synflow.cflow.cflow.ExpressionVariable;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.StatementWrite;
import com.synflow.cflow.cflow.Task;
import com.synflow.cflow.cflow.VarRef;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.internal.services.VoidCflowSwitch;
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

	public void visitInst(Inst inst, Instance instance) {
		this.instance = instance;
		visit(this, inst.getTask());
	}

	private void visitPort(VarRef ref) {
		Port port = instantiator.getMapping(instance.getEntity(), ref);
		if (port == null) {
			INode node = NodeModelUtils.getNode(ref);
			final String link = NodeModelUtils.getTokenText(node);

			// reference to a port from an instance
			Variable cxPort = ref.getVariable();
			Instance otherInst = maker.getInstance(ref);
			Port otherPort = instantiator.getMapping(otherInst.getEntity(), cxPort);

			port = maker.getConnectedPort(link, instance, otherPort, ref);
			instantiator.putMapping(instance.getEntity(), ref, port);
		}
	}

}
