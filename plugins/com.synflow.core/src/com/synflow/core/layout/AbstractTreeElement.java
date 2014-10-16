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
import org.eclipse.core.runtime.PlatformObject;

/**
 * This class defines an abstract tree element, adaptable to IResource.
 * 
 * @author Matthieu Wipliez
 *
 */
public abstract class AbstractTreeElement extends PlatformObject implements ITreeElement {

	private IResource resource;

	public AbstractTreeElement(IResource resource) {
		this.resource = resource;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (IResource.class.isAssignableFrom(adapter)) {
			return getResource();
		}
		return super.getAdapter(adapter);
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
