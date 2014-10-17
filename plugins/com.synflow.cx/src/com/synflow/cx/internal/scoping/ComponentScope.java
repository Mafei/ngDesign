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
package com.synflow.cx.internal.scoping;

import static com.synflow.core.ISynflowConstants.FILE_EXT_CX;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.AbstractScope;

/**
 * This class implements a scoping for built-in components.
 * 
 * @author Matthieu Wipliez
 */
public class ComponentScope extends AbstractScope {

	private static final Map<String, URI> uriMap = new HashMap<>();

	static {
		// registers built-in components in the 'fifo' package
		// synchronous FIFO
		uriMap.put("std.fifo.SynchronousFIFO", createURI("Fifo", 0));

		// registers built-in components in the 'lib' package
		// synchronizer FF and synchronizer Mux
		uriMap.put("std.lib.SynchronizerFF", createURI("Lib", 0));
		uriMap.put("std.lib.SynchronizerMux", createURI("Lib", 1));

		// registers built-in components in the 'mem' package
		// single-port RAM, dual-port RAM, pseudo dual-port RAM
		uriMap.put("std.mem.SinglePortRAM", createURI("Mem", 0));
		uriMap.put("std.mem.DualPortRAM", createURI("Mem", 1));
		uriMap.put("std.mem.PseudoDualPortRAM", createURI("Mem", 2));
	}

	private static URI createURI(String name, int index) {
		String pathName = "com.synflow.cx/model/" + name + "." + FILE_EXT_CX;
		URI uri = URI.createPlatformPluginURI(pathName, true);
		return uri.appendFragment("//@entities." + index);
	}

	private ResourceSet resourceSet;

	public ComponentScope(IScope parent, ResourceSet resourceSet) {
		super(parent, false);
		this.resourceSet = resourceSet;
	}

	@Override
	protected Iterable<IEObjectDescription> getAllLocalElements() {
		return Collections.emptySet();
	}

	@Override
	public Iterable<IEObjectDescription> getElements(QualifiedName name) {
		// we only have one description
		IEObjectDescription result = getSingleElement(name);
		if (result != null)
			return singleton(result);
		return emptySet();
	}

	@Override
	public IEObjectDescription getSingleElement(QualifiedName name) {
		URI uri = uriMap.get(name.toString());
		if (uri != null) {
			EObject eObject = resourceSet.getEObject(uri, true);
			return new EObjectDescription(name, eObject, null);
		}

		// no parent scope anyway so we simply return null
		return null;
	}

}
