/*******************************************************************************
 * Copyright (c) 2012-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.resource;

import java.util.Collection;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.resource.IResourceDescription.Manager.AllChangeAware;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionManager;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.synflow.cx.cx.CxEntity;
import com.synflow.cx.cx.CxPackage.Literals;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Instantiable;
import com.synflow.cx.cx.Network;
import com.synflow.cx.instantiation.IInstantiator;

/**
 * This class describes a resource description manager.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CxResourceDescriptionManager extends DefaultResourceDescriptionManager implements
		AllChangeAware {

	@Inject
	private IInstantiator instantiator;

	@Override
	public boolean isAffectedByAny(Collection<Delta> deltas, IResourceDescription candidate,
			IResourceDescriptions context) throws IllegalArgumentException {
		for (Delta delta : deltas) {
			if (!Iterables.isEmpty(candidate.getExportedObjectsByType(Literals.BUNDLE))) {
				// a candidate is a bundle, is it loaded by the deltas?
				if (isAffected(getImportedNames(delta.getNew()), candidate)) {
					return true;
				}
			}

			// check instantiator to see if necessary to revalidate specialized sub-entities
			IResourceDescription resDesc = delta.getNew();
			for (IEObjectDescription objDesc : resDesc.getExportedObjectsByType(Literals.NETWORK)) {
				CxEntity entity = instantiator.getEntity(objDesc.getEObjectURI());
				if (entity != null) {
					Network network = (Network) entity;
					if (isAffected(network, candidate)) {
						return true;
					}
				}
			}
		}

		return isAffected(deltas, candidate, context);
	}

	private boolean isAffected(Network network, IResourceDescription candidate) {
		for (Inst inst : network.getInstances()) {
			Instantiable entity = inst.getEntity();
			URI uri = EcoreUtil.getURI(entity);
			if (candidate.getURI().equals(uri.trimFragment())) {
				// candidate is being instantiated
				if (instantiator.isSpecialized(uri)) {
					return true;
				}
			}
		}
		return false;
	}

}
