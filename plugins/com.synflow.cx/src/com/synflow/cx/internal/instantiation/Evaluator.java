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
package com.synflow.cx.internal.instantiation;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.emf.ecore.EObject;

import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.CxExpression;
import com.synflow.cx.cx.CxFactory;
import com.synflow.cx.cx.ExpressionBinary;
import com.synflow.cx.cx.ExpressionBoolean;
import com.synflow.cx.cx.ExpressionFloat;
import com.synflow.cx.cx.ExpressionIf;
import com.synflow.cx.cx.ExpressionInteger;
import com.synflow.cx.cx.ExpressionList;
import com.synflow.cx.cx.ExpressionString;
import com.synflow.cx.cx.ExpressionUnary;
import com.synflow.cx.cx.ExpressionVariable;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.cx.util.CxSwitch;
import com.synflow.cx.instantiation.IInstantiator;
import com.synflow.models.dpn.Entity;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.OpUnary;
import com.synflow.models.ir.Var;
import com.synflow.models.ir.util.ValueUtil;

/**
 * This class defines an expression evaluator.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class Evaluator extends CxSwitch<Object> {

	/**
	 * Returns the Cx value that matches the given runtime value. Value is expected to be one of
	 * Boolean, BigDecimal, BigInteger, String, or Array.
	 * 
	 * @param value
	 *            a runtime value
	 * @return a Cx value (ValueExpr or ValueList)
	 */
	public static CxExpression getCxExpression(Object value) {
		if (ValueUtil.isBool(value)) {
			ExpressionBoolean expr = CxFactory.eINSTANCE.createExpressionBoolean();
			expr.setValue((Boolean) value);
			return expr;
		} else if (ValueUtil.isFloat(value)) {
			ExpressionFloat expr = CxFactory.eINSTANCE.createExpressionFloat();
			expr.setValue((BigDecimal) value);
			return expr;
		} else if (ValueUtil.isInt(value)) {
			ExpressionInteger expr = CxFactory.eINSTANCE.createExpressionInteger();
			expr.setValue((BigInteger) value);
			return expr;
		} else if (ValueUtil.isString(value)) {
			ExpressionString expr = CxFactory.eINSTANCE.createExpressionString();
			expr.setValue((String) value);
			return expr;
		} else if (ValueUtil.isList(value)) {
			ExpressionList list = CxFactory.eINSTANCE.createExpressionList();
			int length = Array.getLength(value);
			for (int i = 0; i < length; i++) {
				list.getValues().add(getCxExpression(Array.get(value, i)));
			}
			return list;
		} else {
			return null;
		}
	}

	private Entity entity;

	private IInstantiator instantiator;

	Evaluator(IInstantiator instantiator) {
		this.instantiator = instantiator;
	}

	@Override
	public Object caseExpressionBinary(ExpressionBinary expression) {
		OpBinary op = OpBinary.getOperator(expression.getOperator());
		Object val1 = doSwitch(expression.getLeft());
		Object val2 = doSwitch(expression.getRight());
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
		Object condition = doSwitch(expression.getCondition());

		if (ValueUtil.isBool(condition)) {
			if (ValueUtil.isTrue(condition)) {
				return doSwitch(expression.getThen());
			} else {
				return doSwitch(expression.getElse());
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
	public Object caseExpressionList(ExpressionList valueList) {
		int size = valueList.getValues().size();
		Object[] objects = new Object[size];
		int i = 0;
		for (CxExpression value : valueList.getValues()) {
			objects[i] = doSwitch(value);
			i++;
		}
		return objects;
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
		Object value = doSwitch(expression.getExpression());
		return ValueUtil.compute(op, value);
	}

	@Override
	public Object caseExpressionVariable(ExpressionVariable expression) {
		Variable variable = expression.getSource().getVariable();
		Object value;
		if (CxUtil.isConstant(variable)) {
			// only returns the value for constants
			// no cross-variable initializations
			Var var = instantiator.getMapping(entity, variable);
			if (var == null) {
				value = doSwitch(variable.getValue());
			} else {
				value = ValueUtil.getValue(var.getInitialValue());
			}
		} else {
			return null;
		}

		for (CxExpression index : expression.getIndexes()) {
			Object indexValue = doSwitch(index);
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
	public Object doSwitch(EObject eObject) {
		if (eObject == null) {
			return null;
		}

		return super.doSwitch(eObject);
	}

	/**
	 * Returns the integer value associated with the given object using its URI. Returns -1 if the
	 * value is not an integer.
	 * 
	 * @param eObject
	 *            an AST node
	 * @return the integer value associated with the given object
	 */
	int getIntValue(Entity entity, EObject eObject) {
		Object value = getValue(entity, eObject);
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
	Object getValue(Entity entity, EObject eObject) {
		Entity oldEntity = this.entity;
		this.entity = entity;
		try {
			return doSwitch(eObject);
		} catch (IllegalArgumentException e) {
			return null;
		} finally {
			this.entity = oldEntity;
		}
	}

}
