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

import com.synflow.models.dpn.State;
import com.synflow.models.node.Node;

/**
 * This class defines a if behavior to handle multi-cycle if statements.
 * 
 * @author Matthieu Wipliez
 *
 */
public class IfBehaviorMulti implements IfBehavior {

	private ScheduleFsm schedule;

	private State state;

	public IfBehaviorMulti(ScheduleFsm schedule, State fork) {
		this.schedule = schedule;
		this.state = fork;
	}

	@Override
	public Node fork() {
		return schedule.getNode();
	}

	@Override
	public void join(Node fork) {
		schedule.setNode(fork);
	}

	@Override
	public void startBranch(Node fork) {
		schedule.setNode(new Node(fork));
		schedule.addTransitionFrom(state);
	}

}
