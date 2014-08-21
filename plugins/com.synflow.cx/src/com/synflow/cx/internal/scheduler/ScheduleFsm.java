/*******************************************************************************
 * Copyright (c) 2013-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.internal.scheduler;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableSet;
import com.synflow.cx.instantiation.IInstantiator;
import com.synflow.models.dpn.Action;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.DpnFactory;
import com.synflow.models.dpn.FSM;
import com.synflow.models.dpn.Port;
import com.synflow.models.dpn.State;
import com.synflow.models.dpn.Transition;
import com.synflow.models.node.Node;

/**
 * This class defines FSM capabilities on top of the Schedule class. It overrides
 * {@link #startNewCycle()} to create a new transition, and has several methods to manipulate the
 * FSM being created.
 * 
 * @author Matthieu
 * 
 */
public class ScheduleFsm extends Schedule {

	/**
	 * Creates a new empty schedule that will use the given actor.
	 */
	public ScheduleFsm(IInstantiator instantiator, Actor actor) {
		super(instantiator, actor);

		setNode(new Node());

		FSM fsm = actor.getFsm();
		if (fsm == null) {
			// if the actor has no FSM, create one
			fsm = DpnFactory.eINSTANCE.createFSM();
			actor.setFsm(fsm);

			// adds an initial state
			State source = DpnFactory.eINSTANCE.createState();
			fsm.add(source);
			fsm.setInitialState(source);

			// and adds a transition departing from that state
			addTransitionFrom(source);
		}
	}

	/**
	 * Adds a transition from the given source state to a new target state (whose name is taken from
	 * the stateName attribute).
	 * 
	 * @param source
	 *            source state
	 */
	protected final Transition addTransitionFrom(State source) {
		FSM fsm = (FSM) source.eContainer();

		// create new transition
		Transition transition = DpnFactory.eINSTANCE.createTransition();
		transition.setSource(source);
		fsm.add(transition);

		// create action and associate to transition
		createAction(transition);

		// update node
		getNode().setContent(transition);

		return transition;
	}

	private void createAction(Transition transition) {
		Action action = DpnFactory.eINSTANCE.createActionNop();
		actor.getActions().add(action);
		transition.setAction(action);
	}

	/**
	 * Adds a new state and merges all transitions at this state.
	 * 
	 * @return the new state
	 */
	State fence() {
		State join = DpnFactory.eINSTANCE.createState();
		getFsm().add(join);
		mergeTransitions(join);
		return join;
	}

	private void fillTransitions(List<Transition> transitions, Node node) {
		for (Node child : node.getChildren()) {
			if (child.hasChildren()) {
				fillTransitions(transitions, child);
			} else {
				transitions.add(getTransition(child));
			}
		}
	}

	@Override
	protected Action getAction(Node node) {
		return getTransition(node).getAction();
	}

	Actor getActor() {
		return actor;
	}

	public FSM getFsm() {
		return actor.getFsm();
	}

	/**
	 * Returns the current transition.
	 * 
	 * @return a transition
	 */
	public Transition getTransition() {
		return getTransition(getNode());
	}

	/**
	 * Returns the transition associated with the given node.
	 * 
	 * @param node
	 *            a node
	 * @return a transition
	 */
	protected final Transition getTransition(Node node) {
		return (Transition) node.getContent();
	}

	/**
	 * Returns the current transition.
	 * 
	 * @return a transition
	 */
	public Iterable<Transition> getTransitions() {
		if (hasMultipleTransitions()) {
			List<Transition> transitions = new ArrayList<>();
			fillTransitions(transitions, getNode());
			return transitions;
		}
		return ImmutableSet.of(getTransition(getNode()));
	}

	@Override
	protected boolean hasBeenRead(Port port) {
		for (Transition transition : getTransitions()) {
			if (transition.getAction().getInputPattern().contains(port)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean hasBeenWritten(Port port) {
		for (Transition transition : getTransitions()) {
			if (transition.getAction().getOutputPattern().contains(port)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if there are more than one open-ended transition in parallel at
	 * this point. Equivalent to <code>getNode().hasChildren()</code>.
	 * 
	 * @return a boolean
	 */
	public boolean hasMultipleTransitions() {
		return getNode().hasChildren();
	}

	/**
	 * Merges all the current transitions at the given join state. Promotes peeks of all transitions
	 * in the process, and clears the current node's children afterwards.
	 * 
	 * @param join
	 *            a join state
	 */
	public void mergeTransitions(State join) {
		for (Transition transition : getTransitions()) {
			promotePeeks(transition.getAction());
			transition.setTarget(join);
		}
		getNode().clearChildren();
	}

	/**
	 * Sets the name of the current transition's source (or if it is already set, target).
	 * 
	 * @param name
	 *            state name
	 */
	public void setStateName(String name) {
		Transition transition = getTransition();
		if (transition != null) {
			State source = transition.getSource();
			if (source.getName() == null) {
				source.setName(name);
			}
		}
	}

	public void setTransition(Transition transition) {
		getNode().setContent(transition);
		createAction(transition);
	}

	@Override
	public void startNewCycle() {
		// adds a new state and join all transitions at this state
		State join = fence();

		// start a new cycle from target
		startNewCycleFrom(join);
	}

	/**
	 * Starts a new cycle from the given source state.
	 * 
	 * @param source
	 *            source state
	 * @return the new transition created
	 */
	public Transition startNewCycleFrom(State source) {
		return addTransitionFrom(source);
	}

}
