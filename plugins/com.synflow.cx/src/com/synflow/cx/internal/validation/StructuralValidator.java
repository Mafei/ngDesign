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
package com.synflow.cx.internal.validation;

import static com.synflow.cx.validation.IssueCodes.ERR_DUPLICATE_DECLARATIONS;
import static com.synflow.cx.validation.IssueCodes.ERR_EXPECTED_CONST;
import static com.synflow.cx.validation.IssueCodes.ERR_ILLEGAL_FENCE;
import static com.synflow.cx.validation.IssueCodes.ERR_SIDE_EFFECTS_FUNCTION;
import static com.synflow.cx.validation.IssueCodes.ERR_TYPE_ONE_BIT;
import static com.synflow.cx.validation.IssueCodes.ERR_VAR_DECL;
import static java.math.BigInteger.ZERO;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.IScopeProvider;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator;
import org.eclipse.xtext.validation.Check;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.Block;
import com.synflow.cx.cx.CExpression;
import com.synflow.cx.cx.ExpressionVariable;
import com.synflow.cx.cx.Instantiable;
import com.synflow.cx.cx.Module;
import com.synflow.cx.cx.PortDef;
import com.synflow.cx.cx.SinglePortDecl;
import com.synflow.cx.cx.Statement;
import com.synflow.cx.cx.StatementFence;
import com.synflow.cx.cx.StatementIdle;
import com.synflow.cx.cx.StatementIf;
import com.synflow.cx.cx.StatementLoop;
import com.synflow.cx.cx.TypeDecl;
import com.synflow.cx.cx.TypeGen;
import com.synflow.cx.cx.Value;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.cx.CxPackage.Literals;
import com.synflow.cx.internal.services.BoolCxSwitch;
import com.synflow.cx.internal.services.Typer;
import com.synflow.cx.services.Evaluator;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.util.ValueUtil;

/**
 * This class defines a structural validator.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class StructuralValidator extends AbstractDeclarativeValidator {

	/**
	 * This class defines a visitor that checks if a value has side-effects, which is the case if it
	 * references any variable that is not constant (this includes functions and ports).
	 * 
	 * @author Matthieu Wipliez
	 * 
	 */
	private static class ValueVisitor extends BoolCxSwitch {

		@Override
		public Boolean caseExpressionVariable(ExpressionVariable expr) {
			Variable variable = expr.getSource().getVariable();
			if (!CxUtil.isConstant(variable)) {
				// any reference to a port and non-constant function
				return true;
			}

			return super.caseExpressionVariable(expr);
		}

	}

	@Inject
	private IQualifiedNameProvider nameProvider;

	@Inject
	private IScopeProvider scopeProvider;

	@Inject
	private Typer typer;

	@Check
	public void checkArrayMultiDimPowerOfTwo(Variable variable) {
		if (CxUtil.isPort(variable)) {
			return;
		}

		Type type = typer.getType(variable);
		if (type == null) {
			return;
		}

		int dimensions = Typer.getNumDimensions(type);
		if (dimensions >= 2) {
			// for (int dim : ((TypeArray) type).getDimensions()) {
			// if (!ValueUtil.isPowerOfTwo(dim)) {
			// error("Multi-dimensional arrays must have dimensions that are power-of-two",
			// variable, Literals.VARIABLE__DIMENSIONS,
			// ERR_ARRAY_MULTI_NON_POWER_OF_TWO);
			// }
			// }
		}
	}

	@Check
	public void checkDuplicateDeclaration(Variable variable) {
		Module module = EcoreUtil2.getContainerOfType(variable, Module.class);
		QualifiedName name = nameProvider.getFullyQualifiedName(variable);
		if (name == null) {
			// name is null when the variable declaration is incomplete
			return;
		}

		IScope scope = scopeProvider.getScope(module, Literals.VAR_REF__VARIABLE);
		Iterable<IEObjectDescription> it = scope.getElements(name);
		int n = Iterables.size(it);

		if (n > 1) {
			error("Duplicate variable declaration '" + variable.getName() + "'", variable,
					Literals.VARIABLE__NAME, ERR_DUPLICATE_DECLARATIONS);
		}
	}

	@Check
	public void checkFence(StatementFence fence) {
		Block compound = (Block) fence.eContainer();
		List<Statement> stmts = compound.getStmts();
		int index = stmts.indexOf(fence);
		boolean illegal = false;
		if (index == 0 || index == stmts.size() - 1) {
			// first or last => illegal
			illegal = true;
		} else {
			Statement previous = stmts.get(index - 1);
			if (previous instanceof StatementFence) {
				// fence before a fence => illegal
				illegal = true;
			} else {
				Statement next = stmts.get(index + 1);
				if ((previous instanceof StatementIdle || next instanceof StatementIdle)
						|| (previous instanceof StatementIf)
						|| (previous instanceof StatementLoop || next instanceof StatementLoop)) {
					// fence before/after idle, if, loop => illegal
					illegal = true;
				}
			}
		}

		if (illegal) {
			error("Illegal fence: a fence must be placed between two statements.", fence, null,
					ERR_ILLEGAL_FENCE);
		}
	}

	@Check
	public void checkFunction(Variable variable) {
		if (CxUtil.isFunction(variable)) {
			// functions declared as constant must not have side effects
			if (CxUtil.isConstant(variable) && CxUtil.hasSideEffects(variable)) {
				error("Constant function '" + variable.getName() + "' cannot have side effects",
						variable, Literals.VARIABLE__NAME, ERR_SIDE_EFFECTS_FUNCTION);
			}

			// functions declared as constant must not have side effects
			if (!CxUtil.isConstant(variable) && !CxUtil.isVoid(variable)) {
				error("Function '" + variable.getName()
						+ "' returns a result and must be declared const", variable,
						Literals.VARIABLE__NAME, ERR_SIDE_EFFECTS_FUNCTION);
			}
		}
	}

	@Check
	public void checkIdle(StatementIdle idle) {
		CExpression numCycles = idle.getNumCycles();
		Object value = Evaluator.getValue(numCycles);
		if (!ValueUtil.isInt(value)) {
			error("Illegal idle: the number of cycles must be a compile-time constant integer",
					numCycles, null, ERR_EXPECTED_CONST);
		} else if (!ValueUtil.isTrue(ValueUtil.gt(value, ZERO))) {
			error("Illegal idle: the number of cycles must be greater than zero", numCycles, null,
					ERR_EXPECTED_CONST);
		}
	}

	@Check
	public void checkPortDecl(SinglePortDecl decl) {
		if (!decl.getPorts().isEmpty()) {
			PortDef def = decl.getPorts().get(0);
			if (def.getVar().getType() == null) {
				error("Port declaration: this port must have a type", def.getVar(),
						Literals.VARIABLE__NAME);
			}
		}
	}

	@Check
	public void checkStateVariable(Variable variable) {
		// this is only for global variables (not local, not functions)
		if (!CxUtil.isGlobal(variable) || CxUtil.isFunction(variable)) {
			return;
		}

		// check dimensions
		for (CExpression dim : variable.getDimensions()) {
			boolean hasSideEffects = new ValueVisitor().doSwitch(dim);
			if (hasSideEffects) {
				error("This expression is not a compile-time constant", dim, null,
						ERR_EXPECTED_CONST);
			}
		}

		// set flag "module is actor"
		Instantiable entity = EcoreUtil2.getContainerOfType(variable, Instantiable.class);

		// check initial value
		if (!checkStateVarValue(entity != null, variable)) {
			return;
		}

		// check type of value is compatible with type of state variable
		// TODO do it differently so we don't have to compute the type of arrays
		// Value value = (Value) variable.getValue();
		// Type typeExpr = ValueUtil.getType(Evaluator.getValue(value));
		// new TypeChecker(getMessageAcceptor()).checkAssign(variable, variable, typeExpr);

		// in a header, a state variable is implicitly constant
	}

	private boolean checkStateVarValue(boolean isActor, Variable variable) {
		Value value = (Value) variable.getValue();
		if (value == null) {
			if (!isActor) {
				// in a header, a state variable must have an initial value
				error("The variable " + variable.getName() + " must have "
						+ "an initial value because it is defined in a header", variable, null,
						ERR_VAR_DECL);
				return false;
			}

			// a variable declared as "const" must have an initial value
			if (CxUtil.isConstant(variable)) {
				error("The variable " + variable.getName() + " must have "
						+ "an initial value because it is declared constant", variable, null,
						ERR_VAR_DECL);
			}

			return false;
		}

		// check if value has side-effects
		boolean hasSideEffects = new ValueVisitor().doSwitch(value);
		if (hasSideEffects) {
			error("The initial value of the variable '" + variable.getName()
					+ "' is not a compile-time constant", value, null, ERR_EXPECTED_CONST);
			return false;
		}
		return true;
	}

	@Check
	public void checkTypeDecl(TypeDecl type) {
		String spec = type.getSpec();
		if ("i1".equals(spec) || "u1".equals(spec)) {
			error("Integer types must be at least two bits large, use bool to declare a single-bit variable",
					type, null, ERR_TYPE_ONE_BIT);
		}
	}

	@Check
	public void checkTypeGen(TypeGen type) {
		String spec = type.getSpec();
		if (spec == null) {
			return;
		}

		if (!"i".equals(spec) && !"u".equals(spec)) {
			error("Generic type only supports i<expr> and u<expr>", type, null);
		}
	}

	@Override
	protected List<EPackage> getEPackages() {
		List<EPackage> result = new ArrayList<EPackage>();
		result.add(com.synflow.cx.cx.CxPackage.eINSTANCE);
		return result;
	}

}
