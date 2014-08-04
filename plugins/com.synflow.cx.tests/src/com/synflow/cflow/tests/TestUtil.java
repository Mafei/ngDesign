/*******************************************************************************
 * Copyright (c) 2012 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cflow.tests;

import java.util.List;

import com.synflow.models.dpn.Port;
import com.synflow.models.ir.Expression;

/**
 * This class contains utility methods for test suites.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class TestUtil {

	/**
	 * Returns the tokens associated with the given port.
	 * 
	 * @param port
	 *            a port of a C~ actor
	 * @param attrName
	 *            "expected" or "stimulus"
	 * @return tokens as a list of expressions
	 */
	public static List<Expression> getTokens(Port port, String attrName) {
//		Attribute attribute = port.getAttribute(attrName);
//		Assert.assertNotNull(attribute);
//
//		EObject value = attribute.getContainedValue();
//		Assert.assertTrue(value instanceof ExprList);
//
//		ExprList expected = (ExprList) value;
//		return expected.getValue();
		return null;
	}

}
