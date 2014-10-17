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
import org.eclipse.xtext.ui.containers.WorkspaceProjectsStateHelper;

/**
 * This class defines a project state helper for Cx.
 * 
 * @author Matthieu Wipliez
 *
 */
public class CxProjectsStateHelper extends WorkspaceProjectsStateHelper {

	@Override
	public Collection<URI> initContainedURIs(String containerHandle) {
		return super.initContainedURIs(containerHandle);
	}

	@Override
	public String initHandle(URI uri) {
		return super.initHandle(uri);
	}

	@Override
	public List<String> initVisibleHandles(String handle) {
		return super.initVisibleHandles(handle);
	}

}
