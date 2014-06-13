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
package com.synflow.cflow.internal.instantiation;

import static com.synflow.cflow.CflowConstants.PROP_AVAILABLE;
import static com.synflow.cflow.CflowConstants.PROP_READ;
import static com.synflow.models.util.SwitchUtil.DONE;
import static com.synflow.models.util.SwitchUtil.visit;

import com.synflow.cflow.cflow.ExpressionVariable;
import com.synflow.cflow.cflow.StatementWrite;
import com.synflow.cflow.cflow.Task;
import com.synflow.cflow.cflow.VarRef;
import com.synflow.cflow.internal.services.VoidCflowSwitch;
import com.synflow.models.util.Void;

/**
 * This class visits and replaces references to implicit ports by actual ports.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ImplicitPortSwitch extends VoidCflowSwitch {

	private IInstantiator instantiator;

	public ImplicitPortSwitch(IInstantiator instantiator) {
		this.instantiator = instantiator;
	}

	@Override
	public Void caseExpressionVariable(ExpressionVariable expr) {
		VarRef ref = expr.getSource();
		String property = expr.getProperty();
		if (PROP_READ.equals(property) || PROP_AVAILABLE.equals(property)) {
			instantiator.getPort(ref);
		}

		return super.caseExpressionVariable(expr);
	}

	@Override
	public Void caseStatementWrite(StatementWrite stmt) {
		// visit value first
		super.caseStatementWrite(stmt);

		// visit port
		instantiator.getPort(stmt.getPort());
		return DONE;
	}

	@Override
	public Void caseTask(Task task) {
		// must implement caseTask because it is not in VoidCflowSwitch
		return visit(this, task.getDecls());
	}

}
