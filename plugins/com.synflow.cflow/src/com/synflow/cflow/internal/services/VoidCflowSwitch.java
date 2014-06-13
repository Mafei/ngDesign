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
package com.synflow.cflow.internal.services;

import static com.synflow.models.util.SwitchUtil.DONE;
import static com.synflow.models.util.SwitchUtil.visit;

import com.google.common.collect.Iterables;
import com.synflow.cflow.cflow.Block;
import com.synflow.cflow.cflow.Branch;
import com.synflow.cflow.cflow.CExpression;
import com.synflow.cflow.cflow.ExpressionBinary;
import com.synflow.cflow.cflow.ExpressionCast;
import com.synflow.cflow.cflow.ExpressionIf;
import com.synflow.cflow.cflow.ExpressionUnary;
import com.synflow.cflow.cflow.ExpressionVariable;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.Module;
import com.synflow.cflow.cflow.Network;
import com.synflow.cflow.cflow.Statement;
import com.synflow.cflow.cflow.StatementAssert;
import com.synflow.cflow.cflow.StatementAssign;
import com.synflow.cflow.cflow.StatementIf;
import com.synflow.cflow.cflow.StatementLabeled;
import com.synflow.cflow.cflow.StatementLoop;
import com.synflow.cflow.cflow.StatementPrint;
import com.synflow.cflow.cflow.StatementReturn;
import com.synflow.cflow.cflow.StatementVariable;
import com.synflow.cflow.cflow.StatementWrite;
import com.synflow.cflow.cflow.ValueExpr;
import com.synflow.cflow.cflow.ValueList;
import com.synflow.cflow.cflow.VarDecl;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.cflow.util.CflowSwitch;
import com.synflow.models.util.Void;

/**
 * This class defines a full void switch.
 * 
 * @author Matthieu Wipliez
 * 
 */
public abstract class VoidCflowSwitch extends CflowSwitch<Void> {

	@Override
	public Void caseBlock(Block block) {
		return visit(this, block.getStmts());
	}

	@Override
	public Void caseBranch(Branch branch) {
		return visit(this, branch.getCondition(), branch.getBody());
	}

	@Override
	public Void caseCExpression(CExpression expr) {
		return DONE;
	}

	@Override
	public Void caseExpressionBinary(ExpressionBinary expr) {
		return visit(this, expr.getLeft(), expr.getRight());
	}

	@Override
	public Void caseExpressionCast(ExpressionCast expr) {
		return visit(this, expr.getExpression());
	}

	@Override
	public Void caseExpressionIf(ExpressionIf expr) {
		return visit(this, expr.getCondition(), expr.getThen(), expr.getElse());
	}

	@Override
	public Void caseExpressionUnary(ExpressionUnary expr) {
		return visit(this, expr.getExpression());
	}

	@Override
	public Void caseExpressionVariable(ExpressionVariable expr) {
		return visit(this, Iterables.concat(expr.getIndexes(), expr.getParameters()));
	}

	@Override
	public Void caseInst(Inst inst) {
		return visit(this, inst.getTask());
	}

	@Override
	public Void caseModule(Module module) {
		return visit(this, module.getEntities());
	}

	@Override
	public Void caseNetwork(Network network) {
		return visit(this, network.getInstances());
	}

	@Override
	public Void caseStatement(Statement stmt) {
		return DONE;
	}

	@Override
	public Void caseStatementAssert(StatementAssert stmt) {
		return visit(this, stmt.getCondition());
	}

	@Override
	public Void caseStatementAssign(StatementAssign stmt) {
		return visit(this, stmt.getTarget(), stmt.getValue());
	}

	@Override
	public Void caseStatementIf(StatementIf stmtIf) {
		return visit(this, stmtIf.getBranches());
	}

	@Override
	public Void caseStatementLabeled(StatementLabeled stmt) {
		return visit(this, stmt.getStmt());
	}

	@Override
	public Void caseStatementLoop(StatementLoop stmt) {
		return visit(this, stmt.getInit(), stmt.getCondition(), stmt.getBody(), stmt.getAfter());
	}

	@Override
	public Void caseStatementPrint(StatementPrint stmt) {
		return visit(this, stmt.getArgs());
	}

	@Override
	public Void caseStatementReturn(StatementReturn stmt) {
		return visit(this, stmt.getValue());
	}

	@Override
	public Void caseStatementVariable(StatementVariable stmt) {
		return visit(this, stmt.getVariables());
	}

	@Override
	public Void caseStatementWrite(StatementWrite write) {
		return visit(this, write.getValue());
	}

	@Override
	public Void caseValueExpr(ValueExpr value) {
		return visit(this, value.getExpression());
	}

	@Override
	public Void caseValueList(ValueList list) {
		return visit(this, list.getValues());
	}

	@Override
	public Void caseVarDecl(VarDecl decl) {
		return visit(this, decl.getVariables());
	}

	@Override
	public Void caseVariable(Variable variable) {
		visit(this, variable.getDimensions());
		visit(this, variable.getBody());
		return visit(this, variable.getValue());
	}

}