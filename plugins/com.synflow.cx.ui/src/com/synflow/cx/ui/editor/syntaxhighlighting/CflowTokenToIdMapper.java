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
package com.synflow.cx.ui.editor.syntaxhighlighting;

import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultAntlrTokenToAttributeIdMapper;
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration;

/**
 * This class maps tokens to attribute ids.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CflowTokenToIdMapper extends DefaultAntlrTokenToAttributeIdMapper {

	@Override
	protected String calculateId(String tokenName, int tokenType) {
		if ("RULE_TYPE_INT".equals(tokenName)
				|| "RULE_TYPE_UINT".equals(tokenName)) {
			return DefaultHighlightingConfiguration.KEYWORD_ID;
		}

		return super.calculateId(tokenName, tokenType);
	}

}
