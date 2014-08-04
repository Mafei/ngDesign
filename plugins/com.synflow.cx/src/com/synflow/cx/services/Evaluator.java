/*******************************************************************************
 * Copyright (c) 2012-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * Copyright (c) 2009-2011, IETR/INSA of Rennes
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
package com.synflow.cx.services;

import java.lang.reflect.Array;
import java.math.BigInteger;

import org.eclipse.emf.ecore.EObject;

import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.CExpression;
import com.synflow.cx.cx.ExpressionBinary;
import com.synflow.cx.cx.ExpressionBoolean;
import com.synflow.cx.cx.ExpressionFloat;
import com.synflow.cx.cx.ExpressionIf;
import com.synflow.cx.cx.ExpressionInteger;
import com.synflow.cx.cx.ExpressionString;
import com.synflow.cx.cx.ExpressionUnary;
import com.synflow.cx.cx.ExpressionVariable;
import com.synflow.cx.cx.Value;
import com.synflow.cx.cx.ValueExpr;
import com.synflow.cx.cx.ValueList;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.cx.util.CxSwitch;
import com.synflow.models.OrccRuntimeException;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.OpUnary;
import com.synflow.models.ir.util.ValueUtil;

/**
 * This class defines an expression evaluator.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class Evaluator extends CxSwitch<Object> {

	/**
	 * Returns the integer value associated with the given object using its URI. Returns -1 if the
	 * value is not an integer.
	 * 
	 * @param eObject
	 *            an AST node
	 * @return the integer value associated with the given object
	 */
	public static int getIntValue(EObject eObject) {
		Object value = getValue(eObject);
		if (value != null && ValueUtil.isInt(value)) {
			BigInteger intExpr = (BigInteger) value;
			if (intExpr.bitLength() < Integer.SIZE) {
				return intExpr.intValue();
			}
		}

		// evaluated ok, but not as an integer
		return -1;
	}

	/**
	 * Returns the value associated with the given object using its URI.
	 * 
	 * @param eObject
	 *            an AST node
	 * @return the value associated with the given object
	 */
	public static Object getValue(EObject eObject) {
		try {
			return new Evaluator().doSwitch(eObject);
		} catch (OrccRuntimeException e) {
			return null;
		}
	}

	@Override
	public Object caseExpressionBinary(ExpressionBinary expression) {
		OpBinary op = OpBinary.getOperator(expression.getOperator());
		Object val1 = getValue(expression.getLeft());
		Object val2 = getValue(expression.getRight());
		return ValueUtil.compute(val1, op, val2);
	}

	@Override
	public Object caseExpressionBoolean(ExpressionBoolean expression) {
		return expression.isValue();
	}

	@Override
	public Object caseExpressionFloat(ExpressionFloat expr) {
		return expr.getValue();
	}

	@Override
	public Object caseExpressionIf(ExpressionIf expression) {
		Object condition = getValue(expression.getCondition());

		if (ValueUtil.isBool(condition)) {
			if (ValueUtil.isTrue(condition)) {
				return getValue(expression.getThen());
			} else {
				return getValue(expression.getElse());
			}
		} else {
			return null;
		}
	}

	@Override
	public Object caseExpressionInteger(ExpressionInteger expression) {
		return expression.getValue();
	}

	@Override
	public Object caseExpressionString(ExpressionString expression) {
		String str = expression.getValue();
		BigInteger bi = BigInteger.ZERO;
		if (!str.isEmpty()) {
			for (int i = 0; i < str.length(); i++) {
				int cp = str.codePointAt(i);
				BigInteger biCodepoint = BigInteger.valueOf(cp);

				// rounds to the nearest byte
				int amount = 8 * (int) Math.ceil(biCodepoint.bitLength() / 8.0);
				bi = bi.shiftLeft(amount).or(biCodepoint);
			}
		}
		return bi;
	}

	@Override
	public Object caseExpressionUnary(ExpressionUnary expression) {
		OpUnary op = OpUnary.getOperator(expression.getUnaryOperator());
		Object value = getValue(expression.getExpression());
		return ValueUtil.compute(op, value);
	}

	@Override
	public Object caseExpressionVariable(ExpressionVariable expression) {
		Variable variable = expression.getSource().getVariable();
		Object value;
		if (CxUtil.isConstant(variable)) {
			// only returns the value for constants
			// no cross-variable initializations
			value = getValue(variable.getValue());
		} else {
			return null;
		}

		for (CExpression index : expression.getIndexes()) {
			Object indexValue = getValue(index);
			if (ValueUtil.isList(value)) {
				if (ValueUtil.isInt(indexValue)) {
					int ind = ((BigInteger) indexValue).intValue();
					try {
						value = Array.get(value, ind);
					} catch (ArrayIndexOutOfBoundsException e) {
						return null;
					}
				}
			}
		}

		return value;
	}

	@Override
	public Object caseValueExpr(ValueExpr value) {
		return doSwitch(value.getExpression());
	}

	@Override
	public Object caseValueList(ValueList valueList) {
		int size = valueList.getValues().size();
		Object[] objects = new Object[size];
		int i = 0;
		for (Value value : valueList.getValues()) {
			objects[i] = doSwitch(value);
			i++;
		}
		return objects;
	}

	@Override
	public Object doSwitch(EObject eObject) {
		if (eObject == null) {
			return null;
		}

		return super.doSwitch(eObject);
	}

}
