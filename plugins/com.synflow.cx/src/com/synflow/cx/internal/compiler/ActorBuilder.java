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
package com.synflow.cx.internal.compiler;

import static com.synflow.models.ir.IrFactory.eINSTANCE;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.synflow.cx.cx.VarRef;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.instantiation.IInstantiator;
import com.synflow.cx.internal.compiler.helpers.AvailableRemover;
import com.synflow.cx.internal.services.Typer;
import com.synflow.models.dpn.Action;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.Pattern;
import com.synflow.models.dpn.Port;
import com.synflow.models.dpn.Transition;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.InstLoad;
import com.synflow.models.ir.IrFactory;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.Var;

/**
 * This class defines a dataflow builder that inherits from the IR builder.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ActorBuilder extends IrBuilder {

	private ImmutableSet<String> existingSet;

	protected Integer line;

	private Pattern readPattern;

	private Transition transition;

	private Pattern writePattern;

	public ActorBuilder(IInstantiator instantiator, Typer typer, Actor actor) {
		super(instantiator, typer, actor);

		existingSet = ImmutableSet.copyOf(Iterables.transform(
				Iterables.concat(actor.getInputs(), actor.getOutputs(), actor.getVariables()),
				new Function<Var, String>() {
					@Override
					public String apply(Var var) {
						return var.getName();
					}
				}));
	}

	/**
	 * Creates the body of the action associated with the given transition info. This method simply
	 * uses the transformer to visit the objects associated with the info's body.
	 * 
	 * @param info
	 *            transition info
	 */
	private void createBody() {
		// use the 'body' procedure, and set up patterns
		Action action = transition.getAction();
		setProcedure(action.getBody());
		readPattern = action.getInputPattern();
		writePattern = action.getOutputPattern();

		for (EObject eObject : transition.getBody()) {
			transformer.doSwitch(eObject);
		}
	}

	/**
	 * Creates the scheduler of the action associated with the given transition info.
	 * 
	 * @param info
	 *            transition info
	 */
	private void createScheduler() {
		// use the 'scheduler' procedure, and set up patterns
		Action action = transition.getAction();
		setProcedure(action.getScheduler());
		readPattern = action.getPeekPattern();
		writePattern = null;

		// translate statements and condition
		Expression expr = null;
		List<EObject> eObjects = transition.getScheduler();
		for (EObject eObject : eObjects) {
			// translate object
			EObject irObject = transformer.doSwitch(eObject);
			if (irObject instanceof Expression) {
				expr = translateCondition(expr, (Expression) irObject);
			}
		}

		// adds a return if the expression is not null
		if (expr == null) {
			expr = IrFactory.eINSTANCE.createExprBool(true);
		}
		add(eINSTANCE.createInstReturn(expr));
	}

	/**
	 * This overrides IrBuilder's implementation by also looking into an existing set of names
	 * (including ports, state variables).
	 */
	@Override
	public final Var createVar(int lineNumber, Type type, String hint) {
		String name = hint;
		boolean existing = existingSet.contains(name);
		int i = 0;
		while (existing) {
			name = hint + i;
			existing = existingSet.contains(name);
			i++;
		}

		return super.createVar(lineNumber, type, name);
	}

	final Actor getActor() {
		return (Actor) entity;
	}

	public Var getOutput(VarRef ref) {
		if (writePattern == null) {
			return null;
		}
		return getPatternVar(writePattern, ref);
	}

	/**
	 * Returns the variable that holds the contents of the given port (read or written). The
	 * variable is created if it does not already exist.
	 * 
	 * @param pattern
	 *            IR pattern
	 * @param variable
	 *            port declaration
	 * @return an IR Var
	 */
	final Var getPatternVar(Pattern pattern, VarRef ref) {
		Port port = instantiator.getPort(entity, ref);
		if (!pattern.contains(port)) {
			pattern.add(port);
		}
		return port;
	}

	private Expression translateCondition(Expression expr, Expression condition) {
		// remove calls to available
		condition = new AvailableRemover().visit(condition);
		if (condition == null) {
			return expr;
		}

		// assign to new 'cond' variable
		Var condVar = createVar(0, eINSTANCE.createTypeBool(), "cond");
		add(eINSTANCE.createInstAssign(condVar, condition));
		Expression cond = eINSTANCE.createExprVar(condVar);

		if (expr == null) {
			return cond;
		} else {
			return eINSTANCE.createExprBinary(expr, OpBinary.LOGIC_AND, cond);
		}
	}

	/**
	 * Translates the given read statement, by loading a token from the given pattern (input or
	 * peek).
	 * 
	 * @param readPattern
	 *            a pattern (input or peek)
	 * @param read
	 *            a read statement
	 */
	final Expression translateRead(int lineNumber, VarRef ref) {
		Var var = getPatternVar(readPattern, ref);

		// add load
		Variable variable = ref.getVariable();
		Type type = typer.getType(entity, variable);
		Var target = createVar(lineNumber, type, "local_" + variable.getName());
		InstLoad load = eINSTANCE.createInstLoad(lineNumber, target, var);
		add(load);

		return eINSTANCE.createExprVar(target);
	}

	final void updateLineInfo(int lineNumber) {
		if (line == null) {
			transition.getLines().add(lineNumber);
		} else {
			transition.getLines().add(line);
		}
	}

	/**
	 * Visits the given transition and creates the IR of the action associated with it.
	 * 
	 * @param transition
	 *            a transition
	 */
	public void visitTransition(Transition transition) {
		this.transition = transition;

		createScheduler();
		createBody();

		transition.getBody().clear();
		transition.getScheduler().clear();
	}

}
