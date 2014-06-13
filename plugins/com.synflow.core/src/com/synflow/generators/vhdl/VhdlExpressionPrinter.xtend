/*******************************************************************************
 * Copyright (c) 2012-2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nicolas Siret - initial API and implementation and/or initial documentation
 *    Matthieu Wipliez - refactoring and maintenance
 *******************************************************************************/
package com.synflow.generators.vhdl

import com.synflow.generators.ExpressionPrinter
import com.synflow.models.ir.ExprBinary
import com.synflow.models.ir.ExprBool
import com.synflow.models.ir.ExprCast
import com.synflow.models.ir.ExprInt
import com.synflow.models.ir.ExprList
import com.synflow.models.ir.ExprString
import com.synflow.models.ir.ExprUnary
import com.synflow.models.ir.ExprVar
import com.synflow.models.ir.OpBinary
import com.synflow.models.ir.OpUnary
import com.synflow.models.ir.TypeInt
import com.synflow.models.ir.Var
import com.synflow.models.ir.util.TypeUtil
import java.math.BigInteger
import java.util.Map

/**
 * This class defines the expression printer for the VHDL code generator.
 * 
 * @author Matthieu Wipliez
 * @author Nicolas Siret
 */
class VhdlExpressionPrinter extends ExpressionPrinter {

	def private static printQuotedValue(int size, BigInteger value) {
		if (size % 4 == 0) {
			// print hexadecimal format with the correct size
			'''x"«String.format("%0" + (size / 4) + "x", value)»"'''
		} else {
			val str = value.toString(2)
			if (size > str.length) {
				// if necessary, append n zeroes before the string
				'''"«String.format("%0" + (size - str.length) + "d", 0)»«str»"'''
			} else if (str.length > size) {
				// if necessary, remove the first n characters
				'''"«str.substring(str.length - size)»"'''
			} else {
				'''"«str»"'''
			}
		}
	}
	
	private boolean useBoolean
	
	protected Map<Var, String> varMap

	override caseExprBinary(ExprBinary expr) {
		val e1 = expr.e1
		val e2 = expr.e2
		val op = expr.op

		// special cases, for these operations always put parentheses
		// to avoid warnings and make code more readable
		val nextPrec =
			if (op == OpBinary.BITOR
				|| op == OpBinary.BITXOR
				|| op == OpBinary.LOGIC_OR) Integer.MIN_VALUE else op.precedence

		if (op == OpBinary.SHIFT_LEFT) {
			val amount = (e2 as ExprInt).value
			if (e1 instanceof ExprVar) {
				val name = getName(e1.use.variable)
				'''(«name» & «printQuotedValue(amount.intValue, BigInteger.ZERO)»)'''
			} else {
				// always resize before shift left
				// because return type is type of first operand
				val operand = doSwitch(e1, Integer.MAX_VALUE, 0)
				'''shift_left(«operand», «amount»)'''
			}
		} else if (op == OpBinary.SHIFT_RIGHT) {
			val amount = (expr.e2 as ExprInt).value
			if (e1 instanceof ExprVar) {
				val name = getName(e1.use.variable)
				'''«name»(«(TypeUtil.getType(e1) as TypeInt).size» - 1 downto «amount»)'''
			} else {
				// always resize after shift right
				// because VHDL may not agree with the IR here
				// (since the IR is more precise and actually reduce the type)
				'''resize(shift_right(«doSwitch(e1, Integer.MAX_VALUE, 0)», «amount»), 5)'''
			}
		} else {
			var first = doSwitch(e1, nextPrec, 0)
			val second = doSwitch(e2, nextPrec, 1)
			// special case because of VHDL typing
			if (op.comparison && e1.exprInt && e2.exprInt) {
				first = '''unsigned'(«first»)'''
			}

			val seq = '''(«first» «toString(op)» «second»)'''

			// update target and signed
			if (op.comparison) {
				if (useBoolean) {
					'''«seq»'''
				} else {
					'''to_std_logic(«seq»)'''
				}
			} else {
				seq
			}
		}
	}

	override caseExprBool(ExprBool expr) {
		if (useBoolean) {
			expr.value.toString
		} else {
			if (expr.value) "'1'" else "'0'"
		}
	}

	override caseExprCast(ExprCast expr) {
		if (expr.toSigned) {
			'''signed(«doSwitch(expr.expr)»)'''
		} else if (expr.toUnsigned) {
			'''unsigned(«doSwitch(expr.expr)»)'''
		} else {
			doSwitch(expr.expr)
		}
	}

	override caseExprInt(ExprInt expr) {
		var value = expr.value
		val size = TypeUtil.getSize(value)

		// always print as sized literal to make lint happier
		if (value < 0bi) {
			val unsignedValue = 1bi.shiftLeft(size) + value
			'''x"«unsignedValue.toString(16)»"'''
		} else {
			'''x"«value.toString(16)»"'''
		}
	}

	override caseExprList(ExprList expr) {
		'''«FOR subExpr : expr.value SEPARATOR ", "»«doSwitch(subExpr, Integer.MAX_VALUE, 0)»«ENDFOR»'''
	}

	override caseExprString(ExprString expr)
		// returns a quoted string
		'''"«expr.getValue()»"'''

	override caseExprUnary(ExprUnary expr) {
		var subExpr = doSwitch(expr.expr, Integer.MIN_VALUE, branch)

		// special case because of VHDL typing
		if (expr.expr.exprInt) {
			subExpr = '''unsigned'(«subExpr»)'''
		}

		switch (expr.op) {
		case OpUnary.MINUS: '''0 - «subExpr»'''
		case OpUnary.BITNOT: '''not («subExpr»)'''
		case OpUnary.LOGIC_NOT: '''not («subExpr»)'''
		}
	}

	override caseExprVar(ExprVar expr) {
		val variable = expr.use.variable
		val name = getName(variable)
		if (variable.type.bool && useBoolean) {
			'''to_boolean(«name»)'''
		} else {
			name
		}
	}

	/**
	 * Returns the adjusted name of the given variable (if present in varMap,
	 * otherwise returns variable.name)
	 */
	def protected getName(Var variable) {
		if (varMap != null) {
			val name = varMap.get(variable)
			if (name != null) {
				return name
			}
		}

		variable.name
	}

	def toString(OpBinary op) {
		switch (op) {
		case OpBinary.BITAND: "and"
		case OpBinary.BITOR: "or"
		case OpBinary.BITXOR: "xor"

		case OpBinary.DIV: "/"
		case OpBinary.DIV_INT: "/"

		case OpBinary.EQ: "="
		case OpBinary.LOGIC_AND: "and"
		case OpBinary.LOGIC_OR: "or"
		case OpBinary.MOD: "get_mod"
		case OpBinary.NE: "/="

		default:
			op.text
		}
	}

}
