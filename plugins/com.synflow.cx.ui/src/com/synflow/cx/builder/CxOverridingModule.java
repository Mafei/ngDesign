/*******************************************************************************
 * Copyright (c) 2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.builder;

import org.eclipse.xtext.builder.builderState.IBuilderState;
import org.eclipse.xtext.builder.clustering.ClusteringBuilderState;
import org.eclipse.xtext.service.AbstractGenericModule;

import com.google.inject.Binder;
import com.google.inject.Scopes;

/**
 * This class defines an overriding module referenced in plugin.xml that replaces the binding of
 * IBuilderState from {@link ClusteringBuilderState} to {@link CxBuilderState}.
 * 
 * @author Matthieu Wipliez
 *
 */
@SuppressWarnings("restriction")
public class CxOverridingModule extends AbstractGenericModule {

	@Override
	public void configure(Binder binder) {
		binder.bind(IBuilderState.class).to(CxBuilderState.class).in(Scopes.SINGLETON);
	}

}
