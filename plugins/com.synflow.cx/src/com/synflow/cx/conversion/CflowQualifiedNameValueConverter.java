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
package com.synflow.cx.conversion;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.QualifiedNameValueConverter;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.util.Strings;

/**
 * This class defines a qualified name value converter. Implementation is copied from Xbase. No idea
 * why it is necessary.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CflowQualifiedNameValueConverter extends QualifiedNameValueConverter {

	@Override
	protected String getDelegateRuleName() {
		return "ValidID";
	}

	@Override
	public String toValue(String string, INode node) throws ValueConverterException {
		StringBuilder buffer = new StringBuilder();
		boolean isFirst = true;
		if (node != null) {
			for (INode child : node.getAsTreeIterable()) {
				EObject grammarElement = child.getGrammarElement();
				if (isDelegateRuleCall(grammarElement) || isWildcardLiteral(grammarElement)) {
					if (!isFirst)
						buffer.append(getValueNamespaceDelimiter());
					isFirst = false;
					if (isDelegateRuleCall(grammarElement))
						for (ILeafNode leafNode : child.getLeafNodes()) {
							if (!leafNode.isHidden())
								buffer.append(delegateToValue(leafNode));
						}
					else
						buffer.append(getWildcardLiteral());
				}
			}
		} else {
			for (String segment : Strings.split(string, getStringNamespaceDelimiter())) {
				if (!isFirst)
					buffer.append(getValueNamespaceDelimiter());
				isFirst = false;
				if (getWildcardLiteral().equals(segment)) {
					buffer.append(getWildcardLiteral());
				} else {
					buffer.append((String) valueConverterService.toValue(segment,
							getDelegateRuleName(), null));
				}
			}
		}
		return buffer.toString();
	}

}
