/*******************************************************************************
 * Copyright (c) 2012 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.ngDesign.generators.vhdl

import com.synflow.models.ir.ExprBool
import com.synflow.models.ir.ExprInt
import com.synflow.models.ir.ExprString
import com.synflow.models.ir.util.IrSwitch

/**
 * This class defines a value printer for instance arguments
 * 
 * @author Matthieu Wipliez
 */
class VhdlArgValuePrinter extends IrSwitch<CharSequence> {

	override caseExprBool(ExprBool expr) {
		if (expr.value) "'1'" else "'0'"
	}

	override caseExprInt(ExprInt expr) {
		expr.value.toString
	}

	override caseExprString(ExprString expr) {
		'''"«expr.value»"'''
	}

}
