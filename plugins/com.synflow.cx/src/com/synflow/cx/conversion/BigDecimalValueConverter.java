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
package com.synflow.cx.conversion;

import java.math.BigDecimal;

import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.util.Strings;

/**
 * This class defines a value converter for floating point numbers.
 * 
 * @author Matthieu Wipliez
 */
public class BigDecimalValueConverter extends AbstractLexerBasedConverter<BigDecimal> {

	public BigDecimalValueConverter() {
		super();
	}

	@Override
	protected String toEscapedString(BigDecimal value) {
		return value.toString();
	}

	@Override
	public BigDecimal toValue(String string, INode node) {
		if (Strings.isEmpty(string)) {
			throw new ValueConverterException("Couldn't convert empty string to BigDecimal", node,
					null);
		}

		return new BigDecimal(string);
	}

	@Override
	public String toString(BigDecimal value) {
		return value.toString();
	}

}
