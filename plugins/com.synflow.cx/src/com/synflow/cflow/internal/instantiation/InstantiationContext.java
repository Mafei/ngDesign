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
package com.synflow.cflow.internal.instantiation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Joiner;
import com.synflow.cflow.cflow.CExpression;
import com.synflow.cflow.cflow.Element;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.Null;
import com.synflow.cflow.cflow.Obj;
import com.synflow.cflow.cflow.Pair;
import com.synflow.cflow.cflow.Primitive;

/**
 * This class defines an instantiation context as the path and properties obtained throughout the
 * hierarchy.
 * 
 * @author Matthieu Wipliez
 *
 */
public class InstantiationContext {

	private List<String> path;

	private Map<String, CExpression> properties;

	/**
	 * Creates a new instantiation context using the given parent context and the given name.
	 * 
	 * @param parent
	 *            parent context
	 * @param name
	 *            name of an instance
	 */
	public InstantiationContext(InstantiationContext parent, Inst inst) {
		path = new ArrayList<>(parent.path);
		path.add(inst.getName());

		// first add properties from parent context
		properties = new LinkedHashMap<>(parent.properties);

		// then add inst's properties (may override parent's)
		Obj obj = inst.getArguments();
		if (obj != null) {
			for (Pair pair : obj.getMembers()) {
				String key = pair.getKey();
				Element element = pair.getValue();

				// only support primitive values for now
				if (element instanceof Primitive) {
					Primitive primitive = (Primitive) element;
					EObject value = primitive.getValue();
					if (value instanceof CExpression) {
						properties.put(key, (CExpression) value);
					} else if (value instanceof Null) {
						properties.put(key, null);
					}
				}
			}
		}
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

		properties = new LinkedHashMap<>();
	}

	/**
	 * Returns the full name as an underscore-separated list of names.
	 * 
	 * @return a string
	 */
	public String getName() {
		return Joiner.on('_').join(path);
	}

	/**
	 * Returns an unmodifiable map of the properties of this instantiation context.
	 * 
	 * @return a map
	 */
	public Map<String, CExpression> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

}
