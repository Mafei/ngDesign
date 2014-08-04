/*******************************************************************************
 * Copyright (c) 2013-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.services;

import static com.synflow.models.util.SwitchUtil.DONE;
import static org.eclipse.xtext.nodemodel.util.NodeModelUtils.getNode;
import static org.eclipse.xtext.nodemodel.util.NodeModelUtils.getTokenText;

import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;

import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.CExpression;
import com.synflow.cx.cx.ExpressionBinary;
import com.synflow.cx.cx.ExpressionBoolean;
import com.synflow.cx.cx.ExpressionCast;
import com.synflow.cx.cx.ExpressionFloat;
import com.synflow.cx.cx.ExpressionIf;
import com.synflow.cx.cx.ExpressionInteger;
import com.synflow.cx.cx.ExpressionString;
import com.synflow.cx.cx.ExpressionUnary;
import com.synflow.cx.cx.ExpressionVariable;
import com.synflow.cx.cx.TypeDecl;
import com.synflow.cx.cx.TypeGen;
import com.synflow.cx.cx.TypeRef;
import com.synflow.cx.cx.Value;
import com.synflow.cx.cx.ValueExpr;
import com.synflow.cx.cx.ValueList;
import com.synflow.cx.cx.VarRef;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.cx.util.CxSwitch;
import com.synflow.models.util.Void;

/**
 * This class defines a simple C~ pretty printer (a lightweight alternative to the whole Xtext
 * serialization thing).
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CxPrinter extends CxSwitch<Void> {

	private StringBuilder builder;

	public CxPrinter() {
		this.builder = new StringBuilder();
	}

	public CxPrinter(StringBuilder builder) {
		this.builder = builder;
	}

	@Override
	public Void caseExpressionBinary(ExpressionBinary expr) {
		builder.append('(');
		doSwitch(expr.getLeft());
		builder.append(expr.getOperator());
		doSwitch(expr.getRight());
		builder.append(')');
		return DONE;
	}

	@Override
	public Void caseExpressionBoolean(ExpressionBoolean expr) {
		builder.append(expr.isValue());
		return DONE;
	}

	@Override
	public Void caseExpressionCast(ExpressionCast expr) {
		builder.append('(');
		doSwitch(expr.getType());
		builder.append(')');

		builder.append('(');
		doSwitch(expr.getExpression());
		builder.append(')');

		return DONE;
	}

	@Override
	public Void caseExpressionFloat(ExpressionFloat expr) {
		builder.append(expr.getValue());
		return DONE;
	}

	@Override
	public Void caseExpressionIf(ExpressionIf expr) {
		builder.append('(');
		doSwitch(expr.getCondition());
		builder.append('?');
		doSwitch(expr.getThen());
		builder.append(':');
		doSwitch(expr.getElse());
		builder.append(')');
		return DONE;
	}

	@Override
	public Void caseExpressionInteger(ExpressionInteger expr) {
		builder.append(expr.getValue());
		return DONE;
	}

	@Override
	public Void caseExpressionString(ExpressionString expr) {
		builder.append('\'');
		builder.append(expr.getValue());
		builder.append('\'');
		return DONE;
	}

	@Override
	public Void caseExpressionUnary(ExpressionUnary expr) {
		builder.append(expr.getUnaryOperator());
		doSwitch(expr.getExpression());
		return DONE;
	}

	@Override
	public Void caseExpressionVariable(ExpressionVariable expr) {
		doSwitch(expr.getSource());

		for (CExpression index : expr.getIndexes()) {
			builder.append('[');
			doSwitch(index);
			builder.append(']');
		}

		String property = expr.getProperty();
		if (property != null) {
			builder.append('.');
			builder.append(property);
		}

		Iterator<CExpression> it = expr.getParameters().iterator();
		if (it.hasNext()) {
			builder.append('(');
			doSwitch(it.next());
			while (it.hasNext()) {
				builder.append(',');
				doSwitch(it.next());
			}
			builder.append(')');
		}

		return DONE;
	}

	@Override
	public Void caseTypeDecl(TypeDecl type) {
		if (type.getSpec().charAt(0) != 'u' && type.isUnsigned()) {
			builder.append("unsigned ");
		}
		builder.append(type.getSpec());
		return DONE;
	}

	@Override
	public Void caseTypeGen(TypeGen type) {
		builder.append(type.getSpec());
		builder.append('<');
		CExpression size = type.getSize();
		if (size != null) {
			doSwitch(size);
		}
		builder.append('>');
		return DONE;
	}

	@Override
	public Void caseTypeRef(TypeRef ref) {
		builder.append(getTokenText(getNode(ref)));
		return DONE;
	}

	@Override
	public Void caseValueExpr(ValueExpr value) {
		doSwitch(value.getExpression());
		return DONE;
	}

	@Override
	public Void caseValueList(ValueList value) {
		builder.append('{');
		Iterator<Value> it = value.getValues().iterator();
		if (it.hasNext()) {
			doSwitch(it.next());
			while (it.hasNext()) {
				builder.append(',');
				doSwitch(it.next());
			}
		}
		builder.append('}');
		return DONE;
	}

	@Override
	public Void caseVariable(Variable variable) {
		builder.append(variable.getName());
		if (CxUtil.isFunction(variable)) {
			builder.append('(');
			Iterator<Variable> it = variable.getParameters().iterator();
			if (it.hasNext()) {
				doSwitch(CxUtil.getType(it.next()));
				while (it.hasNext()) {
					builder.append(", ");
					doSwitch(CxUtil.getType(it.next()));
				}
			}
			builder.append(')');
		}
		return DONE;
	}

	@Override
	public Void caseVarRef(VarRef ref) {
		builder.append(getTokenText(getNode(ref)));
		return DONE;
	}

	public String toString(EObject eObject) {
		doSwitch(eObject);
		return builder.toString();
	}

}
