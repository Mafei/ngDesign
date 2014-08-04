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
package com.synflow.cflow.ui;

import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.xtext.ui.editor.XtextSourceViewer;

/**
 * This class defines a factory that creates XtextSourceViewer instances with
 * different styles.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class SourceViewerFactory implements XtextSourceViewer.Factory {

	public static Integer style;

	@Override
	public XtextSourceViewer createSourceViewer(Composite parent,
			IVerticalRuler ruler, IOverviewRuler overviewRuler,
			boolean showsAnnotationOverview, int styles) {
		if (style != null) {
			styles = style;
		}

		return new XtextSourceViewer(parent, ruler, overviewRuler,
				showsAnnotationOverview, styles);
	}

}
