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
package com.synflow.models.dpn;

import com.synflow.models.graph.Vertex;

/**
 * This class defines an endpoint.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class Endpoint {

	private final Instance instance;

	private final Port port;

	public Endpoint(DPN dpn, Port port) {
		if (port.eContainer() != dpn) {
			throw new IllegalArgumentException("port must be contained in dpn");
		}

		this.instance = null;
		this.port = port;
	}

	public Endpoint(Instance instance, Port port) {
		this.instance = instance;
		this.port = port;
	}

	public Instance getInstance() {
		return instance;
	}

	public Port getPort() {
		return port;
	}

	public Vertex getVertex() {
		if (instance == null) {
			DPN dpn = (DPN) port.eContainer();
			return dpn.getVertex();
		} else {
			return instance;
		}
	}

	/**
	 * Equivalent to <code>getInstance() != null</code>
	 * 
	 * @return a boolean
	 */
	public boolean hasInstance() {
		return instance != null;
	}

	@Override
	public String toString() {
		if (hasInstance()) {
			return instance.getName() + "." + port.getName();
		} else {
			return port.getName();
		}
	}

}
