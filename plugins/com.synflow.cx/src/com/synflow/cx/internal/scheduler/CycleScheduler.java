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

import static com.synflow.cx.internal.AstUtil.and;
import static com.synflow.cx.internal.AstUtil.exprTrue;
import static com.synflow.cx.internal.AstUtil.not;
import static com.synflow.models.util.SwitchUtil.DONE;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.ImmutableList;
import com.synflow.cx.cx.Branch;
import com.synflow.cx.cx.CxExpression;
import com.synflow.cx.cx.CxFactory;
import com.synflow.cx.cx.StatementIf;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.instantiation.IInstantiator;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.FSM;
import com.synflow.models.dpn.State;
import com.synflow.models.dpn.Transition;
import com.synflow.models.graph.Edge;
import com.synflow.models.node.Node;
import com.synflow.models.util.SwitchUtil;
import com.synflow.models.util.Void;

/**
 * This class defines a cycle scheduler.
 * 
 * @author Matthieu Wipliez
 * 
 */
public final class CycleScheduler extends AbstractCycleScheduler {

	private boolean associate = true;

	/**
	 * Creates a new cycle scheduler that will schedule cycles in the given actor.
	 * 
	 * @param instantiator
	 *            instantiator
	 * @param actor
	 *            an actor
	 */
	public CycleScheduler(IInstantiator instantiator, Actor actor) {
		super(instantiator, actor);
	}

	@Override
	protected Void associate(EObject eObject) {
		if (associate) {
			super.associate(eObject);
		}
		return DONE;
	}

	/**
	 * Returns <code>true</code> if the given transition can be safely removed. This means: no
	 * input/peek pattern, empty body, empty scheduler.
	 * 
	 * @param transition
	 *            a transition
	 * @return a boolean indicating whether the transition can be deleted
	 */
	private boolean canDelete(Transition transition) {
		return transition.getAction().getInputPattern().isEmpty()
				&& transition.getAction().getPeekPattern().isEmpty()
				&& isEmpty(transition.getBody()) && isEmpty(transition.getScheduler());
	}

	@Override
	public Void caseStatementIf(StatementIf stmtIf) {
		if (new CycleDetector(schedule).hasCycleBreaks(stmtIf)) {
			translateMultiCycleIf(stmtIf);
		} else {
			translateSimpleIf(stmtIf);
		}

		return DONE;
	}

	private boolean disableAssociate() {
		boolean oldAssociate = associate;
		this.associate = false;
		return oldAssociate;
	}

	/**
	 * Returns an iterable of branches based on the given 'if' statement, including an explicit
	 * 'else' branch if the statement does not have one.
	 * 
	 * @param stmtIf
	 * @return
	 */
	private Iterable<Branch> getBranches(StatementIf stmtIf) {
		List<Branch> branches = stmtIf.getBranches();
		Branch lastBranch = branches.get(branches.size() - 1);
		if (lastBranch.getCondition() == null) {
			return branches;
		} else {
			Branch branch = CxFactory.eINSTANCE.createBranch();
			return ImmutableList.<Branch> builder().addAll(branches).add(branch).build();
		}
	}

	/**
	 * Includes the tBefore transition in the given transition.
	 * 
	 * @param tBefore
	 *            transition before a multi-cycle if
	 * @param transition
	 *            first transition of the current branch
	 */
	private void includePreamble(Transition tBefore, Transition transition) {
		// we visit tBefore to register any reads/writes
		// also incidentally this copies any statement to transition's body
		List<EObject> body = tBefore.getBody();
		SwitchUtil.visit(this, body);

		// we also add tBefore's scheduler and body to transition's scheduler and body
		// (but this time we just copy it the simple way)
		transition.getScheduler().addAll(0, tBefore.getScheduler());
		transition.getScheduler().addAll(0, body);
	}

	/**
	 * Returns <code>true</code> if a cycle break is required before the 'if', which happens only
	 * when a *condition* causes a cycle break. We don't care about the branch's body because this
	 * does not cause trouble.
	 * 
	 * @param stmtIf
	 *            'if' statement
	 * @return
	 */
	private boolean isBreakRequired(StatementIf stmtIf) {
		if (schedule.hasMultipleTransitions()) {
			return true;
		}

		List<Branch> branches = stmtIf.getBranches();
		for (Branch branch : branches) {
			CxExpression condition = branch.getCondition();
			if (condition != null) {
				if (new CycleDetector(schedule).hasCycleBreaks(condition)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Schedules the 'setup' and then the 'loop' function.
	 * 
	 * @param setup
	 *            setup function
	 * @param loop
	 *            loop function
	 */
	public void schedule(Variable setup, Variable loop) {
		// visit setup
		if (setup != null) {
			doSwitch(setup);
			schedule.startNewCycle();
		}

		// visit loop
		State loopInitial = schedule.getTransition().getSource();
		if (loop != null) {
			doSwitch(loop);
		}

		// make FSM loop back to loop's initial state
		schedule.mergeTransitions(loopInitial);

		// remove empty transitions
		FSM fsm = schedule.getFsm();
		for (Transition transition : new ArrayList<>(fsm.getTransitions())) {
			if (canDelete(transition)) {
				State source = transition.getSource();
				State target = transition.getTarget();
				if (source == target && source == loopInitial) {
					fsm.remove(transition);
					continue;
				}

				// re-attach source's incoming transitions to target
				for (Edge edge : new ArrayList<>(source.getIncoming())) {
					edge.setTarget(target);
				}

				// remove source and its outgoing transitions
				fsm.remove(source);

				if (source == fsm.getInitialState()) {
					fsm.setInitialState(target);
				}
			}
		}
	}

	/**
	 * Translates a multi-cycle if statement.
	 * 
	 * @param stmtIf
	 */
	private void translateMultiCycleIf(StatementIf stmtIf) {
		// must compute before any modification of the schedule
		// if no break is required, we will include the 'before' transition
		boolean breakRequired = isBreakRequired(stmtIf);

		Transition tBefore = null;
		State fork;

		// updates fork
		if (breakRequired) {
			fork = schedule.fence();
		} else {
			tBefore = schedule.getTransition();
			fork = tBefore.getSource();
		}

		IfBehavior behavior = new IfBehaviorMulti(schedule, fork);
		Node forkNode = behavior.fork();

		// visits all branches
		List<CxExpression> previousConditions = new ArrayList<>();
		for (Branch branch : getBranches(stmtIf)) {
			behavior.startBranch(forkNode);
			Transition transition = schedule.getTransition();

			if (!breakRequired) {
				includePreamble(tBefore, transition);
			}

			// reverse previous conditions
			CxExpression condition = exprTrue();
			for (CxExpression previous : previousConditions) {
				// visit each previous condition as if it were reversed
				// to properly update peek patterns
				visitNotCondition(previous);
				condition = and(condition, not(previous));
			}

			// visit this branch's condition
			if (branch.getCondition() != null) {
				condition = and(condition, branch.getCondition());
				previousConditions.add(branch.getCondition());
			}

			// adds the condition to this scheduler
			transition.getScheduler().add(condition);

			// visits the branch
			schedule.visitBranch(this, branch);
		}

		// when no break is required, we must remove the 'tBefore' transition and action
		if (!breakRequired) {
			schedule.getFsm().remove(tBefore);
		}

		behavior.join(forkNode);
	}

	/**
	 * Translates a simple if statement (that does not span over multiple cycles).
	 * 
	 * @param stmtIf
	 */
	private void translateSimpleIf(StatementIf stmtIf) {
		// don't actually associate any object when visiting branches
		// keep in mind that only the stmtIf will be associated (see below)
		// and it is developed later by the IfDeveloper
		boolean oldAssociate = disableAssociate();

		IfBehavior behavior = new IfBehaviorMono(schedule);
		Node fork = behavior.fork();
		for (Branch branch : stmtIf.getBranches()) {
			behavior.startBranch(fork);
			schedule.visitBranch(this, branch);
		}
		behavior.join(fork);

		// records this statement and associate this statement
		this.associate = oldAssociate;
		associate(stmtIf);
	}

}
