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

import static com.synflow.cflow.CflowConstants.PROP_AVAILABLE;
import static com.synflow.cflow.CflowConstants.PROP_READ;
import static com.synflow.models.util.SwitchUtil.DONE;
import static com.synflow.models.util.SwitchUtil.visit;

import org.eclipse.emf.ecore.EObject;

import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.Branch;
import com.synflow.cflow.cflow.ExpressionVariable;
import com.synflow.cflow.cflow.StatementFence;
import com.synflow.cflow.cflow.StatementIdle;
import com.synflow.cflow.cflow.StatementIf;
import com.synflow.cflow.cflow.StatementLoop;
import com.synflow.cflow.cflow.StatementWrite;
import com.synflow.cflow.cflow.VarRef;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.internal.instantiation.IMapper;
import com.synflow.cflow.internal.scheduler.node.Node;
import com.synflow.cflow.internal.services.VoidCflowSwitch;
import com.synflow.models.util.Void;

/**
 * This class defines a cycle detector.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CycleDetector extends VoidCflowSwitch implements ICycleListener {

	private final Schedule schedule;

	/**
	 * Creates a new cycle detector with a new schedule.
	 */
	public CycleDetector(IMapper mapper) {
		schedule = new Schedule(mapper);
	}

	/**
	 * Creates a new cycle detector with a copy of the given schedule, so if cycle breaks occur, the
	 * given schedule is not modified.
	 * 
	 * @param schedule
	 *            a schedule
	 */
	public CycleDetector(Schedule schedule) {
		this.schedule = new Schedule(schedule);
	}

	@Override
	public Void caseExpressionVariable(ExpressionVariable expr) {
		VarRef ref = expr.getSource();
		String property = expr.getProperty();
		if (PROP_READ.equals(property) || PROP_AVAILABLE.equals(property)) {
			schedule.read(ref);
		}

		Variable variable = ref.getVariable();
		super.caseExpressionVariable(expr);
		if (CflowUtil.isFunctionNotConstant(variable)) {
			// if variable is a function with side-effect, we visit it
			doSwitch(variable);
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
		schedule.startNewCycle();
		return DONE;
	}

	@Override
	public Void caseStatementIf(StatementIf stmtIf) {
		IfBehavior ifBehavior = new IfBehaviorBasic(schedule);
		Node fork = ifBehavior.fork();
		for (Branch branch : stmtIf.getBranches()) {
			ifBehavior.startBranch(fork);
			schedule.visitBranch(this, branch);
		}
		ifBehavior.join(fork);

		return DONE;
	}

	@Override
	public Void caseStatementLoop(StatementLoop stmt) {
		if (CflowUtil.isLoopSimple(stmt)) {
			return DONE;
		}

		visit(this, stmt.getInit());
		schedule.startNewCycle();

		// visit condition and body
		schedule.visitCondition(this, stmt.getCondition());
		visit(this, stmt.getBody(), stmt.getAfter());

		// starts a new cycle
		schedule.startNewCycle();

		// this deserves an explanation
		// we visit the condition again because we're on the exit edge of the loop
		// and we must record the peeks we do here
		schedule.visitCondition(this, stmt.getCondition());

		return DONE;
	}

	@Override
	public Void caseStatementWrite(StatementWrite stmt) {
		schedule.write(this, stmt);
		return DONE;
	}

	/**
	 * Returns <code>true</code> if the given object contains cycle breaks.
	 * 
	 * @param eObject
	 *            an EObject
	 * @return a boolean indicating if the object has cycle breaks
	 */
	public boolean hasCycleBreaks(EObject eObject) {
		Schedule oldSchedule = schedule;
		try {
			oldSchedule.addListener(this);
			doSwitch(eObject);
			return false;
		} catch (CycleBreakException e) {
			return true;
		} finally {
			oldSchedule.removeListener(this);
		}
	}

	@Override
	public void newCycleStarted() {
		throw new CycleBreakException();
	}

}
