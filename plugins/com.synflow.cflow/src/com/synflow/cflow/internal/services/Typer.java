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
 * Copyright (c) 2010-2011, IETR/INSA of Rennes
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
package com.synflow.cflow.internal.services;

import static com.synflow.cflow.CflowConstants.PROP_AVAILABLE;
import static com.synflow.cflow.CflowConstants.PROP_READ;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.google.inject.Inject;
import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.CExpression;
import com.synflow.cflow.cflow.CType;
import com.synflow.cflow.cflow.ExpressionBinary;
import com.synflow.cflow.cflow.ExpressionBoolean;
import com.synflow.cflow.cflow.ExpressionCast;
import com.synflow.cflow.cflow.ExpressionFloat;
import com.synflow.cflow.cflow.ExpressionIf;
import com.synflow.cflow.cflow.ExpressionInteger;
import com.synflow.cflow.cflow.ExpressionString;
import com.synflow.cflow.cflow.ExpressionUnary;
import com.synflow.cflow.cflow.ExpressionVariable;
import com.synflow.cflow.cflow.TypeDecl;
import com.synflow.cflow.cflow.TypeGen;
import com.synflow.cflow.cflow.TypeRef;
import com.synflow.cflow.cflow.Typedef;
import com.synflow.cflow.cflow.Value;
import com.synflow.cflow.cflow.ValueExpr;
import com.synflow.cflow.cflow.ValueList;
import com.synflow.cflow.cflow.VarRef;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.cflow.util.CflowSwitch;
import com.synflow.cflow.internal.instantiation.IInstantiator;
import com.synflow.cflow.services.Evaluator;
import com.synflow.models.dpn.Port;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.IrFactory;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.OpUnary;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.TypeArray;
import com.synflow.models.ir.util.ExpressionEvaluator;
import com.synflow.models.ir.util.IrUtil;
import com.synflow.models.ir.util.TypeUtil;
import com.synflow.models.ir.util.ValueUtil;

/**
 * This class defines a typer for C~ AST. Note that types must have been transformed to IR types
 * first.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class Typer extends CflowSwitch<Type> {

	/**
	 * Returns an array containing the dimensions of the given type. Returns an empty array if
	 * <code>type</code> is not an array.
	 * 
	 * @param type
	 *            a type
	 * @return an array of dimensions
	 */
	public static int getNumDimensions(final Type type) {
		if (type instanceof TypeArray) {
			return ((TypeArray) type).getDimensions().size();
		} else {
			return 0;
		}
	}

	@Inject
	private IInstantiator instantiator;

	@Override
	public Type caseExpressionBinary(ExpressionBinary expression) {
		OpBinary op = OpBinary.getOperator(expression.getOperator());
		Type t1 = doSwitch(expression.getLeft());
		Type t2 = doSwitch(expression.getRight());

		CExpression right = expression.getRight();
		Object amount = Evaluator.getValue(right);
		return TypeUtil.getTypeBinary(op, t1, t2, amount);
	}

	@Override
	public Type caseExpressionBoolean(ExpressionBoolean expression) {
		return IrFactory.eINSTANCE.createTypeBool();
	}

	@Override
	public Type caseExpressionCast(ExpressionCast expression) {
		return doSwitch(expression.getType());
	}

	@Override
	public Type caseExpressionFloat(ExpressionFloat expr) {
		return IrFactory.eINSTANCE.createTypeFloat();
	}

	@Override
	public Type caseExpressionIf(ExpressionIf expression) {
		Type t1 = doSwitch(expression.getThen());
		Type t2 = doSwitch(expression.getElse());
		return TypeUtil.getLargest(t1, t2);
	}

	@Override
	public Type caseExpressionInteger(ExpressionInteger expression) {
		BigInteger value = expression.getValue();
		return IrFactory.eINSTANCE.createTypeIntOrUint(value);
	}

	@Override
	public Type caseExpressionString(ExpressionString expression) {
		return IrFactory.eINSTANCE.createTypeString();
	}

	@Override
	public Type caseExpressionUnary(ExpressionUnary expression) {
		CExpression subExpr = expression.getExpression();
		OpUnary op = OpUnary.getOperator(expression.getUnaryOperator());

		Type type = doSwitch(subExpr);
		return TypeUtil.getTypeUnary(op, type);
	}

	@Override
	public Type caseExpressionVariable(ExpressionVariable expression) {
		VarRef ref = expression.getSource();
		Type type = doSwitch(ref);

		String prop = expression.getProperty();
		boolean isPort = CflowUtil.isPort(ref.getVariable());
		if (PROP_AVAILABLE.equals(prop)) {
			return isPort ? IrFactory.eINSTANCE.createTypeBool() : null;
		} else if (PROP_READ.equals(prop)) {
			return isPort ? doSwitch(ref) : null;
		}

		// if type is not valid
		if (type == null || type.isArray() && ((TypeArray) type).getElementType() == null) {
			return null;
		}

		int numDims = getNumDimensions(type);
		List<CExpression> indexes = expression.getIndexes();
		if (indexes.isEmpty()) {
			return type;
		} else if (indexes.size() > numDims + 1) {
			// too many indexes
			return null;
		} else if (indexes.size() == numDims + 1) {
			// bit selection
			return IrFactory.eINSTANCE.createTypeBool();
		} else {
			// scalar
			type = ((TypeArray) type).getElementType();
			return IrUtil.copy(type);
		}

		// Variable function = call.getRef().getFunction();
		// Type type = doSwitch(function);
		// return IrUtil.copy(type);
	}

	@Override
	public Type caseTypeDecl(TypeDecl type) {
		boolean signed = !type.isUnsigned();
		String spec = type.getSpec();
		int size;

		if ("bool".equals(spec)) {
			return IrFactory.eINSTANCE.createTypeBool();
		} else if ("void".equals(spec)) {
			return IrFactory.eINSTANCE.createTypeVoid();
		} else if ("float".equals(spec)) {
			return IrFactory.eINSTANCE.createTypeFloat();
		} else if ("char".equals(spec)) {
			size = 8;
		} else if ("short".equals(spec)) {
			size = 16;
		} else if ("int".equals(spec)) {
			size = 32;
		} else if ("long".equals(spec)) {
			size = 64;
		} else if ("uchar".equals(spec)) {
			signed = false;
			size = 8;
		} else if ("ushort".equals(spec)) {
			signed = false;
			size = 16;
		} else if ("uint".equals(spec)) {
			signed = false;
			size = 32;
		} else if ("ulong".equals(spec)) {
			signed = false;
			size = 64;
		} else if (spec == null) {
			return IrFactory.eINSTANCE.createTypeInt();
		} else {
			signed = (spec.charAt(0) == 'i');
			size = new BigInteger(spec.substring(1)).intValue();
		}

		return IrFactory.eINSTANCE.createTypeInt(size, signed);
	}

	@Override
	public Type caseTypedef(Typedef typedef) {
		return doSwitch(typedef.getType());
	}

	@Override
	public Type caseTypeGen(TypeGen type) {
		String spec = type.getSpec();
		Expression expr = new ExpressionTransformer(instantiator).doSwitch(type.getSize());
		int size = new ExpressionEvaluator().evaluateAsInteger(expr);
		return IrFactory.eINSTANCE.createTypeInt(size, "i".equals(spec));
	}

	@Override
	public Type caseTypeRef(TypeRef type) {
		return doSwitch(type.getTypeDef().getType());
	}

	@Override
	public Type caseValueExpr(ValueExpr value) {
		return doSwitch(value.getExpression());
	}

	@Override
	public Type caseValueList(ValueList list) {
		List<Value> values = list.getValues();
		int size = values.size();

		// compute the LUB of all expressions
		Type type = getType(values);
		return IrFactory.eINSTANCE.createTypeArray(type, size);
	}

	@Override
	public Type caseVariable(Variable variable) {
		if (CflowUtil.isFunction(variable) && CflowUtil.isVoid(variable)) {
			return IrFactory.eINSTANCE.createTypeVoid();
		}

		CType varType = CflowUtil.getType(variable);

		Type type = doSwitch(varType);
		List<CExpression> dimensions = variable.getDimensions();
		if (dimensions.isEmpty()) {
			return type;
		}

		TypeArray array = IrFactory.eINSTANCE.createTypeArray();
		array.setElementType(type);

		for (CExpression dimension : dimensions) {
			Object value = Evaluator.getValue(dimension);
			if (ValueUtil.isInt(value)) {
				array.getDimensions().add(((BigInteger) value).intValue());
			} else {
				array.getDimensions().add(0);
			}
		}
		return array;
	}

	@Override
	public Type caseVarRef(VarRef ref) {
		Variable variable = ref.getVariable();
		if (CflowUtil.isPort(variable)) {
			Port port = instantiator.getPort(ref);
			return port.getType();
		}

		return doSwitch(ref.getVariable());
	}

	@Override
	public Type doSwitch(EObject eObject) {
		if (eObject == null) {
			return null;
		}

		return super.doSwitch(eObject);
	}

	/**
	 * Computes and returns the type of the given object.
	 * 
	 * @param eObject
	 *            an AST node
	 * @return the type of the given object
	 */
	public Type getType(EObject eObject) {
		return doSwitch(eObject);
	}

	/**
	 * Returns the type of the given list of objects using their URI.
	 * 
	 * @param eObject
	 *            an AST node
	 * @return the type of the given object
	 */
	public Type getType(List<? extends EObject> eObjects) {
		Iterator<? extends EObject> it = eObjects.iterator();
		if (!it.hasNext()) {
			return null;
		}

		Type type = getType(it.next());
		while (it.hasNext()) {
			type = TypeUtil.getLargest(type, getType(it.next()));
		}

		return type;
	}

}
