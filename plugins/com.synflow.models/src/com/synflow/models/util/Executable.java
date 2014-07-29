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
package com.synflow.models.util;

/**
 * This interface defines an executable piece of code that takes an argument.
 * 
 * @author Matthieu Wipliez
 *
 * @param <T>
 */
public interface Executable<T> {

	void exec(T argument);

}
