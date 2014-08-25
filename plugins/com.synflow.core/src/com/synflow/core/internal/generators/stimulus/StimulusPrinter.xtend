/*******************************************************************************
 * Copyright (c) 2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.core.internal.generators.stimulus

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.synflow.models.dpn.Port
import com.synflow.models.ir.Expression
import com.synflow.models.ir.IrFactory
import com.synflow.models.ir.TypeInt
import com.synflow.models.ir.util.ValueUtil
import java.util.ArrayList
import java.util.List

/**
 * This class defines a printer that prints stimulus/trace files.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class StimulusPrinter {

	private val List<Port> ports

	new(List<Port> ports) {
		this.ports = ports
	}

	def getPortNames() {
		'''
			«FOR port : ports»
				«port.name»«IF port.type.int»(«(port.type as TypeInt).size - 1»:0)«ENDIF»
				«IF port.sync»
					«port.name»_send
				«ENDIF»
			«ENDFOR»
		'''
	}

	def getValues(JsonObject test) {
		if (test == null) {
			return ''
		}

		val lines = new ArrayList<List<Expression>>
		for (Port port : ports) {
			val list = test.getAsJsonArray(port.name)
			if (list != null) {
				list.forEach [ value, i |
					val line = if (i < lines.size) {
							lines.get(i)
						} else {
							val newLine = new ArrayList<Expression>()
							lines.add(newLine)
							newLine
						}

					val expr =
						if (value.jsonNull) {
							if (port.sync) {
								null
							} else {
								IrFactory.eINSTANCE.createExprBool
							}
						} else {
							val primitive = value as JsonPrimitive							
							if (primitive.boolean) {
								ValueUtil.getExpression(primitive.asBoolean)
							} else {
								ValueUtil.getExpression(primitive.asBigInteger)
							}
						}
					line.add(expr)
					if (port.sync) {
						// send unless expr is null
						val send = expr != null
						line.add(IrFactory.eINSTANCE.createExprBool(send))
					}
				]
			}
		}

		val printer = new TestExprPrinter

		'''
			«FOR line : lines»
				«FOR expr : line SEPARATOR ' '»«printer.doSwitch(expr)»«ENDFOR»
			«ENDFOR»
		'''
	}

}
