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
		expr.setE1(doSwitch(expr.getE1()));
		expr.setE2(doSwitch(expr.getE2()));
		return expr;
	}

	@Override
	public Expression caseExprCast(ExprCast expr) {
		expr.setExpr(doSwitch(expr.getExpr()));
		return expr;
	}

	@Override
	public final Expression caseExpression(Expression expr) {
		return expr;
	}

	@Override
	public Expression caseExprUnary(ExprUnary expr) {
		expr.setExpr(doSwitch(expr.getExpr()));
		return expr;
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
		this.target = target;
		Expression result = doSwitch(expression);
		this.target = null;
		return result;
	}

}
