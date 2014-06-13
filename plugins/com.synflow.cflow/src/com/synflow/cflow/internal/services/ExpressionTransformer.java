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
package com.synflow.cflow.internal.services;

import static com.synflow.models.ir.IrFactory.eINSTANCE;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.synflow.cflow.cflow.CExpression;
import com.synflow.cflow.cflow.ExpressionBinary;
import com.synflow.cflow.cflow.ExpressionBoolean;
import com.synflow.cflow.cflow.ExpressionFloat;
import com.synflow.cflow.cflow.ExpressionIf;
import com.synflow.cflow.cflow.ExpressionInteger;
import com.synflow.cflow.cflow.ExpressionString;
import com.synflow.cflow.cflow.ExpressionUnary;
import com.synflow.cflow.cflow.ExpressionVariable;
import com.synflow.cflow.cflow.util.CflowSwitch;
import com.synflow.cflow.internal.compiler.Transformer;
import com.synflow.cflow.internal.instantiation.IInstantiator;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.OpUnary;
import com.synflow.models.ir.Var;

/**
 * This class transforms C~ expressions into IR expressions.
 * 
 * @author Matthieu Wipliez
 */
public class ExpressionTransformer extends CflowSwitch<Expression> implements Transformer {

	private IInstantiator instantiator;

	public ExpressionTransformer(IInstantiator instantiator) {
		this.instantiator = instantiator;
	}

	@Override
	public Expression caseExpressionBinary(ExpressionBinary expression) {
		OpBinary op = OpBinary.getOperator(expression.getOperator());
		Expression e1 = doSwitch(expression.getLeft());
		Expression e2 = doSwitch(expression.getRight());
		return eINSTANCE.createExprBinary(e1, op, e2);
	}

	@Override
	public Expression caseExpressionBoolean(ExpressionBoolean expression) {
		return eINSTANCE.createExprBool(expression.isValue());
	}

	@Override
	public Expression caseExpressionFloat(ExpressionFloat expression) {
		return eINSTANCE.createExprFloat(expression.getValue());
	}

	@Override
	public Expression caseExpressionIf(ExpressionIf expression) {
		return eINSTANCE.createExprTernary(doSwitch(expression.getCondition()),
				doSwitch(expression.getThen()), doSwitch(expression.getElse()));
	}

	@Override
	public Expression caseExpressionInteger(ExpressionInteger expression) {
		return eINSTANCE.createExprInt(expression.getValue());
	}

	@Override
	public Expression caseExpressionString(ExpressionString expression) {
		return eINSTANCE.createExprString(expression.getValue());
	}

	@Override
	public Expression caseExpressionUnary(ExpressionUnary expression) {
		CExpression subExpr = expression.getExpression();
		OpUnary op = OpUnary.getOperator(expression.getUnaryOperator());
		switch (op) {
		case MINUS:
			// replace ExprUnary(-, n) by ExprInt(-n)
			if (subExpr instanceof ExpressionInteger) {
				ExpressionInteger exprInt = (ExpressionInteger) subExpr;
				return eINSTANCE.createExprInt(exprInt.getValue().negate());
			}
			// fall-through for other expressions
		default: {
			Expression expr = doSwitch(expression.getExpression());
			return eINSTANCE.createExprUnary(op, expr);
		}
		}
	}

	@Override
	public Expression caseExpressionVariable(ExpressionVariable expression) {
		if (expression.getProperty() != null || !expression.getIndexes().isEmpty()
				|| !expression.getParameters().isEmpty()) {
			throw new UnsupportedOperationException();
		}

		Var var = instantiator.getVar(expression.getSource().getVariable());
		return eINSTANCE.createExprVar(var);
	}

	@Override
	public Expression transformExpr(CExpression expression) {
		return doSwitch(expression);
	}

	/**
	 * Returns an IR expression from the given JSON element.
	 * 
	 * @param json
	 *            a JSON element (should be a primitive)
	 * @return an expression, or <code>null</code>
	 */
	public Expression transformJson(JsonElement json) {
		if (json.isJsonPrimitive()) {
			JsonPrimitive primitive = json.getAsJsonPrimitive();
			if (primitive.isBoolean()) {
				return eINSTANCE.createExprBool(primitive.getAsBoolean());
			} else if (primitive.isNumber()) {
				return eINSTANCE.createExprInt(primitive.getAsBigInteger());
			} else if (primitive.isString()) {
				return eINSTANCE.createExprString(primitive.getAsString());
			}
		}
		return null;
	}

}
