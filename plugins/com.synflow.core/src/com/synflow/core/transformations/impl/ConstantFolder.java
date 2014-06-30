/*******************************************************************************
 * Copyright (c) 2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.core.transformations.impl;

import com.synflow.models.ir.ExprBinary;
import com.synflow.models.ir.ExprBool;
import com.synflow.models.ir.ExprResize;
import com.synflow.models.ir.ExprUnary;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.IrFactory;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.OpUnary;
import com.synflow.models.ir.util.IrSwitch;

/**
 * This class defines a constant folder.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ConstantFolder extends IrSwitch<Expression> {

	public static boolean isFalse(Expression expr) {
		return expr.isExprBool() && !((ExprBool) expr).isValue();
	}

	public static boolean isTrue(Expression expr) {
		return expr.isExprBool() && ((ExprBool) expr).isValue();
	}

	@Override
	public Expression caseExprBinary(ExprBinary expr) {
		Expression e1 = doSwitch(expr.getE1());
		Expression e2 = doSwitch(expr.getE2());

		OpBinary op = expr.getOp();
		if (op == OpBinary.LOGIC_AND) {
			if (isFalse(e1) || isFalse(e2)) {
				return IrFactory.eINSTANCE.createExprBool(false);
			} else if (isTrue(e1)) {
				return e2;
			} else if (isTrue(e2)) {
				return e1;
			}
		}

		expr.setE1(e1);
		expr.setE2(e2);

		return expr;
	}

	@Override
	public Expression caseExpression(Expression expr) {
		return expr;
	}

	@Override
	public Expression caseExprResize(ExprResize expr) {
		Expression subExpr = doSwitch(expr.getExpr());
		expr.setExpr(subExpr);

		return expr;
	}

	@Override
	public Expression caseExprUnary(ExprUnary expr) {
		Expression subExpr = doSwitch(expr.getExpr());

		OpUnary op = expr.getOp();
		if (op == OpUnary.LOGIC_NOT) {
			if (isTrue(subExpr)) {
				return IrFactory.eINSTANCE.createExprBool(false);
			} else if (isFalse(subExpr)) {
				return IrFactory.eINSTANCE.createExprBool(true);
			}
		}

		expr.setExpr(subExpr);

		return expr;
	}

}
