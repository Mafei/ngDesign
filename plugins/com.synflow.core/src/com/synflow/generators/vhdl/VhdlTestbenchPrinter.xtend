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

import com.google.common.collect.Iterables
import com.google.gson.JsonArray
import com.synflow.generators.Namer
import com.synflow.models.dpn.Actor
import com.synflow.models.dpn.DPN
import com.synflow.models.dpn.Entity
import com.synflow.models.dpn.Port
import com.synflow.models.dpn.util.DpnSwitch
import java.util.ArrayList
import java.util.Calendar
import java.util.List
import org.eclipse.core.runtime.Path

import static com.synflow.core.IProperties.*

/**
 * This class defines the testbench printer for VHDL.
 * 
 * @author Nicolas Siret
 * @author Matthieu Wipliez
 */
class VhdlTestbenchPrinter extends DpnSwitch<CharSequence> {

	val VhdlDfPrinter dfPrinter
	
	val Namer namer
	
	new(Namer namer) {
		this.namer = namer
		dfPrinter = new VhdlDfPrinter(namer)
	}

	def private addMappings(List<CharSequence> mappings, Port port) {
		mappings.add('''«port.name»   => «port.name»''')
		if (port.sync) {
			mappings.add('''«port.name»_send   => «port.name»_send''')
		}
	}

	override caseActor(Actor actor) {
		printTestbench(actor.fileName, actor)
	}

	override caseDPN(DPN network) {
		printTestbench(network.fileName, network)
	}

	def private printSignals(Port port)
		'''
		signal «port.name»       : «dfPrinter.printPortType(port)»;
		«IF port.sync»
		signal «port.name»_send  : std_logic;
		«ENDIF»
		'''

	def private printTestbench(String fileName, Entity entity) {
		val ports = Iterables.concat(entity.inputs, entity.outputs)

		val name = entity.simpleName
		val project = new Path(fileName).segment(0)

		val mappings = new ArrayList<CharSequence>
		for (port : entity.inputs) {
			addMappings(mappings, port)
		}
		for (port : entity.outputs) {
			addMappings(mappings, port)
		}

		var clocks = entity.properties.getAsJsonArray(PROP_CLOCKS)
		val combinational = clocks.empty
		if (combinational) {
			// if combinational, uses a default clock for the testbench
			clocks = new JsonArray()
			clocks.add(DEFAULT_CLOCK)
		}

		'''
		-------------------------------------------------------------------------------
		-- Title      : Generated from «name» by Synflow Studio
		-- Project    : «project»
		--
		-- File       : «name».tb.vhd
		-- Author     : «System.getProperty("user.name")»
		-- Standard   : VHDL'93
		--
		-------------------------------------------------------------------------------
		-- Copyright (c) «Calendar.instance.get(Calendar.YEAR)»
		-------------------------------------------------------------------------------


		------------------------------------------------------------------------------
		library ieee;
		use ieee.std_logic_1164.all;
		use ieee.std_logic_textio.all;
		use ieee.numeric_std.all;

		library std;
		use std.textio.all;

		library work;
		use work.sim_package.all;

		-------------------------------------------------------------------------------
		entity «name»_tb is
		end «name»_tb;

		-------------------------------------------------------------------------------
		architecture arch_«name»_tb of «name»_tb is 

		  type severity_level is (note, warning, error, failure);

		  constant INIT_RESET : time := 10 * 10 ns;

		  signal reset_n : std_logic := '0';
		  signal reset_dut : std_logic := '0';
		  «IF !combinational»
		  signal startRecv : std_logic := '0';
		  «ENDIF»

		  file fd_stim  : fd open read_mode is "stimSigValues.txt";
		  file fd_trace : fd open read_mode is "traceSigValues.txt";

		  -- Clocks
		  «FOR clock : clocks»
		  constant PERIOD_«clock.asString» : time := 10 ns;
		  «ENDFOR»
		  «FOR clock : clocks»
		  signal «clock.asString» : std_logic := '1';
		  «ENDFOR»

		  -- Ports
		  «FOR port : ports»
		    «printSignals(port)»
		  «ENDFOR»

		  ---------------------------------------------------------------------------

		begin

		  «namer.getName(entity)» : entity work.«namer.getName(entity)»
		  port map (
		    «IF !combinational»
		    «FOR clock : clocks»
		    «clock.asString»    => «clock.asString»,
		    «ENDFOR»
		    reset_n    => reset_dut«IF !mappings.empty»,«ENDIF»
		    «ENDIF»
		    «mappings.join(",\n")»
		  );

		  -- clock gen
		  «FOR clock : clocks»
		  «clock.asString» <= not «clock.asString» after PERIOD_«clock.asString» / 2;
		  «ENDFOR»

		  -- reset gen
		  reset_n   <= '1' after INIT_RESET;

		  «printTest(entity, combinational, clocks)»

		end architecture arch_«name»_tb;
		'''
	}

	def private printTest(Entity entity, boolean combinational, JsonArray clocks) {
		'''
		process (reset_n, «clocks.get(0).asString»)
		begin
		  if reset_n = '0' then
		    «FOR port : entity.inputs»
		    «port.name» <= «IF port.type.bool»'0'«ELSE»(others => '0')«ENDIF»;
		    «IF port.sync»
		    «port.name»_send <= '0';
		    «ENDIF»
		    «ENDFOR»
		    --
		  elsif rising_edge(«clocks.get(0).asString») then
		    «FOR port : entity.inputs»
		      «IF port.sync»
		      «port.name»_send <= '0';
		      «ENDIF»
		    «ENDFOR»
		    if not endfile(fd_stim) then
		      -- write values to input ports
		      «FOR port : entity.inputs»
		      writeValue(fd_stim, «port.name»«IF port.sync», «port.name»_send«ENDIF»);
		      «ENDFOR»
		    end if;

		    if «IF combinational»reset_dut«ELSE»startRecv«ENDIF» = '1' and not endfile(fd_trace) then
		      -- check values on output ports
		      «FOR port : entity.outputs»
		      checkValue(fd_trace, "«port.name»", «port.name»«IF port.sync», «port.name»_send«ENDIF»);
		      «ENDFOR»
		    end if;

		    «IF !combinational»
		    startRecv <= reset_dut;
		    «ENDIF»
		    reset_dut <= '1';
		  end if;
		end process;
		'''
	}

}
