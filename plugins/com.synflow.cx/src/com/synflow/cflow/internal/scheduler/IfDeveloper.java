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

import static com.synflow.cflow.internal.AstUtil.and;
import static com.synflow.cflow.internal.AstUtil.exprTrue;
import static com.synflow.cflow.internal.AstUtil.not;
import static com.synflow.models.util.SwitchUtil.DONE;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.Branch;
import com.synflow.cflow.cflow.CExpression;
import com.synflow.cflow.cflow.Enter;
import com.synflow.cflow.cflow.Leave;
import com.synflow.cflow.cflow.StatementIf;
import com.synflow.cflow.internal.instantiation.IInstantiator;
import com.synflow.cflow.internal.scheduler.path.Path;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.DpnFactory;
import com.synflow.models.dpn.Transition;
import com.synflow.models.util.SwitchUtil;
import com.synflow.models.util.Void;

/**
 * This class develops a single transition with 'if' statements into multiple transitions according
 * to a code path. This extends CycleScheduler with cases for statement if (obviously) but also for
 * Enter and Leave. Indeed, these are artificial objects (in the sense not generated directly from
 * the source) created by the CycleScheduler in the first pass. When the IfDeveloper develops a
 * transition, these objects must be associated with the current transition.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class IfDeveloper extends AbstractCycleScheduler {

	private Path path;

	public IfDeveloper(IInstantiator instantiator, Actor actor) {
		super(instantiator, actor);
	}

	@Override
	protected Void associate(EObject eObject) {
		Transition transition = schedule.getTransition();
		transition.getBody().add(eObject);
		transition.getScheduler().add(eObject);
		return DONE;
	}

	@Override
	public Void caseEnter(Enter enter) {
		// simply associate this enter with the current transition
		associate(enter);
		return DONE;
	}

	@Override
	public Void caseLeave(Leave leave) {
		// simply associate this leave with the current transition
		associate(leave);
		return DONE;
	}

	@Override
	public Void caseStatementIf(StatementIf stmt) {
		if (CflowUtil.isIfSimple(stmt)) {
			return DONE;
		}

		Branch chosen = path.getNext();
		List<Branch> branches = stmt.getBranches();
		CExpression condition = exprTrue();
		for (Branch branch : branches) {
			if (branch == chosen) {
				break;
			}

			visitNotCondition(branch.getCondition());
			condition = and(condition, not(branch.getCondition()));
		}

		Transition transition = schedule.getTransition();
		List<EObject> scheduler = transition.getScheduler();
		if (chosen.getCondition() != null) {
			condition = and(condition, chosen.getCondition());
		}

		// adds condition
		scheduler.add(condition);

		// visits branch
		schedule.visitBranch(this, chosen);

		return DONE;
	}

	/**
	 * Visits the given transition with the given code path.
	 * 
	 * @param transition
	 *            a transition
	 * @param path
	 *            the path to take
	 * @return the new transition
	 */
	public Transition visit(Transition transition, Path path) {
		Transition newTrans = DpnFactory.eINSTANCE.createTransition(transition.getSource(),
				transition.getTarget());
		schedule.setTransition(newTrans);

		// set iterator, visit objects, save pattern
		this.path = path;
		SwitchUtil.visit(this, transition.getBody());
		schedule.promotePeeks(transition.getAction());

		return newTrans;
	}

}
