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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.synflow.core.util.CoreUtil;
import com.synflow.cx.cx.CxEntity;
import com.synflow.cx.cx.CxPackage.Literals;
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
	private IResourceDescription.Manager manager;

	@Inject
	private ResourceDescriptionsProvider provider;

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
			portMap.putAll(instance, instance.getEntity().getInputs());
		}

		implicitConnector.connect(portMap, network, dpn);
		explicitConnector.connect(portMap, network, dpn);
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
			// we normalize the URI because URIs of reference descriptions are normalized too
			// note that 'normalized' by EMF means from resource to plugin
			URI uri = resourceSet.getURIConverter().normalize(objDesc.getEObjectURI());

			// filters out objects whose URI is platform:/plugin (they can never be 'top' URIs)
			if (!uri.isPlatformPlugin()) {
				topUris.add(uri);
			}
		}

		// remove all entities that are instantiated
		// we use the manager to get an IResourceDescription because
		// ResourceDescriptionsProvider may return CopiedResourceDescriptions
		// which do not have reference descriptions
		for (IResourceDescription resDesc : resourceDescriptions.getAllResourceDescriptions()) {
			URI uri = resDesc.getURI();
			Resource resource;
			try {
				resource = resourceSet.getResource(uri, true);
			} catch (WrappedException e) {
				// resource can't be created/loaded, just skip
				continue;
			}

			resDesc = manager.getResourceDescription(resource);
			for (IReferenceDescription refDesc : resDesc.getReferenceDescriptions()) {
				if (refDesc.getEReference() == Literals.INST__ENTITY) {
					URI uriInstantiable = refDesc.getTargetEObjectUri();
					topUris.remove(uriInstantiable);
				}
			}
		}

		// loads objects from topUris
		// note that URIs in topUris must be normalized in the Xtext sense for this to work
		// (platform:/plugin mapped to platform:/resource)
		List<CxEntity> entities = new ArrayList<>(topUris.size());
		for (URI uri : topUris) {
			URI uriRes = uri.trimFragment();
			IResourceDescription resDesc = resourceDescriptions.getResourceDescription(uriRes);
			type = Literals.CX_ENTITY;
			for (IEObjectDescription objDesc : resDesc.getExportedObjectsByType(type)) {
				if (uri.equals(objDesc.getEObjectURI())) {
					EObject resolved = EcoreUtil.resolve(objDesc.getEObjectOrProxy(), resourceSet);
					entities.add((CxEntity) resolved);
				}
			}
		}
		return entities;
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
	public <T extends EObject, U extends EObject> U getMapping(Entity entity, T cxObj) {
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
		Entity entity = entityMapper.doSwitch(cxEntity);

		// add to resource
		Resource resource = resourceSet.createResource(info.getURI());
		resource.getContents().add(entity);

		// configure entity
		entityMapper.configureEntity(entity, info, ctx);

		// update mapping, optionally add to builtins
		data.updateMapping(cxEntity, entity, ctx);
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
		DPN dpn = (DPN) entity;
		for (Inst inst : network.getInstances()) {
			Instance instance = DpnFactory.eINSTANCE.createInstance(inst.getName());
			putMapping(dpn, inst, instance);
			dpn.add(instance);

			Entity subEntity;
			if (ctx == null) {
				subEntity = data.getMapping(inst.getEntity());
				if (subEntity == null) {
					EntityInfo info = entityMapper.createEntityInfo(inst.getEntity());
					instantiate(info, null);
					subEntity = data.getMapping(inst.getEntity());
				}
			} else {
				InstantiationContext subCtx = new InstantiationContext(ctx, inst, instance);
				EntityInfo info = entityMapper.createEntityInfo(inst, subCtx);
				subEntity = instantiate(info, subCtx);
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
	public <T extends EObject, U extends EObject> void putMapping(Entity entity, T cxObj, U irObj) {
		data.putMapping(entity, cxObj, irObj);
	}

	@Override
	public void update(Module module) {
		resourceSet = module.eResource().getResourceSet();

		try {
			Iterable<CxEntity> entities;
			if (data == null) {
				data = new InstantiatorData();
				entities = findTopFrom(resourceSet);
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
		CxEntity oldEntity = data.getCurrentMapping(cxEntity);
		if (cxEntity == oldEntity) {
			// data is up to date
			return;
		}

		// look up specialization info using previous entity
		Map<InstantiationContext, Entity> map = data.getSpecialization(oldEntity);
		if (map == null) {
			// no previous record of specialization info exists
			EntityInfo info = entityMapper.createEntityInfo(cxEntity);
			instantiate(info, null);
		} else {
			// update specialization info
			updateSpecialized(cxEntity, map);
		}
	}

	private void updateSpecialized(CxEntity cxEntity, Map<InstantiationContext, Entity> map) {
		// copy entry set because map is modified by instantiation
		// old contexts are discarded, new ones added
		Set<Entry<InstantiationContext, Entity>> set = ImmutableSet.copyOf(map.entrySet());
		for (Entry<InstantiationContext, Entity> entry : set) {
			InstantiationContext ctx = entry.getKey();
			InstantiationContext parent = (InstantiationContext) ctx.getParent();
			ctx.delete();

			Inst inst = ctx.getInst();
			if (inst == null) {
				EntityInfo info = entityMapper.createEntityInfo(cxEntity);
				instantiate(info, ctx);
			} else {
				Instance instance = ctx.getInstance();
				InstantiationContext newCtx = new InstantiationContext(parent, inst, instance);

				// update inst's entity to the latest version
				inst.setEntity((Instantiable) cxEntity);

				// instantiate
				EntityInfo info = entityMapper.createEntityInfo(inst, newCtx);
				Entity entity = instantiate(info, newCtx);
				instance.setEntity(entity);
			}
		}
	}

}
