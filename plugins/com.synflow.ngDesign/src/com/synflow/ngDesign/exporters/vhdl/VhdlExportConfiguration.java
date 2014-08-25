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
package com.synflow.ngDesign.exporters.vhdl;

import com.google.common.collect.ImmutableList;
import com.synflow.core.IExportConfiguration;

/**
 * This class defines the export configuration for VHDL.
 * 
 * @author Matthieu Wipliez
 *
 */
public class VhdlExportConfiguration implements IExportConfiguration {

	@Override
	public Iterable<String> getLibraryFiles() {
		return ImmutableList.of("com.synflow.lib.Helper_functions");
	}

	@Override
	public Iterable<String> getSimulationFiles() {
		return ImmutableList.of("com.synflow.lib.sim_package");
	}

}
