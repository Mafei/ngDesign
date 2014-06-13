/*******************************************************************************
 * Copyright (c) 2012-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cflow.validation;

import static com.synflow.cflow.CflowConstants.NAME_LOOP;
import static com.synflow.cflow.CflowConstants.NAME_LOOP_DEPRECATED;
import static com.synflow.cflow.CflowConstants.NAME_SETUP;
import static com.synflow.cflow.CflowConstants.NAME_SETUP_DEPRECATED;
import static com.synflow.cflow.validation.IssueCodes.ERR_MAIN_FUNCTION_BAD_TYPE;
import static com.synflow.core.IProperties.PROP_CLOCKS;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.xtext.validation.Check;

import com.google.inject.Inject;
import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.CflowPackage.Literals;
import com.synflow.cflow.cflow.GenericEntity;
import com.synflow.cflow.cflow.Module;
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.cflow.cflow.Network;
import com.synflow.cflow.cflow.Task;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.internal.ErrorMarker;
import com.synflow.cflow.internal.instantiation.IInstantiator;
import com.synflow.cflow.internal.scheduler.CycleDetector;
import com.synflow.cflow.internal.services.Typer;
import com.synflow.cflow.internal.validation.NetworkChecker;
import com.synflow.cflow.internal.validation.TypeChecker;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.DPN;

/**
 * This class defines a validator for C~ source files.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CflowJavaValidator extends AbstractCflowJavaValidator {

	@Inject
	private IInstantiator instantiator;

	@Inject
	private Typer typer;

	@Check
	public void checkModule(Module module) {
		EList<Diagnostic> errors = module.eResource().getErrors();
		if (!errors.isEmpty()) {
			// skip validation as long as the module has syntax errors or link errors
			return;
		}

		NetworkChecker networkChecker = new NetworkChecker(this, instantiator);

		for (NamedEntity cfEntity : module.getEntities()) {
			try {
				if (cfEntity instanceof Network) {
					Network network = (Network) cfEntity;

					// step 1: instantiate
					// translates and checks properties
					// instantiates and checks arguments
					DPN dpn = (DPN) instantiator.getEntity(network);

					// step 2: check connectivity
					// must occur after instantiation
					networkChecker.checkDPN(network, dpn);
				} else {
					instantiator.getEntity(cfEntity);
				}
			} finally {
				if (cfEntity instanceof GenericEntity) {
					printErrors((GenericEntity) cfEntity);
				}
			}
		}

		// TODO type checking for each configuration of each generic task
		for (NamedEntity entity : module.getEntities()) {
			new TypeChecker(this, typer).doSwitch(entity);
		}
	}

	@Check
	public void checkTask(Task task) {
		Variable loop = CflowUtil.getFunction(task, NAME_LOOP);
		if (loop == null) {
			loop = CflowUtil.getFunction(task, NAME_LOOP_DEPRECATED);
			if (loop == null) {
				return;
			}
		}

		if (!CflowUtil.isVoid(loop)) {
			String message = "The 'loop' function must have type void";
			error(message, loop, Literals.VARIABLE__NAME, ERR_MAIN_FUNCTION_BAD_TYPE);
		}

		Variable setup = CflowUtil.getFunction(task, NAME_SETUP);
		if (setup == null) {
			setup = CflowUtil.getFunction(task, NAME_SETUP_DEPRECATED);
		}

		if (setup != null && !CflowUtil.isVoid(setup)) {
			String message = "The 'setup' function must have type void";
			error(message, setup, Literals.VARIABLE__NAME, ERR_MAIN_FUNCTION_BAD_TYPE);
		}

		Actor actor = (Actor) instantiator.getEntity(task);
		if (actor.getProperties().getAsJsonArray(PROP_CLOCKS).size() == 0) {
			validate(task, setup, loop);
		}
	}

	private void printErrors(GenericEntity entity) {
		for (ErrorMarker error : entity.getErrors()) {
			acceptError(error.getMessage(), error.getSource(), error.getFeature(),
					error.getIndex(), null);
		}
	}

	/**
	 * Validates the given task.
	 * 
	 * @param module
	 *            a module with a combinational main function
	 * @param scope
	 *            scope of functions
	 */
	public void validate(Task task, Variable setup, Variable run) {
		for (Variable variable : CflowUtil.getStateVars(task.getDecls())) {
			if (!CflowUtil.isFunction(variable) && !CflowUtil.isConstant(variable)) {
				String message = "A combinational task cannot declare state variables";
				error(message, variable, Literals.VARIABLE__NAME, -1);
				return;
			}
		}

		if (setup != null) {
			String message = "A combinational task cannot have a 'setup' function";
			error(message, setup, Literals.VARIABLE__NAME, -1);
		}

		if (run != null) {
			if (new CycleDetector(instantiator).hasCycleBreaks(run)) {
				String message = "A combinational task must not have cycle breaks";
				error(message, run, null, -1);
			}
		}
	}
}
