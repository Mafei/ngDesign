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

import static org.eclipse.emf.ecore.util.EcoreUtil.getURI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.synflow.core.util.CoreUtil;
import com.synflow.cx.cx.Bundle;
import com.synflow.cx.cx.CxEntity;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Instantiable;
import com.synflow.cx.cx.Module;
import com.synflow.cx.cx.Network;
import com.synflow.cx.cx.VarRef;
import com.synflow.cx.instantiation.IInstantiator;
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

	private InstantiatorData data;

	@Inject
	private EntityMapper entityMapper;

	@Inject
	private ExplicitConnector explicitConnector;

	@Inject
	private ImplicitConnector implicitConnector;

	@Inject
	private TopEntitiesLoader loader;

	private ResourceSet resourceSet;

	public InstantiatorImpl() {
		builtins = new ArrayList<>();
	}

	@Override
	public void clearData() {
		data = null;
	}

	/**
	 * Creates a new connection maker and connects the given network. Visits inner tasks first, and
	 * then connect statements.
	 * 
	 * @param network
	 * @param dpn
	 */
	private void connect(Network network, DPN dpn) {
		Multimap<EObject, Port> portMap = LinkedHashMultimap.create();
		portMap.putAll(dpn, dpn.getOutputs());
		for (Instance instance : dpn.getInstances()) {
			Entity entity = instance.getEntity();
			if (entity != null) {
				portMap.putAll(instance, entity.getInputs());
			}
		}

		implicitConnector.connect(portMap, network, dpn);
		explicitConnector.connect(portMap, network, dpn);
	}

	@Override
	public void forEachMapping(CxEntity cxEntity, Executable<Entity> executable) {
		Objects.requireNonNull(cxEntity, "cxEntity must not be null in forEachMapping");

		Collection<Entity> entities = data.getEntities(cxEntity);
		for (Entity entity : entities) {
			executable.exec(entity);
		}
	}

	@Override
	public Iterable<Entity> getBuiltins() {
		List<Entity> result = builtins;
		builtins = new ArrayList<>();
		return result;
	}

	@Override
	public CxEntity getEntity(URI uri) {
		return data.getCxEntity(uri);
	}

	@Override
	public <T extends EObject> T getMapping(Entity entity, Object cxObj) {
		return data.getMapping(entity, cxObj);
	}

	@Override
	public Port getPort(Entity entity, VarRef refOrCopyOfRef) {
		final VarRef ref = CopyOf.getOriginal(refOrCopyOfRef);

		// first try port in named task/network
		Port port = getMapping(entity, ref.getVariable());
		if (port == null) {
			// otherwise get mapping of reference (anonymous task)
			port = getMapping(entity, ref);
		}
		return port;
	}

	/**
	 * Instantiates a Cx entity based on the given info and instantiation context.
	 * 
	 * @param info
	 *            entity info (URI of IR, name, reference to original Cx entity)
	 * @param ctx
	 *            instantiation context (hierarchical path, inherited properties). May be
	 *            <code>null</code>.
	 * @return a specialized IR entity
	 */
	private Entity instantiate(EntityInfo info, InstantiationContext ctx) {
		CxEntity cxEntity = info.getCxEntity();

		// load bundles first (if necessary)
		Iterable<Bundle> bundles = loader.loadBundles(resourceSet, cxEntity);
		for (Bundle bundle : bundles) {
			updateEntity(bundle);
		}

		// map entity and update mapping
		Entity entity = entityMapper.doSwitch(cxEntity);
		if (entity == null) {
			// happens with unresolved references to instantiable entities
			return null;
		}
		data.updateMapping(cxEntity, entity, ctx);

		// add to resource
		Resource resource = resourceSet.getResource(info.getURI(), false);
		if (resource == null) {
			resource = resourceSet.createResource(info.getURI());
		} else {
			// IR entity already created during this build, discard
			// happens when revalidating the instantiating parent of a specialized entity
			resource.getContents().clear();
		}
		resource.getContents().add(entity);

		// configure entity, if built-in add to builtins list
		entityMapper.configureEntity(entity, info, ctx);
		if (CoreUtil.isBuiltin(entity)) {
			builtins.add(entity);
		}

		// instantiate network
		if (cxEntity instanceof Network) {
			instantiate((Network) cxEntity, entity, ctx);
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
	private void instantiate(Network network, Entity entity, InstantiationContext ctx) {
		if (ctx == null) {
			// create a context for this network to serve as a parent context
			// only useful for specialized instances
			ctx = new InstantiationContext(entity.getName());
		}

		DPN dpn = (DPN) entity;
		for (Inst inst : network.getInstances()) {
			Instance instance = DpnFactory.eINSTANCE.createInstance(inst.getName());
			data.putMapping(dpn, inst, instance);
			dpn.add(instance);

			InstantiationContext subCtx = new InstantiationContext(ctx, inst, instance);
			EntityInfo info = entityMapper.createEntityInfo(subCtx);

			Entity subEntity;
			if (info.isSpecialized()) {
				// specialized: instantiate with sub-context
				subEntity = instantiate(info, subCtx);
			} else {
				// not specialized: remove sub-context (not needed anymore)
				subCtx.delete();

				// try to look up existing mapping
				subEntity = data.getIrEntity(info.getCxEntity());
				if (subEntity == null) {
					// no existing mapping, transform Cx entity to IR
					// not specialized => no need for instantiation context (1:1 mapping)
					subEntity = instantiate(info, null);
				}
			}

			// happens with unresolved references to instantiable entities
			if (subEntity == null) {
				continue;
			}

			instance.setEntity(subEntity);

			// set properties. For anonymous tasks, use the task's properties for the instance
			PropertiesSupport support;
			if (inst.getTask() == null) {
				support = new PropertiesSupport(inst);
			} else {
				support = new PropertiesSupport(inst.getTask());
			}
			support.setProperties(instance);
		}

		connect(network, dpn);
	}

	@Override
	public boolean isSpecialized(URI uri) {
		return data.isSpecialized(uri);
	}

	@Override
	public void putMapping(Entity entity, Object cxObj, EObject irObj) {
		data.putMapping(entity, cxObj, irObj);
	}

	@Override
	public void update(Module module) {
		resourceSet = module.eResource().getResourceSet();

		try {
			Iterable<CxEntity> entities;
			if (data == null) {
				data = new InstantiatorData();
				entities = loader.loadTopEntities(resourceSet);
			} else {
				entities = module.getEntities();
			}

			for (CxEntity cxEntity : entities) {
				updateEntity(cxEntity);
			}
		} finally {
			resourceSet = null;
		}
	}

	private void updateEntity(CxEntity cxEntity) {
		CxEntity oldEntity = data.getCxEntity(getURI(cxEntity));
		if (cxEntity == oldEntity) {
			return;
		}

		// look up specialization info using previous entity
		// do this now, before removeSpecialized removes the map
		Map<InstantiationContext, Entity> map = data.getSpecialization(oldEntity);

		// removes specialized info, returns iterable of contexts to delete later
		Iterable<InstantiationContext> contexts = data.removeSpecialized(oldEntity);

		if (map == null) {
			// no previous record of specialization info exists
			EntityInfo info = entityMapper.createEntityInfo(cxEntity);
			instantiate(info, null);
		} else {
			// update specialization info
			updateSpecialized(map, cxEntity);
		}

		// clean up: deletes old contexts
		for (InstantiationContext ctx : contexts) {
			ctx.delete();
		}
	}

	private void updateSpecialized(Map<InstantiationContext, Entity> map, CxEntity cxEntity) {
		// copy context set because map is modified by instantiation
		Set<InstantiationContext> contexts = ImmutableSet.copyOf(map.keySet());
		for (InstantiationContext ctx : contexts) {
			InstantiationContext parent = (InstantiationContext) ctx.getParent();

			Inst inst = ctx.getInst();
			Instance instance = ctx.getInstance();
			InstantiationContext newCtx = new InstantiationContext(parent, inst, instance);

			// update inst's entity to the latest version
			inst.setEntity((Instantiable) cxEntity);

			// instantiate
			EntityInfo info = entityMapper.createEntityInfo(newCtx);
			Entity entity = instantiate(info, newCtx);
			instance.setEntity(entity);
		}
	}

}
