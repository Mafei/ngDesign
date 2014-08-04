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
package com.synflow.cx.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.xtext.formatting.IWhitespaceInformationProvider;
import org.eclipse.xtext.linking.ILinkingDiagnosticMessageProvider;
import org.eclipse.xtext.ui.editor.XtextSourceViewer.Factory;
import org.eclipse.xtext.ui.editor.hover.IEObjectHoverProvider;
import org.eclipse.xtext.ui.editor.outline.impl.OutlineFilterAndSorter.IComparator;
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreInitializer;
import org.eclipse.xtext.ui.editor.syntaxcoloring.AbstractAntlrTokenToAttributeIdMapper;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.ISemanticHighlightingCalculator;

import com.google.inject.Binder;
import com.synflow.cx.formatting.WhitespaceInfoProvider;
import com.synflow.cx.ui.AbstractCxUiModule;
import com.synflow.cx.ui.editor.syntaxhighlighting.CxHighlightingConfiguration;
import com.synflow.cx.ui.editor.syntaxhighlighting.CxSemanticHighlightingCalculator;
import com.synflow.cx.ui.editor.syntaxhighlighting.CxTokenToIdMapper;
import com.synflow.cx.ui.hover.CxEObjectHoverProvider;
import com.synflow.cx.ui.linking.CxLinkingDiagnosticMessageProvider;
import com.synflow.cx.ui.outline.CxOutlineComparator;

/**
 * Use this class to register components to be used within the IDE.
 */
public class CxUiModule extends AbstractCxUiModule {

	public CxUiModule(AbstractUIPlugin plugin) {
		super(plugin);
	}

	public Class<? extends AbstractAntlrTokenToAttributeIdMapper> bindAbstractAntlrTokenToAttributeIdMapper() {
		return CxTokenToIdMapper.class;
	}

	public Class<? extends IEObjectHoverProvider> bindIEObjectHoverProvider() {
		return CxEObjectHoverProvider.class;
	}

	public Class<? extends IHighlightingConfiguration> bindIHighlightingConfiguration() {
		return CxHighlightingConfiguration.class;
	}

	public Class<? extends ILinkingDiagnosticMessageProvider> bindILinkingDiagnosticMessageProvider() {
		return CxLinkingDiagnosticMessageProvider.class;
	}

	public Class<? extends ISemanticHighlightingCalculator> bindISemanticHighlightingCalculator() {
		return CxSemanticHighlightingCalculator.class;
	}

	@Override
	public Class<? extends IWhitespaceInformationProvider> bindIWhitespaceInformationProvider() {
		return WhitespaceInfoProvider.class;
	}

	@Override
	public Class<? extends IComparator> bindOutlineFilterAndSorter$IComparator() {
		return CxOutlineComparator.class;
	}

	public Class<? extends Factory> bindXtextSourceViewerFactory() {
		return SourceViewerFactory.class;
	}

	@Override
	public void configureBuilderPreferenceStoreInitializer(Binder binder) {
		binder.bind(IPreferenceStoreInitializer.class).to(Initializer.class);
	}

}
