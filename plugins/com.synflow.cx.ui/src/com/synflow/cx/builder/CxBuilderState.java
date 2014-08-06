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
package com.synflow.cx.builder;

import java.util.Collection;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.builder.clustering.ClusteringBuilderState;
import org.eclipse.xtext.builder.clustering.CurrentDescriptions;
import org.eclipse.xtext.builder.impl.BuildData;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsData;

import com.google.common.collect.ImmutableList;
import com.synflow.cx.cx.CxPackage.Literals;

/**
 * This class extends the clustering build state to compute the list of resources to rebuild when a
 * network changes.
 * 
 * @author Matthieu Wipliez
 *
 */
@SuppressWarnings("restriction")
public class CxBuilderState extends ClusteringBuilderState {

	private IResourceDescriptions resourceDescriptions;

	@Override
	protected Collection<Delta> doUpdate(BuildData buildData, ResourceDescriptionsData newData,
			IProgressMonitor monitor) {
		ResourceSet resourceSet = buildData.getResourceSet();
		resourceDescriptions = new CurrentDescriptions(resourceSet, newData, buildData);

		Set<URI> toBeUpdated = buildData.getToBeUpdated();
		for (URI uri : ImmutableList.copyOf(toBeUpdated)) {
			fillUriSet(toBeUpdated, resourceDescriptions.getResourceDescription(uri));
		}

		resourceSet.eAdapters().remove(resourceDescriptions);
		resourceDescriptions = null;

		return super.doUpdate(buildData, newData, monitor);
	}

	private void fillUriSet(Set<URI> resourceUris, IResourceDescription description) {
		for (IReferenceDescription refDesc : description.getReferenceDescriptions()) {
			if (refDesc.getEReference() == Literals.INST__ENTITY) {
				URI uriInstantiable = refDesc.getTargetEObjectUri();
				URI uri = uriInstantiable.trimFragment();
				if (resourceUris.add(uri)) {
					fillUriSet(resourceUris, resourceDescriptions.getResourceDescription(uri));
				}
			}
		}
	}
}
