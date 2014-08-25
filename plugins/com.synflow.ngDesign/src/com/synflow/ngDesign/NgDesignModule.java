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
package com.synflow.ngDesign;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.synflow.core.ICodeGenerator;
import com.synflow.core.IExportConfiguration;
import com.synflow.ngDesign.exporters.vhdl.VhdlExportConfiguration;
import com.synflow.ngDesign.generators.vhdl.VhdlCodeGenerator;

/**
 * This class defines the VHDL module.
 * 
 * @author Matthieu Wipliez
 *
 */
public class NgDesignModule extends AbstractModule {

	public static final String VHDL = "VHDL";

	@Override
	protected void configure() {
		bind(ICodeGenerator.class).annotatedWith(Names.named(VHDL)).to(VhdlCodeGenerator.class);
		bind(IExportConfiguration.class).annotatedWith(Names.named(VHDL)).to(
				VhdlExportConfiguration.class);
	}

}
