/*******************************************************************************
 * Copyright (c) 2012-2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cflow.internal.services;

import static com.synflow.models.util.SwitchUtil.check;

import com.google.common.collect.Iterables;
import com.synflow.cflow.cflow.Block;
import com.synflow.cflow.cflow.Branch;
import com.synflow.cflow.cflow.CExpression;
import com.synflow.cflow.cflow.ExpressionBinary;
import com.synflow.cflow.cflow.ExpressionIf;
import com.synflow.cflow.cflow.ExpressionUnary;
import com.synflow.cflow.cflow.ExpressionVariable;
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
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.cflow.util.CflowSwitch;

/**
 * This class defines a full boolean switch.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class BoolCflowSwitch extends CflowSwitch<Boolean> {

	@Override
	public Boolean caseBlock(Block block) {
		return check(this, block.getStmts());
	}

	@Override
	public Boolean caseBranch(Branch branch) {
		return check(this, branch.getCondition(), branch.getBody());
	}

	@Override
	public Boolean caseCExpression(CExpression expr) {
		return false;
	}

	@Override
	public Boolean caseExpressionBinary(ExpressionBinary expr) {
		return check(this, expr.getLeft(), expr.getRight());
	}

	@Override
	public Boolean caseExpressionIf(ExpressionIf expr) {
		return check(this, expr.getCondition(), expr.getThen(), expr.getElse());
	}

	@Override
	public Boolean caseExpressionUnary(ExpressionUnary expr) {
		return doSwitch(expr.getExpression());
	}

	@Override
	public Boolean caseExpressionVariable(ExpressionVariable expr) {
		return check(this, Iterables.concat(expr.getIndexes(), expr.getParameters()));
	}

	@Override
	public Boolean caseStatement(Statement stmt) {
		return false;
	}

	@Override
	public Boolean caseStatementAssert(StatementAssert stmt) {
		return doSwitch(stmt.getCondition());
	}

	@Override
	public Boolean caseStatementAssign(StatementAssign stmt) {
		return check(this, stmt.getTarget(), stmt.getValue());
	}

	@Override
	public Boolean caseStatementIf(StatementIf stmtIf) {
		return check(this, stmtIf.getBranches());
	}

	@Override
	public Boolean caseStatementLabeled(StatementLabeled stmt) {
		return check(this, stmt.getStmt());
	}

	@Override
	public Boolean caseStatementLoop(StatementLoop stmt) {
		return check(this, stmt.getInit(), stmt.getCondition(), stmt.getBody(), stmt.getAfter());
	}

	@Override
	public Boolean caseStatementPrint(StatementPrint print) {
		return check(this, print.getArgs());
	}

	@Override
	public Boolean caseStatementReturn(StatementReturn stmt) {
		return check(this, stmt.getValue());
	}

	@Override
	public Boolean caseStatementVariable(StatementVariable stmt) {
		return check(this, stmt.getVariables());
	}

	@Override
	public Boolean caseStatementWrite(StatementWrite write) {
		return doSwitch(write.getValue());
	}

	@Override
	public Boolean caseValueExpr(ValueExpr value) {
		return doSwitch(value.getExpression());
	}

	@Override
	public Boolean caseValueList(ValueList list) {
		return check(this, list.getValues());
	}

	@Override
	public Boolean caseVariable(Variable variable) {
		if (check(this, Iterables.concat(variable.getDimensions(), variable.getParameters()))) {
			return true;
		}
		return check(this, variable.getBody(), variable.getValue());
	}

}