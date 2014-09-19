/*******************************************************************************
 * Copyright (c) 2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.internal.scoping;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.DefaultGlobalScopeProvider;

import com.google.common.base.Predicate;

/**
 * This class extends the default global scope provider with a parent scope that knows about
 * built-in components.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CxGlobalScopeProvider extends DefaultGlobalScopeProvider {

	@Override
	protected IScope getScope(final Resource resource, boolean ignoreCase, EClass type,
			Predicate<IEObjectDescription> filter) {
		ComponentScope parent = new ComponentScope(IScope.NULLSCOPE, resource.getResourceSet());
		return getScope(parent, resource, ignoreCase, type, filter);
	}

}
