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
package com.synflow.cflow.internal.validation;

import static com.synflow.cflow.CflowConstants.PROP_AVAILABLE;
import static com.synflow.cflow.CflowConstants.PROP_READ;
import static com.synflow.cflow.validation.IssueCodes.ERR_AVAILABLE;
import static com.synflow.cflow.validation.IssueCodes.ERR_LOCAL_NOT_INITIALIZED;
import static com.synflow.cflow.validation.IssueCodes.ERR_MULTIPLE_READS;
import static com.synflow.cflow.validation.IssueCodes.ERR_NO_SIDE_EFFECTS;
import static com.synflow.cflow.validation.IssueCodes.ERR_TYPE_MISMATCH;
import static com.synflow.models.util.SwitchUtil.check;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator;
import org.eclipse.xtext.validation.Check;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.inject.Inject;
import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.Branch;
import com.synflow.cflow.cflow.CExpression;
import com.synflow.cflow.cflow.CflowPackage.Literals;
import com.synflow.cflow.cflow.ExpressionVariable;
import com.synflow.cflow.cflow.StatementAssign;
import com.synflow.cflow.cflow.StatementLoop;
import com.synflow.cflow.cflow.StatementVariable;
import com.synflow.cflow.cflow.VarRef;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.internal.instantiation.IMapper;
import com.synflow.cflow.internal.services.BoolCflowSwitch;
import com.synflow.cflow.internal.services.Typer;
import com.synflow.models.dpn.InterfaceType;
import com.synflow.models.dpn.Port;
import com.synflow.models.ir.Type;

/**
 * This class defines a validator for C~ expressions.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ExpressionValidator extends AbstractDeclarativeValidator {

	/**
	 * This class defines a visitor that returns true when a function called is not constant.
	 * 
	 * @author Matthieu Wipliez
	 * 
	 */
	private static class ConstantCallSwitch extends BoolCflowSwitch {

		@Override
		public Boolean caseExpressionVariable(ExpressionVariable expr) {
			Variable variable = expr.getSource().getVariable();
			if (CflowUtil.isFunctionNotConstant(variable)) {
				return true;
			}

			return super.caseExpressionVariable(expr);
		}

	}

	@Inject
	private IMapper mapper;

	@Inject
	private Typer typer;

	@Check
	public void checkCondition(Branch stmt) {
		checkFunctionCalls(stmt.getCondition());
	}

	@Check
	public void checkCondition(StatementLoop stmt) {
		checkFunctionCalls(stmt.getCondition());
	}

	private void checkFunctionCalls(CExpression condition) {
		if (check(new ConstantCallSwitch(), condition)) {
			error("Scheduling: this expression cannot call functions with side effects", condition,
					null, ERR_NO_SIDE_EFFECTS);
		}
	}

	@Check
	public void checkLocalVariableUse(ExpressionVariable expr) {
		Variable variable = expr.getSource().getVariable();
		if (!CflowUtil.isLocal(variable)) {
			return;
		}

		Type type = typer.getType(variable);
		if (type != null && !type.isArray() && !variable.isInitialized()) {
			error("The local variable '" + variable.getName() + "' may not have been initialized",
					expr, Literals.EXPRESSION_VARIABLE__SOURCE, ERR_LOCAL_NOT_INITIALIZED);
		}
	}

	@Check
	public void checkMultipleReads(CExpression expr) {
		// Checks that there are at most one read per port in the expression. Otherwise indicate an
		// error.
		Multiset<Port> portsRead = LinkedHashMultiset.create();
		Multiset<Port> portsAvailable = LinkedHashMultiset.create();
		computePortSets(portsAvailable, portsRead, expr);

		boolean hasMultipleReads = false;
		for (Entry<Port> entry : portsRead.entrySet()) {
			hasMultipleReads |= entry.getCount() > 1;
		}

		if (hasMultipleReads) {
			error("Port error: cannot have more than one read per port in expression", expr, null,
					ERR_MULTIPLE_READS);
		}
	}

	@Check
	public void checkPort(ExpressionVariable expr) {
		Variable variable = expr.getSource().getVariable();
		if (CflowUtil.isPort(variable) && CflowUtil.isInput(variable)) {
			// checks that the given reference to a port variable has the proper semantics.
			checkPortExpression(expr);
		}
	}

	/**
	 * Checks the given expression that refers to an input port.
	 * 
	 * @param expr
	 *            an expression variable
	 */
	private void checkPortExpression(ExpressionVariable expr) {
		String prop = expr.getProperty();
		if (PROP_AVAILABLE.equals(prop)) {
			Variable variable = expr.getSource().getVariable();
			if (CflowUtil.isPort(variable)) {
				InterfaceType iface = CflowUtil.getInterface(variable);
				if (!iface.isSync()) {
					error("Port error: '" + PROP_AVAILABLE + "' can only be used on 'sync' ports",
							expr, null, ERR_AVAILABLE);
				}
			}

			EObject cter = CflowUtil.getTarget(expr);
			if (!(cter instanceof Branch || cter instanceof StatementLoop)) {
				error("Port error: '" + PROP_AVAILABLE
						+ "' can only be used in the condition of if/for/while statements", expr,
						null, ERR_AVAILABLE);
			}
		} else if (!PROP_READ.equals(prop)) {
			error("Port error: an input port can only be used with the '" + PROP_AVAILABLE
					+ "' or '" + PROP_READ + "' function", expr, null, ERR_TYPE_MISMATCH);
			return;
		}

		if (!expr.getIndexes().isEmpty()) {
			error("Port error: an input port cannot be used with indexes", expr, null,
					ERR_TYPE_MISMATCH);
		}

		if (!expr.getParameters().isEmpty()) {
			error("Port error: the '" + prop + "' function does not accept arguments", expr, null,
					ERR_TYPE_MISMATCH);
		}
	}

	/**
	 * Computes the two port sets: one containing ports that are available, the other one containing
	 * ports that are read.
	 * 
	 * @param available
	 *            a set in which ports available are put
	 * @param read
	 *            a set in which ports read are put
	 * @param condition
	 *            the condition to visit
	 */
	public void computePortSets(Multiset<Port> available, Multiset<Port> read, CExpression condition) {
		List<ExpressionVariable> exprs;
		if (condition == null) {
			return;
		}

		exprs = EcoreUtil2.eAllOfType(condition, ExpressionVariable.class);
		for (ExpressionVariable expr : exprs) {
			VarRef ref = expr.getSource();
			Variable variable = ref.getVariable();
			if (CflowUtil.isPort(variable)) {
				Port port = mapper.getPort(ref);
				String prop = expr.getProperty();
				if (PROP_AVAILABLE.equals(prop)) {
					available.add(port);
				} else if (PROP_READ.equals(prop)) {
					read.add(port);
				}
			}
		}
	}

	@Override
	protected List<EPackage> getEPackages() {
		List<EPackage> result = new ArrayList<EPackage>();
		result.add(com.synflow.cflow.cflow.CflowPackage.eINSTANCE);
		return result;
	}

	@Check
	public void setInitialized(StatementAssign stmt) {
		Variable variable = stmt.getTarget().getSource().getVariable();

		// set variable as defined if the assignment has a value
		// MUST NOT USE variable.setDefined EVER BECAUSE IT WILL CLEAR THE INSTANTIATOR'S CACHE
		// don't use setInitialized(value != null)
		// because once a value has been defined, it must not be un-defined
		if (CflowUtil.isLocal(variable) && stmt.getValue() != null) {
			setInitialized(variable);
		}
	}

	@Check
	public void setInitialized(StatementVariable stmt) {
		// set the 'defined' flag for each variable that has a value
		for (Variable variable : stmt.getVariables()) {
			if (variable.getValue() != null) {
				// MUST NOT USE variable.setDefined EVER
				setInitialized(variable);
			}
		}
	}

	/**
	 * Sets the 'initialized' field of variable to <code>true</code> without notifying adapters.
	 * Necessary so that resources stay cached.
	 * 
	 * @param variable
	 *            a variable
	 */
	private void setInitialized(Variable variable) {
		boolean deliver = variable.eDeliver();
		variable.eSetDeliver(false);
		variable.setInitialized(true);
		variable.eSetDeliver(deliver);
	}

}
