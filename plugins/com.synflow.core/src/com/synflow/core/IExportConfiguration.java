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
package com.synflow.core;

/**
 * This interface defines an export configuration, i.e. what is necessary for proper simulation,
 * synthesis, etc. for a given language.
 * 
 * @author Matthieu Wipliez
 * 
 */
public interface IExportConfiguration {

	enum Target {
		SIMULATION, SYNTHESIS
	}

	Iterable<String> getRootDependencies(Target target);

}
