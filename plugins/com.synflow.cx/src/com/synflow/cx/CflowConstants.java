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

import com.google.gson.JsonPrimitive;

/**
 * This interface defines constants.
 * 
 * @author Matthieu Wipliez
 * 
 */
public interface CflowConstants {

	/**
	 * value of the direction attribute to indicate an input port
	 */
	String DIR_IN = "in";

	/**
	 * value of the direction attribute to indicate an output port
	 */
	String DIR_OUT = "out";

	/**
	 * name of the 'loop' special function.
	 */
	String NAME_LOOP = "loop";

	String NAME_LOOP_DEPRECATED = "run";

	/**
	 * name of the 'setup' special function.
	 */
	String NAME_SETUP = "setup";

	String NAME_SETUP_DEPRECATED = "init";

	String PROP_AVAILABLE = "available";

	String PROP_READ = "read";

	String PROP_TYPE = "type";

	JsonPrimitive TYPE_COMBINATIONAL = new JsonPrimitive("combinational");

	/**
	 * value of the type attribute to indicate a "reads" connection
	 */
	String TYPE_READS = "reads";

	/**
	 * value of the type attribute to indicate a "writes" connection
	 */
	String TYPE_WRITES = "writes";

}
