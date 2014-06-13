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
package com.synflow.models.ir.transform;

import com.synflow.models.dpn.Argument;
import com.synflow.models.dpn.Instance;
import com.synflow.models.ir.ExprBinary;
import com.synflow.models.ir.ExprUnary;
import com.synflow.models.ir.ExprVar;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.IrFactory;
import com.synflow.models.ir.Var;
import com.synflow.models.ir.util.IrSwitch;
import com.synflow.models.ir.util.IrUtil;

/**
 * This class defines a switch that visits an expression and returns a new expression in which
 * variables are replaced with actual values defined by the instance given to the constructor.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ExprResolver extends IrSwitch<Expression> {

	private Instance instance;

	/**
	 * Creates a new resolver with the given instance.
	 * 
	 * @param instance
	 *            an instance to use to solve variables
	 */
	public ExprResolver(Instance instance) {
		this.instance = instance;
	}

	@Override
	public ExprBinary caseExprBinary(ExprBinary expr) {
		Expression e1 = doSwitch(expr.getE1());
		Expression e2 = doSwitch(expr.getE2());
		return IrFactory.eINSTANCE.createExprBinary(e1, expr.getOp(), e2);
	}

	@Override
	public Expression caseExpression(Expression expr) {
		// by default, copy the expression
		return IrUtil.copy(expr);
	}

	@Override
	public ExprUnary caseExprUnary(ExprUnary expr) {
		Expression subExpr = doSwitch(expr.getExpr());
		return IrFactory.eINSTANCE.createExprUnary(expr.getOp(), subExpr);
	}

	@Override
	public Expression caseExprVar(ExprVar expr) {
		Var var = expr.getUse().getVariable();
		for (Argument arg : instance.getArguments()) {
			if (arg.getVariable() == var) {
				// found the right variable
				Expression value = arg.getValue();
				if (value == null) {
					// default parameters
					value = var.getInitialValue();
				}

				if (value == null) {
					// no value AND no default parameter?
					// something's wrong, just returns the original expression
					break;
				}

				// return a copy to avoid any unintended side-effects
				return IrUtil.copy(value);
			}
		}

		// cascade (delegates to caseExpression)
		// cannot use CASCADE because this switch is not void
		return null;
	}

}
