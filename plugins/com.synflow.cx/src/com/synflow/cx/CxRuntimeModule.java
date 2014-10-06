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
package com.synflow.cx;

import static com.google.inject.name.Names.named;
import static org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider.NAMED_DELEGATE;

import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.debug.IStratumBreakpointSupport;
import org.eclipse.xtext.formatting.IWhitespaceInformationProvider;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.linking.impl.ImportedNamesAdapter;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.resource.IDefaultResourceDescriptionStrategy;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.scoping.IGlobalScopeProvider;
import org.eclipse.xtext.scoping.IScopeProvider;

import com.google.inject.Binder;
import com.google.inject.Provider;
import com.synflow.cx.conversion.CxValueConverter;
import com.synflow.cx.debug.CxStratumBreakpointSupport;
import com.synflow.cx.formatting.WhitespaceInfoProvider;
import com.synflow.cx.generator.CxGenerator;
import com.synflow.cx.internal.scoping.CxGlobalScopeProvider;
import com.synflow.cx.internal.scoping.CxImportedNamesAdapterProvider;
import com.synflow.cx.internal.scoping.CxImportedNamespaceScopeProvider;
import com.synflow.cx.resource.CxResourceDescriptionManager;
import com.synflow.cx.resource.CxResourceStrategy;
import com.synflow.cx.services.CxQualifiedNameProvider;

/**
 * Use this class to register components to be used at runtime / without the Equinox extension
 * registry.
 */
@SuppressWarnings("restriction")
public class CxRuntimeModule extends AbstractCxRuntimeModule {

	public Class<? extends IDefaultResourceDescriptionStrategy> bindIDefaultResourceDescriptionStrategy() {
		return CxResourceStrategy.class;
	}

	public Class<? extends IGenerator> bindIGenerator() {
		return CxGenerator.class;
	}

	@Override
	public Class<? extends IGlobalScopeProvider> bindIGlobalScopeProvider() {
		return CxGlobalScopeProvider.class;
	}

	@Override
	public Class<? extends IQualifiedNameProvider> bindIQualifiedNameProvider() {
		return CxQualifiedNameProvider.class;
	}

	public Class<? extends IResourceDescription.Manager> bindIResourceDescription$Manager() {
		return CxResourceDescriptionManager.class;
	}

	public Class<? extends IStratumBreakpointSupport> bindIStratumBreakpointSupport() {
		return CxStratumBreakpointSupport.class;
	}

	@Override
	public Class<? extends IValueConverterService> bindIValueConverterService() {
		return CxValueConverter.class;
	}

	public Class<? extends IWhitespaceInformationProvider> bindIWhitespaceInformationProvider() {
		return WhitespaceInfoProvider.class;
	}

	@Override
	public void configureIScopeProviderDelegate(Binder binder) {
		binder.bind(IScopeProvider.class).annotatedWith(named(NAMED_DELEGATE))
				.to(CxImportedNamespaceScopeProvider.class);
	}

	public Provider<? extends ImportedNamesAdapter> provideImportedNamesAdapter() {
		return new CxImportedNamesAdapterProvider();
	}

}
