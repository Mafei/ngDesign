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

import com.synflow.core.transformations.AbstractExpressionTransformer;
import com.synflow.models.ir.ExprInt;
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
	public ExprInt caseExprInt(ExprInt expr) {
		int size = TypeUtil.getSize(getTarget());
		expr.setSize(size);
		return expr;
	}

}
