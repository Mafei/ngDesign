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
package com.synflow.generators.stimulus

import com.synflow.models.ir.ExprBool
import com.synflow.models.ir.ExprFloat
import com.synflow.models.ir.ExprInt
import com.synflow.models.ir.util.IrSwitch
import org.eclipse.emf.ecore.EObject

/**
 * This class defines a custom expression printer used for tests, that prints
 * boolean values as integers, with "1" representing <code>true</code>, and "0"
 * representing <code>false</code>.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class TestExprPrinter extends IrSwitch<CharSequence> {

	override caseExprBool(ExprBool expr) {
		'''2#«IF expr.value»1«ELSE»0«ENDIF»'''
	}

	override caseExprFloat(ExprFloat expr) {
		'''float#«expr.value.stripTrailingZeros»'''
	}

	override caseExprInt(ExprInt expr) {
		var String base
		var int radix

		if (expr.value < 0bi) {
			base = 'signed'
			radix = 10
		} else {
			if (expr.value.bitLength <= 8) {
				base = '2'
				radix = 2
			} else {
				base = '16'
				radix = 16
			}
		}

		'''«base»#«expr.value.toString(radix)»'''
	}

	override doSwitch(EObject expr) {
		if (expr == null) {
			'8#666' // any value will do, really, it is ignored anyway
		} else {
			doSwitch(expr.eClass, expr)
		}
	}

}
