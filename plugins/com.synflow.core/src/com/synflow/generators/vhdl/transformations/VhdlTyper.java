/*******************************************************************************
 * Copyright (c) 2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.generators.vhdl.transformations;

import static com.synflow.models.ir.IrFactory.eINSTANCE;

import com.synflow.core.transformations.impl.HDLTyper;
import com.synflow.models.ir.ExprBinary;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.util.TypeUtil;

/**
 * This class add resize and type conversions for VHDL.
 * 
 * @author Matthieu Wipliez
 *
 */
public class VhdlTyper extends HDLTyper {

	@Override
	public Expression caseExprBinary(ExprBinary expr) {
		OpBinary op = expr.getOp();
		if (op == OpBinary.TIMES) {
			// cast to their respective type
			Type t1 = TypeUtil.getType(expr.getE1());
			Type t2 = TypeUtil.getType(expr.getE2());
			expr.setE1(transform(t1, expr.getE1()));
			expr.setE2(transform(t2, expr.getE2()));
			return expr;
		} else {
			return super.caseExprBinary(expr);
		}
	}

	@Override
	protected Expression cast(Type target, Type source, Expression expr) {
		// resize only if necessary
		boolean forceResize = false;
		if (expr.isExprBinary()) {
			OpBinary op = ((ExprBinary) expr).getOp();
			forceResize = (op == OpBinary.SHIFT_LEFT || op == OpBinary.SHIFT_RIGHT);
		}

		return eINSTANCE.cast(target, source, expr, forceResize);
	}

}
