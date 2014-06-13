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
package com.synflow.cflow.internal.validation;

import static com.synflow.cflow.validation.IssueCodes.ERR_CANNOT_ASSIGN_CONST;
import static com.synflow.cflow.validation.IssueCodes.ERR_DIV_MOD_NOT_CONST_POW_Of_TWO;
import static com.synflow.cflow.validation.IssueCodes.ERR_EXPECTED_CONST;
import static com.synflow.cflow.validation.IssueCodes.ERR_TYPE_MISMATCH;
import static com.synflow.models.util.SwitchUtil.DONE;
import static com.synflow.models.util.SwitchUtil.visit;
import static org.eclipse.xtext.EcoreUtil2.getContainerOfType;
import static org.eclipse.xtext.validation.ValidationMessageAcceptor.INSIGNIFICANT_INDEX;

import java.math.BigInteger;
import java.util.Iterator;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.Branch;
import com.synflow.cflow.cflow.CExpression;
import com.synflow.cflow.cflow.CflowPackage.Literals;
import com.synflow.cflow.cflow.ExpressionBinary;
import com.synflow.cflow.cflow.ExpressionCast;
import com.synflow.cflow.cflow.ExpressionIf;
import com.synflow.cflow.cflow.ExpressionUnary;
import com.synflow.cflow.cflow.ExpressionVariable;
import com.synflow.cflow.cflow.StatementAssign;
import com.synflow.cflow.cflow.StatementLoop;
import com.synflow.cflow.cflow.StatementPrint;
import com.synflow.cflow.cflow.StatementReturn;
import com.synflow.cflow.cflow.StatementVariable;
import com.synflow.cflow.cflow.StatementWrite;
import com.synflow.cflow.cflow.Task;
import com.synflow.cflow.cflow.VarDecl;
import com.synflow.cflow.cflow.VarRef;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.internal.AstUtil;
import com.synflow.cflow.internal.services.Typer;
import com.synflow.cflow.services.CflowPrinter;
import com.synflow.cflow.services.Evaluator;
import com.synflow.models.ir.IrFactory;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.OpUnary;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.TypeArray;
import com.synflow.models.ir.TypeBool;
import com.synflow.models.ir.util.TypePrinter;
import com.synflow.models.ir.util.TypeUtil;
import com.synflow.models.ir.util.ValueUtil;
import com.synflow.models.util.Void;

/**
 * This class defines the type checker for C~ with package-access methods that are only accessed by
 * the C~ java validator class.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class TypeChecker extends Checker {

	private final Typer typer;

	public TypeChecker(ValidationMessageAcceptor acceptor, Typer typer) {
		super(acceptor);
		this.typer = typer;
	}

	@Override
	public Void caseBranch(Branch stmt) {
		Type typeExpr = typer.getType(stmt.getCondition());
		checkAssign(IrFactory.eINSTANCE.createTypeBool(), typeExpr, stmt,
				Literals.BRANCH__CONDITION);

		return visit(this, stmt.getCondition(), stmt.getBody());
	}

	@Override
	public Void caseExpressionBinary(ExpressionBinary expression) {
		OpBinary op = OpBinary.getOperator(expression.getOperator());
		CExpression e1 = expression.getLeft();
		CExpression e2 = expression.getRight();
		if (e1 == null || e2 == null) {
			return DONE;
		}

		// check sub expressions
		doSwitch(e1);
		doSwitch(e2);

		// get types
		Type t1 = typer.getType(e1);
		Type t2 = typer.getType(e2);
		if (t1 == null || t2 == null) {
			return DONE;
		}

		Type type = typer.getType(expression);
		if (type == null) {
			error("The operator " + op.getText() + " is undefined for the argument types "
					+ new TypePrinter().toString(t1) + " and " + new TypePrinter().toString(t2),
					expression, null, INSIGNIFICANT_INDEX);
		}

		if (op.isComparison() && t1 != null && t2 != null) {
			// Range<BigInteger> rangeT1 = getRange(e1, t1);
			// Range<BigInteger> rangeT2 = getRange(e2, t2);
			// if (op == OpBinary.LT) {
			// if (rangeT2.upperEndpoint().compareTo(rangeT1.lowerEndpoint()) <= 0) {
			// error("This condition is always false", expression, null, ERR_CMP_ALWAYS_FALSE);
			// return;
			// }
			//
			// if (rangeT2.lowerEndpoint().compareTo(rangeT1.upperEndpoint()) > 0) {
			// error("This condition is always true", expression, null, ERR_CMP_ALWAYS_TRUE);
			// return;
			// }
			// } else if (op == OpBinary.GT) {
			// if (rangeT2.upperEndpoint().compareTo(rangeT1.lowerEndpoint()) < 0) {
			// error("This condition is always true", expression, null, ERR_CMP_ALWAYS_TRUE);
			// return;
			// }
			//
			// if (rangeT2.lowerEndpoint().compareTo(rangeT1.upperEndpoint()) >= 0) {
			// error("This condition is always false", expression, null, ERR_CMP_ALWAYS_FALSE);
			// return;
			// }
			// }
		}

		return DONE;
	}

	@Override
	public Void caseExpressionCast(ExpressionCast expr) {
		// TODO check cast is valid
		return visit(this, expr.getExpression());
	}

	@Override
	public Void caseExpressionIf(ExpressionIf expr) {
		Type typeExpr = typer.getType(expr.getCondition());
		if (!(typeExpr instanceof TypeBool)) {
			error("Type mismatch: cannot convert from " + new TypePrinter().toString(typeExpr)
					+ " to bool", expr, Literals.EXPRESSION_IF__CONDITION, ERR_TYPE_MISMATCH);
		}

		return DONE;
	}

	@Override
	public Void caseExpressionUnary(ExpressionUnary expression) {
		OpUnary op = OpUnary.getOperator(expression.getUnaryOperator());
		CExpression subExpr = expression.getExpression();
		if (subExpr == null) {
			return DONE;
		}

		// check sub expressions
		doSwitch(subExpr);

		if (typer.getType(subExpr) == null) {
			return DONE;
		}

		Type type = typer.getType(expression);
		if (type == null) {
			error("The operator " + op.getText() + " is undefined for the argument type "
					+ new TypePrinter().toString(type), expression, null, INSIGNIFICANT_INDEX);
		}
		return DONE;
	}

	@Override
	public Void caseExpressionVariable(ExpressionVariable expression) {
		doSwitch(expression.getSource());

		Variable variable = expression.getSource().getVariable();
		if (CflowUtil.isFunction(variable)) {
			checkParameters(variable, expression);
		} else {
			if (!expression.getParameters().isEmpty()) {
				error("Type mismatch: '" + variable.getName()
						+ "' is not a function and is given arguments", expression, null,
						ERR_TYPE_MISMATCH);
			}
		}

		return DONE;
	}

	@Override
	public Void caseStatementAssign(StatementAssign stmt) {
		if (stmt.getValue() == null) {
			// increment or decrement, check the variable has been initialized before
			doSwitch(stmt.getTarget());
		} else {
			doSwitch(stmt.getValue());
		}

		ExpressionVariable target = stmt.getTarget();
		VarRef ref = target.getSource();
		Variable variable = ref.getVariable();

		// if this is an assignment, check target is constant
		if (CflowUtil.isConstant(variable) && stmt.getOp() != null) {
			error("The constant '" + variable.getName() + "' cannot be assigned.", stmt,
					Literals.STATEMENT_ASSIGN__TARGET, ERR_CANNOT_ASSIGN_CONST);
			return DONE;
		}

		// compute type of variable
		Type targetType = typer.getType(ref);
		if (targetType == null) {
			return DONE;
		}

		// check array access
		checkArrayAccess(stmt, targetType, target.getIndexes());

		// compute type of target
		int dimensions = Typer.getNumDimensions(targetType);
		int indexes = target.getIndexes().size();
		if (indexes == dimensions + 1) {
			targetType = IrFactory.eINSTANCE.createTypeBool();
		} else if (indexes == dimensions) {
			if (targetType.isArray()) {
				targetType = ((TypeArray) targetType).getElementType();
			}
		} else {
			return null;
		}

		// check type
		CExpression value = AstUtil.getAssignValue(stmt);
		checkAssign(targetType, typer.getType(value), stmt, Literals.STATEMENT_ASSIGN__VALUE);

		return DONE;
	}

	@Override
	public Void caseStatementLoop(StatementLoop stmt) {
		Type typeExpr = typer.getType(stmt.getCondition());
		checkAssign(IrFactory.eINSTANCE.createTypeBool(), typeExpr, stmt,
				Literals.STATEMENT_LOOP__CONDITION);

		return visit(this, stmt.getInit(), stmt.getCondition(), stmt.getBody(), stmt.getAfter());
	}

	@Override
	public Void caseStatementPrint(StatementPrint stmt) {
		for (CExpression expr : stmt.getArgs()) {
			doSwitch(expr);

			Type type = typer.getType(expr);
			if (type != null && type.isVoid()) {
				error("Type mismatch: cannot print void", expr, null, ERR_TYPE_MISMATCH);
			}
		}

		return DONE;
	}

	@Override
	public Void caseStatementReturn(StatementReturn stmt) {
		Variable function = getContainerOfType(stmt, Variable.class);
		Type target = typer.getType(function);

		CExpression value = stmt.getValue();
		if (value == null) {
			if (target != null && !target.isVoid()) {
				error("This method must return a result of type "
						+ new TypePrinter().toString(target), stmt, null, ERR_TYPE_MISMATCH);
			}
		} else {
			checkAssign(target, typer.getType(value), stmt, Literals.STATEMENT_RETURN__VALUE);
		}

		return DONE;
	}

	@Override
	public Void caseStatementVariable(StatementVariable stmt) {
		int index = 0;
		for (Variable variable : stmt.getVariables()) {
			Type typeVar = typer.getType(variable);
			EObject value = variable.getValue();
			if (value != null) {
				// type check of value
				doSwitch(value);

				// check assignment
				checkAssign(typeVar, typer.getType(value), stmt,
						Literals.STATEMENT_VARIABLE__VARIABLES, index);
			}
			index++;
		}

		return DONE;
	}

	@Override
	public Void caseStatementWrite(StatementWrite stmt) {
		Type typePort = typer.getType(stmt.getPort());
		Type typeExpr = typer.getType(stmt.getValue());
		checkAssign(typePort, typeExpr, stmt, Literals.STATEMENT_WRITE__VALUE);

		return visit(this, stmt.getValue());
	}

	@Override
	public Void caseTask(Task task) {
		return visit(this, CflowUtil.getFunctions(task.getDecls()));
	}

	private void checkArrayAccess(EObject source, Type type, EList<CExpression> indexes) {
		if (indexes.isEmpty()) {
			// no indexes, nothing to check
			return;
		}

		if (type == null) {
			// no valid type, nothing to check at this point
			return;
		}

		int actual = indexes.size();
		int dimensions = Typer.getNumDimensions(type);
		if (actual < dimensions) {
			// there are not enough indexes
			error("Type mismatch: cannot convert from array to scalar", source, null,
					ERR_TYPE_MISMATCH);
		} else if (actual > dimensions + 1) {
			// there are too many indexes
			error("Type mismatch: cannot convert from number to array", source, null,
					ERR_TYPE_MISMATCH);
		} else if (actual == dimensions + 1) {
			// bit selection
			checkBitSelect(type, indexes.get(actual - 1));
		}
	}

	public void checkBitSelect(ExpressionVariable expr) {
		Type type = typer.getType(expr.getSource());
		checkArrayAccess(expr, type, expr.getIndexes());
	}

	/**
	 * Checks bit selection on a variable with the given type and at the given index is valid.
	 * 
	 * @param type
	 *            type of the variable
	 * @param index
	 *            index as an expression
	 */
	private void checkBitSelect(Type type, CExpression index) {
		Object value = Evaluator.getValue(index);
		if (!ValueUtil.isInt(value)) {
			error("Type mismatch: cannot convert value to constant integer in bit selection",
					index, null, ERR_TYPE_MISMATCH);
			return;
		}

		// Range<BigInteger> rangeActual = Ranges.singleton((BigInteger) value);
		// Range<BigInteger> rangeExpected = Ranges.closedOpen(BigInteger.ZERO,
		// BigInteger.valueOf(((TypeInt) type).getSize()));
		// if (rangeExpected.intersection(rangeActual).isEmpty()) {
		// error("Type mismatch: bit-selection index is outside of range", index, null,
		// ERR_TYPE_MISMATCH);
		// }
	}

	/**
	 * When the operator is /, %, &lt;&lt;, &gt;&gt;, and the expression is used outside of state
	 * variable initialization, checks that the right operand of the given binary expression is
	 * constant.
	 * 
	 * @param expr
	 */
	public void checkExprBinaryHasConstantRightOperand(ExpressionBinary expr) {
		Variable var = getContainerOfType(expr, Variable.class);
		if (var != null && var.eContainer() instanceof VarDecl) {
			// use of /, %, <<, >> allowed in initialization of state variables
			return;
		}

		String op = expr.getOperator();
		if ("/".equals(op) || "%".equals(op)) {
			Object value = Evaluator.getValue(expr.getRight());
			if (value == null || !ValueUtil.isInt(value)) {
				error("The right operand of operator " + op + " must be constant", expr, null,
						ERR_DIV_MOD_NOT_CONST_POW_Of_TWO);
				return;
			}

			int v = ((BigInteger) value).intValue();
			if (!ValueUtil.isPowerOfTwo(v)) {
				error("The right operand of operator " + op
						+ " must be a power of two and greater than zero", expr, null,
						ERR_DIV_MOD_NOT_CONST_POW_Of_TWO);
			}
		} else if ("<<".equals(op) || ">>".equals(op)) {
			Object value = Evaluator.getValue(expr.getRight());
			if (value == null || !ValueUtil.isInt(value)) {
				error("The right operand of operator " + op + " must be constant", expr, null,
						ERR_EXPECTED_CONST);
			}
		}
	}

	private void checkParameters(Variable function, ExpressionVariable call) {
		EList<CExpression> arguments = call.getParameters();
		Iterable<Type> types = Iterables.transform(arguments, new Function<CExpression, Type>() {
			@Override
			public Type apply(CExpression expression) {
				return typer.getType(expression);
			}
		});

		EList<Variable> parameters = function.getParameters();
		if (parameters.size() == arguments.size()) {
			Iterator<Variable> itV = parameters.iterator();
			Iterator<Type> itT = types.iterator();
			boolean hasErrors = false;
			while (itV.hasNext() && itT.hasNext()) {
				Type target = typer.getType(itV.next());
				Type source = itT.next();
				if (target == null || source == null) {
					continue;
				} else if (!TypeUtil.canAssign(source, target)) {
					hasErrors = true;
					break;
				}
			}

			if (!hasErrors) {
				return;
			}
		}

		Iterable<String> typeStr = Iterables.transform(types, new Function<Type, String>() {
			@Override
			public String apply(Type type) {
				return new TypePrinter().toString(type);
			}
		});

		error("The method " + new CflowPrinter().toString(function)
				+ " is not applicable for the arguments (" + Joiner.on(", ").join(typeStr) + ")",
				call, null, ERR_TYPE_MISMATCH);
	}

	// private Range<BigInteger> getRange(CExpression expr, Type type) {
	// Object value = Evaluator.getValue(expr);
	// if (ValueUtil.isInt(value)) {
	// return Ranges.singleton((BigInteger) value);
	// } else {
	// return getRange(type);
	// }
	// }

	// private Range<BigInteger> getRange(Type type) {
	// if (type.isInt()) {
	// TypeInt typeInt = (TypeInt) type;
	// int size = typeInt.getSize();
	// if (typeInt.isSigned()) {
	// BigInteger lower = ONE.shiftLeft(size - 1).negate();
	// BigInteger upper = ONE.shiftLeft(size - 1).subtract(ONE);
	// return Ranges.closed(lower, upper);
	// } else {
	// BigInteger lower = ZERO;
	// BigInteger upper = ONE.shiftLeft(size).subtract(ONE);
	// return Ranges.closed(lower, upper);
	// }
	// }
	// return null;
	// }

}
