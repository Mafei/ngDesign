/*******************************************************************************
 * Copyright (c) 2013 Synflow SAS.
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

import org.eclipse.emf.ecore.EObject;

import com.synflow.cx.instantiation.IInstantiator;
import com.synflow.cx.internal.scheduler.path.Path;
import com.synflow.cx.internal.scheduler.path.PathIterable;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.FSM;
import com.synflow.models.dpn.Transition;
import com.synflow.models.node.Node;
import com.synflow.models.util.SwitchUtil;

/**
 * This class defines a scheduler of 'if' statements.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class IfScheduler {

	private final Actor actor;

	private final IInstantiator instantiator;

	public IfScheduler(IInstantiator instantiator, Actor actor) {
		this.instantiator = instantiator;
		this.actor = actor;
	}

	public void visit() {
		FSM fsm = actor.getFsm();
		for (Transition transition : new ArrayList<>(fsm.getTransitions())) {
			visit(transition);
		}
	}

	private void visit(Transition transition) {
		List<EObject> eObjects = transition.getBody();
		IfAnalyzer analyzer = new IfAnalyzer();
		SwitchUtil.visit(analyzer, eObjects);

		Node node = analyzer.getRoot();
		if (!node.hasChildren()) {
			// no 'if' statement
			return;
		}

		List<Transition> transitions = new ArrayList<>();
		IfDeveloper developer = new IfDeveloper(instantiator, actor);
		for (Path path : new PathIterable(node)) {
			// System.out.println(path);
			transitions.add(developer.visit(transition, path));
		}

		FSM fsm = actor.getFsm();
		actor.getActions().remove(transition.getAction());

		// watch this: we must insert the new transitions AT THE SAME PLACE as the old one
		// why? because order is important: in the case of a loop, we test the condition first
		// so this order MUST BE MAINTAINED
		int index = fsm.getTransitions().indexOf(transition);
		fsm.remove(transition);
		fsm.getTransitions().addAll(index, transitions);
	}

}
