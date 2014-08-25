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
package com.synflow.ngDesign.generators.vhdl

import com.synflow.models.dpn.Action
import com.synflow.models.dpn.Actor
import com.synflow.models.dpn.FSM
import com.synflow.models.dpn.Port
import com.synflow.models.dpn.State
import com.synflow.models.dpn.Transition
import com.synflow.models.ir.TypeInt
import java.util.List

import static extension com.synflow.ngDesign.generators.vhdl.HdlGeneratorUtil.*

/**
 * This class extends the IR printer for load/store to pattern variables, and
 * declares methods to create the asynchronous and synchronous processes.
 * 
 * @author Nicolas Siret
 * @author Matthieu Wipliez
 */
class VhdlActionPrinter {
	
	val VhdlIrPrinter irPrinter = new VhdlIrPrinter

	def printActions(Actor actor)
		'''
		«FOR action : actor.actions»
		«IF !action.scheduler.inlinable»
		«printSchedulerFunction(action)»
		«ENDIF»
		«ENDFOR»
		'''

	def private printActionBody(Action action)
		'''
		to_boolean(«FOR port : action.inputPattern.ports»«IF port.sync»«port.name»_send and «ENDIF»«ENDFOR»«printSchedulerCall(action)») then
		  -- Body of «action.name» (line «action.body.lineNumber»)
		  «irPrinter.doSwitch(action.body.blocks)»
		  «FOR port : action.outputPattern.ports»
		  «IF port.sync»
		  «port.name»_send <= '1';
		  «ENDIF»
		  «ENDFOR»
		'''

	/**
	 * Print the asynchronous process which manages the firing of the tasks
	 */
	def printAsyncProcess(Actor actor)
		'''
		«actor.simpleName»_comb : process («FOR port : actor.inputs SEPARATOR ", "»«port.name»«IF port.sync», «port.name»_send«ENDIF»«ENDFOR») is
		  «printBodyVariables(actor)»
		begin
		  «FOR port : actor.outputs»
		    «printPortInit(port)»
		  «ENDFOR»
		  «printSyncActions(actor.actions)»
		end process «actor.simpleName»_comb;
		'''

	def private printBodyVariables(Actor actor) {
		val variables = irPrinter.computeVarMap(actor.actions.map([a|a.body]))

		'''
		«irPrinter.declareTypeList(variables)»
		«FOR variable : variables»
		variable «irPrinter.doSwitch(variable)»;
		«ENDFOR»
		'''
	}

	def private printPortInit(Port port) {
		'''
		«port.name» <= «IF port.type.bool»'0'«ELSE»(others => '0')«ENDIF»;
		«IF port.sync»
		«port.name»_send <= '0';
		«ENDIF»
		'''
	}

	def private printSchedulerCall(Action action) {
		if (action.scheduler.inlinable) {
			irPrinter.doSwitch(action.scheduler.expression)
		} else {
			'''«action.scheduler.name»«IF !action.peekPattern.empty»(«action.peekPattern.ports.join(', ',
				[p|if (p.type.int && (p.type as TypeInt).signed) '''signed(«p.name»)''' else if (p.type.int) '''unsigned(«p.name»)''' else p.name]
			)»)«ENDIF»'''
		}
	}

	def private printSchedulerFunction(Action action) {
		'''
		-- Scheduler of «action.name» (line «action.scheduler.lineNumber»)
		impure function «action.scheduler.name»«printSchedulerParameters(action)» return std_logic is
		  «irPrinter.declareTypeList(action.scheduler.locals)»
		  «FOR variable : action.scheduler.locals»
		  variable «irPrinter.doSwitch(variable)»;
		  «ENDFOR»
		begin
		  «irPrinter.doSwitch(action.scheduler.blocks)»
		end function «action.scheduler.name»;

		'''
	}

	def private printSchedulerParameters(Action action) {
		if (!action.peekPattern.empty) {
			irPrinter.printTypeSize = false
			val seq = '''(«FOR port : action.peekPattern.ports SEPARATOR '; '»«port.name»_in : «irPrinter.doSwitch(port.type)»«ENDFOR»)'''
			irPrinter.printTypeSize = true
			seq
		}
	}

	def private printSyncFsm(FSM fsm)
		'''
		case FSM is
		  «FOR state : fsm.states SEPARATOR "\n"»
		  when s_«state.name» =>
		    «printSyncTransitions(state)»
		  «ENDFOR»
		end case;
		'''	

	def private printSyncTransitions(State state) {
		val transitions = state.outgoing as List<?> as List<Transition>
		'''
		«IF !transitions.empty»
		if «printActionBody(transitions.head.action)»
		  FSM <= s_«transitions.head.target.name»;
		«FOR transition : transitions.tail»
		elsif «printActionBody(transition.action)»
		  FSM <= s_«transition.target.name»;
		«ENDFOR»
		end if;
		«ENDIF»
		'''
	}

	def private printSyncActions(List<Action> actions)
		'''
		«IF !actions.empty»
		if «printActionBody(actions.head)»
		«FOR action : actions.tail»
			elsif «printActionBody(action)»
		«ENDFOR»
		end if;
		«ENDIF»
		'''

	/**
	 * Print the synchronous process which contains the body of the tasks
	 */
	def printSyncProcess(Actor actor) {
		'''
		«actor.simpleName»_execute : process (reset_n, clock) is
		  «printBodyVariables(actor)»
		begin
		  if reset_n = '0' then
		    «FOR variable : actor.variables»
		      «IF variable.assignable»
		      «variable.name» <= «irPrinter.printInitialValue(variable)»;
		      «ENDIF»
		    «ENDFOR»
		    «FOR port : actor.outputs»
		      «printPortInit(port)»
		    «ENDFOR»
		    «IF actor.hasFsm»
		      FSM    <= s_«actor.fsm.initialState.name»;
		    «ENDIF»
		  --
		  elsif rising_edge(clock) then
		    «FOR port : actor.outputs»
		      «IF port.sync»
		        «port.name»_send <= '0';
		      «ENDIF»
		    «ENDFOR»
		    --
		    «IF actor.hasFsm»
		    «printSyncFsm(actor.fsm)»
		    «ELSE»
		    «printSyncActions(actor.actionsOutsideFsm)»
		    «ENDIF»
		  end if;
		end process «actor.simpleName»_execute;
		'''
	}
	
}
