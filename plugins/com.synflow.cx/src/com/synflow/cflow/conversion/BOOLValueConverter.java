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
package com.synflow.cflow.conversion;

import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.util.Strings;

/**
 * This class defines a value converter for BOOL rule.
 * 
 * @author Matthieu Wipliez
 */
public class BOOLValueConverter extends AbstractLexerBasedConverter<Boolean> {

	public BOOLValueConverter() {
		super();
	}

	@Override
	protected String toEscapedString(Boolean value) {
		return value.toString();
	}

	@Override
	public Boolean toValue(String string, INode node) {
		if (Strings.isEmpty(string)) {
			throw new ValueConverterException(
					"Couldn't convert empty string to boolean", node, null);
		}

		return ("true".equals(string));
	}

	@Override
	public String toString(Boolean value) {
		return value.toString();
	}

}
