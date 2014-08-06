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
/*
 * Copyright (c) 2009-2011, IETR/INSA of Rennes
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the IETR/INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.synflow.cx.internal.compiler;

import static com.synflow.cx.internal.TransformerUtil.getStartLine;
import static com.synflow.models.ir.IrFactory.eINSTANCE;
import static java.math.BigInteger.ONE;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.Block;
import com.synflow.cx.cx.Branch;
import com.synflow.cx.cx.CExpression;
import com.synflow.cx.cx.ExpressionBinary;
import com.synflow.cx.cx.ExpressionBoolean;
import com.synflow.cx.cx.ExpressionCast;
import com.synflow.cx.cx.ExpressionFloat;
import com.synflow.cx.cx.ExpressionIf;
import com.synflow.cx.cx.ExpressionInteger;
import com.synflow.cx.cx.ExpressionString;
import com.synflow.cx.cx.ExpressionUnary;
import com.synflow.cx.cx.ExpressionVariable;
import com.synflow.cx.cx.Statement;
import com.synflow.cx.cx.StatementAssert;
import com.synflow.cx.cx.StatementAssign;
import com.synflow.cx.cx.StatementIf;
import com.synflow.cx.cx.StatementLabeled;
import com.synflow.cx.cx.StatementLoop;
import com.synflow.cx.cx.StatementPrint;
import com.synflow.cx.cx.StatementReturn;
import com.synflow.cx.cx.StatementVariable;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.cx.util.CxSwitch;
import com.synflow.cx.instantiation.IInstantiator;
import com.synflow.cx.internal.AstUtil;
import com.synflow.cx.internal.services.Typer;
import com.synflow.cx.services.Evaluator;
import com.synflow.models.dpn.Entity;
import com.synflow.models.ir.BlockIf;
import com.synflow.models.ir.BlockWhile;
import com.synflow.models.ir.ExprBinary;
import com.synflow.models.ir.ExprInt;
import com.synflow.models.ir.ExprVar;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.InstCall;
import com.synflow.models.ir.InstReturn;
import com.synflow.models.ir.IrFactory;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.OpUnary;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.Var;
import com.synflow.models.ir.util.IrUtil;

/**
 * This class transforms Cx statement into IR blocks and instructions.
 * 
 * @author Matthieu Wipliez
 * @see IrBuilder
 */
public class FunctionTransformer extends CxSwitch<EObject> implements Transformer {

	protected final IrBuilder builder;

	protected final Typer typer;

	/**
	 * Creates a new function transformer with the given entity.
	 * 
	 * @param entity
	 *            IR entity being created
	 */
	public FunctionTransformer(IInstantiator instantiator, Typer typer, Entity entity) {
		this(typer, new IrBuilder(instantiator, typer, entity));
	}

	/**
	 * Creates a new FunctionTransformer with the given IR builder, and set its expression
	 * transformer to <code>this</code>.
	 * 
	 * @param builder
	 *            IR builder
	 */
	protected FunctionTransformer(Typer typer, IrBuilder builder) {
		this.builder = builder;
		this.typer = typer;
		builder.setTransformer(this);
	}

	@Override
	public EObject caseBlock(Block block) {
		for (Statement statement : block.getStmts()) {
			doSwitch(statement);
		}

		return null;
	}

	@Override
	public Expression caseExpressionBinary(ExpressionBinary expression) {
		OpBinary op = OpBinary.getOperator(expression.getOperator());
		Expression e1 = transformExpr(expression.getLeft());
		Expression e2;

		if (op == OpBinary.DIV || op == OpBinary.MOD) {
			Object value = Evaluator.getValue(expression.getRight());
			BigInteger expr = (BigInteger) value;
			BigInteger n;

			if (op == OpBinary.DIV) {
				// div n <=> right shift by log2(n)
				// when n is constant and power of two
				op = OpBinary.SHIFT_RIGHT;
				n = BigInteger.valueOf(expr.bitLength() - 1);
			} else {
				// mod n <=> & (n - 1) when n is constant power of two
				op = OpBinary.BITAND;
				n = expr.subtract(ONE);
			}
			e2 = IrFactory.eINSTANCE.createExprInt(n);
		} else {
			e2 = transformExpr(expression.getRight());
		}

		return eINSTANCE.createExprBinary(e1, op, e2);
	}

	@Override
	public Expression caseExpressionBoolean(ExpressionBoolean expression) {
		return eINSTANCE.createExprBool(expression.isValue());
	}

	@Override
	public Expression caseExpressionCast(ExpressionCast expression) {
		Type targetType = typer.getType(expression.getType());
		Type sourceType = typer.getType(expression.getExpression());
		Expression expr = transformExpr(expression.getExpression());

		return eINSTANCE.cast(targetType, sourceType, expr);
	}

	@Override
	public Expression caseExpressionFloat(ExpressionFloat expression) {
		return eINSTANCE.createExprFloat(expression.getValue());
	}

	@Override
	public Expression caseExpressionIf(ExpressionIf expression) {
		builder.saveBlocks();

		// create block
		BlockIf block = eINSTANCE.createBlockIf();
		block.setJoinBlock(eINSTANCE.createBlockBasic());
		int lineNumber = getStartLine(expression);
		block.setLineNumber(lineNumber);

		// translate condition
		Expression condition = transformExpr(expression.getCondition());
		block.setCondition(condition);

		// add block (must be done after condition has been transformed)
		builder.add(block);

		// update target if necessary
		Var target = builder.createVar(lineNumber, typer.getType(expression), "tmp_if");

		// transforms "then" and "else" expressions
		builder.setBlocks(block.getThenBlocks());
		builder.storeExpr(lineNumber, target, null, expression.getThen());

		builder.setBlocks(block.getElseBlocks());
		builder.storeExpr(lineNumber, target, null, expression.getElse());
		builder.restoreBlocks();

		// return expr
		return eINSTANCE.createExprVar(target);
	}

	@Override
	public Expression caseExpressionInteger(ExpressionInteger expression) {
		return eINSTANCE.createExprInt(expression.getValue());
	}

	@Override
	public Expression caseExpressionString(ExpressionString expression) {
		return eINSTANCE.createExprString(expression.getValue());
	}

	@Override
	public Expression caseExpressionUnary(ExpressionUnary expression) {
		CExpression subExpr = expression.getExpression();

		Expression value;
		OpUnary op = OpUnary.getOperator(expression.getUnaryOperator());
		switch (op) {
		case MINUS:
			// replace ExprUnary(-, n) by ExprInt(-n)
			if (subExpr instanceof ExpressionInteger) {
				ExpressionInteger exprInt = (ExpressionInteger) subExpr;
				value = eINSTANCE.createExprInt(exprInt.getValue().negate());
				break;
			}
			// fall-through for other expressions
		default: {
			Expression expr = transformExpr(expression.getExpression());
			value = eINSTANCE.createExprUnary(op, expr);
			break;
		}
		}

		return value;
	}

	@Override
	public Expression caseExpressionVariable(ExpressionVariable expression) {
		Variable variable = expression.getSource().getVariable();
		if (CxUtil.isFunction(variable)) {
			return translateCall(expression);
		}

		Var source = builder.getMapping(variable);
		Type type = source.getType();
		int dimensions = Typer.getNumDimensions(type);

		// loads variable (do not perform bit selection though)
		int lineNumber = getStartLine(expression);
		Var target = builder.loadVariable(lineNumber, source, expression.getIndexes());

		// bit selection
		ExprVar exprVar = eINSTANCE.createExprVar(target);
		if (dimensions < expression.getIndexes().size()) {
			CExpression exprIndex = expression.getIndexes().get(dimensions);
			int index = Evaluator.getIntValue(exprIndex);
			ExprInt mask = eINSTANCE.createExprInt(ONE.shiftLeft(index));
			ExprBinary exprBin = eINSTANCE.createExprBinary(exprVar, OpBinary.BITAND, mask);

			ExprInt shift = eINSTANCE.createExprInt(0);
			return eINSTANCE.createExprBinary(exprBin, OpBinary.NE, shift);
		} else {
			return exprVar;
		}
	}

	@Override
	public EObject caseStatementAssert(StatementAssert stmtAssert) {
		hookBefore(stmtAssert);

		int lineNumber = getStartLine(stmtAssert);
		Expression condition = transformExpr(stmtAssert.getCondition());

		InstCall call = eINSTANCE.createInstCall(lineNumber, null, null, Arrays.asList(condition));
		call.setAssert(true);
		builder.add(call);

		return null;
	}

	@Override
	public EObject caseStatementAssign(StatementAssign assign) {
		hookBefore(assign);

		if (assign.getOp() == null) {
			// no target
			transformExpr(assign.getValue());
		} else {
			// get target
			Variable variable = assign.getTarget().getSource().getVariable();
			Var target = builder.getMapping(variable);

			// transform value
			int lineNumber = getStartLine(assign);
			CExpression value = AstUtil.getAssignValue(assign);
			builder.storeExpr(lineNumber, target, assign.getTarget().getIndexes(), value);
		}

		return null;
	}

	@Override
	public EObject caseStatementIf(StatementIf stmtIf) {
		hookBefore(stmtIf);

		builder.saveBlocks();

		// transforms all branches (including 'else' branch)
		for (Branch stmt : stmtIf.getBranches()) {
			CExpression condition = stmt.getCondition();
			if (condition == null) {
				// 'else' branch
				doSwitch(stmt.getBody());
			} else {
				// create If block
				BlockIf node = eINSTANCE.createBlockIf();
				node.setJoinBlock(eINSTANCE.createBlockBasic());
				node.setLineNumber(getStartLine(stmtIf));
				node.setCondition(transformExpr(condition));

				// add If to blocks
				builder.add(node);

				// transforms body in the "then" blocks
				builder.setBlocks(node.getThenBlocks());
				doSwitch(stmt.getBody());

				// next branch/else will be appended to the "else" blocks
				builder.setBlocks(node.getElseBlocks());
			}
		}

		builder.restoreBlocks();

		return null;
	}

	@Override
	public EObject caseStatementLabeled(StatementLabeled stmt) {
		return doSwitch(stmt.getStmt());
	}

	@Override
	public EObject caseStatementLoop(StatementLoop stmtFor) {
		hookBefore(stmtFor);

		// translate init
		doSwitch(stmtFor.getInit());

		builder.saveBlocks();

		// to track the instructions created when condition was transformed
		List<com.synflow.models.ir.Block> initNodes = new ArrayList<>();
		builder.setBlocks(initNodes);

		// create the while
		BlockWhile nodeWhile = eINSTANCE.createBlockWhile();
		nodeWhile.setJoinBlock(eINSTANCE.createBlockBasic());
		int lineNumber = getStartLine(stmtFor);
		nodeWhile.setLineNumber(lineNumber);

		// transform condition
		Expression condition = transformExpr(stmtFor.getCondition());
		nodeWhile.setCondition(condition);

		// transform body and after
		builder.setBlocks(nodeWhile.getBlocks());
		doSwitch(stmtFor.getBody());
		doSwitch(stmtFor.getAfter());

		// copy instructions
		nodeWhile.getBlocks().addAll(IrUtil.copy(initNodes));

		builder.restoreBlocks();

		// add init nodes and while
		builder.addAll(initNodes);
		builder.add(nodeWhile);

		return null;
	}

	@Override
	public EObject caseStatementPrint(StatementPrint print) {
		hookBefore(print);

		int lineNumber = getStartLine(print);
		InstCall call = eINSTANCE.createInstCall(lineNumber, null, null,
				builder.transformExpressions(print.getArgs()));
		call.setPrint(true);
		builder.add(call);

		return null;
	}

	@Override
	public EObject caseStatementReturn(StatementReturn stmtReturn) {
		hookBefore(stmtReturn);

		InstReturn instReturn = eINSTANCE.createInstReturn(transformExpr(stmtReturn.getValue()));
		builder.add(instReturn);
		return instReturn;
	}

	@Override
	public EObject caseStatementVariable(StatementVariable stmtVar) {
		for (Variable variable : stmtVar.getVariables()) {
			doSwitch(variable);
		}

		return null;
	}

	@Override
	public EObject caseVariable(Variable variable) {
		if (CxUtil.isFunction(variable)) {
			// set current procedure
			builder.setProcedure(variable);

			// transform parameters
			for (Variable parameter : variable.getParameters()) {
				builder.addParameter(parameter);
			}

			// transform body
			doSwitch(variable.getBody());

			return builder.getProcedure();
		}

		hookBefore(variable);

		// creates local variable and adds it to this procedure
		Var var = builder.addLocal(variable);

		// assign a value (if any) to the variable
		CExpression value = (CExpression) variable.getValue();
		if (value != null) {
			builder.storeExpr(var.getLineNumber(), var, null, value);
		}

		return null;
	}

	@Override
	public EObject doSwitch(EObject eObject) {
		if (eObject == null) {
			return null;
		}
		return doSwitch(eObject.eClass(), eObject);
	}

	/**
	 * Hook called at the beginning of a statement/variable.
	 */
	protected void hookBefore(EObject eObject) {
		// only used by ActionTransformer
	}

	@Override
	public final Expression transformExpr(CExpression expression) {
		return (Expression) doSwitch(expression);
	}

	/**
	 * Translates a call represented by the given expression to an IR InstCall.
	 * 
	 * @param expression
	 *            an expression variable referencing a function
	 * @return an ExprVar
	 */
	private Expression translateCall(ExpressionVariable expression) {
		Variable variable = expression.getSource().getVariable();

		// retrieve IR procedure
		Procedure proc = builder.getProcedure(variable);

		// transform parameters
		List<Expression> parameters = builder.transformExpressions(expression.getParameters());

		// add call
		int lineNumber = getStartLine(expression);
		Var target = builder.createVar(lineNumber, proc.getReturnType(),
				"call_" + variable.getName());
		InstCall call = eINSTANCE.createInstCall(lineNumber, target, proc, parameters);
		builder.add(call);

		// return expr
		return eINSTANCE.createExprVar(target);
	}

}
