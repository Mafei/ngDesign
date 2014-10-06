/*******************************************************************************
 * Copyright (c) 2012-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez
 *    Nicolas Siret
 *******************************************************************************/
package com.synflow.cx.tests.codegen.vhdl;

import static com.synflow.ngDesign.NgDesignModule.VHDL;

import java.io.File;

import org.eclipse.xtext.junit4.InjectWith;
import org.junit.Test;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.synflow.core.ICodeGenerator;
import com.synflow.core.IExportConfiguration;
import com.synflow.cx.tests.StreamCopier;
import com.synflow.cx.tests.codegen.HdlPassTests;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Unit;

/**
 * This class defines Cx tests that are expected to succeed.
 * 
 * @author Matthieu Wipliez
 * @author Nicolas Siret
 * 
 */
@InjectWith(CxInjectorProviderVhdl.class)
public class VhdlPassTests extends HdlPassTests {

	@Inject
	@Named(VHDL)
	private IExportConfiguration configuration;

	@Inject
	@Named(VHDL)
	private ICodeGenerator generator;

	@Test
	public void app_external() throws Exception {
		// TODO make external app in VHDL testApp("External");
	}

	@Override
	protected void compileAndSimulate(Entity entity) throws Exception {
		compile(entity);

		if (!(entity instanceof Unit)) {
			compileTb(entity);
			simulate(entity);
		}
	}

	@Override
	protected ICodeGenerator getCodeGenerator() {
		return generator;
	}

	@Override
	protected IExportConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	protected String getLibraryPath() {
		String name = getCodeGenerator().getName().toLowerCase();
		return "../../fragments/com.synflow.ngDesign.libraries/lib/" + name + "/src";
	}

	@Override
	protected int runCompileCommand(File directory, String name) throws Exception {
		Process process = executeCommand(directory, "vcom", "-2008", "-quiet", "-work", "work",
				name);
		new StreamCopier(process.getInputStream(), System.out).start();
		return process.waitFor();
	}

}
