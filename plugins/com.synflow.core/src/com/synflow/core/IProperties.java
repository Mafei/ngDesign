/*******************************************************************************
 * Copyright (c) 2013-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.core;

import com.google.gson.JsonPrimitive;

/**
 * This interface defines useful well-known constants.
 * 
 * @author Matthieu Wipliez
 * 
 */
public interface IProperties {

	JsonPrimitive ACTIVE_HIGH = new JsonPrimitive("high");

	JsonPrimitive ACTIVE_LOW = new JsonPrimitive("low");

	JsonPrimitive DEFAULT_CLOCK = new JsonPrimitive("clock");

	/**
	 * implementation: builtin
	 */
	JsonPrimitive IMPL_BUILTIN = new JsonPrimitive("builtin");

	/**
	 * implementation: external
	 */
	JsonPrimitive IMPL_EXTERNAL = new JsonPrimitive("external");

	/**
	 * active: high, low
	 */
	String PROP_ACTIVE = "active";

	/**
	 * clocks
	 */
	String PROP_CLOCKS = "clocks";

	/**
	 * comments: an object whose keys are lines and values are comments at those lines
	 */
	String PROP_COMMENTS = "comments";

	/**
	 * copyright: copyright statement (before package declaration)
	 */
	String PROP_COPYRIGHT = "copyright";

	/**
	 * dependencies: a list of strings, where each string is either a path or class name
	 */
	String PROP_DEPENDENCIES = "dependencies";

	/**
	 * domains: an association between ports and clocks, or clocks and ports
	 */
	String PROP_DOMAINS = "domains";

	/**
	 * in implementation, specifies the file in which the external entity is implemented
	 */
	String PROP_FILE = "file";

	/**
	 * implementation
	 */
	String PROP_IMPLEMENTATION = "implementation";

	/**
	 * javadoc: documentation of current task/network
	 */
	String PROP_JAVADOC = "javadoc";

	/**
	 * name. Applies to: reset.
	 */
	String PROP_NAME = "name";

	/**
	 * reset
	 */
	String PROP_RESET = "reset";

	/**
	 * test property
	 */
	String PROP_TEST = "test";

	/**
	 * type: synchronous, asynchronous, combinational
	 */
	String PROP_TYPE = "type";

	/**
	 * reset type: asynchronous
	 */
	JsonPrimitive RESET_ASYNCHRONOUS = new JsonPrimitive("asynchronous");

	/**
	 * reset type: synchronous
	 */
	JsonPrimitive RESET_SYNCHRONOUS = new JsonPrimitive("synchronous");

}
