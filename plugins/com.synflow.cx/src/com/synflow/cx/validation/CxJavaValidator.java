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
package com.synflow.cx.validation;

import static com.synflow.core.IProperties.PROP_CLOCKS;
import static com.synflow.cx.CxConstants.NAME_LOOP;
import static com.synflow.cx.CxConstants.NAME_LOOP_DEPRECATED;
import static com.synflow.cx.CxConstants.NAME_SETUP;
import static com.synflow.cx.CxConstants.NAME_SETUP_DEPRECATED;
import static com.synflow.cx.validation.IssueCodes.ERR_MAIN_FUNCTION_BAD_TYPE;
import static org.eclipse.xtext.validation.CheckType.NORMAL;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.xtext.validation.Check;

import com.google.inject.Inject;
import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.CxEntity;
import com.synflow.cx.cx.Instantiable;
import com.synflow.cx.cx.Module;
import com.synflow.cx.cx.Network;
import com.synflow.cx.cx.Task;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.cx.CxPackage.Literals;
import com.synflow.cx.internal.ErrorMarker;
import com.synflow.cx.internal.instantiation.IInstantiator;
import com.synflow.cx.internal.scheduler.CycleDetector;
import com.synflow.cx.internal.services.Typer;
import com.synflow.cx.internal.validation.NetworkChecker;
import com.synflow.cx.internal.validation.TypeChecker;
import com.synflow.cx.validation.AbstractCxJavaValidator;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Entity;
import com.synflow.models.util.Executable;

/**
 * This class defines a validator for Cx source files.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CxJavaValidator extends AbstractCxJavaValidator {

	@Inject
	private IInstantiator instantiator;

	@Inject
	private Typer typer;

	// TODO remove NORMAL when everything works again
	@Check(NORMAL)
	public void checkModule(Module module) {
		EList<Diagnostic> errors = module.eResource().getErrors();
		if (!errors.isEmpty()) {
			// skip validation as long as the module has syntax errors or link errors
			return;
		}

		// TODO add check to update only when necessary
		//if (!instantiator.isUpToDate()) {
			instantiator.update(module.eResource());
		//}

		final NetworkChecker networkChecker = new NetworkChecker(this, instantiator);

		// for each entity of the module
		for (final CxEntity cxEntity : module.getEntities()) {
			instantiator.forEachMapping(cxEntity, new Executable<Entity>() {
				@Override
				public void exec(Entity entity) {
					if (entity instanceof DPN) {
						// check connectivity
						Network network = (Network) cxEntity;
						DPN dpn = (DPN) entity;
						networkChecker.checkDPN(network, dpn);
					}

					// check types
					new TypeChecker(CxJavaValidator.this, instantiator, typer).doSwitch(cxEntity);

					if (cxEntity instanceof Instantiable) {
						printErrors((Instantiable) cxEntity);
					}
				}
			});
		}
	}

	@Check(NORMAL)
	public void checkTask(final Task task) {
		Variable function = CxUtil.getFunction(task, NAME_LOOP);
		if (function == null) {
			function = CxUtil.getFunction(task, NAME_LOOP_DEPRECATED);
			if (function == null) {
				return;
			}
		}

		final Variable loop = function;
		if (!CxUtil.isVoid(loop)) {
			String message = "The 'loop' function must have type void";
			error(message, loop, Literals.VARIABLE__NAME, ERR_MAIN_FUNCTION_BAD_TYPE);
		}

		function = CxUtil.getFunction(task, NAME_SETUP);
		if (function == null) {
			function = CxUtil.getFunction(task, NAME_SETUP_DEPRECATED);
		}

		final Variable setup = function;
		if (setup != null && !CxUtil.isVoid(setup)) {
			String message = "The 'setup' function must have type void";
			error(message, setup, Literals.VARIABLE__NAME, ERR_MAIN_FUNCTION_BAD_TYPE);
		}

		instantiator.forEachMapping(task, new Executable<Entity>() {
			public void exec(Entity entity) {
				if (entity.getProperties().getAsJsonArray(PROP_CLOCKS).size() == 0) {
					validate(task, setup, loop);
				}
			}
		});
	}

	private void printErrors(Instantiable entity) {
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
	public void validate(Task task, Variable setup, final Variable run) {
		for (Variable variable : CxUtil.getStateVars(task.getDecls())) {
			if (!CxUtil.isFunction(variable) && !CxUtil.isConstant(variable)) {
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
			instantiator.forEachMapping(task, new Executable<Entity>() {
				@Override
				public void exec(Entity entity) {
					if (new CycleDetector(instantiator).hasCycleBreaks(run)) {
						String message = "A combinational task must not have cycle breaks";
						error(message, run, null, -1);
					}
				}
			});
		}
	}

}
