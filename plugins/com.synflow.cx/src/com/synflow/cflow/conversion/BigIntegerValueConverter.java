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
package com.synflow.cflow.conversion;

import java.math.BigInteger;

import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.util.Strings;

/**
 * This class defines a value converter for integer numbers.
 * 
 * @author Matthieu Wipliez
 */
public class BigIntegerValueConverter extends AbstractLexerBasedConverter<BigInteger> {

	@Override
	protected void assertValidValue(BigInteger value) {
		super.assertValidValue(value);
		if (value.signum() == -1) {
			throw new ValueConverterException(getRuleName() + "-value may not be negative (value:"
					+ value + ").", null, null);
		}
	}

	@Override
	protected String toEscapedString(BigInteger value) {
		return value.toString();
	}

	@Override
	public BigInteger toValue(String string, INode node) {
		if (Strings.isEmpty(string))
			throw new ValueConverterException("Couldn't convert empty string to int.", node, null);
		try {
			// strip underscores if necessary
			string = string.replace("_", "");

			// compute radix
			int radix;
			if (string.startsWith("0b")) {
				radix = 2;
			} else if (string.startsWith("0x")) {
				radix = 16;
			} else {
				radix = 10;
			}

			return new BigInteger(radix == 10 ? string : string.substring(2), radix);
		} catch (NumberFormatException e) {
			throw new ValueConverterException(string + " is not a valid integer", node, e);
		}
	}
}
