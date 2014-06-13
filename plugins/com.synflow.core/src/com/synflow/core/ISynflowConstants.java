/*******************************************************************************
 * Copyright (c) 2012-2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.core;

import static com.synflow.core.SynflowCore.PLUGIN_ID;

/**
 * This interface defines useful well-known constants.
 * 
 * @author Matthieu Wipliez
 * 
 */
public interface ISynflowConstants {

	/**
	 * name of the attribute to indicate the call is to the "assert" function
	 */
	String ATTR_ASSERT = "assert";

	/**
	 * name of the attribute "comments" for a procedure
	 */
	String ATTR_COMMENTS = "comments";

	/**
	 * name of the attribute "information" for a transition
	 */
	String ATTR_INFO = "info";

	/**
	 * name of the attribute to indicate the call is to the "print" function
	 */
	String ATTR_PRINT = "print";

	String FILE_EXT_CFLOW = "cf";

	String FILE_EXT_IR = "ir";

	/**
	 * name of the folder where IR files are generated
	 */
	String FOLDER_IR = ".ir";

	/**
	 * name of the folder with generated Java files
	 */
	String FOLDER_JAVA_GEN = "java-gen";

	/**
	 * name of the folder where synthesis files are generated
	 */
	String FOLDER_PROJECTS = "projects";

	/**
	 * name of the folder where simulation files are generated
	 */
	String FOLDER_SIM = "sim";

	/**
	 * name of the "testbench" folder
	 */
	String FOLDER_TESTBENCH = "testbench";

	/**
	 * name of the folder with generated Verilog files
	 */
	String FOLDER_VERILOG_GEN = "verilog-gen";

	/**
	 * name of the folder with generated VHDL files
	 */
	String FOLDER_VHDL_GEN = "vhdl-gen";

	/**
	 * license key
	 */
	String PREF_LICENSE_KEY = "key";

	/**
	 * login for license
	 */
	String PREF_LICENSE_LOGIN = "login";

	String PROP_GENERATOR = PLUGIN_ID + ".generator";

	/**
	 * suffix of folders for generated files
	 */
	String SUFFIX_GEN = "-gen";

	/**
	 * target: simulation
	 */
	int TARGET_SIMULATION = 0;

	/**
	 * target: synthesis
	 */
	int TARGET_SYNTHESIS = 1;

}
