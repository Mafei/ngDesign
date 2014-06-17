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
package com.synflow.core.transformations;

import static com.synflow.models.util.SwitchUtil.DONE;

import org.eclipse.emf.common.util.EList;

import com.synflow.models.ir.BlockIf;
import com.synflow.models.ir.BlockWhile;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.InstAssign;
import com.synflow.models.ir.InstCall;
import com.synflow.models.ir.InstLoad;
import com.synflow.models.ir.InstReturn;
import com.synflow.models.ir.InstStore;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.util.AbstractIrVisitor;
import com.synflow.models.util.Void;

/**
 * This class defines a visitor that extracts expressions from expressions.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ExprExtractor extends AbstractIrVisitor {

	private AbstractExpressionTransformer transformer;

	public ExprExtractor(AbstractExpressionTransformer transformer) {
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