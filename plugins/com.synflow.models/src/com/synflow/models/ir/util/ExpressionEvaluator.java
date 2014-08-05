/*
 * Copyright (c) 2009, IETR/INSA of Rennes
 * Copyright (c) 2012-2013, Synflow SAS
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the IETR/INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.synflow.models.ir.util;

import java.math.BigInteger;

import com.synflow.models.ir.ExprBinary;
import com.synflow.models.ir.ExprBool;
import com.synflow.models.ir.ExprFloat;
import com.synflow.models.ir.ExprInt;
import com.synflow.models.ir.ExprString;
import com.synflow.models.ir.ExprUnary;
import com.synflow.models.ir.ExprVar;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.Var;

/**
 * This class defines an expression evaluator.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ExpressionEvaluator extends IrSwitch<Object> {

	@Override
	public Object caseExprBinary(ExprBinary expr) {
		Object val1 = doSwitch(expr.getE1());
		Object val2 = doSwitch(expr.getE2());
		return ValueUtil.compute(val1, expr.getOp(), val2);
	}

	@Override
	public Object caseExprBool(ExprBool expr) {
		return expr.isValue();
	}

	@Override
	public Object caseExprFloat(ExprFloat expr) {
		return expr.getValue();
	}

	@Override
	public Object caseExprInt(ExprInt expr) {
		return expr.getValue();
	}

	@Override
	public Object caseExprString(ExprString expr) {
		// note the difference with the caseExprString method from the
		// expression printer: here we return the string without quotes
		return expr.getValue();
	}

	@Override
	public Object caseExprUnary(ExprUnary expr) {
		Object value = doSwitch(expr.getExpr());
		return ValueUtil.compute(expr.getOp(), value);
	}

	@Override
	public Object caseExprVar(ExprVar expr) {
		Var var = expr.getUse().getVariable();
		Object value = var.getValue();
		if (value == null) {
			// return the variable's initial value
			value = ValueUtil.getValue(var.getInitialValue());
			var.setValue(value);
		}

		return value;
	}

	/**
	 * Evaluates this expression and return its value as an integer.
	 * 
	 * @param expr
	 *            an expression to evaluate
	 * @return the expression evaluated as an integer
	 * @throws OrccRuntimeException
	 *             if the expression cannot be evaluated as an integer
	 */
	public int evaluateAsInteger(Expression expr) {
		Object value = doSwitch(expr);
		if (ValueUtil.isInt(value)) {
			return ((BigInteger) value).intValue();
		}

		// evaluated ok, but not as an integer
		throw new IllegalArgumentException("expected integer expression");
	}

}
