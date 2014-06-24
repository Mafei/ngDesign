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
package com.synflow.core.transformations;

import static com.synflow.models.util.SwitchUtil.CASCADE;
import static com.synflow.models.util.SwitchUtil.DONE;

import org.eclipse.emf.common.util.EList;

import com.google.common.collect.Iterables;
import com.synflow.models.dpn.Action;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Unit;
import com.synflow.models.ir.BlockIf;
import com.synflow.models.ir.BlockWhile;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.InstAssign;
import com.synflow.models.ir.InstCall;
import com.synflow.models.ir.InstLoad;
import com.synflow.models.ir.InstReturn;
import com.synflow.models.ir.InstStore;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Var;
import com.synflow.models.ir.util.AbstractIrVisitor;
import com.synflow.models.util.Void;

/**
 * This class defines a module transformation that visits all expressions in an actor/unit.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ExpressionTransformation extends ModuleTransformation {

	/**
	 * This class defines a visitor that visits all expressions in blocks, instructions, and initial
	 * values of variables.
	 * 
	 * @author Matthieu Wipliez
	 * 
	 */
	private static class ExpressionVisitor extends AbstractIrVisitor {

		private AbstractExpressionTransformer transformer;

		public ExpressionVisitor(AbstractExpressionTransformer transformer) {
			this.transformer = transformer;
		}

		@Override
		public Void caseBlockIf(BlockIf block) {
			block.setCondition(visitExpr(block.getCondition()));

			visit(block.getThenBlocks());
			visit(block.getElseBlocks());
			return doSwitch(block.getJoinBlock());
		}

		@Override
		public Void caseBlockWhile(BlockWhile block) {
			block.setCondition(visitExpr(block.getCondition()));

			visit(block.getBlocks());
			doSwitch(block.getJoinBlock());
			return DONE;
		}

		@Override
		public Void caseInstAssign(InstAssign assign) {
			assign.setValue(visitExpr(assign.getValue()));
			return DONE;
		}

		@Override
		public Void caseInstCall(InstCall call) {
			handleIndexes(call.getArguments());
			return DONE;
		}

		@Override
		public Void caseInstLoad(InstLoad load) {
			if (!load.getIndexes().isEmpty()) {
				handleIndexes(load.getIndexes());
			}
			return DONE;
		}

		@Override
		public Void caseInstReturn(InstReturn instReturn) {
			final Expression value = instReturn.getValue();
			if (value != null) {
				instReturn.setValue(visitExpr(value));
			}
			return DONE;
		}

		@Override
		public Void caseInstStore(InstStore store) {
			if (!store.getIndexes().isEmpty()) {
				handleIndexes(store.getIndexes());
			}

			store.setValue(visitExpr(store.getValue()));
			return DONE;
		}

		@Override
		public Void caseProcedure(Procedure procedure) {
			this.procedure = procedure;
			transformer.setProcedure(procedure);
			return visit(procedure.getBlocks());
		}

		@Override
		public Void caseVar(Var variable) {
			variable.setInitialValue(visitExpr(variable.getInitialValue()));
			return DONE;
		}

		private void handleIndexes(EList<Expression> indexes) {
			int i = 0;
			while (i < indexes.size()) {
				final Expression expr = indexes.get(i);
				final Expression res = visitExpr(expr);
				if (res == expr) {
					i++;
				} else {
					indexes.set(i, res);
					i++;
				}
			}
		}

		private Expression visitExpr(Expression expression) {
			return transformer.transform(expression);
		}

	}

	public ExpressionTransformation(AbstractExpressionTransformer transformer) {
		super(new ExpressionVisitor(transformer));
	}

	@Override
	public Void caseAction(Action action) {
		doSwitch(action.getBody());
		doSwitch(action.getScheduler());
		return DONE;
	}

	@Override
	public Void caseActor(Actor actor) {
		caseEntity(actor);

		for (Action action : actor.getActions()) {
			doSwitch(action);
		}

		return DONE;
	}

	@Override
	public Void caseEntity(Entity entity) {
		for (Var var : Iterables.concat(entity.getParameters(), entity.getVariables())) {
			doSwitch(var);
		}

		for (Procedure procedure : entity.getProcedures()) {
			doSwitch(procedure);
		}

		return DONE;
	}

	@Override
	public Void caseUnit(Unit unit) {
		return CASCADE;
	}

}
