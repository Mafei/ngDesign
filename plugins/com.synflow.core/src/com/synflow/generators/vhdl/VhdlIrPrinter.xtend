/*******************************************************************************
 * Copyright (c) 2012-2014 Synflow SAS.
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

import com.synflow.models.OrccRuntimeException
import com.synflow.models.ir.BlockBasic
import com.synflow.models.ir.BlockIf
import com.synflow.models.ir.BlockWhile
import com.synflow.models.ir.ExprInt
import com.synflow.models.ir.Expression
import com.synflow.models.ir.InstAssign
import com.synflow.models.ir.InstCall
import com.synflow.models.ir.InstLoad
import com.synflow.models.ir.InstReturn
import com.synflow.models.ir.InstStore
import com.synflow.models.ir.IrFactory
import com.synflow.models.ir.Procedure
import com.synflow.models.ir.Type
import com.synflow.models.ir.TypeArray
import com.synflow.models.ir.TypeBool
import com.synflow.models.ir.TypeFloat
import com.synflow.models.ir.TypeInt
import com.synflow.models.ir.TypeString
import com.synflow.models.ir.Var
import com.synflow.models.ir.util.TypeUtil
import java.util.ArrayList
import java.util.Collection
import java.util.List
import org.eclipse.emf.ecore.EObject
import java.util.HashMap
import com.synflow.models.dpn.Port

class VhdlIrPrinter extends VhdlExpressionPrinter {
	
	protected static TypeBool typeBool = IrFactory.eINSTANCE.createTypeBool
	
	protected boolean printTypeSize = true

	override caseBlockBasic(BlockBasic node)
		'''
		«doSwitch(node.instructions)»
		'''
	
	override caseBlockIf(BlockIf ifNode)
		'''
		if («doSwitch(ifNode.condition)») then
		  «doSwitch(ifNode.thenBlocks)»
		«IF ifNode.elseRequired»
		else
		  «doSwitch(ifNode.elseBlocks)»
		«ENDIF»
		end if;
		«IF ifNode.joinBlock != null»
		«doSwitch(ifNode.joinBlock)»
		«ENDIF»
		'''

	override caseBlockWhile(BlockWhile whileNode)
		'''
		while («doSwitch(whileNode.condition)») loop
		  «doSwitch(whileNode.blocks)»
		  «doSwitch(whileNode.joinBlock)»
		end loop;
		'''

	override caseInstAssign(InstAssign assign) {
		val target = assign.target.variable
		'''
		«getName(target)» := «doSwitch(assign.value)»;
		'''
	}

	override caseInstCall(InstCall call) {
		if (call.assert) {
			val expr = call.arguments.get(0)
			val exprStr = doSwitch(expr)
			'''assert «exprStr» report "«exprStr.toString.replace("\"", "'")»" severity failure;'''
		} else if (call.print) {
			'''
			write(output, «printWriteParams(call.arguments)»);
			'''
		} else {
			val called =
				if (call.procedure.parameters.empty) {
					call.procedure.name
				} else {
					'''«call.procedure.name»(«printCallParams(call.procedure.parameters, call.arguments)»)'''
				}

			if (call.target == null) {
				'''«called»;'''
			} else {
				val target = call.target.variable

				'''
				«IF target.type.bool»
				«target.name» := «called»;
				«ELSE»
				«target.name» := resize(«called», «(target.type as TypeInt).size»);
				«ENDIF»
				'''
			}
		}
	}

	override caseInstReturn(InstReturn instReturn) {
		if (instReturn.value != null) {
			'''
			return «doSwitch(instReturn.value)»;
			'''
		}
	}

	def private printIndexes(TypeArray type, List<Expression> indexes) {
		var list = new ArrayList<CharSequence>
		var i = 0
		for (index : indexes) {
			val dim = type.dimensions.get(i) - 1
			val dimSize = TypeUtil.getSize(dim)
			val res =
				if (index instanceof ExprInt) {
					val value = index.value
					'''to_unsigned(«value», «dimSize»)'''
				} else {
					doSwitch(index)
				}
			list.add(res)

			i = i + 1
		}

		'''to_integer(«list.join(" & ")»)'''
	}

	override caseInstLoad(InstLoad load)
		'''
		«getName(load.target.variable)» := «load.source.variable.name»«IF !load.indexes.empty»(«printIndexes(load.source.variable.type as TypeArray, load.indexes)»)«ENDIF»;
		'''

	override caseInstStore(InstStore store) {
		val target = store.target.variable
		val type = target.type
		
		if (target instanceof Port && type.int && !store.value.exprInt) {
			'''
			«target.name» <= std_logic_vector(«doSwitch(store.value)»);
			'''
		} else {
			'''
			«IF store.indexes.empty»
			«target.name» <= «doSwitch(store.value)»;
			«ELSE»
			«target.name»(«printIndexes(type as TypeArray, store.indexes)») «IF target.local» := «ELSE» <= «ENDIF»«doSwitch(store.value)»;
			«ENDIF»
			'''
		}
	}

	override caseTypeBool(TypeBool type) '''std_logic'''

	override caseTypeFloat(TypeFloat type) '''real'''

	override caseTypeInt(TypeInt type) {
		'''«IF type.signed»signed«ELSE»unsigned«ENDIF»«IF printTypeSize»(«type.size - 1» downto 0)«ENDIF»'''
	}

	override caseTypeArray(TypeArray type) {
		var depth = 1
		for (dim : type.dimensions) {
			depth = depth * dim
		}
		depth = depth - 1
		'''array (0 to «depth») of «doSwitch(type.elementType)»'''
	}

	override caseTypeString(TypeString type) {
		throw new OrccRuntimeException("unsupported String type")
	}

	override caseVar(Var variable) {
		val name = getName(variable)
		'''«IF variable.type.array»«name» : «variable.name»_type«ELSE»«name» : «doSwitch(variable.type)»«ENDIF»'''
	}

	def declareTypeList(Collection<Var> variables)
		'''
		«FOR variable : variables»
		«IF variable.type.array»
		type «variable.name»_type is «doSwitch(variable.type)»;
		«ENDIF»
		«ENDFOR»
		'''

	def doSwitch(List<? extends EObject> objects)
		'''
		«FOR eObject : objects»
			«doSwitch(eObject)»
		«ENDFOR»
		'''
	
	def private printCallParams(List<Var> parameters, List<Expression> arguments) {
		'''«FOR expr : arguments SEPARATOR ", "»«doSwitch(expr)»«ENDFOR»'''
	}

	/**
	 * Prints the given state variable.
	 */
	def printStateVar(Var variable)
		'''
		«IF variable.assignable»
		signal «doSwitch(variable)»;
		«ELSE»
		constant «doSwitch(variable)» := «printInitialValue(variable)»;
		«ENDIF»
		'''

	/**
	 * prints the initial value of the given variable. If the variable has no
	 * initial value, the value that corresponds to the neutral element of
	 * the variable's type is returned.
	 */
	def printInitialValue(Var variable) {
		val type = variable.type
		val value = variable.initialValue
		
		if (type.array) {
			val innermostType = (type as TypeArray).elementType
			if (value == null) {
				'''(others => «getInitValue(innermostType)»)'''
			} else {
				'''(«doSwitch(value)»)'''
			}
		} else {
			if (value == null) {
				getInitValue(type)
			} else {
				doSwitch(value)
			}
		}
	}

	def private getInitValue(Type type) {
		if (type.bool) {
			"'0'"
		} else {
			printQuotedValue(TypeUtil.getSize(type), 0bi)
		}
	}

	def printFunction(Procedure procedure)
		'''
		«printFunctionSignature(procedure)» is
		  «declareTypeList(procedure.locals)»
		  «FOR local : procedure.locals»
		  	variable «doSwitch(local)»;
		  «ENDFOR»
		begin
		  «doSwitch(procedure.blocks)»
		end «procedure.name»;

		'''

	def printFunctionSignature(Procedure procedure) {
		printTypeSize = false
		val seq = '''impure function «procedure.name»«IF !procedure.parameters.empty»(«FOR param : procedure.parameters SEPARATOR "; "»«doSwitch(param)»«ENDFOR»)«ENDIF» return «doSwitch(procedure.returnType)»'''
		printTypeSize = true
		seq
	}

	def private printWriteParams(List<Expression> arguments) {
		val res = new ArrayList<CharSequence>
		for (expr : arguments) {
			val toStringExpr =
				if (expr.exprString) {
					doSwitch(expr)
				} else if (TypeUtil.getType(expr).bool) {
					'''to_string_93(to_bit(«doSwitch(expr)»))'''
				} else {
					'''to_hstring_93(to_bitvector(std_logic_vector(«doSwitch(expr)»)))'''
				}
			res.add(toStringExpr)
		}

		res.add('''LF''')
		res.join(" & ")
	}
	
	def computeVarMap(List<Procedure> procedures) {
		varMap = new HashMap
		return HdlGeneratorUtil.computeVarMap(procedures, varMap)
	}
	
}
