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
package com.synflow.cx.ui.containers;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.ui.containers.WorkspaceProjectsState;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This class defines a project state for Cx.
 * 
 * @author Matthieu Wipliez
 *
 */
@Singleton
public class CxProjectsState extends WorkspaceProjectsState {

	private CxProjectsStateHelper cxHelper;

	@Inject
	public CxProjectsState(CxProjectsStateHelper helper) {
		this.cxHelper = helper;
	}

	@Override
	protected Collection<URI> doInitContainedURIs(String containerHandle) {
		return cxHelper.initContainedURIs(containerHandle);
	}

	@Override
	protected String doInitHandle(URI uri) {
		return cxHelper.initHandle(uri);
	}

	@Override
	protected List<String> doInitVisibleHandles(String handle) {
		return cxHelper.initVisibleHandles(handle);
	}

}
