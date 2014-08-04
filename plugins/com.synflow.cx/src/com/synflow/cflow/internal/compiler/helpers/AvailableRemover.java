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
package com.synflow.cflow.internal.compiler.helpers;

import org.eclipse.emf.ecore.EObject;

import com.synflow.models.ir.ExprBinary;
import com.synflow.models.ir.ExprBool;
import com.synflow.models.ir.ExprFloat;
import com.synflow.models.ir.ExprInt;
import com.synflow.models.ir.ExprResize;
import com.synflow.models.ir.ExprString;
import com.synflow.models.ir.ExprTypeConv;
import com.synflow.models.ir.ExprUnary;
import com.synflow.models.ir.ExprVar;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.util.IrSwitch;

/**
 * This class defines a visitor that removes null expressions (created when translating available).
 * Concrete implementations of all ExprXXX is by design. DO NOT rely on delegate caseExpression.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class AvailableRemover extends IrSwitch<EObject> {

	@Override
	public Expression caseExprBinary(ExprBinary expr) {
		Expression e1 = visit(expr.getE1());
		Expression e2 = visit(expr.getE2());

		if (e1 == null && e2 == null) {
			return null;
		} else if (e1 == null) {
			return e2;
		} else if (e2 == null) {
			return e1;
		}

		expr.setE1(e1);
		expr.setE2(e2);
		return expr;
	}

	@Override
	public ExprBool caseExprBool(ExprBool expr) {
		return expr;
	}

	@Override
	public Expression caseExpression(Expression expr) {
		// this is the default case for expression
		// it must return null and not expr
		// because it is only invoked when other caseExpr have returned null
		return null;
	}

	@Override
	public ExprFloat caseExprFloat(ExprFloat expr) {
		return expr;
	}

	@Override
	public ExprInt caseExprInt(ExprInt expr) {
		return expr;
	}

	@Override
	public ExprResize caseExprResize(ExprResize expr) {
		Expression e = visit(expr.getExpr());
		if (e == null) {
			return null;
		}

		expr.setExpr(e);
		return expr;
	}

	@Override
	public ExprString caseExprString(ExprString expr) {
		return expr;
	}

	@Override
	public ExprTypeConv caseExprTypeConv(ExprTypeConv expr) {
		Expression e = visit(expr.getExpr());
		if (e == null) {
			return null;
		}

		expr.setExpr(e);
		return expr;
	}

	@Override
	public ExprUnary caseExprUnary(ExprUnary expr) {
		Expression e = visit(expr.getExpr());
		if (e == null) {
			return null;
		}

		expr.setExpr(e);
		return expr;
	}

	@Override
	public ExprVar caseExprVar(ExprVar expr) {
		return expr;
	}

	/**
	 * Returns an expression where calls to available have been removed. The result may be
	 * <code>null</code> if the expression only contains calls to available. If the expression does
	 * not contain any references to available, this method returns it unchanged.
	 * 
	 * @param expr
	 *            an expression (may be <code>null</code>)
	 * @return an expression (may be <code>null</code>)
	 */
	public Expression visit(Expression expr) {
		if (expr == null) {
			return null;
		}
		return (Expression) doSwitch(expr);
	}

}
