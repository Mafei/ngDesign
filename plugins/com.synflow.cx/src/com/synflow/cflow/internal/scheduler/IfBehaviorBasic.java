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
package com.synflow.cflow.internal.scheduler;

import com.synflow.cflow.internal.scheduler.node.Node;
import com.synflow.models.dpn.Action;
import com.synflow.models.dpn.DpnFactory;

/**
 * This class defines a basic if behavior for use in CycleDetector based on Schedule.
 * 
 * @author Matthieu Wipliez
 *
 */
public class IfBehaviorBasic implements IfBehavior {

	private Schedule schedule;

	public IfBehaviorBasic(Schedule schedule) {
		this.schedule = schedule;
	}

	@Override
	public Node fork() {
		return schedule.getNode();
	}

	@Override
	public void join(Node fork) {
		schedule.setNode(fork);
		Action action = schedule.getAction();
		for (Node child : fork.getChildren()) {
			DpnFactory.eINSTANCE.addPatterns(action, schedule.getAction(child));
		}
		fork.clearChildren();
	}

	@Override
	public void startBranch(Node fork) {
		Node node = new Node(fork, DpnFactory.eINSTANCE.copy((Action) fork.getContent()));
		schedule.setNode(node);
	}

}
