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
package com.synflow.core.transformations.impl;

import static com.synflow.models.ir.IrFactory.eINSTANCE;

import com.synflow.core.transformations.AbstractExpressionTransformer;
import com.synflow.models.ir.ExprBinary;
import com.synflow.models.ir.ExprInt;
import com.synflow.models.ir.ExprResize;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.TypeInt;
import com.synflow.models.ir.util.TypeUtil;

/**
 * This class resizes integer literals to match the size of their target's type (when assigning to a
 * variable) or the size of the surrounding expression.
 * 
 * @author Matthieu Wipliez
 *
 */
public class HDLTyper extends AbstractExpressionTransformer {

	@Override
	public Expression caseExprBinary(ExprBinary expr) {
		OpBinary op = expr.getOp();
		Type t1 = TypeUtil.getType(expr.getE1());
		Type t2 = TypeUtil.getType(expr.getE2());

		Type type;
		if (op == OpBinary.TIMES) {
			// cast to their respective type
			expr.setE1(transform(t1, expr.getE1()));
			expr.setE2(transform(t2, expr.getE2()));
			type = getTarget();
		} else {
			if (op.isArithmetic() || op == OpBinary.SHIFT_LEFT) {
				// cast to target size (as computed by IR type system)
				type = TypeUtil.getType(expr, true);
			} else {
				// cast to largest common type
				type = TypeUtil.getLargest(t1, t2);
			}

			expr.setE1(transform(type, expr.getE1()));
			expr.setE2(transform(type, expr.getE2()));
		}

		if (op.isComparison()) {
			if (t1.isInt() && t2.isInt()) {
				boolean isSignedT1 = ((TypeInt) t1).isSigned();
				boolean isSignedT2 = ((TypeInt) t2).isSigned();
				if (isSignedT1 ^ isSignedT2) {
					if (isSignedT2) {
						expr.setE1(eINSTANCE.cast(type, t1, expr.getE1()));
					} else {
						expr.setE2(eINSTANCE.cast(type, t2, expr.getE2()));
					}
				}
			}
			return expr;
		}

		return eINSTANCE.cast(getTarget(), type, expr);
	}

	@Override
	public Expression caseExpression(Expression expr) {
		if (expr.isExprList()) {
			return expr;
		}

		Type type = TypeUtil.getType(expr);
		return eINSTANCE.cast(getTarget(), type, expr);
	}

	@Override
	public Expression caseExprInt(ExprInt expr) {
		// set the size of integer literals
		int size = TypeUtil.getSize(getTarget());
		expr.setSize(size);
		return expr;
	}

	@Override
	public Expression caseExprResize(ExprResize resize) {
		// resize literals directly
		Expression expr = resize.getExpr();
		if (expr.isExprInt()) {
			((ExprInt) expr).setSize(resize.getTargetSize());
			return expr;
		}

		// visit sub expression
		return super.caseExprResize(resize);
	}

}
