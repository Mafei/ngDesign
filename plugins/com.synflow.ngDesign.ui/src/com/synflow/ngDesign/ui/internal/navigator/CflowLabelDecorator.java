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
package com.synflow.ngDesign.ui.internal.navigator;

import static com.synflow.core.ISynflowConstants.FILE_EXT_CFLOW;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.common.collect.Iterables;
import com.synflow.cx.cx.Module;
import com.synflow.cx.cx.Network;
import com.synflow.cx.ui.internal.CxActivator;
import com.synflow.models.util.EcoreHelper;

/**
 * This class defines a label decorator.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CflowLabelDecorator implements ILightweightLabelDecorator {

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IFile) {
			IFile file = (IFile) element;
			if (FILE_EXT_CFLOW.equals(file.getFileExtension())) {
				ResourceSet set = new XtextResourceSet();
				Module module = EcoreHelper.getEObject(set, file);
				if (module != null) {
					Iterable<Network> networks = Iterables.filter(module.getEntities(),
							Network.class);
					if (!Iterables.isEmpty(networks)) {
						ImageDescriptor descriptor = CxActivator.imageDescriptorFromPlugin(
								"com.synflow.cx.ui", "icons/overlay_network.png");
						decoration.addOverlay(descriptor);
					}
				}
			}
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

}
