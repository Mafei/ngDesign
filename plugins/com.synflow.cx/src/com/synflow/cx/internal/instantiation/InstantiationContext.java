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

import static com.synflow.cx.cx.CxFactory.eINSTANCE;

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
import com.synflow.cx.cx.ExpressionInteger;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Null;
import com.synflow.cx.cx.Obj;
import com.synflow.cx.cx.Pair;
import com.synflow.cx.cx.Primitive;
import com.synflow.cx.cx.VariableArgument;
import com.synflow.models.dpn.Instance;
import com.synflow.models.ir.ExprInt;
import com.synflow.models.ir.Var;
import com.synflow.models.node.Node;

/**
 * This class defines an instantiation context as the path and properties obtained throughout the
 * hierarchy.
 * 
 * @author Matthieu Wipliez
 *
 */
public class InstantiationContext extends Node {

	private final Inst inst;

	private final Instance instance;

	private final Map<String, CExpression> properties;

	/**
	 * Creates a new instantiation context using the given parent context and the given name.
	 * 
	 * @param parent
	 *            parent context
	 * @param name
	 *            name of an instance
	 */
	public InstantiationContext(InstantiationContext parent, Inst inst, Instance instance) {
		super(parent, inst.getName());
		this.inst = inst;
		this.instance = instance;

		// first add properties from parent context
		properties = new LinkedHashMap<>();
		if (parent != null) {
			properties.putAll(parent.properties);
		}

		// then add inst's properties (may override parent's)
		Obj obj = inst.getArguments();
		if (obj != null) {
			for (Pair pair : obj.getMembers()) {
				String key = pair.getKey();
				Element element = pair.getValue();

				// support for primitive values or const globals
				if (element instanceof Primitive) {
					Primitive primitive = (Primitive) element;
					EObject value = primitive.getValue();
					if (value instanceof CExpression) {
						properties.put(key, (CExpression) value);
					} else if (value instanceof Null) {
						properties.put(key, null);
					}
				} else if (element instanceof VariableArgument) { // The value of a key in json arguments for this instantiation has text that looks like a variable name
					VariableArgument varArgument = (VariableArgument) element;
					String variableName = varArgument.getValue();

					// flag
					Boolean foundVariable = false;

					// look through variables in context to find one matching by name
					for (Var var : instance.getDPN().getVariables()) {

						// compare names
						if (var != null && var.getName().equals(variableName)) {

							if( var.isAssignable() )
							{
								// compiler error: var must be const   (is there a better way to check for const than isAssignable?)
								break;
							}

							// Is there a better way to convert into something properties will accept?
							ExprInt intVal = (ExprInt)var.getInitialValue();
							ExpressionInteger epp = eINSTANCE.createExpressionInteger();
							epp.setValue(intVal.getValue());

							// add property
							properties.put(key, epp);



							foundVariable = true;
							break;
						}
					}

					if(!foundVariable)
					{
						// compiler error: 
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
		inst = null;
		instance = null;
	}

	public Inst getInst() {
		return inst;
	}

	public Instance getInstance() {
		return instance;
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
