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

import java.util.LinkedHashSet;
import java.util.Set;

import com.synflow.core.transformations.AbstractExpressionTransformer;
import com.synflow.models.ir.ExprBinary;
import com.synflow.models.ir.ExprCast;
import com.synflow.models.ir.ExprInt;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.Procedure;
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

	private Set<Expression> visited;

	@Override
	public Expression caseExprBinary(ExprBinary expr) {
		OpBinary op = expr.getOp();
		if (op.isComparison()) {
			Type t1 = TypeUtil.getType(expr.getE1());
			Type t2 = TypeUtil.getType(expr.getE2());

			Type common = TypeUtil.getLargest(t1, t2);
			expr.setE1(transform(common, expr.getE1()));
			expr.setE2(transform(common, expr.getE2()));

			if (t1.isInt() && t2.isInt()) {
				TypeInt ti1 = (TypeInt) t1;
				TypeInt ti2 = (TypeInt) t2;
				TypeInt tiCommon = (TypeInt) TypeUtil.getLargest(t1, t2);

				if (ti1.isSigned() ^ ti2.isSigned()) {
					if (ti2.isSigned()) {
						expr.setE1(castIfNeeded(tiCommon, ti1, expr.getE1()));
					} else {
						expr.setE2(castIfNeeded(tiCommon, ti2, expr.getE2()));
					}
				}
			}
		} else {
			// cast to size computed by IR type system
			// example: in "(a * b) >> 1", (a * b) is cast to size(a) * size(b)

			Type type = TypeUtil.getType(expr, true);
			expr.setE1(transform(type, expr.getE1()));
			expr.setE2(transform(type, expr.getE2()));
		}

		return caseExpression(expr);
	}

	@Override
	public Expression caseExprCast(ExprCast cast) {
		if (visited.contains(cast)) {
			return cast;
		}
		visited.add(cast);

		// visit sub expression
		return super.caseExprCast(cast);
	}

	@Override
	public Expression caseExpression(Expression expr) {
		if (expr.isExprList()) {
			return expr;
		}

		Type type = TypeUtil.getType(expr);
		return castIfNeeded(getTarget(), type, expr);
	}

	private Expression castIfNeeded(Type target, Type type, Expression expr) {
		int size = TypeUtil.getSize(target);
		if (expr.isExprInt()) {
			((ExprInt) expr).setSize(size);
		} else if (size != TypeUtil.getSize(type)) {
			return eINSTANCE.createExprCast(target, type, expr);
		}
		return expr;
	}

	@Override
	public void setProcedure(Procedure procedure) {
		visited = new LinkedHashSet<>();
		super.setProcedure(procedure);
	}

}
