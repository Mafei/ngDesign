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

import com.google.common.collect.Iterables
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

	def addPorts(Port port, String dirSend) {
		var ports = new ArrayList<CharSequence>(3)
		ports.add('''«port.name»       : «dirSend»«printPortType(port)»''')
		if (port.sync) {
			ports.addAll('''«port.name»_send  : «dirSend»std_logic''')
		}
		ports
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

	/**
	 * If the port has a type and it is not boolean, prints "[size - 1 : 0]"
	 */
	def printPortTypeOld(Port port) {
		if (port.type.bool) {
			'''      : std_logic'''
		} else {
			'''      : std_logic_vector(«(port.type as TypeInt).size - 1» downto 0)'''			
		}
	}

	override caseActor(Actor actor) {
		var ports = Iterables.concat(
			actor.inputs.map([p|addPorts(p,  "in ")]),
			actor.outputs.map([p|addPorts(p,  "out ")])
		).flatten

		val combinational = actor.properties.getAsJsonArray(PROP_CLOCKS).empty

		'''
		-------------------------------------------------------------------------------
		-- Title      : Generated from «actor.name» by Synflow ngDesign
		-- Project    : «new Path(actor.fileName).segment(0)»
		--
		-- File       : «actor.simpleName».vhd
		-- Author     : «System.getProperty("user.name")»
		-- Standard   : VHDL'93
		--
		-------------------------------------------------------------------------------
		-- Copyright (c) «Calendar.instance.get(Calendar.YEAR)»
		-------------------------------------------------------------------------------
		
		-------------------------------------------------------------------------------
		library ieee;
		use ieee.std_logic_1164.all;
		use ieee.numeric_std.all;
		use ieee.std_logic_unsigned.all;
		
		library std;
		use std.textio.all;

		«printImports(actor)»

		-------------------------------------------------------------------------------
		entity «namer.getName(actor)» is
		  port (
		    «IF !combinational»
		                                          -- Standard I/Os
		    clock    : in  std_logic;
		    reset_n  : in  std_logic;
		    «ENDIF»
		                                          -- Actor I/Os
		    «ports.join(";\n")»);
		end «namer.getName(actor)»;


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

	override caseDPN(DPN network) {
		val project = new Path(network.fileName).segment(0)
		val clocks = network.properties.getAsJsonArray(PROP_CLOCKS)

		var ports = Iterables.concat(
			network.inputs.map([p|addPorts(p,  "in ")]),
			network.outputs.map([p|addPorts(p,  "out ")])
		).flatten

		'''
		-------------------------------------------------------------------------------
		-- Title      : Generated from «network.name» by Synflow ngDesign
		-- Project    : «project»
		--
		-- File       : «network.simpleName».vhd
		-- Author     : «System.getProperty("user.name")»
		-- Standard   : VHDL'93
		--
		-------------------------------------------------------------------------------
		-- Copyright (c) «Calendar.instance.get(Calendar.YEAR)»
		-------------------------------------------------------------------------------

		-------------------------------------------------------------------------------
		library ieee;
		use ieee.std_logic_1164.all;
		use ieee.numeric_std.all;

		------------------------------------------------------------------------------
		entity «network.simpleName» is
		  port (
		                                          -- Clock signal«IF clocks.size > 1»s«ENDIF»
		  «FOR clock: clocks»
		  «clock.asString»    : in  std_logic;
		  «ENDFOR»

		  reset_n  : in  std_logic«FOR port : ports»;
		  «port»«ENDFOR»);
		end «network.simpleName»;

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
	}

	override caseUnit(Unit unit)
		'''
		-------------------------------------------------------------------------------
		-- Title      : Generated from «unit.name» by Synflow ngDesign
		-- Project    : «new Path(unit.fileName).segment(0)»
		--
		-- File       : «unit.simpleName».vhd
		-- Author     : «System.getProperty("user.name")»
		-- Standard   : VHDL'93
		--
		-------------------------------------------------------------------------------
		-- Copyright (c) «Calendar.instance.get(Calendar.YEAR)»
		-------------------------------------------------------------------------------

		------------------------------------------------------------------------------
		library ieee;
		use ieee.std_logic_1164.all;
		use ieee.numeric_std.all;

		library std;
		use std.textio.all;

		«printImports(unit)»

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
