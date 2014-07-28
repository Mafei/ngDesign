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
package com.synflow.cflow.internal.compiler;

import static com.synflow.cflow.CflowConstants.NAME_LOOP;
import static com.synflow.cflow.CflowConstants.NAME_LOOP_DEPRECATED;
import static com.synflow.cflow.CflowConstants.NAME_SETUP;
import static com.synflow.cflow.CflowConstants.NAME_SETUP_DEPRECATED;
import static com.synflow.models.util.SwitchUtil.DONE;
import static com.synflow.models.util.SwitchUtil.visit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.IFileSystemAccess;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.Bundle;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.Module;
import com.synflow.cflow.cflow.Network;
import com.synflow.cflow.cflow.Task;
import com.synflow.cflow.cflow.VarDecl;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.cflow.util.CflowSwitch;
import com.synflow.cflow.internal.compiler.helpers.FsmBeautifier;
import com.synflow.cflow.internal.compiler.helpers.LoadStoreReplacer;
import com.synflow.cflow.internal.compiler.helpers.SideEffectRemover;
import com.synflow.cflow.internal.compiler.helpers.VariablePromoter;
import com.synflow.cflow.internal.instantiation.IMapper;
import com.synflow.cflow.internal.instantiation.v2.IInstantiator;
import com.synflow.cflow.internal.scheduler.CycleScheduler;
import com.synflow.cflow.internal.scheduler.IfScheduler;
import com.synflow.cflow.internal.services.Typer;
import com.synflow.core.ISynflowConstants;
import com.synflow.core.transformations.ProcedureTransformation;
import com.synflow.core.transformations.SchedulerTransformation;
import com.synflow.core.transformations.impl.StoreOnceTransformation;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Unit;
import com.synflow.models.ir.util.IrUtil;
import com.synflow.models.util.Void;

/**
 * This class transforms a C~ module to IR entities (actors, networks, units).
 * 
 * @author Matthieu Wipliez
 * 
 */
@Singleton
public class ModuleCompilerImpl extends CflowSwitch<Void> implements IModuleCompiler {

	private IFileSystemAccess fsa;

	@Inject
	private IInstantiator instantiator;

	@Inject
	private IMapper mapper;

	@Inject
	private Typer typer;

	@Override
	public Void caseBundle(Bundle bundle) {
		for (Entity entity : instantiator.getEntities(bundle)) {
			Entity oldEntity = instantiator.setEntity(entity);
			transformBundle(bundle, (Unit) entity);
			instantiator.setEntity(oldEntity);
			serialize(entity);
		}
		return DONE;
	}

	@Override
	public Void caseInst(Inst inst) {
		return visit(this, inst.getTask());
	}

	@Override
	public Void caseModule(Module module) {
		// translate comments for this module
		new CommentTranslator(instantiator).doSwitch(module);

		return visit(this, module.getEntities());
	}

	@Override
	public Void caseNetwork(Network network) {
		for (Entity entity : instantiator.getEntities(network)) {
			Entity oldEntity = instantiator.setEntity(entity);
			DPN dpn = (DPN) entity;
			for (Inst inst : network.getInstances()) {
				doSwitch(inst);
			}

			instantiator.setEntity(oldEntity);
			serialize(dpn);
		}
		return DONE;
	}

	@Override
	public Void caseTask(Task task) {
		for (Entity entity : instantiator.getEntities(task)) {
			Entity oldEntity = instantiator.setEntity(entity);
			transformTask(task, (Actor) entity);
			instantiator.setEntity(oldEntity);
			serialize(entity);
		}
		return DONE;
	}

	@Override
	public void serialize(Entity entity) {
		// create resource
		String name = entity.getName();

		// serializes to byte array (never throws exception)
		OutputStream os = new ByteArrayOutputStream();
		try {
			entity.eResource().save(os, null);
		} catch (IOException e) {
			// byte array output stream never throws exception
		}

		// writes IR
		String file = IrUtil.getFile(name) + "." + ISynflowConstants.FILE_EXT_IR;
		fsa.generateFile(file, os.toString());
	}

	@Override
	public void serializeBuiltins() {
		for (Resource resource : mapper.getBuiltins()) {
			EObject contents = resource.getContents().get(0);
			if (contents instanceof Entity) {
				serialize((Entity) contents);
			}
		}
	}

	@Override
	public void setFileSystemAccess(IFileSystemAccess fsa) {
		this.fsa = fsa;
	}

	/**
	 * Transforms the given bundle into a unit.
	 * 
	 * @param bundle
	 *            Cx bundle
	 * @param unit
	 *            IR unit
	 */
	private void transformBundle(Bundle bundle, Unit unit) {
		transformDeclarations(unit, bundle.getDecls());
		new ProcedureTransformation(new LoadStoreReplacer()).doSwitch(unit);
	}

	/**
	 * Transforms the given declarations (variables, procedures) to IR variables and procedures.
	 * 
	 * @param procedures
	 *            a list of IR procedures that will be created
	 * @param module
	 *            a list of declarations
	 */
	private void transformDeclarations(Entity entity, List<VarDecl> variables) {
		for (Variable variable : CflowUtil.getStateVars(variables)) {
			if (CflowUtil.isFunction(variable)) {
				if (CflowUtil.isConstant(variable)) {
					// visit constant functions
					FunctionTransformer transformer = new FunctionTransformer(mapper, typer, entity);
					transformer.doSwitch(variable);
				}
			} else {
				// visit variables (they are automatically added to the entity by mapper)
				mapper.getVar(variable);
			}
		}
	}

	/**
	 * Transforms the given task to an actor. Runs schedulers, transforms actor, beautifies FSM,
	 * runs several transformations on the code.
	 * 
	 * @param task
	 *            Cx task
	 * @param actor
	 *            IR actor
	 */
	private void transformTask(Task task, Actor actor) {
		transformDeclarations(actor, task.getDecls());

		// finds init and run functions
		Variable setup = null;
		Variable loop = null;
		for (Variable function : CflowUtil.getFunctions(task.getDecls())) {
			String name = function.getName();
			if (NAME_SETUP.equals(name) || NAME_SETUP_DEPRECATED.equals(name)) {
				setup = function;
			} else if (NAME_LOOP.equals(name) || NAME_LOOP_DEPRECATED.equals(name)) {
				loop = function;
			}
		}

		// schedules cycles, if statements, and transforms actor
		CycleScheduler scheduler = new CycleScheduler(mapper, actor);
		scheduler.schedule(setup, loop);
		new IfScheduler(mapper, actor).visit();
		new ActorTransformer(mapper, typer, actor).visit();

		// post-process FSM: rename states and actions
		new FsmBeautifier().visit(actor);

		// promotes local variables used over more than one cycle to state variables
		// and replaces load/stores of local variables by use/assigns
		new VariablePromoter(actor.getVariables()).visit(actor);
		new ProcedureTransformation(new LoadStoreReplacer()).doSwitch(actor);

		// apply store once transformation to scheduler and removes side effects
		new SchedulerTransformation(new StoreOnceTransformation()).doSwitch(actor);
		new SchedulerTransformation(new SideEffectRemover()).doSwitch(actor);
	}

}
