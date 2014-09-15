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
import org.eclipse.xtext.builder.clustering.ClusteringBuilderState;
import org.eclipse.xtext.builder.clustering.CurrentDescriptions;
import org.eclipse.xtext.builder.impl.BuildData;
import org.eclipse.xtext.builder.impl.QueuedBuildData;
import org.eclipse.xtext.builder.impl.ToBeBuilt;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.resource.IResourceDescriptions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
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

	/**
	 * This class defines an ordered ToBeBuilt implementation that uses a linked hash set to store
	 * URIs to be built in the order in which they are added.
	 * 
	 * @author Matthieu Wipliez
	 *
	 */
	private static class ToBeBuiltOrdered extends ToBeBuilt {

		private Set<URI> myToBeUpdated;

		public ToBeBuiltOrdered(BuildData buildData) {
			myToBeUpdated = Sets.newLinkedHashSet(buildData.getToBeUpdated());
		}

		@Override
		public Set<URI> getToBeUpdated() {
			return myToBeUpdated;
		}
	}

	@Inject
	private QueuedBuildData queuedBuildData;

	/**
	 * Using the given IResourceDescriptions, adds to toBeUpdated all resources that are
	 * transitively instantiated by entities in the resource with the given URI.
	 * 
	 * @param newState
	 *            instance of IResourceDescriptions
	 * @param toBeUpdated
	 *            set of resource URIs
	 * @param uri
	 *            a resource URI
	 */
	private void fillUriSet(IResourceDescriptions newState, Set<URI> toBeUpdated, URI uri) {
		// just ignore resource descriptions that are not yet loaded
		IResourceDescription description = newState.getResourceDescription(uri);
		if (description == null) {
			return;
		}

		for (IReferenceDescription refDesc : description.getReferenceDescriptions()) {
			if (refDesc.getEReference() == Literals.INST__ENTITY) {
				URI uriInstantiable = refDesc.getTargetEObjectUri().trimFragment();
				if (toBeUpdated.add(uriInstantiable)) {
					fillUriSet(newState, toBeUpdated, uriInstantiable);
				}
			}
		}

		// add other references to variables
		// will add bundles to the list of resources to be updated
		for (IReferenceDescription refDesc : description.getReferenceDescriptions()) {
			if (refDesc.getEReference() == Literals.VAR_REF__VARIABLE) {
				toBeUpdated.add(refDesc.getTargetEObjectUri().trimFragment());
			}
		}
	}

	@Override
	protected void queueAffectedResources(Set<URI> allRemainingURIs,
			IResourceDescriptions oldState, CurrentDescriptions newState,
			Collection<Delta> changedDeltas, Collection<Delta> allDeltas, BuildData buildData,
			final IProgressMonitor monitor) {
		// temporary fix: don't queue affected resources because it messes up instantiation

		// this means that we potentially won't properly regenerate entities when a bundle changes
		// that's ok for now, all this should be replaced anyway, probably using the instantiator to
		// track dependencies
	}

	@Override
	protected void writeNewResourceDescriptions(BuildData buildData,
			IResourceDescriptions oldState, CurrentDescriptions newState,
			final IProgressMonitor monitor) {
		// we create a new build data that uses our ToBeBuiltOrdered
		// so we retain the order of URIs (top down from the hierarchy)
		buildData = new BuildData(buildData.getProjectName(), buildData.getResourceSet(),
				new ToBeBuiltOrdered(buildData), queuedBuildData);

		// add dependent resources to the toBeUpdated set
		// a better solution in the future would be to use information computed by the instantiator
		// and store it in the IEObjectDescription
		Set<URI> toBeUpdated = buildData.getToBeUpdated();
		for (URI uri : ImmutableList.copyOf(toBeUpdated)) {
			fillUriSet(newState, toBeUpdated, uri);
		}

		super.writeNewResourceDescriptions(buildData, oldState, newState, monitor);
	}

}
