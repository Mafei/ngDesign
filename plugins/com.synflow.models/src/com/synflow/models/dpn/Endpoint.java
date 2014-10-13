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

import java.util.Objects;

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
		Objects.requireNonNull(dpn, "dpn must not be null in Endpoint");
		Objects.requireNonNull(port, "port must not be null in Endpoint");

		if (port.eContainer() != dpn) {
			throw new IllegalArgumentException("port must be contained in dpn");
		}

		this.instance = null;
		this.port = port;
	}

	public Endpoint(Instance instance, Port port) {
		Objects.requireNonNull(instance, "instance must not be null in Endpoint");
		Objects.requireNonNull(port, "port must not be null in Endpoint");

		this.instance = instance;
		this.port = port;
	}

	@Override
	public boolean equals(Object anObject) {
		if (!(anObject instanceof Endpoint)) {
			return false;
		}

		Endpoint endpoint = (Endpoint) anObject;
		return Objects.equals(instance, endpoint.instance) && Objects.equals(port, endpoint.port);
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

	@Override
	public int hashCode() {
		if (instance == null) {
			return port.hashCode();
		}
		return instance.hashCode() ^ port.hashCode();
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
