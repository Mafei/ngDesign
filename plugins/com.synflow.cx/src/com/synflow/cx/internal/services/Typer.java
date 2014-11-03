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
package com.synflow.cx.internal.services;

import static com.synflow.cx.CxConstants.PROP_AVAILABLE;
import static com.synflow.cx.CxConstants.PROP_READ;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.google.inject.Inject;
import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.CType;
import com.synflow.cx.cx.CxExpression;
import com.synflow.cx.cx.ExpressionBinary;
import com.synflow.cx.cx.ExpressionBoolean;
import com.synflow.cx.cx.ExpressionCast;
import com.synflow.cx.cx.ExpressionFloat;
import com.synflow.cx.cx.ExpressionIf;
import com.synflow.cx.cx.ExpressionInteger;
import com.synflow.cx.cx.ExpressionList;
import com.synflow.cx.cx.ExpressionString;
import com.synflow.cx.cx.ExpressionUnary;
import com.synflow.cx.cx.ExpressionVariable;
import com.synflow.cx.cx.TypeDecl;
import com.synflow.cx.cx.TypeGen;
import com.synflow.cx.cx.TypeRef;
import com.synflow.cx.cx.Typedef;
import com.synflow.cx.cx.VarRef;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.cx.util.CxSwitch;
import com.synflow.cx.instantiation.IInstantiator;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Port;
import com.synflow.models.ir.IrFactory;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.OpUnary;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.TypeArray;
import com.synflow.models.ir.TypeInt;
import com.synflow.models.ir.util.IrUtil;
import com.synflow.models.ir.util.TypeUtil;
import com.synflow.models.ir.util.ValueUtil;

/**
 * This class defines a typer for Cx AST. Note that types must have been transformed to IR types
 * first.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class Typer extends CxSwitch<Type> {

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

	private Entity entity;

	@Inject
	private IInstantiator instantiator;

	@Override
	public Type caseExpressionBinary(ExpressionBinary expression) {
		OpBinary op = OpBinary.getOperator(expression.getOperator());
		Type t1 = doSwitch(expression.getLeft());
		Type t2 = doSwitch(expression.getRight());

		CxExpression right = expression.getRight();
		Object amount = instantiator.evaluate(entity, right);
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
	public Type caseExpressionList(ExpressionList list) {
		List<CxExpression> values = list.getValues();
		int size = values.size();

		// compute the LUB of all expressions
		Type type = getType(values);
		return IrFactory.eINSTANCE.createTypeArray(type, size);
	}

	@Override
	public Type caseExpressionString(ExpressionString expression) {
		if (expression.eContainer() instanceof CxExpression) {
			BigInteger value = (BigInteger) instantiator.evaluate(entity, expression);
			return IrFactory.eINSTANCE.createTypeIntOrUint(value);
		}
		return IrFactory.eINSTANCE.createTypeString();
	}

	@Override
	public Type caseExpressionUnary(ExpressionUnary expression) {
		CxExpression subExpr = expression.getExpression();
		OpUnary op = OpUnary.getOperator(expression.getUnaryOperator());

		Type type = doSwitch(subExpr);
		return TypeUtil.getTypeUnary(op, type);
	}

	@Override
	public Type caseExpressionVariable(ExpressionVariable expression) {
		VarRef ref = expression.getSource();
		Type type = doSwitch(ref);

		String prop = expression.getProperty();
		boolean isPort = CxUtil.isPort(ref.getVariable());
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
		List<CxExpression> indexes = expression.getIndexes();
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
		String spec = type.getSpec();

		if ("bool".equals(spec)) {
			return IrFactory.eINSTANCE.createTypeBool();
		} else if ("void".equals(spec)) {
			return IrFactory.eINSTANCE.createTypeVoid();
		} else if ("float".equals(spec)) {
			return IrFactory.eINSTANCE.createTypeFloat();
		}

		boolean signed = type.isSigned() || !type.isUnsigned();
		int size;

		if (spec == null || spec.isEmpty()) {
			size = 0;
		} else {
			char ch = spec.charAt(0);
			String rest;
			if (ch == 'u') {
				rest = spec.substring(1);
				signed = false;
			} else {
				rest = spec;
			}

			if ("char".equals(rest)) {
				size = 8;
			} else if ("short".equals(rest)) {
				size = 16;
			} else if ("int".equals(rest)) {
				size = 32;
			} else if ("long".equals(rest)) {
				size = 64;
			} else {
				// like i15 or u29
				if (rest == spec) {
					// must use substring
					rest = spec.substring(1);
				}
				size = new BigInteger(rest).intValue();
			}
		}

		return IrFactory.eINSTANCE.createTypeInt(size, signed);
	}

	@Override
	public Type caseTypedef(Typedef typedef) {
		Type type = instantiator.getMapping(entity, typedef);
		return EcoreUtil.copy(type);
	}

	@Override
	public Type caseTypeGen(TypeGen typeGen) {
		Type type = doSwitch(typeGen.getSpec());
		int size = instantiator.evaluateInt(entity, typeGen.getSize());
		boolean signed = type.isInt() && ((TypeInt) type).isSigned();
		return IrFactory.eINSTANCE.createTypeInt(size, signed);
	}

	@Override
	public Type caseTypeRef(TypeRef type) {
		return doSwitch(type.getTypeDef());
	}

	@Override
	public Type caseVariable(Variable variable) {
		if (CxUtil.isFunction(variable) && CxUtil.isVoid(variable)) {
			return IrFactory.eINSTANCE.createTypeVoid();
		}

		CType varType = CxUtil.getType(variable);

		Type type = doSwitch(varType);
		List<CxExpression> dimensions = variable.getDimensions();
		if (dimensions.isEmpty()) {
			return type;
		}

		TypeArray array = IrFactory.eINSTANCE.createTypeArray();
		array.setElementType(type);

		for (CxExpression dimension : dimensions) {
			Object value = instantiator.evaluate(entity, dimension);
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
		if (CxUtil.isPort(variable)) {
			Port port = instantiator.getPort(entity, ref);
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
	public Type getType(Entity entity, EObject eObject) {
		Entity oldEntity = this.entity;
		this.entity = entity;
		try {
			return doSwitch(eObject);
		} finally {
			this.entity = oldEntity;
		}
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

		Type type = doSwitch(it.next());
		while (it.hasNext()) {
			type = TypeUtil.getLargest(type, doSwitch(it.next()));
		}

		return type;
	}

}
