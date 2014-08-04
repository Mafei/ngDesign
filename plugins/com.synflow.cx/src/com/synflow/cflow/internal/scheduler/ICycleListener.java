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
package com.synflow.cflow.internal.scheduler;

/**
 * This class defines a cycle listener. It has a single method, called when a new cycle has started.
 * 
 * @author Matthieu Wipliez
 * 
 */
public interface ICycleListener {

	/**
	 * This method is called when a new cycle is being started.
	 */
	void newCycleStarted();

}
