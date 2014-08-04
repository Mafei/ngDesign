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
package com.synflow.cx.internal.instantiation.properties;

import java.util.IdentityHashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.synflow.cx.cx.Array;
import com.synflow.cx.cx.Element;
import com.synflow.cx.cx.ExpressionBoolean;
import com.synflow.cx.cx.ExpressionFloat;
import com.synflow.cx.cx.ExpressionInteger;
import com.synflow.cx.cx.ExpressionString;
import com.synflow.cx.cx.Null;
import com.synflow.cx.cx.Obj;
import com.synflow.cx.cx.Pair;
import com.synflow.cx.cx.Primitive;
import com.synflow.cx.cx.util.CxSwitch;
import com.synflow.cx.internal.ErrorMarker;

/**
 * This class transforms our Javascript-like syntax to pure JSON.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class JsonMaker extends CxSwitch<JsonElement> {

	private Map<JsonElement, ErrorMarker> mapping;

	public JsonMaker() {
		mapping = new IdentityHashMap<>();
	}

	@Override
	public JsonArray caseArray(Array array) {
		JsonArray jsonArray = new JsonArray();
		mapping.put(jsonArray, new ErrorMarker(array));
		for (Element element : array.getElements()) {
			jsonArray.add(doSwitch(element));
		}
		return jsonArray;
	}

	@Override
	public JsonPrimitive caseExpressionBoolean(ExpressionBoolean expression) {
		JsonPrimitive primitive = new JsonPrimitive(expression.isValue());
		mapping.put(primitive, new ErrorMarker(expression));
		return primitive;
	}

	@Override
	public JsonPrimitive caseExpressionFloat(ExpressionFloat expression) {
		JsonPrimitive primitive = new JsonPrimitive(expression.getValue());
		mapping.put(primitive, new ErrorMarker(expression));
		return primitive;
	}

	@Override
	public JsonPrimitive caseExpressionInteger(ExpressionInteger expression) {
		JsonPrimitive primitive = new JsonPrimitive(expression.getValue());
		mapping.put(primitive, new ErrorMarker(expression));
		return primitive;
	}

	@Override
	public JsonPrimitive caseExpressionString(ExpressionString string) {
		JsonPrimitive primitive = new JsonPrimitive(string.getValue());
		mapping.put(primitive, new ErrorMarker(string));
		return primitive;
	}

	@Override
	public JsonNull caseNull(Null null_) {
		return JsonNull.INSTANCE;
	}

	@Override
	public JsonObject caseObj(Obj obj) {
		JsonObject jsonObj = new JsonObject();
		mapping.put(jsonObj, new ErrorMarker(obj));
		for (Pair pair : obj.getMembers()) {
			Element value = pair.getValue();
			if (value != null) {
				String key = pair.getKey();
				jsonObj.add(key, doSwitch(value));
			}
		}
		return jsonObj;
	}

	@Override
	public JsonElement casePrimitive(Primitive primitive) {
		return doSwitch(primitive.getValue());
	}

	/**
	 * Returns the error marker that corresponds to the given JSON element.
	 * 
	 * @param element
	 *            a JSON element
	 * @return an error marker
	 */
	public ErrorMarker getMapping(JsonElement element) {
		return mapping.get(element);
	}

	/**
	 * Transforms the given object to JSON.
	 * 
	 * @param obj
	 *            an object
	 * @return a JSON object
	 */
	public JsonObject toJson(Obj obj) {
		return (JsonObject) doSwitch(obj);
	}

}
