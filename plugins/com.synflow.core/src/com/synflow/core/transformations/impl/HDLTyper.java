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
import static java.math.BigInteger.ONE;

import java.math.BigInteger;

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
 * This class add resize and type conversions to match common HDL behavior. This class must be
 * extended to handle VHDL-specific and Verilog-specific behavior.
 * 
 * @author Matthieu Wipliez
 *
 */
public abstract class HDLTyper extends AbstractExpressionTransformer {

	@Override
	public Expression caseExprBinary(ExprBinary expr) {
		OpBinary op = expr.getOp();
		Type t1 = TypeUtil.getType(expr.getE1());
		Type t2 = TypeUtil.getType(expr.getE2());

		Type type;
		if (op.isArithmetic() || op == OpBinary.SHIFT_LEFT) {
			// cast to target size (as computed by IR type system)
			type = TypeUtil.getType(expr, true);
		} else {
			// cast to largest common type
			type = TypeUtil.getLargest(t1, t2);
		}

		expr.setE1(transform(type, expr.getE1()));
		expr.setE2(transform(type, expr.getE2()));

		if (op.isComparison()) {
			if (t1.isInt() && t2.isInt()) {
				boolean isSignedT1 = ((TypeInt) t1).isSigned();
				boolean isSignedT2 = ((TypeInt) t2).isSigned();
				if (isSignedT1 ^ isSignedT2) {
					if (isSignedT2) {
						expr.setE1(cast(type, t1, expr.getE1()));
					} else {
						expr.setE2(cast(type, t2, expr.getE2()));
					}
				}
			}
			return expr;
		}

		return cast(getTarget(), type, expr);
	}

	@Override
	public Expression caseExpression(Expression expr) {
		if (expr.isExprList()) {
			return expr;
		}

		Type type = TypeUtil.getType(expr);
		return cast(getTarget(), type, expr);
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
			ExprInt exprInt = (ExprInt) expr;
			BigInteger value = exprInt.getValue();
			int size = TypeUtil.getSize(value);

			if (resize.getTargetSize() < size) {
				BigInteger unsigned = getUnsigned(value, size);
				BigInteger mask = ONE.shiftLeft(resize.getTargetSize()).subtract(ONE);
				value = unsigned.and(mask);
			} else {
				value = getUnsigned(value, resize.getTargetSize());
			}

			exprInt.setValue(value);
			exprInt.setSize(resize.getTargetSize());
			return expr;
		}

		// visit sub expression
		return super.caseExprResize(resize);
	}

	protected Expression cast(Type target, Type source, Expression expr) {
		return eINSTANCE.cast(target, source, expr);
	}

	private BigInteger getUnsigned(BigInteger value, int size) {
		if (value.signum() < 0) {
			return ONE.shiftLeft(size).add(value);
		} else {
			return value;
		}
	}

}
