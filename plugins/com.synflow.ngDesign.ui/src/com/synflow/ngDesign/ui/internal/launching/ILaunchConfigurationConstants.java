/*******************************************************************************
 * Copyright (c) 2012 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.ngDesign.ui.internal.launching;

/**
 * This class defines constants for launch configuration.
 * 
 * @author Matthieu Wipliez
 * 
 */
public interface ILaunchConfigurationConstants {

	String CHECK_PORTS = "com.synflow.ngDesign.ui.configuration.checkPorts";

	String CLASS = "com.synflow.ngDesign.ui.launchClass";

	String CREATE_VCD = "com.synflow.ngDesign.ui.configuration.createVcd";

	/**
	 * default: enable assertions
	 */
	boolean DEFAULT_ASSERT = true;

	/**
	 * default: do not check ports
	 */
	boolean DEFAULT_CHECK_PORTS = false;

	/**
	 * default: do not create VCD
	 */
	boolean DEFAULT_CREATE_VCD = false;

	/**
	 * default number of cycles: 100
	 */
	int DEFAULT_NUM_CYCLES = 100;

	/**
	 * default: do not print cycle number
	 */
	boolean DEFAULT_PRINT_CYCLES = false;

	String ENABLE_ASSERTIONS = "com.synflow.ngDesign.ui.configuration.enableAssertions";

	String NUM_CYCLES = "com.synflow.ngDesign.ui.configuration.numberOfCycles";

	String PRINT_CYCLES = "com.synflow.ngDesign.ui.configuration.printCycles";

	String PROJECT = "com.synflow.ngDesign.ui.launchProject";

	String TYPE_SIMULATION = "com.synflow.ngDesign.ui.launchConfigurationSimulation";

}
