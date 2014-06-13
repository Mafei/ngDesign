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
package com.synflow.generators

import com.synflow.models.ir.Expression
import com.synflow.models.ir.util.IrSwitch

/**
 * This class defines common methods to expression printer in code generators.
 * 
 * @author Matthieu Wipliez
 */
class ExpressionPrinter extends IrSwitch<CharSequence> {

	protected int branch = 0 // left

	protected int precedence = Integer.MAX_VALUE

	def doSwitch(Expression expression, int newPrecedence, int newBranch) {
		val oldBranch = branch
		val oldPrecedence = precedence

		branch = newBranch
		precedence = newPrecedence

		val result = doSwitch(expression)

		precedence = oldPrecedence
		branch = oldBranch

		result
	}

}
