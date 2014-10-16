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
package com.synflow.ngDesign.ui.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IContributorResourceAdapter;

import com.synflow.core.layout.ITreeElement;

/**
 * This class defines an adapter factory.
 * 
 * @author Matthieu Wipliez
 *
 */
@SuppressWarnings("rawtypes")
public class AdapterFactory implements IAdapterFactory, IContributorResourceAdapter {

	@Override
	public IResource getAdaptedResource(IAdaptable adaptable) {
		ITreeElement element = getTreeElement(adaptable);
		if (element != null) {
			return element.getResource();
		}
		return null;
	}

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		ITreeElement element = getTreeElement(adaptableObject);

		if (IResource.class.equals(adapterType)) {
			return element.getResource();
		} else if (IContributorResourceAdapter.class.equals(adapterType)) {
			return this;
		}

		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] { IResource.class, IContributorResourceAdapter.class };
	}

	private ITreeElement getTreeElement(Object object) {
		if (object instanceof ITreeElement) {
			return (ITreeElement) object;
		}
		return null;
	}

}
