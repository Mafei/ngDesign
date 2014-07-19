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

import static com.synflow.cflow.CflowConstants.NAME_LOOP;
import static com.synflow.cflow.CflowConstants.NAME_LOOP_DEPRECATED;
import static com.synflow.cflow.CflowConstants.NAME_SETUP;
import static com.synflow.cflow.CflowConstants.NAME_SETUP_DEPRECATED;
import static org.eclipse.xtext.EcoreUtil2.getContainerOfType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil.UsageCrossReferencer;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator;
import org.eclipse.xtext.validation.Check;

import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.CflowPackage.Literals;
import com.synflow.cflow.cflow.ExpressionVariable;
import com.synflow.cflow.cflow.Instantiable;
import com.synflow.cflow.cflow.StatementAssign;
import com.synflow.cflow.cflow.StatementVariable;
import com.synflow.cflow.cflow.VarRef;
import com.synflow.cflow.cflow.Variable;

/**
 * This class defines a validator that produces warnings.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class WarningValidator extends AbstractDeclarativeValidator {

	private void checkFunction(Variable function) {
		String name = function.getName();
		if (NAME_SETUP.equals(name) || NAME_LOOP.equals(name)) {
			// do not warn for setup/loop
			return;
		}

		if (NAME_SETUP_DEPRECATED.equals(name)) {
			warning("The function '" + name + "' should be named 'setup'", function,
					Literals.VARIABLE__NAME, INSIGNIFICANT_INDEX);
			return;
		}

		if (NAME_LOOP_DEPRECATED.equals(name)) {
			warning("The function '" + name + "' should be named 'loop'", function,
					Literals.VARIABLE__NAME, INSIGNIFICANT_INDEX);
			return;
		}

		ResourceSet set = function.eResource().getResourceSet();
		Collection<Setting> settings = UsageCrossReferencer.find(function, set);
		if (settings.isEmpty()) {
			warning("The function " + name + " is never called", function, Literals.VARIABLE__NAME,
					INSIGNIFICANT_INDEX);
		}
	}

	@Check
	public void checkUnusedVariable(Variable variable) {
		if (CflowUtil.isPort(variable)) {
			// do not check ports
			return;
		}

		Instantiable entity = getContainerOfType(variable, Instantiable.class);
		if (entity == null) {
			// do not warn for variables in bundles
			return;
		}

		if (CflowUtil.isFunction(variable)) {
			checkFunction(variable);
		} else {
			checkVariable(variable);
		}
	}

	private void checkVariable(Variable variable) {
		boolean isRead = false, isWritten = false;
		ResourceSet set = variable.eResource().getResourceSet();
		Collection<Setting> settings = UsageCrossReferencer.find(variable, set);
		for (Setting setting : settings) {
			if (setting.getEStructuralFeature() == Literals.VAR_REF__VARIABLE) {
				VarRef ref = (VarRef) setting.getEObject();
				EObject cter = ref.eContainer();
				if (cter instanceof ExpressionVariable) {
					ExpressionVariable expr = (ExpressionVariable) cter;
					if (expr.eContainingFeature() == Literals.STATEMENT_ASSIGN__TARGET) {
						StatementAssign assign = (StatementAssign) expr.eContainer();
						// increment/decrement and compound operators
						// also read the target variable
						if (assign.getOp() != null) {
							isRead |= assign.getOp().length() > 1;
						}
						isWritten = true;
					} else {
						isRead = true;
					}
				}
			}
		}

		EObject cter = variable.eContainer();
		if (cter instanceof StatementVariable) {
			isWritten |= variable.getValue() != null;
		}

		if (!isRead && !isWritten) {
			warning("The variable " + variable.getName() + " is never used", variable,
					Literals.VARIABLE__NAME, INSIGNIFICANT_INDEX);
		} else if (!isRead) {
			warning("The variable " + variable.getName() + " is never read", variable,
					Literals.VARIABLE__NAME, INSIGNIFICANT_INDEX);
		} else if (!isWritten) {
			if (CflowUtil.isConstant(variable)) {
				// it can never be written
				return;
			}

			if (variable.eContainingFeature() == Literals.VARIABLE__PARAMETERS) {
				// a parameter is often read but not written
				return;
			}

			warning("The variable " + variable.getName() + " is never written", variable,
					Literals.VARIABLE__NAME, INSIGNIFICANT_INDEX);
		}
	}

	@Override
	protected List<EPackage> getEPackages() {
		List<EPackage> result = new ArrayList<EPackage>();
		result.add(com.synflow.cflow.cflow.CflowPackage.eINSTANCE);
		return result;
	}

}
