/*******************************************************************************
 * Copyright (c) 2013-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - cleaning/rewrite
 *******************************************************************************/
/*
 * Copyright (c) 2009-2011, IETR/INSA of Rennes
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the IETR/INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.synflow.models.dpn;

import java.util.List;

import org.eclipse.emf.common.util.EList;

import com.synflow.models.graph.Graph;
import com.synflow.models.graph.Vertex;

/**
 * <!-- begin-user-doc -->This class defines a hierarchical XDF network. It extends both entity and
 * graph.<!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link com.synflow.models.dpn.DPN#getGraph <em>Graph</em>}</li>
 * <li>{@link com.synflow.models.dpn.DPN#getInstances <em>Instances</em>}</li>
 * <li>{@link com.synflow.models.dpn.DPN#getVertex <em>Vertex</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.synflow.models.dpn.DpnPackage#getDPN()
 * @model
 * @generated
 */
public interface DPN extends Entity {

	/**
	 * Adds the given instance to this network.
	 * 
	 * @param instance
	 *            an instance
	 */
	void add(Instance instance);

	/**
	 * @model containment="true"
	 * @generated
	 */
	Graph getGraph();

	/**
	 * Returns the incoming connection of the given instance.
	 * 
	 * @param instance
	 *            an instance
	 * @return a list of connections (never <code>null</code>)
	 */
	List<Connection> getIncoming(Instance instance);

	/**
	 * Returns the incoming endpoint of the given instance and input port.
	 * 
	 * @param instance
	 *            an instance
	 * @param port
	 *            an input port of the instance
	 * @return an incoming endpoint (may be <code>null</code>)
	 */
	Endpoint getIncoming(Instance instance, Port port);

	/**
	 * Returns the incoming endpoint of the given output port.
	 * 
	 * @param port
	 *            an output port
	 * @return an incoming endpoint (may be <code>null</code>)
	 */
	Endpoint getIncoming(Port port);

	List<Connection> getIncomingConnections(Port port);

	/**
	 * Returns the value of the '<em><b>Instances</b></em>' reference list. The list contents are of
	 * type {@link com.synflow.models.dpn.Instance}. <!-- begin-user-doc --><!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Instances</em>' reference list.
	 * @see com.synflow.models.dpn.DpnPackage#getDPN_Instances()
	 * @model
	 * @generated
	 */
	EList<Instance> getInstances();

	/**
	 * Returns the number of incoming connections of the given input port of the given instance.
	 * 
	 * @param instance
	 *            an instance of this dpn
	 * @param port
	 *            an input port of <code>instance</code>
	 * @return an int
	 */
	int getNumIncoming(Instance instance, Port port);

	/**
	 * Returns the number of incoming connections of the given output port.
	 * 
	 * @param port
	 *            an output port of this dpn
	 * @return an int
	 */
	int getNumIncoming(Port port);

	/**
	 * Returns a list of endpoints outgoing from the given endpoint.
	 * 
	 * @param endpoint
	 *            an endpoint
	 * @return a list of endpoints (never <code>null</code>)
	 */
	List<Endpoint> getOutgoing(Endpoint endpoint);

	/**
	 * Returns the outgoing connection of the given instance.
	 * 
	 * @param instance
	 *            an instance
	 * @return a list of connections (never <code>null</code>)
	 */
	List<Connection> getOutgoing(Instance instance);

	/**
	 * Returns the list of endpoints outgoing of the given input port.
	 * 
	 * @param port
	 *            an input port
	 * @return a list of connections (never <code>null</code>)
	 */
	List<Connection> getOutgoing(Port port);

	/**
	 * Returns the value of the '<em><b>Vertex</b></em>' reference. <!-- begin-user-doc -->Returns
	 * the vertex associated with this DPN itself.<!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Vertex</em>' reference.
	 * @see #setVertex(Vertex)
	 * @see com.synflow.models.dpn.DpnPackage#getDPN_Vertex()
	 * @model
	 * @generated
	 */
	Vertex getVertex();

	void init();

	/**
	 * Sets the value of the '{@link com.synflow.models.dpn.DPN#getGraph <em>Graph</em>}'
	 * containment reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Graph</em>' containment reference.
	 * @see #getGraph()
	 * @generated
	 */
	void setGraph(Graph value);

	/**
	 * Sets the value of the '{@link com.synflow.models.dpn.DPN#getVertex <em>Vertex</em>}'
	 * reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Vertex</em>' reference.
	 * @see #getVertex()
	 * @generated
	 */
	void setVertex(Vertex value);

}
