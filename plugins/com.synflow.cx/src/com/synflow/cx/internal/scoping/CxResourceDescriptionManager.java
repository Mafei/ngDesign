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
package com.synflow.cx.internal.scoping;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionManager;

import com.google.inject.Inject;
import com.synflow.cx.cx.CxEntity;
import com.synflow.cx.cx.CxPackage.Literals;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Instantiable;
import com.synflow.cx.cx.Network;
import com.synflow.cx.internal.instantiation.IInstantiator;
import com.synflow.cx.internal.instantiation.InstantiationContext;
import com.synflow.cx.internal.instantiation.InstantiatorData;
import com.synflow.models.dpn.Entity;
import com.synflow.models.node.Node;

/**
 * This class defines a resource description manager that overrides the default resource description
 * manager to handle hierarchical dependency management.
 * 
 * @author Matthieu
 *
 */
public class CxResourceDescriptionManager extends DefaultResourceDescriptionManager {

	@Inject
	private IInstantiator instantiator;

	private Set<URI> resourceUris;

	private void computeSet(InstantiationContext ctx) {
		Inst inst = ctx.getInst();
		if (inst != null) {
			Instantiable instantiable = inst.getEntity();
			if (instantiable == null) {
				instantiable = inst.getTask();
			}
			resourceUris.add(instantiable.eResource().getURI());
		}

		for (Node child : ctx.getChildren()) {
			computeSet((InstantiationContext) child);
		}
	}

	@Override
	public IResourceDescription getResourceDescription(Resource resource) {
		return super.getResourceDescription(resource);
	}

	@Override
	public boolean isAffected(Collection<Delta> deltas, IResourceDescription candidate,
			IResourceDescriptions context) {
		InstantiatorData data = instantiator.getData();

		boolean hasBundlesOnly = true, mustUpdateSet = false;
		for (Delta delta : deltas) {
			IResourceDescription desc = delta.getNew();
			if (desc == null) {
				desc = delta.getOld();
			}

			for (IEObjectDescription objDesc : desc.getExportedObjectsByType(Literals.CX_ENTITY)) {
				EClass eClass = objDesc.getEClass();
				if (eClass != Literals.BUNDLE) {
					hasBundlesOnly = false;
					if (eClass == Literals.NETWORK) {
						Network network = (Network) objDesc.getEObjectOrProxy();
						if (!data.isAssociated(network)) {
							mustUpdateSet = true;
						}
					}
				}
			}
		}

		if (hasBundlesOnly) {
			return super.isAffected(deltas, candidate, context);
		} else if (mustUpdateSet) {
			resourceUris = new HashSet<>();
			for (Delta delta : deltas) {
				IResourceDescription desc = delta.getNew();
				if (desc == null) {
					desc = delta.getOld();
				}

				for (IEObjectDescription obj : desc.getExportedObjectsByType(Literals.CX_ENTITY)) {
					CxEntity cxEntity = (CxEntity) obj.getEObjectOrProxy();
					Map<InstantiationContext, Entity> map = data.getPreviousAssociation(cxEntity);
					for (InstantiationContext ctx : map.keySet()) {
						computeSet(ctx);
					}
				}
			}
		}

		return resourceUris.contains(candidate.getURI());
	}

}
