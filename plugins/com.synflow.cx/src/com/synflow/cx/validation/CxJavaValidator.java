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
import static org.eclipse.xtext.validation.CheckType.NORMAL;

import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.util.Triple;
import org.eclipse.xtext.util.Tuples;
import org.eclipse.xtext.validation.Check;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.synflow.core.SynflowCore;
import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.CxEntity;
import com.synflow.cx.cx.CxPackage.Literals;
import com.synflow.cx.cx.Instantiable;
import com.synflow.cx.cx.Module;
import com.synflow.cx.cx.Network;
import com.synflow.cx.cx.Task;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.instantiation.IInstantiator;
import com.synflow.cx.internal.ErrorMarker;
import com.synflow.cx.internal.scheduler.CycleDetector;
import com.synflow.cx.internal.services.Typer;
import com.synflow.cx.internal.validation.NetworkChecker;
import com.synflow.cx.internal.validation.TypeChecker;
import com.synflow.models.dpn.Actor;
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

	/**
	 * stores already-registered issues (copy/paste from Xtext)
	 */
	private Set<Triple<EObject, EStructuralFeature, String>> accepted;

	@Inject
	private IInstantiator instantiator;

	@Inject
	private Typer typer;

	@Override
	public void acceptError(String message, EObject object, EStructuralFeature feature, int index,
			String code, String... issueData) {
		if (accepted.add(Tuples.create(object, feature, message))) {
			super.acceptError(message, object, feature, index, code, issueData);
		}
	}

	@Override
	public void acceptInfo(String message, EObject object, EStructuralFeature feature, int index,
			String code, String... issueData) {
		if (accepted.add(Tuples.create(object, feature, message))) {
			super.acceptInfo(message, object, feature, index, code, issueData);
		}
	}

	@Override
	public void acceptWarning(String message, EObject object, EStructuralFeature feature,
			int index, String code, String... issueData) {
		if (accepted.add(Tuples.create(object, feature, message))) {
			super.acceptWarning(message, object, feature, index, code, issueData);
		}
	}

	@Check(NORMAL)
	public void checkModule(Module module) {
		accepted = Sets.newHashSet();

		// updates the instantiator to reflect changes in this module
		// this method only performs an actual update if the instantiator is out of date
		try {
			instantiator.update(module);
		} catch (Exception e) {
			// log exceptions
			SynflowCore.log(e);
		}

		// for each entity of the module
		final NetworkChecker networkChecker = new NetworkChecker(this, instantiator);
		for (final CxEntity cxEntity : module.getEntities()) {
			instantiator.forEachMapping(cxEntity, new Executable<Entity>() {
				@Override
				public void exec(Entity entity) {
					if (entity instanceof DPN) {
						// check connectivity
						Network network = (Network) cxEntity;
						DPN dpn = (DPN) entity;
						networkChecker.checkDPN(network, dpn);
					} else if (entity instanceof Actor) {
						checkTask((Task) cxEntity, (Actor) entity);
					}

					// check types
					new TypeChecker(CxJavaValidator.this, instantiator, typer, entity)
							.doSwitch(cxEntity);

					if (cxEntity instanceof Instantiable) {
						printErrors((Instantiable) cxEntity);
					}
				}
			});
		}
	}

	private void checkTask(Task task, Actor actor) {
		Variable loop = CxUtil.getFunction(task, NAME_LOOP);
		if (loop == null) {
			loop = CxUtil.getFunction(task, NAME_LOOP_DEPRECATED);
		}

		Variable setup = CxUtil.getFunction(task, NAME_SETUP);
		if (setup == null) {
			setup = CxUtil.getFunction(task, NAME_SETUP_DEPRECATED);
		}

		if (actor.getProperties().getAsJsonArray(PROP_CLOCKS).size() == 0) {
			validateCombinational(task, setup, loop);
		}
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
	private void validateCombinational(Task task, Variable setup, final Variable loop) {
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

		if (loop != null) {
			instantiator.forEachMapping(task, new Executable<Entity>() {
				@Override
				public void exec(Entity entity) {
					if (new CycleDetector(instantiator, (Actor) entity).hasCycleBreaks(loop)) {
						String message = "A combinational task must not have cycle breaks";
						error(message, loop, null, -1);
					}
				}
			});
		}
	}

}
