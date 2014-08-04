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
package com.synflow.cx.formatting;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.formatting.ILineSeparatorInformation;
import org.eclipse.xtext.formatting.IWhitespaceInformationProvider;

/**
 * This class defines a whitespace information provider that returns a line
 * separator that is always "\n", because using the default implementation does
 * not work when formatting files from a different platform...
 * 
 * @author Matthieu Wipliez
 * 
 */
public class WhitespaceInfoProvider extends
		IWhitespaceInformationProvider.Default {

	@Override
	public ILineSeparatorInformation getLineSeparatorInformation(URI uri) {
		return new ILineSeparatorInformation() {
			public String getLineSeparator() {
				return "\n";
			}
		};
	}

}
