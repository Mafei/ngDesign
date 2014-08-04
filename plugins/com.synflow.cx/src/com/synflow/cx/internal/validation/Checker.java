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

import static com.synflow.cx.validation.IssueCodes.ERR_TYPE_MISMATCH;
import static org.eclipse.xtext.validation.ValidationMessageAcceptor.INSIGNIFICANT_INDEX;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

import com.synflow.cx.internal.services.VoidCflowSwitch;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.util.TypePrinter;
import com.synflow.models.ir.util.TypeUtil;

/**
 * This class defines an abstract checker, that provides "error" protected methods similar to
 * Validator.
 * 
 * @author Matthieu Wipliez
 * 
 */
public abstract class Checker extends VoidCflowSwitch {

	protected ValidationMessageAcceptor acceptor;

	public Checker() {
	}

	public Checker(ValidationMessageAcceptor acceptor) {
		this.acceptor = acceptor;
	}

	/**
	 * Checks the expression can be assigned to the variable.
	 * 
	 * @param source
	 *            AST node to use to signal an error
	 * @param typeVar
	 *            type of target
	 * @param typeExpr
	 *            type of source
	 */
	protected void checkAssign(Type typeVar, Type typeExpr, EObject source,
			EStructuralFeature feature) {
		checkAssign(typeVar, typeExpr, source, feature, INSIGNIFICANT_INDEX);
	}

	/**
	 * Checks the expression can be assigned to the variable.
	 * 
	 * @param source
	 *            AST node to use to signal an error
	 * @param typeTgt
	 *            type of target
	 * @param typeSrc
	 *            type of source
	 */
	protected void checkAssign(Type typeTgt, Type typeSrc, EObject source,
			EStructuralFeature feature, int index) {
		if (typeTgt == null || typeSrc == null) {
			return;
		}

		if (!TypeUtil.canAssign(typeSrc, typeTgt)) {
			error("Type mismatch: cannot convert from " + new TypePrinter().toString(typeSrc)
					+ " to " + new TypePrinter().toString(typeTgt), source, feature, index,
					ERR_TYPE_MISMATCH);
		}
	}

	protected void error(String message, EObject source, EStructuralFeature feature, int index) {
		error(message, source, feature, index, null);
	}

	protected void error(String message, EObject source, EStructuralFeature feature, int index,
			String code, String... issueData) {
		acceptor.acceptError(message, source, feature, index, code, issueData);
	}

	protected void error(String message, EObject source, EStructuralFeature feature, String code,
			String... issueData) {
		acceptor.acceptError(message, source, feature,
				ValidationMessageAcceptor.INSIGNIFICANT_INDEX, code, issueData);
	}

	/**
	 * If the acceptor is not set at construction time (for example if this checker is injected),
	 * this method allows users to set it later.
	 * 
	 * @param acceptor
	 */
	public void setValidator(ValidationMessageAcceptor acceptor) {
		this.acceptor = acceptor;
	}

	protected void warning(String message, EObject source, EStructuralFeature feature) {
		warning(message, source, feature, ValidationMessageAcceptor.INSIGNIFICANT_INDEX);
	}

	protected void warning(String message, EObject source, EStructuralFeature feature, int index) {
		warning(message, source, feature, index, null);
	}

	protected void warning(String message, EObject source, EStructuralFeature feature, int index,
			String code, String... issueData) {
		acceptor.acceptWarning(message, source, feature, index, code, issueData);
	}

}
