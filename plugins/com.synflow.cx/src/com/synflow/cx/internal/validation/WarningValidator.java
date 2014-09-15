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
package com.synflow.cx.internal.validation;

import static com.synflow.cx.CxConstants.NAME_LOOP;
import static com.synflow.cx.CxConstants.NAME_LOOP_DEPRECATED;
import static com.synflow.cx.CxConstants.NAME_SETUP;
import static com.synflow.cx.CxConstants.NAME_SETUP_DEPRECATED;
import static com.synflow.cx.validation.IssueCodes.WARN_SHOULD_REPLACE_NAME;
import static org.eclipse.xtext.EcoreUtil2.getContainerOfType;

import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil.UsageCrossReferencer;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.CxEntity;
import com.synflow.cx.cx.CxPackage.Literals;
import com.synflow.cx.cx.ExpressionVariable;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Instantiable;
import com.synflow.cx.cx.Module;
import com.synflow.cx.cx.StatementAssign;
import com.synflow.cx.cx.StatementVariable;
import com.synflow.cx.cx.VarRef;
import com.synflow.cx.cx.Variable;

/**
 * This class defines a validator that produces warnings.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class WarningValidator extends AbstractDeclarativeValidator {

	@Check
	public void checkCodingStyle(CxEntity entity) {
		String name = entity.getName();

		EObject cter = entity.eContainer();
		if (cter instanceof Module) {
			Module module = (Module) cter;
			if (module.getEntities().size() == 1) {
				IPath path = new Path(CxUtil.getFileName(module));
				String fileName = path.removeFileExtension().lastSegment();
				if (!name.equals(fileName)) {
					warning("This entity should be named '" + fileName
							+ "' because it is the only entity declared by '" + path.lastSegment()
							+ "'", entity, Literals.CX_ENTITY__NAME, INSIGNIFICANT_INDEX,
							WARN_SHOULD_REPLACE_NAME, name, fileName);
					return;
				}
			}
		}

		if (startsWithLower(name)) {
			String nameUpper = name.substring(0, 1).toUpperCase() + name.substring(1);
			warning("The entity '" + name
					+ "' should have a name that starts with an uppercase letter", entity,
					Literals.CX_ENTITY__NAME, INSIGNIFICANT_INDEX, WARN_SHOULD_REPLACE_NAME, name,
					nameUpper);
		}
	}

	@Check
	public void checkCodingStyle(Inst inst) {
		String name = inst.getName();
		if (startsWithUpper(name)) {
			String nameLower = name.substring(0, 1).toLowerCase() + name.substring(1);
			warning("The instance '" + name
					+ "' should have a name that starts with a lowercase letter", inst,
					Literals.INST__NAME, INSIGNIFICANT_INDEX, WARN_SHOULD_REPLACE_NAME, name,
					nameLower);
		}
	}

	@Check
	public void checkCodingStyle(Variable variable) {
		if (!CxUtil.isPort(variable)) {
			// only check ports
			return;
		}

		String name = variable.getName();
		if (startsWithUpper(name)) {
			String nameLower = name.substring(0, 1).toLowerCase() + name.substring(1);
			warning("The port '" + name
					+ "' should have a name that starts with a lowercase letter", variable,
					Literals.VARIABLE__NAME, INSIGNIFICANT_INDEX, WARN_SHOULD_REPLACE_NAME, name,
					nameLower);
		}
	}

	private void checkFunction(Variable function) {
		String name = function.getName();
		if (NAME_SETUP.equals(name) || NAME_LOOP.equals(name)) {
			// do not warn for setup/loop
			return;
		}

		if (NAME_SETUP_DEPRECATED.equals(name)) {
			warning("The function '" + name + "' should be named 'setup'", function,
					Literals.VARIABLE__NAME);
			return;
		}

		if (NAME_LOOP_DEPRECATED.equals(name)) {
			warning("The function '" + name + "' should be named 'loop'", function,
					Literals.VARIABLE__NAME);
			return;
		}

		ResourceSet set = function.eResource().getResourceSet();
		Collection<Setting> settings = UsageCrossReferencer.find(function, set);
		if (settings.isEmpty()) {
			warning("The function " + name + " is never called", function, Literals.VARIABLE__NAME);
		}
	}

	@Check
	public void checkUnusedVariable(Variable variable) {
		if (CxUtil.isPort(variable)) {
			// do not check ports
			return;
		}

		Instantiable entity = getContainerOfType(variable, Instantiable.class);
		if (entity == null) {
			// do not warn for variables in bundles
			return;
		}

		if (CxUtil.isFunction(variable)) {
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
					Literals.VARIABLE__NAME);
		} else if (!isRead) {
			warning("The variable " + variable.getName() + " is never read", variable,
					Literals.VARIABLE__NAME);
		} else if (!isWritten) {
			if (CxUtil.isConstant(variable)) {
				// it can never be written
				return;
			}

			if (variable.eContainingFeature() == Literals.VARIABLE__PARAMETERS) {
				// a parameter is often read but not written
				return;
			}

			warning("The variable " + variable.getName() + " is never written", variable,
					Literals.VARIABLE__NAME);
		}
	}

	@Override
	public void register(EValidatorRegistrar registrar) {
		// do nothing: packages are already registered by CxJavaValidator
	}

	private boolean startsWithLower(String name) {
		if (name == null || name.isEmpty()) {
			return false;
		}

		char ch = name.charAt(0);
		return Character.isLowerCase(ch);
	}

	private boolean startsWithUpper(String name) {
		if (name == null || name.isEmpty()) {
			return false;
		}

		char ch = name.charAt(0);
		return Character.isUpperCase(ch);
	}

}
