/*******************************************************************************
 * Copyright (c) 2012-2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.internal.services;

import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.CxExpression;
import com.synflow.cx.cx.ExpressionBinary;
import com.synflow.cx.cx.ExpressionVariable;
import com.synflow.cx.cx.Statement;
import com.synflow.cx.cx.StatementAssign;
import com.synflow.cx.cx.StatementLoop;
import com.synflow.cx.cx.StatementVariable;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.instantiation.IInstantiator;
import com.synflow.models.dpn.Entity;

/**
 * This class defines a switch that extends the statement switch to handle loops. It returns true if
 * a loop is complex (i.e. if it is not compile-time unrollable).
 * 
 * @author Matthieu Wipliez
 * 
 */
public class LoopSwitch extends ScheduleModifierSwitch {

	private Entity entity;

	private IInstantiator instantiator;

	public LoopSwitch(IInstantiator instantiator, Entity entity) {
		this.instantiator = instantiator;
		this.entity = entity;
	}

	@Override
	public Boolean caseStatementLoop(StatementLoop stmt) {
		// first check if loop contains cycle modifiers or other complex loops
		if (!super.caseStatementLoop(stmt)) {
			Statement init = stmt.getInit();
			StatementAssign after = stmt.getAfter();
			if (init != null && after != null) {
				if (init instanceof StatementAssign) {
					StatementAssign assign = (StatementAssign) init;
					Variable variable = assign.getTarget().getSource().getVariable();
					if (CxUtil.isLocal(variable)) {
						Object value = instantiator.evaluate(entity, assign.getValue());
						if (value != null) {
							return checkBounds(variable, value, stmt.getCondition());
						}
					}
				} else if (init instanceof StatementVariable) {
					StatementVariable stmtVar = (StatementVariable) init;
					for (Variable variable : stmtVar.getVariables()) {
						Object value = instantiator.evaluate(entity, variable.getValue());
						if (value == null || checkBounds(variable, value, stmt.getCondition())) {
							return true;
						}
					}
					return false;
				}
			}
		}

		return true;
	}

	private boolean checkBounds(Variable variable, Object init, CxExpression condition) {
		if (condition instanceof ExpressionBinary) {
			ExpressionBinary exprBin = (ExpressionBinary) condition;
			CxExpression left = exprBin.getLeft();
			CxExpression right = exprBin.getRight();

			if (left instanceof ExpressionVariable) {
				ExpressionVariable exprVar = (ExpressionVariable) left;
				if (exprVar.getSource().getVariable() == variable && exprVar.getIndexes().isEmpty()) {
					Object max = instantiator.evaluate(entity, right);
					if (max != null) {
						// loop is not complex
						return false;
					}
				}
			}
		}

		return true;
	}

}