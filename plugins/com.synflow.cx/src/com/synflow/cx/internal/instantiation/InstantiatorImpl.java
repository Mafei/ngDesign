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
package com.synflow.cx.internal.instantiation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.synflow.core.util.CoreUtil;
import com.synflow.cx.cx.Bundle;
import com.synflow.cx.cx.Connect;
import com.synflow.cx.cx.CxEntity;
import com.synflow.cx.cx.CxPackage.Literals;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Network;
import com.synflow.cx.cx.VarRef;
import com.synflow.cx.internal.CopyOf;
import com.synflow.cx.internal.instantiation.properties.PropertiesSupport;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.DpnFactory;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Instance;
import com.synflow.models.dpn.Port;
import com.synflow.models.util.Executable;

/**
 * This class defines the default implementation of the instantiator.
 * 
 * @author Matthieu Wipliez
 * 
 */
@Singleton
public class InstantiatorImpl implements IInstantiator {

	private List<Entity> builtins;

	@Inject
	private Provider<ConnectionMaker> connectionMakerProvider;

	private Entity entity;

	@Inject
	private EntityMapper entityMapper;

	private Map<Entity, Map<EObject, EObject>> mapCxToIr;

	private Multimap<CxEntity, Entity> mapEntities;

	@Inject
	private ResourceDescriptionsProvider provider;

	@Inject
	private IResourceServiceProvider.Registry registry;

	public InstantiatorImpl() {
		builtins = new ArrayList<>();
		mapCxToIr = new HashMap<>();
		mapEntities = LinkedHashMultimap.create();
	}

	/**
	 * Creates a new connection maker and connects the given network. Visits inner tasks first, and
	 * then connect statements.
	 * 
	 * @param network
	 * @param dpn
	 */
	private void connect(Network network, DPN dpn) {
		ConnectionMaker maker = connectionMakerProvider.get();
		maker.initialize(dpn);

		// solves references to implicit ports
		ImplicitPortSwitch visitor = new ImplicitPortSwitch(this, maker);
		for (Inst inst : network.getInstances()) {
			Instance instance = getMapping(inst);
			visitor.visitInst(inst, instance);
		}

		for (Connect connect : network.getConnects()) {
			maker.makeConnection(dpn, connect);
		}
	}

	/**
	 * Creates an instance from the given Inst object.
	 * 
	 * @param inst
	 *            a Cx instance
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

	private void execute(Entity entity, Executable<Entity> executable) {
		Entity oldEntity = this.entity;
		this.entity = entity;
		try {
			executable.exec(entity);
		} finally {
			this.entity = oldEntity;
		}
	}

	/**
	 * Finds all CxEntity objects that are at the top of the hierarchy. Computed as the set of URIs
	 * of all entities, minus the set of URIs of entities that are instantiated. Currently the
	 * collection this method returns includes bundles.
	 * 
	 * @param resourceSet
	 *            a resource set from which we obtain an IResourceDescriptions object and that we
	 *            use for solving proxies
	 * @return an iterable over CxEntity
	 */
	private Iterable<CxEntity> findTopFrom(ResourceSet resourceSet) {
		Set<URI> topUris = Sets.newLinkedHashSet();

		IResourceDescriptions resourceDescriptions = provider.getResourceDescriptions(resourceSet);

		// collect all entities (bundles and instantiable entities)
		EClass type = Literals.CX_ENTITY;
		for (IEObjectDescription objDesc : resourceDescriptions.getExportedObjectsByType(type)) {
			topUris.add(objDesc.getEObjectURI());
		}

		// remove all entities that are instantiated
		// we use the registry to get an IResourceDescription because
		// ResourceDescriptionsProvider may return CopiedResourceDescriptions
		// which do not have reference descriptions
		for (IResourceDescription resDesc : resourceDescriptions.getAllResourceDescriptions()) {
			URI uri = resDesc.getURI();
			Resource resource = resourceSet.getResource(uri, false);
			if (resource == null) {
				continue;
			}

			IResourceServiceProvider provider = registry.getResourceServiceProvider(uri);
			IResourceDescription.Manager manager = provider.getResourceDescriptionManager();
			resDesc = manager.getResourceDescription(resource);
			for (IReferenceDescription refDesc : resDesc.getReferenceDescriptions()) {
				if (refDesc.getEReference() == Literals.INST__ENTITY) {
					URI uriInstantiable = refDesc.getTargetEObjectUri();
					topUris.remove(uriInstantiable);
				}
			}
		}

		// loads objects from topUris
		List<CxEntity> entities = new ArrayList<>(topUris.size());
		for (URI uri : topUris) {
			URI uriRes = uri.trimFragment();
			IResourceDescription resDesc = resourceDescriptions.getResourceDescription(uriRes);
			type = Literals.CX_ENTITY;
			for (IEObjectDescription objDesc : resDesc.getExportedObjectsByType(type)) {
				if (uri.equals(objDesc.getEObjectURI())) {
					EObject proxy = objDesc.getEObjectOrProxy();
					EObject resolved = EcoreUtil.resolve(proxy, resourceSet);
					entities.add((CxEntity) resolved);
				}
			}
		}
		return entities;
	}

	@Override
	public void forEachMapping(CxEntity cxEntity, Executable<Entity> executable) {
		Collection<Entity> entities = mapEntities.get(cxEntity);
		for (Entity entity : entities) {
			execute(entity, executable);
		}
	}

	@Override
	public Iterable<Entity> getBuiltins() {
		List<Entity> result = builtins;
		builtins = new ArrayList<>();
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends EObject, U extends EObject> U getMapping(Entity entity, T cxObj) {
		Objects.requireNonNull(entity, "entity must not be null in getMapping");

		Map<EObject, EObject> map = mapCxToIr.get(entity);
		if (map == null) {
			return null;
		}

		U irObj = (U) map.get(cxObj);
		if (irObj == null) {
			CxEntity cxEntity = EcoreUtil2.getContainerOfType(cxObj, CxEntity.class);
			if (cxEntity instanceof Bundle) {
				entity = Iterables.getFirst(mapEntities.get(cxEntity), null);
				if (entity != null) {
					irObj = (U) mapCxToIr.get(entity).get(cxObj);
				}
			}
		}
		return irObj;
	}

	@Override
	public <T extends EObject, U extends EObject> U getMapping(T cxObj) {
		return getMapping(entity, cxObj);
	}

	@Override
	public Port getPort(VarRef refOrCopyOfRef) {
		final VarRef ref = CopyOf.getOriginal(refOrCopyOfRef);

		// first try port in named task/network
		Port port = getMapping(ref.getVariable());
		if (port == null) {
			// otherwise get mapping of reference (anonymous task)
			port = getMapping(ref);
		}
		return port;
	}

	/**
	 * Instantiates a Cx entity based on the given info and instantiation context.
	 * 
	 * @param info
	 *            entity info (URI of IR, name, reference to original Cx entity)
	 * @param ctx
	 *            instantiation context (hierarchical path, inherited properties)
	 * @return a specialized IR entity
	 */
	private Entity instantiate(final EntityInfo info, final InstantiationContext ctx) {
		final CxEntity cxEntity = info.getCxEntity();
		Entity entity = entityMapper.doSwitch(info.getCxEntity());
		execute(entity, new Executable<Entity>() {
			@Override
			public void exec(Entity entity) {
				entityMapper.configureEntity(entity, info, ctx);
			}
		});

		// add mapping, optionally add to builtins
		mapEntities.put(cxEntity, entity);
		if (CoreUtil.isBuiltin(entity)) {
			builtins.add(entity);
		}

		// instantiate network
		if (cxEntity instanceof Network) {
			execute(entity, new Executable<Entity>() {
				@Override
				public void exec(Entity entity) {
					instantiate((Network) cxEntity, ctx);
				}
			});
		}

		return entity;
	}

	/**
	 * Instantiates the given network and its instances recursively, and connects the network.
	 * 
	 * @param network
	 *            the network
	 * @param ctx
	 *            instantiation context (hierarchical path, inherited properties)
	 */
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
	public <T extends EObject, U extends EObject> void putMapping(Entity entity, T cxObj, U irObj) {
		Objects.requireNonNull(entity, "entity must not be null in putMapping");

		Map<EObject, EObject> map = mapCxToIr.get(entity);
		if (map == null) {
			map = new HashMap<>();
			mapCxToIr.put(entity, map);
		}
		map.put(cxObj, irObj);
	}

	@Override
	public <T extends EObject, U extends EObject> void putMapping(T cxObj, U irObj) {
		putMapping(entity, cxObj, irObj);
	}

	@Override
	public void update(ResourceSet resourceSet) {
		mapCxToIr = new HashMap<>();
		mapEntities = LinkedHashMultimap.create();

		for (CxEntity cxEntity : findTopFrom(resourceSet)) {
			EntityInfo info = entityMapper.getEntityInfo(cxEntity);
			instantiate(info, new InstantiationContext(cxEntity.getName()));
		}
	}

}
