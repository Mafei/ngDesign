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
package com.synflow.cflow.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.synflow.cflow.CflowInjectorProvider;
import com.synflow.cflow.CflowStandaloneSetup;
import com.synflow.core.SynflowModule;

/**
 * This class configures additional modules in addition to the CflowRuntimeModule.
 * 
 * @author Matthieu Wipliez
 *
 */
public class CustomCflowInjectorProvider extends CflowInjectorProvider {

	private List<Module> modules;

	public CustomCflowInjectorProvider(Module... modules) {
		this.modules = new ArrayList<>();
		this.modules.add(new SynflowModule());
		this.modules.addAll(Arrays.asList(modules));
	}

	@Override
	protected Injector internalCreateInjector() {
		return new CflowStandaloneSetup() {
			@Override
			public Injector createInjector() {
				return Guice.createInjector(new com.synflow.cflow.CflowRuntimeModule() {
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
