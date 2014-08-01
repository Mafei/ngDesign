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
package com.synflow.cflow.internal.compiler;

import static com.synflow.cflow.CflowConstants.PROP_AVAILABLE;
import static com.synflow.cflow.internal.TransformerUtil.getStartLine;
import static com.synflow.models.ir.IrFactory.eINSTANCE;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.CExpression;
import com.synflow.cflow.cflow.Enter;
import com.synflow.cflow.cflow.ExpressionVariable;
import com.synflow.cflow.cflow.Leave;
import com.synflow.cflow.cflow.StatementWrite;
import com.synflow.cflow.cflow.VarRef;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.internal.TransformerUtil;
import com.synflow.cflow.internal.instantiation.IInstantiator;
import com.synflow.cflow.internal.services.Typer;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.FSM;
import com.synflow.models.dpn.Transition;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.InstStore;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.Var;

/**
 * This class transforms AST statements into IR instructions and/or nodes at the actor level.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ActorTransformer extends FunctionTransformer {

	private Deque<Integer> lines;

	/**
	 * Creates a new actor transformer with the given actor.
	 * 
	 * @param actor
	 *            actor
	 */
	public ActorTransformer(IInstantiator instantiator, Typer typer, Actor actor) {
		super(typer, new ActorBuilder(instantiator, typer, actor));
		lines = new ArrayDeque<>();
	}

	@Override
	public EObject caseEnter(Enter enter) {
		Variable function = enter.getFunction().getVariable();
		int lineNumber = enter.getLineNumber();
		getBuilder().updateLineInfo(lineNumber);

		// all transitions will now correspond to this line
		if (getBuilder().line == null) {
			getBuilder().line = lineNumber;
		} else {
			lines.addFirst(getBuilder().line);
		}

		// transform arguments
		List<CExpression> arguments = enter.getParameters();
		Iterator<CExpression> it = arguments.iterator();
		for (Variable variable : function.getParameters()) {
			Type type = typer.getType(variable);
			Var var = builder.createVar(lineNumber, type, variable.getName());
			builder.put(variable, var);

			CExpression value = it.next();
			builder.storeExpr(getBuilder().line, var, null, value);
		}

		// no need to include void function
		// this has been done by the cycle scheduler
		return null;
	}

	@Override
	public Expression caseExpressionVariable(ExpressionVariable expression) {
		VarRef ref = expression.getSource();
		Variable variable = ref.getVariable();
		if (CflowUtil.isPort(variable)) {
			String prop = expression.getProperty();
			if (PROP_AVAILABLE.equals(prop)) {
				// available has no meaning as an expression, so we return null
				// this is removed later by AvailableRemover (see setCondition)
				return null;
			}

			int lineNumber = getStartLine(expression);
			return getBuilder().translateRead(lineNumber, ref);
		} else {
			return super.caseExpressionVariable(expression);
		}
	}

	@Override
	public Expression caseLeave(Leave leave) {
		// restore previous line behavior
		getBuilder().line = lines.pollFirst();
		return null;
	}

	@Override
	public Expression caseStatementWrite(StatementWrite write) {
		int lineNumber = getStartLine(write);
		getBuilder().updateLineInfo(lineNumber);

		Var var = getBuilder().getOutput(lineNumber, write.getPort());
		if (var == null) {
			// when the write is translated as part of the scheduler
			// we must ignore it
			return null;
		}

		Expression expr = builder.transformExpr(write.getValue(), var.getType());
		InstStore store = eINSTANCE.createInstStore(lineNumber, var, expr);
		builder.add(store);

		return null;
	}

	private ActorBuilder getBuilder() {
		return (ActorBuilder) builder;
	}

	@Override
	protected void hookBefore(EObject eObject) {
		int lineNumber = TransformerUtil.getStartLine(eObject);
		getBuilder().updateLineInfo(lineNumber);
	}

	public void visit() {
		ActorBuilder builder = getBuilder();
		FSM fsm = builder.getActor().getFsm();
		for (Transition transition : fsm.getTransitions()) {
			builder.visitTransition(transition);
		}
	}

}
