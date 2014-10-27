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

import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.IScopeProvider;

import com.google.inject.Inject;
import com.synflow.cx.cx.CxPackage;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.VarRef;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.instantiation.IInstantiator;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Endpoint;
import com.synflow.models.dpn.Instance;
import com.synflow.models.dpn.Port;

/**
 * This class defines an helper class that creates Connections.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ConnectorHelper {

	@Inject
	private IQualifiedNameConverter converter;

	@Inject
	private IInstantiator instantiator;

	@Inject
	private IScopeProvider scopeProvider;

	/**
	 * Returns the endpoint associated with the port associated with the given reference.
	 * 
	 * @param ref
	 *            reference to a port in another instance or in the containing network
	 * @return an endpoint
	 */
	public Endpoint getEndpoint(DPN dpn, VarRef ref) {
		Variable cxPort = ref.getVariable();
		Instance otherInst = getInstance(dpn, ref);
		if (otherInst == null) {
			Port otherPort = instantiator.getMapping(dpn, cxPort);
			return new Endpoint(dpn, otherPort);
		} else {
			Port otherPort = instantiator.getMapping(otherInst.getEntity(), cxPort);
			return new Endpoint(otherInst, otherPort);
		}
	}

	/**
	 * If the given port reference refers to a port in an instance, returns that instance.
	 * Otherwise, if the reference is that of a simple port (no instance), returns null.
	 * 
	 * @param ref
	 *            a port reference
	 * @return an instance
	 */
	public Inst getInst(VarRef ref) {
		String link = NodeModelUtils.getTokenText(NodeModelUtils.getNode(ref));
		QualifiedName name = converter.toQualifiedName(link);
		if (name.getSegmentCount() == 1) {
			return null;
		}

		IScope scope = scopeProvider.getScope(ref, CxPackage.Literals.CONNECT__INSTANCE);
		QualifiedName qualifiedLinkName = converter.toQualifiedName(name.getFirstSegment());
		IEObjectDescription eObjectDescription = scope.getSingleElement(qualifiedLinkName);

		return (Inst) eObjectDescription.getEObjectOrProxy();
	}

	/**
	 * If the given port reference refers to a port in an instance, returns that instance.
	 * Otherwise, if the reference is that of a simple port (no instance), returns null.
	 * 
	 * @param dpn
	 *            dpn
	 * @param ref
	 *            a port reference
	 * @return an instance
	 */
	public Instance getInstance(DPN dpn, VarRef ref) {
		Inst inst = getInst(ref);
		return instantiator.getMapping(dpn, inst);
	}

}
