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
package com.synflow.core.transformations;

import java.util.Iterator;

import org.eclipse.emf.common.util.EList;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.synflow.models.ir.ExprBinary;
import com.synflow.models.ir.ExprList;
import com.synflow.models.ir.ExprResize;
import com.synflow.models.ir.ExprTypeConv;
import com.synflow.models.ir.ExprUnary;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.util.IrSwitch;
import com.synflow.models.ir.util.TypeUtil;

/**
 * This interface defines an IR visitor that transforms expressions to match the type system of the
 * target code.
 * 
 * @author Matthieu Wipliez
 * 
 */
public abstract class AbstractExpressionTransformer extends IrSwitch<Expression> {

	private Procedure procedure;

	private Type target;

	@Override
	public Expression caseExprBinary(ExprBinary expr) {
		Type parent = TypeUtil.getType(expr);
		expr.setE1(transform(parent, expr.getE1()));
		expr.setE2(transform(parent, expr.getE2()));
		return caseExpression(expr);
	}

	@Override
	public Expression caseExpression(Expression expr) {
		return expr;
	}

	@Override
	public Expression caseExprList(ExprList expr) {
		visitExprList(expr.getValue());
		return caseExpression(expr);
	}

	@Override
	public Expression caseExprResize(ExprResize resize) {
		Type parent = TypeUtil.getType(resize.getExpr());
		resize.setExpr(transform(parent, resize.getExpr()));
		return caseExpression(resize);
	}

	@Override
	public Expression caseExprTypeConv(ExprTypeConv typeConv) {
		Type parent = TypeUtil.getType(typeConv.getExpr());
		typeConv.setExpr(transform(parent, typeConv.getExpr()));
		return caseExpression(typeConv);
	}

	@Override
	public Expression caseExprUnary(ExprUnary expr) {
		Type parent = TypeUtil.getType(expr);
		expr.setExpr(transform(parent, expr.getExpr()));
		return caseExpression(expr);
	}

	protected Procedure getProcedure() {
		return procedure;
	}

	protected Type getTarget() {
		return target;
	}

	public void setProcedure(Procedure procedure) {
		this.procedure = procedure;
	}

	public Expression transform(Type target, Expression expression) {
		Type oldTarget = this.target;
		this.target = target;
		Expression result = doSwitch(expression);
		this.target = oldTarget;
		return result;
	}

	private void visitExprList(EList<Expression> indexes) {
		visitExprList(Functions.constant(target), indexes);
	}

	private void visitExprList(Function<Object, Type> fun, EList<Expression> indexes) {
		int i = 0;
		while (i < indexes.size()) {
			final Expression expr = indexes.get(i);
			final Expression res = transform(fun.apply(null), expr);
			if (res != expr) {
				// replace "expr" by "res"
				// we need to use "add" and not "set" because "expr" was removed from the list
				// so the list might not be big enough
				indexes.add(i, res);
			}
			i++;
		}
	}

	protected void visitExprList(Iterable<? extends Type> types, EList<Expression> indexes) {
		final Iterator<? extends Type> it = types.iterator();
		visitExprList(new Function<Object, Type>() {
			@Override
			public Type apply(Object arg0) {
				return it.next();
			}
		}, indexes);
	}

}
