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

import com.synflow.models.ir.ExprBinary;
import com.synflow.models.ir.ExprCast;
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
	public Expression caseExprCast(ExprCast expr) {
		Type parent = TypeUtil.getType(expr.getExpr());
		expr.setExpr(transform(parent, expr.getExpr()));
		return caseExpression(expr);
	}

	@Override
	public Expression caseExpression(Expression expr) {
		return expr;
	}

	@Override
	public Expression caseExprUnary(ExprUnary expr) {
		Type parent = TypeUtil.getType(expr.getExpr());
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

}
