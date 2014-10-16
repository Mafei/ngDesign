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
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof AbstractTreeElement)) {
			return false;
		}
		AbstractTreeElement other = (AbstractTreeElement) obj;
		return resource.equals(other.resource);
	}

	@Override
	public IResource getResource() {
		return resource;
	}

	@Override
	public int hashCode() {
		return resource.hashCode();
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
