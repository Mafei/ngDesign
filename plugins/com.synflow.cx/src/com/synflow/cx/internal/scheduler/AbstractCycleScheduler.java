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

import static com.google.common.collect.Iterables.concat;
import static com.synflow.cx.CxConstants.PROP_AVAILABLE;
import static com.synflow.cx.CxConstants.PROP_READ;
import static com.synflow.cx.internal.AstUtil.not;
import static com.synflow.models.util.SwitchUtil.DONE;
import static com.synflow.models.util.SwitchUtil.visit;

import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.CxExpression;
import com.synflow.cx.cx.CxFactory;
import com.synflow.cx.cx.Enter;
import com.synflow.cx.cx.ExpressionVariable;
import com.synflow.cx.cx.Leave;
import com.synflow.cx.cx.StatementAssert;
import com.synflow.cx.cx.StatementAssign;
import com.synflow.cx.cx.StatementFence;
import com.synflow.cx.cx.StatementIdle;
import com.synflow.cx.cx.StatementIf;
import com.synflow.cx.cx.StatementLabeled;
import com.synflow.cx.cx.StatementLoop;
import com.synflow.cx.cx.StatementPrint;
import com.synflow.cx.cx.StatementReturn;
import com.synflow.cx.cx.StatementWrite;
import com.synflow.cx.cx.VarRef;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.instantiation.IInstantiator;
import com.synflow.cx.internal.TransformerUtil;
import com.synflow.cx.internal.services.VoidCxSwitch;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.State;
import com.synflow.models.dpn.Transition;
import com.synflow.models.util.SwitchUtil;
import com.synflow.models.util.Void;

/**
 * This class defines a cycle scheduler.
 * 
 * @author Matthieu Wipliez
 * 
 */
public abstract class AbstractCycleScheduler extends VoidCxSwitch {

	private Deque<String> callChain = new ArrayDeque<>();

	private boolean ignoreAvailable;

	protected final ScheduleFsm schedule;

	/**
	 * Creates a new cycle scheduler that will schedule cycles in the given actor.
	 * 
	 * @param mapper
	 *            mapper
	 * @param actor
	 *            an actor
	 */
	public AbstractCycleScheduler(IInstantiator instantiator, Actor actor) {
		schedule = new ScheduleFsm(instantiator, actor);
	}

	/**
	 * Associates the given object to the current transition(s).
	 * 
	 * @param eObject
	 *            a statement or an expression
	 * @return DONE
	 */
	protected Void associate(EObject eObject) {
		for (Transition transition : schedule.getTransitions()) {
			transition.getBody().add(eObject);
		}
		return DONE;
	}

	private Void call(VarRef ref, EList<CxExpression> parameters) {
		// visit parameters
		visit(this, parameters);

		// we call 'associate' with a enter object before the call
		// (and later with a leave, see below)
		// this gives us additional flexibility and safety, especially to avoid double visiting
		Enter enter = CxFactory.eINSTANCE.createEnter();
		enter.setFunction(ref);
		enter.setLineNumber(TransformerUtil.getStartLine(ref));
		enter.getParameters().addAll(parameters);
		associate(enter);

		// visit function (schedule its statements)
		Variable variable = ref.getVariable();
		pushName(variable.getName());
		doSwitch(variable);
		popName();

		// and now we call associate with a leave object after the call
		associate(CxFactory.eINSTANCE.createLeave());

		return DONE;
	}

	@Override
	public Void caseExpressionVariable(ExpressionVariable expr) {
		VarRef ref = expr.getSource();
		Variable variable = ref.getVariable();
		if (CxUtil.isFunctionNotConstant(variable)) {
			return call(ref, expr.getParameters());
		} else {
			String property = expr.getProperty();
			if (PROP_READ.equals(property) || !ignoreAvailable && PROP_AVAILABLE.equals(property)) {
				schedule.read(ref);
			}

			return super.caseExpressionVariable(expr);
		}
	}

	@Override
	public Void caseStatementAssert(StatementAssert stmt) {
		super.caseStatementAssert(stmt);
		return associate(stmt);
	}

	@Override
	public Void caseStatementAssign(StatementAssign stmt) {
		super.caseStatementAssign(stmt);

		Variable variable = stmt.getTarget().getSource().getVariable();
		if (!CxUtil.isFunctionNotConstant(variable)) {
			// if variable is a function with side effects, 'associate' has already been called by
			// caseExpressionVariable, so we must not associate this statement again
			associate(stmt);
		}

		return DONE;
	}

	@Override
	public Void caseStatementFence(StatementFence stmt) {
		schedule.startNewCycle();
		return DONE;
	}

	@Override
	public Void caseStatementIdle(StatementIdle stmt) {
		int numCycles = schedule.instantiator.evaluateInt(schedule.actor, stmt.getNumCycles());

		if (isEmptyTransition()) {
			// no action is associated with current transition yet
			// (e.g. when next is first statement in compound statement)
			// so first take one cycle

			// creates one action less
			numCycles--;
		}

		// adds as many transitions as numCycles
		for (int i = 0; i < numCycles; i++) {
			schedule.startNewCycle();
		}

		schedule.startNewCycle();
		return DONE;
	}

	@Override
	public abstract Void caseStatementIf(StatementIf stmtIf);

	@Override
	public Void caseStatementLabeled(StatementLabeled stmt) {
		visit(this, stmt.getStmt());

		String name = getCurrentName();
		if (name == null) {
			name = stmt.getLabel();
		} else {
			name += stmt.getLabel();
		}
		schedule.setStateName(name);

		return DONE;
	}

	@Override
	public Void caseStatementLoop(StatementLoop stmt) {
		if (CxUtil.isLoopSimple(schedule.instantiator, schedule.actor, stmt)) {
			// record this statement
			associate(stmt);
			return DONE;
		}

		SwitchUtil.visit(this, stmt.getInit());

		Transition transition = schedule.getTransition();
		if (!isEmptyTransition()) {
			// start new cycle for loop's body
			schedule.startNewCycle();
			transition = schedule.getTransition();
		}

		// save 'fork' state
		State fork = transition.getSource();

		// add condition to transition's scheduler
		transition.getScheduler().add(stmt.getCondition());

		// visit condition and body
		schedule.visitCondition(this, stmt.getCondition());
		SwitchUtil.visit(this, stmt.getBody(), stmt.getAfter());

		// make edges loop back to fork state
		schedule.mergeTransitions(fork);

		// starts a new cycle from the 'fork' state for exit edge and updates its condition
		transition = schedule.startNewCycleFrom(fork);
		transition.getScheduler().add(not(stmt.getCondition()));

		// we also visit the condition on the exit edge
		// why? because we want to register peeks, so they are later transformed
		// to reads (at the next cycle break).
		//
		// visitNotCondition sets a flag so that calls to available are ignored
		// (because we are in the 'not' edge and we don't want available becoming peeks here)
		visitNotCondition(stmt.getCondition());

		return DONE;
	}

	@Override
	public Void caseStatementPrint(StatementPrint stmt) {
		super.caseStatementPrint(stmt);
		return associate(stmt);
	}

	@Override
	public Void caseStatementReturn(StatementReturn stmt) {
		super.caseStatementReturn(stmt);
		return associate(stmt);
	}

	@Override
	public Void caseStatementWrite(StatementWrite stmt) {
		schedule.write(this, stmt);
		return associate(stmt);
	}

	@Override
	public Void caseVariable(Variable variable) {
		if (CxUtil.isFunction(variable)) {
			visit(this, variable.getBody());

			// must not associate the function with the current transition
			return DONE;
		}

		super.caseVariable(variable);
		return associate(variable);
	}

	private String getCurrentName() {
		if (callChain.isEmpty()) {
			return null;
		} else {
			return Joiner.on('_').join(callChain);
		}
	}

	/**
	 * Returns <code>true</code> if the current transition and its children (if any) are empty.
	 * 
	 * @return a boolean indicating whether the current transition and its children are empty
	 */
	protected final boolean isEmptyTransition() {
		for (Transition transition : schedule.getTransitions()) {
			if (!isEmptyTransition(transition)) {
				return false;
			}
		}
		return true;
	}

	private boolean isEmptyTransition(Transition transition) {
		Iterable<EObject> iterable = concat(transition.getScheduler(), transition.getBody());

		// returns true if iterable is empty or it just contains Enter/Leave instances
		return Iterables.all(iterable, new Predicate<EObject>() {
			@Override
			public boolean apply(EObject eObject) {
				return eObject instanceof Enter || eObject instanceof Leave;
			}
		});
	}

	private void popName() {
		callChain.removeLast();
		schedule.setStateName(getCurrentName());
	}

	private void pushName(String name) {
		callChain.addLast(name);
		schedule.setStateName(getCurrentName());
	}

	/**
	 * Visits the condition as if it were negated. This ignores any call to available, because
	 * '!port.available' is equivalent to 'absence of data on port'.
	 * 
	 * @param condition
	 *            a condition
	 */
	protected final void visitNotCondition(CxExpression condition) {
		ignoreAvailable = true;
		schedule.visitCondition(this, condition);
		ignoreAvailable = false;
	}

}
