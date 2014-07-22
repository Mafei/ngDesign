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
package com.synflow.cflow.internal.instantiation.v2;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsBasedContainer;
import org.eclipse.xtext.scoping.IGlobalScopeProvider;
import org.eclipse.xtext.scoping.IScope;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.synflow.cflow.cflow.CflowPackage.Literals;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.cflow.cflow.Network;
import com.synflow.cflow.internal.scheduler.node.Node;
import com.synflow.models.dpn.Entity;

/**
 * This class defines the default implementation of the instantiator.
 * 
 * @author Matthieu Wipliez
 * 
 */
@Singleton
public class InstantiatorImpl implements IInstantiator {

	private List<Entity> entities;

	@Inject
	private EntityMapper entityMapper;

	@Inject
	private IGlobalScopeProvider globalScopeProvider;

	@Inject
	private ResourceDescriptionsBasedContainer resourceDescriptions;

	public InstantiatorImpl() {
		entities = new ArrayList<>();
	}

	private NamedEntity findTopFrom(NamedEntity entity) {
		Resource resource = entity.eResource();
		IScope scope = globalScopeProvider.getScope(resource, Literals.INST__ENTITY, null);
		for (IEObjectDescription objDesc : scope.getAllElements()) {
			EObject proxy = objDesc.getEObjectOrProxy();
			EObject eObject = EcoreUtil.resolve(proxy, resource);
			EcoreUtil.resolveAll(eObject);

			if (objDesc.getEClass() == Literals.NETWORK) {
				URI uri = objDesc.getEObjectURI().trimFragment();
				IResourceDescription resDesc = resourceDescriptions.getResourceDescription(uri);
				for (IReferenceDescription refDesc : resDesc.getReferenceDescriptions()) {
					URI target = refDesc.getTargetEObjectUri();
				}

				System.out.println(eObject);
			}
		}

		return null;
	}

	public Iterable<Entity> getEntities() {
		List<Entity> result = entities;
		entities = new ArrayList<>();
		return result;
	}

	@Override
	public Iterable<InstInfo> getMappings(NamedEntity entity) {
		Iterable<Node> nodes = getNodes(entity);
		return Iterables.transform(nodes, new Function<Node, InstInfo>() {
			@Override
			public InstInfo apply(Node node) {
				return (InstInfo) node.getContent();
			}
		});
	}

	private Iterable<Node> getNodes(NamedEntity entity) {
		if (!entityMapper.mapCxToNodes.containsKey(entity)) {
			NamedEntity top = findTopFrom(entity);
			if (!entityMapper.mapCxToNodes.containsKey(top)) {
				instantiateFromTop(entity);
			}
		}
		return entityMapper.mapCxToNodes.get(entity);
	}

	private void instantiateFrom(NamedEntity cxEntity, Node parent) {
		Entity entity = entityMapper.getOrCreateEntity(cxEntity);
		InstInfo info = new InstInfo(entity);

		Node node = new Node(parent, info);
		if (cxEntity instanceof Network) {
			Network network = (Network) cxEntity;
			for (Inst inst : network.getInstances()) {
				instantiateFrom(inst, node);
			}
		}
	}

	private void instantiateFrom(Inst inst, Node parent) {
		Entity entity = entityMapper.getOrCreateEntity(inst);
		InstInfo info = new InstInfo(entity);

		// Node node = new Node(parent, info);
		if (inst.getEntity() instanceof Network) {
			// Network network = (Network) inst.getEntity();
			// for (Inst inst : network.getInstances()) {
			// instantiateFrom(inst, node);
			// }
		}
	}

	private void instantiateFromTop(NamedEntity entity) {
		instantiateFrom(entity, null);
	}

}
