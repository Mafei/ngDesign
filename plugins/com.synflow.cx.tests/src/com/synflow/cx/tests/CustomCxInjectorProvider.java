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
package com.synflow.cx.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.synflow.core.SynflowModule;
import com.synflow.cx.CxInjectorProvider;
import com.synflow.cx.CxStandaloneSetup;

/**
 * This class configures additional modules in addition to the CxRuntimeModule.
 * 
 * @author Matthieu Wipliez
 *
 */
public class CustomCxInjectorProvider extends CxInjectorProvider {

	private List<Module> modules;

	public CustomCxInjectorProvider(Module... modules) {
		this.modules = new ArrayList<>();
		this.modules.add(new SynflowModule());
		this.modules.addAll(Arrays.asList(modules));
	}

	@Override
	protected Injector internalCreateInjector() {
		return new CxStandaloneSetup() {
			@Override
			public Injector createInjector() {
				return Guice.createInjector(new com.synflow.cx.CxRuntimeModule() {
					@Override
					public void configure(Binder binder) {
						super.configure(binder);
						for (Module module : modules) {
							module.configure(binder);
						}
					}
				});
			}
		}.createInjectorAndDoEMFRegistration();
	}

}
