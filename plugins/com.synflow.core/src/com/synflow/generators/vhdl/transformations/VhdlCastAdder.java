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
package com.synflow.generators.vhdl.transformations;

import static com.synflow.models.ir.ExprTypeConv.SIGNED;
import static com.synflow.models.ir.ExprTypeConv.UNSIGNED;
import static com.synflow.models.ir.IrFactory.eINSTANCE;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.synflow.core.transformations.AbstractExpressionTransformer;
import com.synflow.models.dpn.Port;
import com.synflow.models.ir.BlockIf;
import com.synflow.models.ir.BlockWhile;
import com.synflow.models.ir.ExprBinary;
import com.synflow.models.ir.ExprInt;
import com.synflow.models.ir.ExprTypeConv;
import com.synflow.models.ir.ExprVar;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.InstCall;
import com.synflow.models.ir.IrFactory;
import com.synflow.models.ir.IrPackage.Literals;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.TypeInt;
import com.synflow.models.ir.Var;

/**
 * This class adds 'signed' or 'unsigned' around accesses from/to ports, and calls to
 * to_boolean/to_std_logic helper functions where necessary.
 * 
 * @author Matthieu Wipliez
 *
 */
public class VhdlCastAdder extends AbstractExpressionTransformer {

	@Override
	public Expression caseExpression(Expression expr) {
		if (!getTarget().isBool()) {
			return expr;
		}

		EObject cter = expr.eContainer();
		boolean booleanExpected = cter instanceof BlockIf || cter instanceof BlockWhile
				|| (cter instanceof InstCall && ((InstCall) cter).isAssert());
		if (expr instanceof ExprBinary) {
			ExprBinary exprBin = (ExprBinary) expr;
			if (exprBin.getOp().isComparison()) {
				if (booleanExpected) {
					return expr;
				}

				return eINSTANCE.convert("to_std_logic", expr);
			}
		}

		if (booleanExpected) {
			return eINSTANCE.convert("to_boolean", expr);
		}

		return expr;
	}

	@Override
	public Expression caseExprInt(ExprInt expr) {
		EStructuralFeature feature = expr.eContainingFeature();
		if (feature == Literals.INST_LOAD__INDEXES || feature == Literals.INST_STORE__INDEXES) {
			// force conversion to unsigned
			return eINSTANCE.convert("unsigned'", expr);
		} else {
			EObject cter = expr.eContainer();
			if (cter instanceof ExprTypeConv) {
				ExprTypeConv typeConv = (ExprTypeConv) cter;
				typeConv.setTypeName(typeConv.getTypeName() + "'");
			}
		}

		return expr;
	}

	@Override
	public Expression caseExprVar(ExprVar expr) {
		Var variable = expr.getUse().getVariable();
		Type type = variable.getType();
		if (type.isInt()) {
			TypeInt typeInt = (TypeInt) type;
			if (variable instanceof Port) {
				String typeName = typeInt.isSigned() ? SIGNED : UNSIGNED;
				return IrFactory.eINSTANCE.convert(typeName, expr);
			}
		}
		return caseExpression(expr);
	}

}
