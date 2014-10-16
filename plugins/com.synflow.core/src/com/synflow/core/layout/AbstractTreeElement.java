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
package com.synflow.core.layout;

import org.eclipse.core.resources.IResource;

public abstract class AbstractTreeElement implements ITreeElement {

	private IResource resource;

	public AbstractTreeElement(IResource resource) {
		this.resource = resource;
	}

	@Override
	public IResource getResource() {
		return resource;
	}

	@Override
	public boolean isPackage() {
		return false;
	}

	@Override
	public boolean isSourceFolder() {
		return false;
	}

}
