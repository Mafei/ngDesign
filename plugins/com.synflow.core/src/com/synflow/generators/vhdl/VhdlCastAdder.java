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
package com.synflow.generators.vhdl;

import com.synflow.core.transformations.AbstractExpressionTransformer;
import com.synflow.models.dpn.Port;
import com.synflow.models.ir.ExprCast;
import com.synflow.models.ir.ExprVar;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.IrFactory;
import com.synflow.models.ir.TypeInt;
import com.synflow.models.ir.Var;

/**
 * This class adds 'signed' or 'unsigned' around accesses from/to ports.
 * 
 * @author Matthieu Wipliez
 *
 */
public class VhdlCastAdder extends AbstractExpressionTransformer {

	@Override
	public Expression caseExprVar(ExprVar expr) {
		Var variable = expr.getUse().getVariable();
		if (variable.getType().isInt()) {
			TypeInt type = (TypeInt) variable.getType();
			if (variable instanceof Port) {
				ExprCast cast = IrFactory.eINSTANCE.createExprCast();
				if (type.isSigned()) {
					cast.setToSigned(true);
				} else {
					cast.setToUnsigned(true);
				}

				cast.setCastedSize(type.getSize());
				cast.setExpr(expr);
				return cast;
			}
		}
		return expr;
	}

}
