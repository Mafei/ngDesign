/*******************************************************************************
 * Copyright (c) 2012-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cflow.internal.compiler.helpers;

import java.util.Iterator;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.synflow.models.dpn.Action;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.FSM;
import com.synflow.models.dpn.State;
import com.synflow.models.dpn.Transition;
import com.synflow.models.graph.Edge;
import com.synflow.models.graph.Vertex;
import com.synflow.models.graph.visit.ReversePostOrder;
import com.synflow.models.ir.Procedure;

/**
 * This class defines a beautifier that renames actions/states of the actor's FSM that it visits. It
 * also sorts by topological order.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class FsmBeautifier {

	private FSM fsm;

	/**
	 * Renames the states of the FSM.
	 * 
	 * @param actorName
	 *            base name of the states (simple name of the actor)
	 */
	private void renameStates(String actorName) {
		// the "currentName" is set by a state with a name
		String currentName = null;
		for (State state : fsm.getStates()) {
			String stateName = state.getName();
			if (stateName == null) {
				if (currentName == null) {
					// this is the case for an actor whose first state has no name
					currentName = "FSM_" + actorName;
				}
				state.setName(currentName);
			} else {
				currentName = stateName;
			}
		}

		// rename consecutive states
		Multiset<String> visited = HashMultiset.create();
		for (State state : fsm.getStates()) {
			String name = state.getName();
			int n = visited.count(name);
			if (n > 0) {
				state.setName(name + "_" + n);
			}

			visited.add(name);
		}
	}

	/**
	 * Sorts the FSM by reverse post-order (equivalent to topological order, but cycle tolerant).
	 */
	private void sortFsm() {
		ReversePostOrder order = new ReversePostOrder(fsm, fsm.getInitialState());
		int i = 0;
		for (Vertex vertex : order.getVertices()) {
			fsm.getVertices().move(i, vertex);
			i++;
		}
	}

	/**
	 * Visits the given actor
	 * 
	 * @param actor
	 *            an actor
	 */
	public void visit(Actor actor) {
		fsm = actor.getFsm();

		// if FSM is empty removes it from actor and leave
		if (fsm.getStates().isEmpty()) {
			actor.setFsm(null);
			return;
		}

		sortFsm();
		renameStates(actor.getSimpleName());
		visitTransitions();

		// if there is only one state, removes the FSM
		if (fsm.getStates().size() <= 1) {
			for (Transition transition : fsm.getTransitions()) {
				actor.getActionsOutsideFsm().add(transition.getAction());
			}

			// remove FSM
			actor.setFsm(null);
		}
	}

	/**
	 * Visits the given transition, renames actions according to the given name, and set up the line
	 * numbers of its body and scheduler procedures.
	 * 
	 * @param transition
	 *            transition
	 * @param name
	 *            name of the action
	 */
	private void visitTransition(Transition transition, String name) {
		Iterator<Integer> it = transition.getLines().iterator();
		int lineNumber = it.hasNext() ? it.next() : 0;

		Action action = transition.getAction();
		action.setName(name);

		Procedure body = action.getBody();
		body.setLineNumber(lineNumber);
		body.setName(name);

		Procedure scheduler = action.getScheduler();
		scheduler.setLineNumber(lineNumber);
		scheduler.setName("isSchedulable_" + name);
	}

	/**
	 * Visits all transitions, renaming actions and setting up their line numbers.
	 */
	private void visitTransitions() {
		for (State state : fsm.getStates()) {
			int i = 0;
			for (Edge edge : state.getOutgoing()) {
				String name = state.getName();
				name += "_" + (char) ('a' + i);
				i++;

				visitTransition((Transition) edge, name);
			}
		}
	}

}
