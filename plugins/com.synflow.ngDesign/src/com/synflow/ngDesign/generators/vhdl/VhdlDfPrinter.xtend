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
package com.synflow.ngDesign.generators.vhdl

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.synflow.core.generators.Namer
import com.synflow.models.dpn.Actor
import com.synflow.models.dpn.DPN
import com.synflow.models.dpn.Endpoint
import com.synflow.models.dpn.Entity
import com.synflow.models.dpn.Instance
import com.synflow.models.dpn.Port
import com.synflow.models.dpn.Unit
import com.synflow.models.dpn.util.DpnSwitch
import com.synflow.models.ir.TypeInt
import com.synflow.models.ir.util.IrUtil
import java.util.ArrayList
import java.util.Calendar
import java.util.List
import org.eclipse.core.runtime.Path

import static com.synflow.core.IProperties.*
import static com.synflow.ngDesign.generators.vhdl.HdlGeneratorUtil.*

/**
 * This class defines a VHDL module (actor or unit) printer.
 * 
 * @author Nicolas Siret
 * @author Matthieu Wipliez
 */
class VhdlDfPrinter extends DpnSwitch<CharSequence> {

	val Namer namer

	val irPrinter = new VhdlIrPrinter

	val VhdlActionPrinter actionPrinter

	new(Namer namer) {
		this.namer = namer
		actionPrinter = new VhdlActionPrinter(namer)
	}

	def private addMappings(List<CharSequence> mappings, DPN network, Instance instance) {
		for (connection : network.getIncoming(instance)) {
			val signal = getSignal(namer, connection.sourceEndpoint)
			val port = connection.targetPort

			mappings.add('''«port.name»      => «signal»''')
			if (port.sync) {
				mappings.add('''«port.name»_send => «signal»_send''')
			}
		}

		val entity = instance.entity
		for (port : entity.outputs) {
			val endpoint = new Endpoint(instance, port)
			val outgoing = network.getOutgoing(endpoint)
			val signal =
				if (isSignalNeeded(outgoing)) {
					// output port connected to a signal
					getSignal(namer, endpoint)
				} else if (outgoing.head != null) {
					// port directly connected to the target output port 
					outgoing.head.port.name
				} else {
					// unconnected output port
					"open"
				}
			addMappings(mappings, port, signal)
		}
	}

	/**
	 * Adds a mapping from "port" to "signal" to the given mappings list.
	 * The characteristics of the port are given by the "port" parameter
	 */
	def private addMappings(List<CharSequence> mappings, Port port, String signal) {
		mappings.add('''«port.name»      => «signal»''')
		if (port.sync) {
			mappings.add('''«port.name»_send => «IF signal != null»«IF signal != "open"»«signal»_send«ELSE»open«ENDIF»«ENDIF»''')
		}
	}

	def private void addPorts(List<CharSequence> signals, Port port) {
		val signalDir = if (IrUtil.isInput(port)) 'in' else 'out'

		signals.add('''«namer.getName(port)» : «signalDir» «printPortType(port)»''')

		for (signal : port.additionalInputs) {
			signals.add('''«signal» : in std_logic''')
		}

		for (signal : port.additionalOutputs) {
			signals.add('''«signal» : out std_logic''')
		}
	}

	/**
	 * If the port is TypeInt, prints "std_logic_vector(size - 1 downto 0)".
	 * If the port is TypeBool, prints "std_logic"
	 */
	def printPortType(Port port) {
		if (port.type.int) {
			val typeInt = port.type as TypeInt
			'''std_logic_vector(«typeInt.size - 1» downto 0)'''
		} else {
			'''std_logic'''
		}
	}

	def static private printImports(Entity importer) {
		var imports = importer.properties.getAsJsonObject("imports")
		'''
		library work;
		use work.Helper_functions.all;
		«FOR entry : imports.entrySet»
		use work.«IrUtil.getSimpleName(entry.key)».all;
		«ENDFOR»
		'''
	}

	override caseActor(Actor actor) {
		val combinational = actor.properties.getAsJsonArray(PROP_CLOCKS).empty

		'''
		«printModuleDeclaration(actor)»

		-------------------------------------------------------------------------------
		architecture rtl_«actor.simpleName» of «namer.getName(actor)» is

		  «irPrinter.declareTypeList(actor.variables)»
		  «FOR stateVar : actor.variables»
		  «irPrinter.printStateVar(stateVar)»
		  «ENDFOR»

		  «IF !actor.procedures.empty»
		  -----------------------------------------------------------------------------
		  -- Declaration of functions
		  -----------------------------------------------------------------------------
		  «FOR proc : actor.procedures»
		  «irPrinter.printFunctionSignature(proc)»;
		  «ENDFOR»

		  -----------------------------------------------------------------------------
		  -- Implementation of functions
		  -----------------------------------------------------------------------------
		  «FOR proc : actor.procedures»
		  «irPrinter.printFunction(proc)»
		  «ENDFOR»
		  «ENDIF»
		  «IF actor.hasFsm»

		  type FSM_type is («FOR state : actor.fsm.states SEPARATOR ", "»s_«state.name»«ENDFOR»);
		  signal FSM : FSM_type;
		  «ENDIF»

		  «actionPrinter.printActions(actor)»

		begin

		  «IF combinational»
		  «actionPrinter.printAsyncProcess(actor)»
		  «ELSE»
		  «actionPrinter.printSyncProcess(actor)»
		  «ENDIF»

		end architecture rtl_«actor.simpleName»;

		'''
	}

	override caseDPN(DPN network)
		'''
		«printModuleDeclaration(network)»

		------------------------------------------------------------------------------
		architecture rtl_«network.simpleName» of «network.simpleName» is

		  ---------------------------------------------------------------------------
		  -- Signals declaration
		  ---------------------------------------------------------------------------
		  «FOR instance : network.instances»
		  «printSignals(network, instance)»
		  «ENDFOR»
		  ---------------------------------------------------------------------------

		begin

		  ---------------------------------------------------------------------------
		  -- Actors and Networks 
		  ---------------------------------------------------------------------------
		  «FOR instance : network.instances SEPARATOR "\n"»
		  «printInstanceMapping(network, instance)»
		  «ENDFOR»

		  «FOR port : network.outputs»
		  «printAssigns(network, port)»
		  «ENDFOR»

		end architecture rtl_«network.simpleName»;
		'''

	override caseUnit(Unit unit)
		'''
		«printHeader(unit)»

		------------------------------------------------------------------------------
		package «unit.simpleName» is

		  ---------------------------------------------------------------------------
		  -- Declaration of constants
		  ---------------------------------------------------------------------------
		  «irPrinter.declareTypeList(unit.variables)»
		  «FOR constant : unit.variables»
		  «irPrinter.printStateVar(constant)»
		  «ENDFOR»

		  «IF !unit.procedures.empty»
		  ---------------------------------------------------------------------------
		  -- Declaration of functions
		  ---------------------------------------------------------------------------
		  «FOR proc : unit.procedures»
		  «irPrinter.printFunctionSignature(proc)»;
		  «ENDFOR»
		  «ENDIF»

		end «unit.simpleName»;

		------------------------------------------------------------------------------
		package body «unit.simpleName» is

		  «IF !unit.procedures.empty»
		  ---------------------------------------------------------------------------
		  -- Implementation of functions
		  ---------------------------------------------------------------------------
		  «FOR proc : unit.procedures»
		  «irPrinter.printFunction(proc)»
		  «ENDFOR»
		  «ENDIF»

		end package body «unit.simpleName»;
		'''

	/**
	 * Prints assigns to the given port when necessary
	 */
	def private printAssigns(DPN network, Port port) {
		val endpoint = network.getIncoming(port)
		val outgoing = network.getOutgoing(endpoint)

		if (isSignalNeeded(outgoing)) {
			val signal = getSignal(namer, endpoint)

			'''
			«port.name»       <= «signal»;
			«IF port.sync»
			«port.name»_send  <= «signal»_send;
			«ENDIF»
			'''
		}
	}
	
	def private printComment(JsonArray lines) {
		'''
		«FOR line : lines»
		-- «line.asString»
		«ENDFOR»
		'''
	}

	def private printHeader(Entity entity) {
		val project = new Path(entity.fileName).segment(0)
		val copyright = entity.properties.getAsJsonArray(PROP_COPYRIGHT)
		val javadoc = entity.properties.getAsJsonArray(PROP_JAVADOC)
		
		val header =
			if (copyright == null && javadoc == null) {
				'''
				-------------------------------------------------------------------------------
				-- Title      : Generated from «entity.name» by Synflow Studio
				-- Project    : «project»
				--
				-- File       : «entity.name».v
				-- Author     : «System.getProperty("user.name")»
				-- Standard   : VHDL'93
				--
				-------------------------------------------------------------------------------
				-- Copyright (c) «Calendar.instance.get(Calendar.YEAR)»
				-------------------------------------------------------------------------------
				'''
			} else {
				'''
				«IF copyright != null»
				-------------------------------------------------------------------------------
				«copyright.printComment»
				-------------------------------------------------------------------------------
				«ENDIF»

				«IF javadoc != null»
				-------------------------------------------------------------------------------
				«javadoc.printComment»
				-------------------------------------------------------------------------------
				«ENDIF»
				'''
			}

		'''
		«header»

		-------------------------------------------------------------------------------
		library ieee;
		use ieee.std_logic_1164.all;
		use ieee.numeric_std.all;
		use ieee.std_logic_unsigned.all;
		
		library std;
		use std.textio.all;

		«printImports(entity)»
		'''
	}

	def private printInstanceMapping(DPN network, Instance instance) {
		val List<CharSequence> mappings = new ArrayList
		
		// clocks
		val clocks = instance.properties.get(PROP_CLOCKS) as JsonObject
		clocks.entrySet.forEach[pair|mappings.add('''«pair.key» => «pair.value.asString»''')]

		// reset
		val entity = instance.entity
		val reset = entity.properties.get(PROP_RESET)
		if (reset != null && reset.jsonObject) {
			val resetObj = reset.asJsonObject
			val name = resetObj.getAsJsonPrimitive(PROP_NAME).asString
			mappings.add('''«name» => reset_n''')
		}

		// ports
		addMappings(mappings, network, instance)

		'''
		«instance.name» : entity work.«entity.simpleName»
		«IF !instance.arguments.empty»generic map(
		  «FOR arg: instance.arguments SEPARATOR ","»
		  «arg.variable.name»   => «new VhdlArgValuePrinter().doSwitch(arg.value)»
		  «ENDFOR»
		)
		«ENDIF»
		port map (
		  «mappings.join(",\n")»
		);
		'''
	}

	def private printModuleDeclaration(Entity entity) {
		val signals = new ArrayList<CharSequence>

		// clocks
		val clocks = entity.properties.getAsJsonArray(PROP_CLOCKS)
		clocks.forEach[clock|signals.add(clock.asString + ' : in std_logic')]

		// reset
		val reset = entity.properties.get(PROP_RESET)
		if (reset != null && reset.jsonObject) {
			val resetObj = reset.asJsonObject
			val name = resetObj.getAsJsonPrimitive('name').asString
			signals.add(name + ' : in std_logic')
		}

		// add ports
		(entity.inputs + entity.outputs).forEach[p|addPorts(signals, p)]

		'''
		«printHeader(entity)»

		-------------------------------------------------------------------------------
		entity «namer.getName(entity)» is
		  port (
		    «signals.join(";\n")»);
		end «namer.getName(entity)»;
		'''
	}

	/**
	 * Declares signals for each output port of the given instance when needed,
	 * as determined by the isSignalNeeded function.
	 */
	def private printSignals(DPN network, Instance instance) {
		val signals = new ArrayList<CharSequence>

		val entity = instance.entity
		for (port : entity.outputs) {
			// add one signal for each output port when needed
			val endpoint = new Endpoint(instance, port)
			val outgoing = network.getOutgoing(endpoint)
			if (isSignalNeeded(outgoing)) {
				val signal = getSignal(namer, endpoint)
				signals.add('''signal «signal» : «printPortType(port)»;''')
				if (port.sync) {
					signals.add('''signal «signal»_send : std_logic;''')
				}
			}
		}

		'''
		«IF !signals.empty»
		-- Module : «instance.name»
		«signals.join("\n")»
		«ENDIF»
		'''
	}

}
