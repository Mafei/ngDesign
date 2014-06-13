/*******************************************************************************
 * Copyright (c) 2012 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.core;

import com.synflow.models.dpn.Entity;

/**
 * This interface defines an exporter.
 * 
 * @author Matthieu Wipliez
 * 
 */
public interface IExporter {

	/**
	 * Runs this exporter on the given entity.
	 * 
	 * @param entity
	 *            an entity
	 */
	void export(Entity entity);

}
