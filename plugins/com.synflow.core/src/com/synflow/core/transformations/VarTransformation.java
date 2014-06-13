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
package com.synflow.core.transformations;

import static com.synflow.models.util.SwitchUtil.CASCADE;
import static com.synflow.models.util.SwitchUtil.DONE;

import com.google.common.collect.Iterables;
import com.synflow.models.dpn.Action;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Port;
import com.synflow.models.dpn.Unit;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Var;
import com.synflow.models.ir.util.AbstractIrVisitor;
import com.synflow.models.util.Void;

/**
 * This class defines a module transformation that transforms all procedures in an actor/unit.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class VarTransformation extends ModuleTransformation {

	public VarTransformation() {
	}

	public VarTransformation(AbstractIrVisitor irVisitor) {
		super(irVisitor);
	}

	@Override
	public Void caseAction(Action action) {
		visitProcedure(action.getBody());
		visitProcedure(action.getScheduler());
		return DONE;
	}

	@Override
	public Void caseActor(Actor actor) {
		for (Action action : actor.getActions()) {
			doSwitch(action);
		}

		return CASCADE;
	}

	@Override
	public Void caseEntity(Entity entity) {
		for (Port port : Iterables.concat(entity.getInputs(), entity.getOutputs())) {
			doSwitch(port);
		}

		for (Var var : Iterables.concat(entity.getParameters(), entity.getVariables())) {
			doSwitch(var);
		}

		for (Procedure procedure : entity.getProcedures()) {
			visitProcedure(procedure);
		}

		return DONE;
	}

	@Override
	public Void casePort(Port port) {
		irVisitor.doSwitch(port);
		return DONE;
	}

	@Override
	public Void caseUnit(Unit unit) {
		return CASCADE;
	}

	private void visitProcedure(Procedure procedure) {
		for (Var var : procedure.getParameters()) {
			doSwitch(var);
		}

		for (Var var : procedure.getLocals()) {
			doSwitch(var);
		}
	}

}
