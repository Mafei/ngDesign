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

import static com.synflow.models.util.SwitchUtil.visit;

import org.eclipse.emf.ecore.util.Switch;

import com.synflow.cx.cx.Branch;
import com.synflow.cx.cx.CExpression;
import com.synflow.cx.cx.StatementWrite;
import com.synflow.cx.cx.VarRef;
import com.synflow.cx.internal.instantiation.IInstantiator;
import com.synflow.models.dpn.Action;
import com.synflow.models.dpn.DpnFactory;
import com.synflow.models.dpn.Pattern;
import com.synflow.models.dpn.Port;
import com.synflow.models.node.Node;
import com.synflow.models.util.Void;

/**
 * This class tracks the reads/writes in a current task, and is used to compute the cycle-accurate
 * behavior.
 * 
 * @author Matthieu
 * 
 */
public class Schedule {

	private final IInstantiator instantiator;

	private ICycleListener listener;

	private Node node;

	private boolean usePeek;

	public Schedule(IInstantiator instantiator) {
		this.instantiator = instantiator;
		node = new Node(DpnFactory.eINSTANCE.createActionEmpty());
	}

	/**
	 * Creates a new schedule whose reads/writes are copied from the given schedule.
	 * 
	 * @param schedule
	 *            an existing schedule
	 */
	public Schedule(Schedule schedule) {
		this(schedule.instantiator);
		DpnFactory.eINSTANCE.addPatterns(getAction(), schedule.getAction());
		this.listener = schedule.listener;
	}

	public void addListener(ICycleListener listener) {
		this.listener = listener;
	}

	/**
	 * Returns the current action holding the peek/input/output patterns.
	 * 
	 * @return an action
	 */
	protected final Action getAction() {
		return getAction(node);
	}

	/**
	 * Returns the action associated with the given node.
	 * 
	 * @param node
	 *            a node
	 * @return an action
	 */
	protected Action getAction(Node node) {
		return (Action) node.getContent();
	}

	public Node getNode() {
		return node;
	}

	/**
	 * Returns <code>true</code> if the port has been read in the current cycle.
	 * 
	 * @param port
	 *            an input port
	 * @return a boolean
	 */
	protected boolean hasBeenRead(Port port) {
		return getAction().getInputPattern().contains(port);
	}

	/**
	 * Returns <code>true</code> if the port has been written in the current cycle.
	 * 
	 * @param port
	 *            an output port
	 * @return a boolean
	 */
	protected boolean hasBeenWritten(Port port) {
		return getAction().getOutputPattern().contains(port);
	}

	/**
	 * Promote peeks to reads (and subsequently clears peeks).
	 * 
	 * @param action
	 */
	public final void promotePeeks(Action action) {
		Pattern peek = action.getPeekPattern();
		action.getInputPattern().add(peek);
		peek.clear();
	}

	/**
	 * Registers a read from the given port.
	 * 
	 * @param ref
	 *            reference to an input port
	 */
	public void read(VarRef ref) {
		Port port = instantiator.getPort(ref);
		if (hasBeenRead(port)) {
			startNewCycle();
		}

		if (usePeek) {
			getAction().getPeekPattern().add(port);
		} else {
			getAction().getInputPattern().add(port);
		}
	}

	public void removeListener(ICycleListener listener) {
		this.listener = null;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	/**
	 * Starts a new cycle, clears the current node's children, and updates its content to a new
	 * empty action.
	 */
	public void startNewCycle() {
		// if the node had any child branches, clear them up
		node.clearChildren();

		node.setContent(DpnFactory.eINSTANCE.createActionEmpty());
		if (listener != null) {
			listener.newCycleStarted();
		}
	}

	@Override
	public String toString() {
		return getAction().toString();
	}

	/**
	 * Visits the given branch (condition and body) with the given void switch, and then promote
	 * peeks.
	 * 
	 * @param voidSwitch
	 *            an EMF Void Switch
	 * @param branch
	 *            a Branch
	 */
	public void visitBranch(Switch<Void> voidSwitch, Branch branch) {
		visitCondition(voidSwitch, branch.getCondition());
		visit(voidSwitch, branch.getBody());
		promotePeeks(getAction());
	}

	/**
	 * Visits the given condition with the given void switch.
	 * 
	 * @param voidSwitch
	 *            an EMF Void Switch
	 * @param condition
	 *            a CExpression (may be <code>null</code>)
	 */
	public void visitCondition(Switch<Void> voidSwitch, CExpression condition) {
		if (condition != null) {
			// condition may be absent (for else statements)
			usePeek = true;
			visit(voidSwitch, condition);
			usePeek = false;
		}
	}

	/**
	 * Visits a write to the given port.
	 * 
	 * @param voidSwitch
	 *            an EMF Void Switch
	 * @param stmt
	 *            a write statement
	 */
	public void write(Switch<Void> voidSwitch, StatementWrite stmt) {
		// first check for existing writes
		Port port = instantiator.getPort(stmt.getPort());
		if (hasBeenWritten(port)) {
			startNewCycle();
		}

		// only then visit the value
		visit(voidSwitch, stmt.getValue());

		// and records the fact that we are doing a write
		getAction().getOutputPattern().add(port);
	}

}
