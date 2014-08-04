/*******************************************************************************
 * Copyright (c) 2012-2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cflow.internal;

import static com.synflow.cflow.cflow.CflowFactory.eINSTANCE;
import static org.eclipse.emf.ecore.util.EcoreUtil.resolveAll;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;

import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.Branch;
import com.synflow.cflow.cflow.CExpression;
import com.synflow.cflow.cflow.CType;
import com.synflow.cflow.cflow.CflowFactory;
import com.synflow.cflow.cflow.ExpressionBinary;
import com.synflow.cflow.cflow.ExpressionBoolean;
import com.synflow.cflow.cflow.ExpressionCast;
import com.synflow.cflow.cflow.ExpressionInteger;
import com.synflow.cflow.cflow.ExpressionUnary;
import com.synflow.cflow.cflow.ExpressionVariable;
import com.synflow.cflow.cflow.StatementAssign;
import com.synflow.cflow.cflow.VarRef;
import com.synflow.cflow.cflow.Variable;
import com.synflow.models.ir.util.IrUtil;

/**
 * This class defines methods to manipulate the AST.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class AstUtil {

	private static void addAdapters(Copier copier) {
		for (Entry<EObject, EObject> entry : copier.entrySet()) {
			if (entry.getKey() instanceof VarRef) {
				final EObject original = entry.getKey();
				final EObject copy = entry.getValue();
				copy.eAdapters().add(new CopyOf(original));
			}
		}
	}

	/**
	 * Creates the AST of <code>expr1 && expr2</code>. Expressions are copied. If
	 * <code>expr1 == true</code>, returns a copy of <code>expr2</code>, and vice-versa.
	 * 
	 * @param expr1
	 *            first operand
	 * @param expr2
	 *            second operand
	 * @return a binary expression
	 */
	public static CExpression and(CExpression expr1, CExpression expr2) {
		if (isTrue(expr1)) {
			return copy(expr2);
		} else if (isTrue(expr2)) {
			return copy(expr1);
		} else {
			ExpressionBinary exprBin = eINSTANCE.createExpressionBinary();
			exprBin.setLeft(copy(expr1));
			exprBin.setOperator("&&");
			exprBin.setRight(copy(expr2));
			return exprBin;
		}
	}

	public static Branch cond(CExpression condition) {
		Branch cond = eINSTANCE.createBranch();
		if (condition != null) {
			cond.setCondition(copy(condition));
		}
		return cond;
	}

	private static <T extends EObject> T copy(T eObject) {
		Copier copier = new Copier();
		EObject result = copier.copy(eObject);
		copier.copyReferences();

		addAdapters(copier);

		@SuppressWarnings("unchecked")
		T t = (T) result;
		return t;
	}

	private static <T extends EObject> Collection<T> copyAll(Collection<? extends T> eObjects) {
		Copier copier = new Copier();
		Collection<T> result = new ArrayList<>(eObjects.size());
		for (EObject eObject : eObjects) {
			@SuppressWarnings("unchecked")
			T copy = (T) copier.copy(eObject);
			result.add(copy);
		}

		copier.copyReferences();
		addAdapters(copier);

		return result;
	}

	private static CExpression createArithmetic(Variable variable, List<CExpression> indexes,
			String op, CExpression value) {
		ExpressionBinary exprBin = eINSTANCE.createExpressionBinary();
		VarRef varRef = eINSTANCE.createVarRef();
		varRef.setVariable(variable);

		ExpressionVariable left = eINSTANCE.createExpressionVariable();
		left.setSource(varRef);
		if (!indexes.isEmpty()) {
			left.getIndexes().addAll(copyAll(indexes));
		}

		exprBin.setLeft(left);
		exprBin.setOperator(op);
		exprBin.setRight(value);

		// add cast
		ExpressionCast cast = eINSTANCE.createExpressionCast();
		CType type = IrUtil.copy(CflowUtil.getType(variable));
		cast.setType(type);
		cast.setExpression(exprBin);
		return cast;
	}

	/**
	 * Creates a new expression variable that references the given variable.
	 * 
	 * @param variable
	 *            a variable
	 * @return an expression variable
	 */
	public static CExpression expr(Variable variable) {
		ExpressionVariable expr = CflowFactory.eINSTANCE.createExpressionVariable();
		VarRef ref = CflowFactory.eINSTANCE.createVarRef();
		ref.setVariable(variable);
		expr.setSource(ref);
		return expr;
	}

	/**
	 * Returns a new C~ 'true' boolean expression.
	 * 
	 * @return a boolean expression
	 */
	public static CExpression exprTrue() {
		ExpressionBoolean trueExpr = eINSTANCE.createExpressionBoolean();
		trueExpr.setValue(true);
		return trueExpr;
	}

	/**
	 * Returns the expression resulting from an assign created from the assignment operator
	 * (post-increment/decrement or compound operator).
	 * 
	 * @param assign
	 *            an assign statement
	 * @return an expression
	 */
	public static CExpression getAssignValue(StatementAssign assign) {
		String op = assign.getOp();
		Variable variable = assign.getTarget().getSource().getVariable();
		List<CExpression> indexes = assign.getTarget().getIndexes();
		CExpression value = assign.getValue();
		if (value == null) {
			// handle post-decrement/increment
			ExpressionInteger one = eINSTANCE.createExpressionInteger();
			one.setValue(BigInteger.ONE);

			if ("++".equals(op)) {
				value = createArithmetic(variable, indexes, "+", one);
			} else if ("--".equals(op)) {
				value = createArithmetic(variable, indexes, "-", one);
			}
		} else {
			// compound op
			if (op.length() > 1) {
				// resolve value now, because proxies in "value" can't be
				// resolved by Xtext since no node model is attached to the AST
				// nodes created
				resolveAll(value);

				String binOp = op.substring(0, op.length() - 1);
				value = copy(value);
				value = createArithmetic(variable, indexes, binOp, value);
			}
		}

		return value;
	}

	private static boolean isTrue(CExpression expression) {
		if (expression instanceof ExpressionBoolean) {
			return ((ExpressionBoolean) expression).isValue();
		}
		return false;
	}

	/**
	 * Returns <code>!expression</code>.
	 * 
	 * @param expression
	 * @return
	 */
	public static CExpression not(CExpression expression) {
		ExpressionUnary not = eINSTANCE.createExpressionUnary();
		not.setExpression(copy(expression));
		not.setUnaryOperator("!");
		return not;
	}

}
