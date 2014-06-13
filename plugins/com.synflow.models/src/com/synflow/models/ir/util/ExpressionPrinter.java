/*
 * Copyright (c) 2009-2011, IETR/INSA of Rennes
 * Copyright (c) 2012, Synflow
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
package com.synflow.models.ir.util;

import static com.synflow.models.util.SwitchUtil.DONE;

import java.util.Iterator;

import com.synflow.models.ir.ExprBinary;
import com.synflow.models.ir.ExprBool;
import com.synflow.models.ir.ExprCast;
import com.synflow.models.ir.ExprFloat;
import com.synflow.models.ir.ExprInt;
import com.synflow.models.ir.ExprList;
import com.synflow.models.ir.ExprString;
import com.synflow.models.ir.ExprTernary;
import com.synflow.models.ir.ExprUnary;
import com.synflow.models.ir.ExprVar;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.util.Void;

/**
 * This class defines the default expression printer.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ExpressionPrinter extends IrSwitch<Void> {

	protected int branch;

	private StringBuilder builder;

	protected int precedence;

	private int radix;

	/**
	 * Creates a new expression printer.
	 */
	public ExpressionPrinter() {
		this(new StringBuilder());
	}

	/**
	 * Creates a new expression printer.
	 */
	public ExpressionPrinter(StringBuilder builder) {
		branch = 0; // left
		precedence = Integer.MAX_VALUE;
		radix = 10;
		this.builder = builder;
	}

	@Override
	public Void caseExprBinary(ExprBinary expr) {
		OpBinary op = expr.getOp();
		boolean needsParen = op.needsParentheses(precedence, branch);
		if (needsParen) {
			builder.append('(');
		}

		doSwitch(expr.getE1(), op.getPrecedence(), 0);
		builder.append(' ');
		builder.append(op.getText());
		builder.append(' ');
		doSwitch(expr.getE2(), op.getPrecedence(), 1);

		if (needsParen) {
			builder.append(')');
		}

		return DONE;
	}

	@Override
	public Void caseExprCast(ExprCast expr) {
		builder.append('(');
		if (expr.isToSigned()) {
			builder.append('i');
		} else if (expr.isToUnsigned()) {
			builder.append('u');
		}
		builder.append(expr.getCastedSize());
		builder.append(") ");
		doSwitch(expr.getExpr());
		return DONE;
	}

	@Override
	public Void caseExprTernary(ExprTernary expr) {
		doSwitch(expr.getE1());
		builder.append(" ? ");
		doSwitch(expr.getE2());
		builder.append(" : ");
		doSwitch(expr.getE3());
		return DONE;
	}

	@Override
	public Void caseExprBool(ExprBool expr) {
		builder.append(expr.isValue());
		return DONE;
	}

	@Override
	public Void caseExprFloat(ExprFloat expr) {
		builder.append(expr.getValue());
		return DONE;
	}

	@Override
	public Void caseExprInt(ExprInt expr) {
		if (radix == 16) {
			builder.append("0x");
		}
		builder.append(expr.getValue().toString(radix));
		return DONE;
	}

	@Override
	public Void caseExprList(ExprList expr) {
		builder.append('{');

		Iterator<Expression> it = expr.getValue().iterator();
		if (it.hasNext()) {
			builder.append(doSwitch(it.next()));
			while (it.hasNext()) {
				builder.append(", ");
				doSwitch(it.next(), Integer.MAX_VALUE, 0);
			}
		}

		builder.append('}');
		return DONE;
	}

	@Override
	public Void caseExprString(ExprString expr) {
		// note the difference with the caseExprString method from the
		// expression evaluator: quotes around the string
		builder.append('"');
		builder.append(expr.getValue());
		builder.append('"');
		return DONE;
	}

	@Override
	public Void caseExprUnary(ExprUnary expr) {
		builder.append(expr.getOp().getText());
		doSwitch(expr.getExpr(), Integer.MIN_VALUE, branch);
		return DONE;
	}

	@Override
	public Void caseExprVar(ExprVar expr) {
		builder.append(expr.getUse().getVariable().getName());
		return DONE;
	}

	public void doSwitch(Expression expression, int newPrecedence, int newBranch) {
		int oldBranch = branch;
		int oldPrecedence = precedence;

		branch = newBranch;
		precedence = newPrecedence;
		doSwitch(expression);
		precedence = oldPrecedence;
		branch = oldBranch;
	}

	public String toString(Expression expr) {
		doSwitch(expr);
		return builder.toString();
	}

}
