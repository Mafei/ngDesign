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
package com.synflow.cx.internal.instantiation.properties;

import static com.synflow.core.IProperties.ACTIVE_HIGH;
import static com.synflow.core.IProperties.ACTIVE_LOW;
import static com.synflow.core.IProperties.DEFAULT_CLOCK;
import static com.synflow.core.IProperties.PROP_ACTIVE;
import static com.synflow.core.IProperties.PROP_CLOCKS;
import static com.synflow.core.IProperties.PROP_NAME;
import static com.synflow.core.IProperties.PROP_RESET;
import static com.synflow.core.IProperties.RESET_ASYNCHRONOUS;
import static com.synflow.core.IProperties.RESET_SYNCHRONOUS;
import static com.synflow.cx.CflowConstants.PROP_TYPE;
import static com.synflow.cx.CflowConstants.TYPE_COMBINATIONAL;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Instantiable;
import com.synflow.cx.cx.Network;
import com.synflow.cx.cx.Obj;
import com.synflow.cx.cx.CflowPackage.Literals;
import com.synflow.cx.internal.ErrorMarker;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Instance;

/**
 * This class defines a properties support class that checks and updates properties to a normal
 * form.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class PropertiesSupport implements IJsonErrorHandler {

	private static final String NO_CLOCK = "<no clock>";

	private List<ErrorMarker> errors;

	private EStructuralFeature feature;

	private JsonMaker maker;

	private final JsonObject properties;

	private EObject source;

	private PropertiesSupport(EObject source, EStructuralFeature feature, List<ErrorMarker> errors) {
		this.errors = errors;
		this.maker = new JsonMaker();

		this.source = source;
		Obj obj = (Obj) source.eGet(feature);
		if (obj == null) {
			this.properties = new JsonObject();
		} else {
			this.feature = feature;
			this.properties = maker.toJson(obj);
		}
	}

	public PropertiesSupport(Instantiable entity) {
		this(entity, Literals.INSTANTIABLE__PROPERTIES, entity.getErrors());
	}

	public PropertiesSupport(Inst inst) {
		this(inst, Literals.INST__ARGUMENTS, ((Network) inst.eContainer()).getErrors());
	}

	@Override
	public void addError(JsonElement element, String message) {
		ErrorMarker marker = maker.getMapping(element);
		if (marker == null) {
			marker = new ErrorMarker(message, source, feature);
		} else {
			marker = new ErrorMarker(message, marker);
		}

		errors.add(marker);
	}

	/**
	 * Check the 'clocks 'array.
	 * 
	 * @param clocks
	 *            an array of clock names
	 * @return <code>true</code> if it is valid
	 */
	private boolean checkClockArray(JsonElement clocks) {
		boolean isValid;
		if (clocks.isJsonArray()) {
			isValid = true;
			JsonArray clocksArray = clocks.getAsJsonArray();
			for (JsonElement clock : clocksArray) {
				if (!clock.isJsonPrimitive() || !clock.getAsJsonPrimitive().isString()) {
					isValid = false;
					break;
				}
			}
		} else {
			isValid = false;
		}

		if (!isValid) {
			addError(clocks, "clocks must be an array of clock names");
		}
		return isValid;
	}

	private void checkClocksDeclared(JsonObject properties) {
		JsonElement clocks = properties.get(PROP_CLOCKS);

		if (clocks == null || !checkClockArray(clocks)) {
			// if no clock, or properly sets default clock
			JsonArray clocksArray = new JsonArray();
			clocksArray.add(DEFAULT_CLOCK);
			properties.add(PROP_CLOCKS, clocksArray);
		}
	}

	/**
	 * Checks that the 'reset' property is properly declared.
	 * 
	 * @param properties
	 *            properties
	 */
	private void checkResetDeclared(JsonObject properties) {
		JsonElement reset = properties.get(PROP_RESET);
		if (reset == null) {
			reset = new JsonObject();
			properties.add(PROP_RESET, reset);
		}

		if (reset.isJsonObject()) {
			JsonObject resetObj = reset.getAsJsonObject();

			JsonPrimitive type = resetObj.getAsJsonPrimitive(PROP_TYPE);
			if (type == null || !RESET_ASYNCHRONOUS.equals(type) && !RESET_SYNCHRONOUS.equals(type)) {
				// default is asynchronous reset
				resetObj.add(PROP_TYPE, RESET_ASYNCHRONOUS);
			}

			JsonPrimitive active = resetObj.getAsJsonPrimitive(PROP_ACTIVE);
			if (active == null || !ACTIVE_HIGH.equals(active) && !ACTIVE_LOW.equals(active)) {
				// default is active low reset
				resetObj.add(PROP_ACTIVE, ACTIVE_LOW);
			}

			if (!resetObj.has(PROP_NAME)) {
				// compute default name
				if (ACTIVE_LOW.equals(resetObj.getAsJsonPrimitive(PROP_ACTIVE))) {
					resetObj.addProperty(PROP_NAME, "reset_n");
				} else {
					resetObj.addProperty(PROP_NAME, "reset");
				}
			}
		}
	}

	/**
	 * This method creates a clocks object from the entity's clocks and the instance's clocks.
	 * 
	 * @param entityClocks
	 *            array of entity's clocks
	 * @param instanceClocks
	 *            array of instance's clocks
	 * @return a clocks object
	 */
	private JsonObject makeClocksObject(JsonArray entityClocks, JsonArray instanceClocks) {
		JsonObject clocks = new JsonObject();
		if (entityClocks.size() == 0) {
			// when the entity declares no clocks (combinational)
			// we associate no clocks
			return clocks;
		}

		Iterator<JsonElement> it = instanceClocks.iterator();
		for (JsonElement clock : entityClocks) {
			String clockName = clock.getAsString();
			if (it.hasNext()) {
				clocks.add(clockName, it.next());
			} else {
				if (instanceClocks.size() == 1) {
					// support repetition of one clock
					clocks.add(clockName, instanceClocks.get(0));
				} else {
					// not enough clocks given
					// this is verified and caught by validateClocks
					// with a !it.hasNext() test.
					break;
				}
			}
		}

		while (it.hasNext()) {
			// too many clocks given
			// the NO_CLOCK string is not a valid identifier, only used internally
			clocks.add(NO_CLOCK, it.next());
		}

		return clocks;
	}

	/**
	 * Sets properties on the given entity.
	 * 
	 * @param entity
	 *            an entity
	 */
	public void setProperties(Entity entity) {
		entity.setProperties(properties);

		JsonElement type = properties.get(PROP_TYPE);
		if (type != null) {
			if (type.isJsonPrimitive()) {
				JsonPrimitive entityType = properties.getAsJsonPrimitive(PROP_TYPE);
				if (TYPE_COMBINATIONAL.equals(entityType)) {
					// set an empty list of clocks, set no reset
					properties.add(PROP_CLOCKS, new JsonArray());
					properties.add(PROP_RESET, null);
					return;
				} else {
					addError(entityType,
							"the only valid value of type is 'combinational', ignored.");
				}
			} else {
				addError(type, "type must be a string");
			}
		}

		checkClocksDeclared(properties);
		checkResetDeclared(properties);
	}

	/**
	 * Sets properties on the given instance.
	 * 
	 * @param instance
	 *            an instance
	 */
	public void setProperties(Instance instance) {
		instance.setProperties(properties);

		Specializer specializer = new Specializer(this);
		specializer.visitArguments(instance);

		updateClocksInstantiated(instance, properties);
	}

	/**
	 * Checks and transforms the clocks in the properties of the given instance. Clocks accepts an
	 * array and an object. If clocks is an array, this method transforms it to an object and checks
	 * it.
	 * 
	 * @param instance
	 * @param properties
	 */
	private void updateClocksInstantiated(Instance instance, JsonObject properties) {
		DPN dpn = instance.getDPN();
		JsonArray parentClocks = dpn.getProperties().getAsJsonArray(PROP_CLOCKS);

		Entity entity = instance.getEntity();
		JsonArray entityClocks = entity.getProperties().getAsJsonArray(PROP_CLOCKS);

		JsonElement clocks = properties.get(PROP_CLOCKS);
		if (clocks == null) {
			clocks = makeClocksObject(entityClocks, parentClocks);
			properties.add(PROP_CLOCKS, clocks);
		} else {
			if (clocks.isJsonArray()) {
				if (checkClockArray(clocks)) {
					clocks = makeClocksObject(entityClocks, clocks.getAsJsonArray());
					properties.add(PROP_CLOCKS, clocks);
				} else {
					// do not check clocks
					return;
				}
			} else if (!clocks.isJsonObject()) {
				// do not check clocks
				addError(clocks, "clocks must be an array or an object");
				return;
			}
		}

		validateClocks(clocks.getAsJsonObject(), parentClocks, entityClocks);
	}

	/**
	 * Validates the given clocks object.
	 * 
	 * @param clocks
	 *            a clocks object
	 * @param parentClocks
	 *            a list of parent clocks
	 * @param entityClocks
	 *            a list of entity clocks
	 */
	private void validateClocks(JsonObject clocks, JsonArray parentClocks, JsonArray entityClocks) {
		int size = entityClocks.size();
		int got = 0;

		Iterator<JsonElement> it = entityClocks.iterator();
		for (Entry<String, JsonElement> pair : clocks.entrySet()) {
			String clockName = pair.getKey();
			if (NO_CLOCK.equals(clockName)) {
				// no more clocks after this one => mismatch in number of clocks
				got = clocks.entrySet().size();
				break;
			}

			if (!it.hasNext()) {
				// no more entity clocks => mismatch in number of clocks
				break;
			}

			// check we use a valid entity clock name
			String expected = it.next().getAsString();
			if (!clockName.equals(expected)) {
				addError(clocks, "given clock name '" + clockName
						+ "' does not match entity's clock '" + expected + "'");
			}

			// check value
			JsonElement value = pair.getValue();
			if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
				got++;
				if (!Iterables.contains(parentClocks, value)) {
					addError(value, "given clock name '" + value.getAsString()
							+ "' does not appear in parent's clocks " + parentClocks);
				}
			} else {
				addError(value, "invalid clock value: " + value.toString());
			}
		}

		if (got < size) {
			addError(clocks, "not enough clocks given, expected " + size + " clocks, got " + got);
		} else if (got > size) {
			addError(clocks, "too many clocks given, expected " + size + " clocks, got " + got);
		}
	}

}
