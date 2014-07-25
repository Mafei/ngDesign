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
package com.synflow.cflow.internal.instantiation.v2;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

/**
 * This class defines an instantiation context as the path and properties obtained throughout the
 * hierarchy.
 * 
 * @author Matthieu Wipliez
 *
 */
public class InstantiationContext {

	private List<String> path;

	/**
	 * Creates a new instantiation context using the given parent context and the given name.
	 * 
	 * @param parent
	 *            parent context
	 * @param name
	 *            name of an instance
	 */
	public InstantiationContext(InstantiationContext parent, String name) {
		path = new ArrayList<>(parent.path);
		path.add(name);
	}

	/**
	 * Creates a new instantiation context using the given root name.
	 * 
	 * @param name
	 *            name of the entity at the root of the hierarchy
	 */
	public InstantiationContext(String name) {
		path = new ArrayList<>();
		path.add(name);
	}

	/**
	 * Returns the full name as an underscore-separated list of names.
	 * 
	 * @return a string
	 */
	public String getName() {
		return Joiner.on('_').join(path);
	}

}
