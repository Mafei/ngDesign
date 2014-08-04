/*******************************************************************************
 * Copyright (c) 2013-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.ui.editor.syntaxhighlighting;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor;
import org.eclipse.xtext.ui.editor.utils.TextStyle;

/**
 * This class extends the default highlighting configuration for C~.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CxHighlightingConfiguration extends DefaultHighlightingConfiguration {

	/**
	 * style for deprecated functions
	 */
	public static final String DEPRECATED_ID = "deprecated";

	/**
	 * style for special functions 'setup' and 'loop'
	 */
	public static final String SPECIAL_ID = "special";

	/**
	 * style for type aliases defined by typedef
	 */
	public static final String TYPE_ID = "type";

	@Override
	public void configure(IHighlightingConfigurationAcceptor acceptor) {
		super.configure(acceptor);
		acceptor.acceptDefaultHighlighting(DEPRECATED_ID, "Deprecated", deprecatedTextStyle());
		acceptor.acceptDefaultHighlighting(SPECIAL_ID, "Special function", entryTextStyle());
		acceptor.acceptDefaultHighlighting(TYPE_ID, "Type alias", typeTextStyle());
	}

	public TextStyle deprecatedTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setStyle(TextAttribute.STRIKETHROUGH);
		return textStyle;
	}

	public TextStyle entryTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setStyle(SWT.BOLD | TextAttribute.UNDERLINE);
		return textStyle;
	}

	public TextStyle typeTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setColor(new RGB(128, 0, 255));
		textStyle.setStyle(SWT.BOLD);
		return textStyle;
	}

}
