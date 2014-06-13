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
package com.synflow.cflow.internal.instantiation.properties;

import com.google.gson.JsonElement;

/**
 * This interface defines a method to report an error with a given JSON element.
 * 
 * @author Matthieu Wipliez
 *
 */
public interface IJsonErrorHandler {

	/**
	 * Adds an error message caused by the given JSON element.
	 * 
	 * @param element
	 *            the JSON element
	 * @param message
	 *            a message
	 */
	void addError(JsonElement element, String message);

}
