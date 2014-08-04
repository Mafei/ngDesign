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
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.resource.IDefaultResourceDescriptionStrategy;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.scoping.IGlobalScopeProvider;
import org.eclipse.xtext.scoping.IScopeProvider;

import com.google.inject.Binder;
import com.synflow.cx.AbstractCflowRuntimeModule;
import com.synflow.cx.conversion.CflowValueConverter;
import com.synflow.cx.debug.CflowStratumBreakpointSupport;
import com.synflow.cx.formatting.WhitespaceInfoProvider;
import com.synflow.cx.generator.CflowGenerator;
import com.synflow.cx.internal.scoping.CflowGlobalScopeProvider;
import com.synflow.cx.internal.scoping.CflowImportedNamespaceScopeProvider;
import com.synflow.cx.internal.scoping.CflowResourceStrategy;
import com.synflow.cx.internal.scoping.CxResourceDescriptionManager;
import com.synflow.cx.services.CflowQualifiedNameProvider;

/**
 * Use this class to register components to be used at runtime / without the Equinox extension
 * registry.
 */
@SuppressWarnings("restriction")
public class CflowRuntimeModule extends AbstractCflowRuntimeModule {

	public Class<? extends IDefaultResourceDescriptionStrategy> bindIDefaultResourceDescriptionStrategy() {
		return CflowResourceStrategy.class;
	}

	public Class<? extends IGenerator> bindIGenerator() {
		return CflowGenerator.class;
	}

	@Override
	public Class<? extends IGlobalScopeProvider> bindIGlobalScopeProvider() {
		return CflowGlobalScopeProvider.class;
	}

	@Override
	public Class<? extends IQualifiedNameProvider> bindIQualifiedNameProvider() {
		return CflowQualifiedNameProvider.class;
	}

	public Class<? extends IResourceDescription.Manager> bindIResourceDescription$Manager() {
		return CxResourceDescriptionManager.class;
	}

	public Class<? extends IStratumBreakpointSupport> bindIStratumBreakpointSupport() {
		return CflowStratumBreakpointSupport.class;
	}

	@Override
	public Class<? extends IValueConverterService> bindIValueConverterService() {
		return CflowValueConverter.class;
	}

	public Class<? extends IWhitespaceInformationProvider> bindIWhitespaceInformationProvider() {
		return WhitespaceInfoProvider.class;
	}

	@Override
	public void configureIScopeProviderDelegate(Binder binder) {
		binder.bind(IScopeProvider.class).annotatedWith(named(NAMED_DELEGATE))
				.to(CflowImportedNamespaceScopeProvider.class);
	}

}
