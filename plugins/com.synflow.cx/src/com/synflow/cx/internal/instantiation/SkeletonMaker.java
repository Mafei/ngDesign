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
package com.synflow.cx.internal.instantiation;

import static com.synflow.cx.internal.TransformerUtil.getStartLine;
import static com.synflow.models.ir.IrFactory.eINSTANCE;
import static com.synflow.models.util.SwitchUtil.DONE;

import org.eclipse.xtext.EcoreUtil2;

import com.google.inject.Inject;
import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.Bundle;
import com.synflow.cx.cx.CxEntity;
import com.synflow.cx.cx.Instantiable;
import com.synflow.cx.cx.Module;
import com.synflow.cx.cx.Network;
import com.synflow.cx.cx.Task;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.internal.services.Typer;
import com.synflow.cx.services.Evaluator;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.DpnFactory;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.InterfaceType;
import com.synflow.models.dpn.Port;
import com.synflow.models.dpn.Unit;
import com.synflow.models.dpn.util.DpnSwitch;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.Var;
import com.synflow.models.ir.util.ValueUtil;
import com.synflow.models.util.Void;

/**
 * This class creates the skeleton of an IR entity: state variables, ports.
 * 
 * @author Matthieu Wipliez
 *
 */
public class SkeletonMaker extends DpnSwitch<Void> {

	private CxEntity cxEntity;

	@Inject
	private IInstantiator instantiator;

	@Inject
	private Typer typer;

	@Override
	public Void caseActor(Actor actor) {
		Task task = (Task) cxEntity;
		translateStateVars(actor, task);
		translatePorts(actor, task);
		return DONE;
	}

	@Override
	public Void caseDPN(DPN dpn) {
		Network network = (Network) cxEntity;
		setFileAndLine(dpn, network);
		translateStateVars(dpn, network);
		translatePorts(dpn, network);
		return DONE;
	}

	@Override
	public Void caseUnit(Unit unit) {
		Bundle bundle = (Bundle) cxEntity;
		setFileAndLine(unit, bundle);
		translateStateVars(unit, bundle);
		return DONE;
	}

	public void createSkeleton(CxEntity cxEntity, Entity entity) {
		this.cxEntity = cxEntity;
		try {
			doSwitch(entity);
		} finally {
			this.cxEntity = null;
		}
	}

	/**
	 * Sets filename and line number of IR entity from Cx entity.
	 * 
	 * @param entity
	 *            IR entity
	 * @param cxEntity
	 *            Cx entity
	 */
	private void setFileAndLine(Entity entity, CxEntity cxEntity) {
		// set file name
		Module module = EcoreUtil2.getContainerOfType(cxEntity, Module.class);
		String fileName = CxUtil.getFileName(module);
		entity.setFileName(fileName);

		// set line number
		int lineNumber = getStartLine(cxEntity);
		entity.setLineNumber(lineNumber);
	}

	public void transformPort(final Entity entity, final Variable port) {
		InterfaceType ifType = CxUtil.getInterface(port);
		Type type = typer.doSwitch(port);
		String name = port.getName();

		Port dpnPort = DpnFactory.eINSTANCE.createPort(type, name, ifType);
		if (CxUtil.isInput(port)) {
			entity.getInputs().add(dpnPort);
		} else {
			entity.getOutputs().add(dpnPort);
		}

		instantiator.putMapping(port, dpnPort);
	}

	/**
	 * Translates the given C~ variable into an IR Procedure or Var.
	 * 
	 * @param variable
	 * @return
	 */
	private void transformVariable(Entity entity, Variable variable) {
		int lineNumber = getStartLine(variable);
		Type type = typer.doSwitch(variable);
		String name = variable.getName();

		if (CxUtil.isFunction(variable)) {
			Procedure procedure = eINSTANCE.createProcedure(name, lineNumber, type);
			entity.getProcedures().add(procedure);
			instantiator.putMapping(variable, procedure);
		} else {
			boolean assignable = !CxUtil.isConstant(variable);

			// retrieve initial value (may be null)
			Object value = Evaluator.getValue(variable.getValue());
			Expression init = ValueUtil.getExpression(value);

			// create var
			Var var = eINSTANCE.createVar(lineNumber, type, name, assignable, init);

			// add to variables list of containing entity
			entity.getVariables().add(var);
			instantiator.putMapping(variable, var);
		}
	}

	private void translatePorts(Entity entity, Instantiable instantiable) {
		// transform ports
		for (Variable variable : CxUtil.getPorts(instantiable.getPortDecls())) {
			transformPort(entity, variable);
		}
	}

	private void translateStateVars(Entity entity, CxEntity cxEntity) {
		// transform variables and constant functions
		for (Variable variable : CxUtil.getStateVars(cxEntity.getDecls())) {
			if (CxUtil.isConstant(variable) || !CxUtil.isFunction(variable)) {
				transformVariable(entity, variable);
			}
		}
	}

}
