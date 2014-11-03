/*******************************************************************************
 * Copyright (c) 2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.internal.compiler;

import org.eclipse.emf.ecore.EObject;

import com.synflow.cx.cx.CxExpression;
import com.synflow.models.ir.Expression;

/**
 * This interface defines a method for a class that can transform a Cx expression into an IR
 * expression.
 * 
 * @author Matthieu Wipliez
 * 
 */
public interface Transformer {

	EObject doSwitch(EObject eObject);

	/**
	 * Transforms the given expression without assigning to a particular target.
	 * 
	 * @param expression
	 *            an AST expression
	 * @return an IR expression
	 */
	Expression transformExpr(CxExpression expression);

}
