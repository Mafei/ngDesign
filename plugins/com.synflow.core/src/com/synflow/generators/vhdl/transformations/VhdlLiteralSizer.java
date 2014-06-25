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

import static com.synflow.models.ir.util.TypeUtil.getLargest;
import static com.synflow.models.ir.util.TypeUtil.getType;

import com.synflow.core.transformations.AbstractExpressionTransformer;
import com.synflow.models.ir.ExprBinary;
import com.synflow.models.ir.ExprInt;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.util.TypeUtil;

/**
 * This class resizes integer literals to match the size of their target's type (when assigning to a
 * variable) or the size of the surrounding expression.
 * 
 * @author Matthieu Wipliez
 *
 */
public class VhdlLiteralSizer extends AbstractExpressionTransformer {

	@Override
	public Expression caseExprBinary(ExprBinary expr) {
		Expression e1 = expr.getE1();
		Expression e2 = expr.getE2();

		Type type;
		OpBinary op = expr.getOp();
		if (op.isComparison() || op == OpBinary.TIMES) {
			type = getLargest(getType(e1), getType(e2));
		} else {
			type = getType(expr);
		}

		expr.setE1(transform(type, e1));
		expr.setE2(transform(type, e2));
		return expr;
	}

	@Override
	public ExprInt caseExprInt(ExprInt expr) {
		int size = TypeUtil.getSize(getTarget());
		expr.setSize(size);
		return expr;
	}

}
