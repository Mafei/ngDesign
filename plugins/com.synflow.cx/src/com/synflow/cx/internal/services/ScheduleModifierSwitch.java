/*******************************************************************************
 * Copyright (c) 2012-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.internal.services;

import static com.synflow.cx.CxConstants.PROP_AVAILABLE;
import static com.synflow.cx.CxConstants.PROP_READ;

import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.ExpressionVariable;
import com.synflow.cx.cx.StatementFence;
import com.synflow.cx.cx.StatementIdle;
import com.synflow.cx.cx.StatementWrite;
import com.synflow.cx.cx.Variable;

/**
 * This class defines a switch that visits a statement and returns true if it (or any expression or
 * statement it contains) may be a schedule modifier like fence and idle, and any action on a port
 * (available, read, write).
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ScheduleModifierSwitch extends BoolCxSwitch {

	@Override
	public Boolean caseExpressionVariable(ExpressionVariable expr) {
		String prop = expr.getProperty();
		if (PROP_AVAILABLE.equals(prop) || PROP_READ.equals(prop)) {
			return true;
		}

		Variable variable = expr.getSource().getVariable();
		if (CxUtil.isFunctionNotConstant(variable)) {
			// if function has side-effect, we visit it
			if (doSwitch(variable)) {
				return true;
			}
		}

		return super.caseExpressionVariable(expr);
	}

	@Override
	public Boolean caseStatementFence(StatementFence stmt) {
		return true;
	}

	@Override
	public Boolean caseStatementIdle(StatementIdle stmt) {
		return true;
	}

	@Override
	public Boolean caseStatementWrite(StatementWrite stmt) {
		return true;
	}

}