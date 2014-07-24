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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.scoping.IGlobalScopeProvider;
import org.eclipse.xtext.scoping.IScope;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
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

	private Entity entity;

	@Inject
	private EntityMapper entityMapper;

	@Inject
	private IGlobalScopeProvider globalScopeProvider;

	private Map<Entity, Map<EObject, EObject>> mapCxToIr;

	private Multimap<NamedEntity, Entity> mapEntities;

	@Inject
	private IResourceDescriptions resourceDescriptions;

	public InstantiatorImpl() {
		entities = new ArrayList<>();
		mapCxToIr = new HashMap<>();
		mapEntities = LinkedHashMultimap.create();
	}

	private NamedEntity findTopFrom(Resource resource) {
		for (IResourceDescription resDesc : resourceDescriptions.getAllResourceDescriptions()) {

		}

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

	@Override
	public Iterable<Entity> getEntities() {
		List<Entity> result = entities;
		entities = new ArrayList<>();
		return result;
	}

	@Override
	public Iterable<Entity> getEntities(NamedEntity cxEntity) {
		return mapEntities.get(cxEntity);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends EObject, U extends EObject> U getMapping(T cxObj) {
		return (U) mapCxToIr.get(entity).get(cxObj);
	}

	private void instantiateFrom(Inst inst, Node parent) {
		EntityInfo info = entityMapper.getEntityInfo(inst);
		Entity entity = info.loadEntity();
		if (entity == null) {
			entity = entityMapper.createEntity(info);
			mapEntities.put(info.getCxEntity(), entity);
		}

		if (inst.getEntity() instanceof Network) {
			// Network network = (Network) inst.getEntity();
			// for (Inst inst : network.getInstances()) {
			// instantiateFrom(inst, node);
			// }
		}
	}

	private void instantiateFrom(NamedEntity cxEntity, Node parent) {
		EntityInfo info = entityMapper.getEntityInfo(cxEntity);
		Entity entity = info.loadEntity();
		if (entity == null) {
			entity = entityMapper.createEntity(info);
			mapEntities.put(info.getCxEntity(), entity);
		}

		Node node = new Node(parent, info);
		if (cxEntity instanceof Network) {
			Network network = (Network) cxEntity;
			for (Inst inst : network.getInstances()) {
				instantiateFrom(inst, node);
			}
		}
	}

	private void instantiateFromTop(NamedEntity entity) {
		instantiateFrom(entity, null);
	}

	@Override
	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	@Override
	public void update(Resource resource) {
		NamedEntity top = findTopFrom(resource);
		instantiateFromTop(top);
	}

}
