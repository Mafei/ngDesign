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
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.synflow.cflow.cflow.CflowPackage.Literals;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.Instantiable;
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.cflow.cflow.Network;
import com.synflow.cflow.internal.instantiation.properties.PropertiesSupport;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.DpnFactory;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Instance;

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

	private Map<Entity, Map<EObject, EObject>> mapCxToIr;

	private Multimap<NamedEntity, Entity> mapEntities;

	@Inject
	private IResourceDescriptions resourceDescriptions;

	public InstantiatorImpl() {
		entities = new ArrayList<>();
		mapCxToIr = new HashMap<>();
		mapEntities = LinkedHashMultimap.create();
	}

	private void connect(Network network, DPN dpn) {
		// TODO Auto-generated method stub

	}

	/**
	 * Creates an instance from the given Inst object.
	 * 
	 * @param inst
	 *            a C~ instance
	 * @return an IR instance
	 */
	private Instance createInstance(DPN dpn, Inst inst, Entity entity) {
		// create instance and adds to DPN
		final Instance instance = DpnFactory.eINSTANCE.createInstance(inst.getName(), entity);
		dpn.add(instance);

		// set properties. For anonymous tasks, use the task's properties for the instance
		PropertiesSupport support;
		if (inst.getTask() == null) {
			support = new PropertiesSupport(inst);
		} else {
			support = new PropertiesSupport(inst.getTask());
		}
		support.setProperties(instance);

		return instance;
	}

	private Iterable<Instantiable> findTopFrom(ResourceSet resourceSet) {
		Set<URI> topUris = Sets.newLinkedHashSet();

		// collect all instantiable entities
		EClass type = Literals.INSTANTIABLE;
		for (IEObjectDescription objDesc : resourceDescriptions.getExportedObjectsByType(type)) {
			topUris.add(objDesc.getEObjectURI());
		}

		// remove all entities that are instantiated
		type = Literals.NETWORK;
		for (IResourceDescription resDesc : resourceDescriptions.getAllResourceDescriptions()) {
			for (IReferenceDescription refDesc : resDesc.getReferenceDescriptions()) {
				if (refDesc.getEReference() == Literals.INST__ENTITY) {
					URI uriInstantiable = refDesc.getTargetEObjectUri();
					topUris.remove(uriInstantiable);
				}
			}
		}

		// loads objects from topUris
		List<Instantiable> instantiables = new ArrayList<>(topUris.size());
		for (URI uri : topUris) {
			URI uriRes = uri.trimFragment();
			IResourceDescription resDesc = resourceDescriptions.getResourceDescription(uriRes);
			type = Literals.INSTANTIABLE;
			for (IEObjectDescription objDesc : resDesc.getExportedObjectsByType(type)) {
				if (uri.equals(objDesc.getEObjectURI())) {
					EObject proxy = objDesc.getEObjectOrProxy();
					EObject resolved = EcoreUtil.resolve(proxy, resourceSet);
					instantiables.add((Instantiable) resolved);
				}
			}
		}
		return instantiables;
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

	private Entity instantiate(EntityInfo info, InstantiationContext ctx) {
		NamedEntity cxEntity = info.getCxEntity();
		Entity entity = info.loadEntity();
		if (entity == null) {
			entity = entityMapper.createEntity(info);
			entities.add(entity);
			mapEntities.put(cxEntity, entity);
		}

		if (cxEntity instanceof Network) {
			Entity oldEntity = setEntity(entity);
			instantiate((Network) cxEntity, ctx);
			setEntity(oldEntity);
		}

		return entity;
	}

	private void instantiate(Network network, InstantiationContext ctx) {
		DPN dpn = (DPN) entity;
		for (Inst inst : network.getInstances()) {
			InstantiationContext subCtx = new InstantiationContext(ctx, inst);
			EntityInfo info = entityMapper.getEntityInfo(inst, subCtx);
			Entity subEntity = instantiate(info, subCtx);

			Instance instance = createInstance(dpn, inst, subEntity);
			putMapping(inst, instance);
		}

		connect(network, dpn);
	}

	@Override
	public <T extends EObject, U extends EObject> void putMapping(T cxObj, U irObj) {
		Map<EObject, EObject> map = mapCxToIr.get(entity);
		if (map == null) {
			map = new HashMap<>();
			mapCxToIr.put(entity, map);
		}
		map.put(cxObj, irObj);
	}

	@Override
	public Entity setEntity(Entity entity) {
		Entity oldEntity = this.entity;
		this.entity = entity;
		return oldEntity;
	}

	@Override
	public void update(ResourceSet resourceSet) {
		for (Instantiable instantiable : findTopFrom(resourceSet)) {
			EntityInfo info = entityMapper.getEntityInfo(instantiable);
			instantiate(info, new InstantiationContext(instantiable.getName()));
		}
	}

}
