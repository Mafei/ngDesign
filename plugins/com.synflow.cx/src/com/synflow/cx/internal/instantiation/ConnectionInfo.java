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

import static com.synflow.cx.CxConstants.TYPE_READS;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Multimap;
import com.synflow.cx.cx.Connect;
import com.synflow.cx.instantiation.IInstantiator;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Instance;
import com.synflow.models.dpn.Port;

/**
 * This class holds information about a connect statement.
 * 
 * @author Matthieu Wipliez
 *
 */
public class ConnectionInfo implements Iterable<Port> {

	private final Instance instance;

	private final String name;

	private final Collection<Port> ports;

	public ConnectionInfo(IInstantiator instantiator, Multimap<EObject, Port> portMap, DPN dpn,
			Connect connect) {
		if (connect.isThis()) {
			instance = null;
		} else {
			instance = instantiator.getMapping(dpn, connect.getInstance());
		}

		name = instance == null ? "this" : instance.getName();
		ports = getPorts(portMap, dpn, connect.getType());
	}

	public Instance getInstance() {
		return instance;
	}

	public String getName() {
		return name;
	}

	public int getNumPorts() {
		return ports.size();
	}

	private Collection<Port> getPorts(Multimap<EObject, Port> portMap, DPN dpn, String type) {
		if (instance == null) {
			if (TYPE_READS.equals(type)) {
				return portMap.get(dpn);
			} else { // TYPE_WRITES
				return dpn.getInputs();
			}
		} else {
			Entity entity = instance.getEntity();
			if (TYPE_READS.equals(type)) {
				return portMap.get(instance);
			} else { // TYPE_WRITES
				return entity.getOutputs();
			}
		}
	}

	@Override
	public Iterator<Port> iterator() {
		return ports.iterator();
	}

	@Override
	public String toString() {
		return name;
	}

}
