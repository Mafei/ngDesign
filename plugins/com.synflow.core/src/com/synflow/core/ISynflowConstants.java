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


/**
 * This interface defines useful well-known constants.
 * 
 * @author Matthieu Wipliez
 * 
 */
public interface ISynflowConstants {

	String FILE_EXT_CFLOW = "cf";

	String FILE_EXT_IR = "ir";

	/**
	 * name of the folder where IR files are generated
	 */
	String FOLDER_IR = ".ir";

	/**
	 * name of the folder where simulation files are generated
	 */
	String FOLDER_SIM = "sim";

	/**
	 * name of the "testbench" folder
	 */
	String FOLDER_TESTBENCH = "testbench";

	/**
	 * suffix of folders for generated files
	 */
	String SUFFIX_GEN = "-gen";

}
