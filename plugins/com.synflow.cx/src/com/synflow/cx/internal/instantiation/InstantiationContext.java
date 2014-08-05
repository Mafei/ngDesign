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
package com.synflow.cx.internal.instantiation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.synflow.cx.cx.CExpression;
import com.synflow.cx.cx.Element;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Null;
import com.synflow.cx.cx.Obj;
import com.synflow.cx.cx.Pair;
import com.synflow.cx.cx.Primitive;
import com.synflow.models.node.Node;

/**
 * This class defines an instantiation context as the path and properties obtained throughout the
 * hierarchy.
 * 
 * @author Matthieu Wipliez
 *
 */
public class InstantiationContext extends Node {

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
		super(parent, inst.getName());

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
		super(name);
		properties = new LinkedHashMap<>();
	}

	/**
	 * Returns the full name as an underscore-separated list of names.
	 * 
	 * @return a string
	 */
	public String getName() {
		List<String> path = new ArrayList<>();
		Node node = this;
		do {
			path.add((String) node.getContent());
			node = node.getParent();
		} while (node != null);

		return Joiner.on('_').join(Lists.reverse(path));
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
