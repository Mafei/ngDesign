/*******************************************************************************
 * Copyright (c) 2014 Synflow SAS.
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

import com.synflow.cx.internal.scheduler.node.Node;
import com.synflow.models.dpn.Action;
import com.synflow.models.dpn.DpnFactory;

/**
 * This class defines a if behavior to handle mono-cycle if statements.
 * 
 * @author Matthieu Wipliez
 *
 */
public class IfBehaviorMono implements IfBehavior {

	private Action action;

	private List<Action> actions;

	private ScheduleFsm schedule;

	public IfBehaviorMono(ScheduleFsm schedule) {
		this.schedule = schedule;
		this.action = schedule.getAction();
		actions = new ArrayList<>();
	}

	@Override
	public Node fork() {
		return schedule.getNode();
	}

	@Override
	public void join(Node fork) {
		schedule.getTransition().setAction(action);

		schedule.setNode(fork);
		for (Action branchAction : actions) {
			DpnFactory.eINSTANCE.addPatterns(action, branchAction);
		}
		fork.clearChildren();
	}

	@Override
	public void startBranch(Node fork) {
		Action copy = DpnFactory.eINSTANCE.copy(action);
		schedule.getTransition().setAction(copy);
		actions.add(copy);

		schedule.setNode(new Node(fork, fork.getContent()));
	}

}
