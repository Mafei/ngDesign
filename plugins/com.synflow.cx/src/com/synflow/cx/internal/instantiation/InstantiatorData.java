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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;

import com.google.common.collect.ImmutableSet;
import com.synflow.cx.cx.CxEntity;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Network;
import com.synflow.models.dpn.Entity;

/**
 * This class contains data used by the instantiator and defines methods to access it.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class InstantiatorData {

	private Map<Entity, Map<EObject, EObject>> mapCxToIr;

	/**
	 * map Cx entity -> IR entity
	 */
	private Map<CxEntity, Entity> mapEntities;

	/**
	 * map (Cx entity, instantiation context) -> IR entity
	 */
	private Map<CxEntity, Map<InstantiationContext, Entity>> mapSpecialized;

	private Map<URI, CxEntity> uriMap;

	public InstantiatorData() {
		mapCxToIr = new HashMap<>();
		mapEntities = new HashMap<>();
		mapSpecialized = new HashMap<>();
		uriMap = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	private <T extends EObject, U extends EObject> U basicGetMapping(Entity entity, T cxObj) {
		Map<EObject, EObject> map = mapCxToIr.get(entity);
		if (map == null) {
			return null;
		} else {
			return (U) map.get(cxObj);
		}
	}

	/**
	 * Adds all Cx entities transitively instantiated by the given entity to the
	 * <code>entities</code> set.
	 * 
	 * @param entities
	 *            a set of entities
	 * @param cxEntity
	 *            a Cx entity
	 */
	private void collectEntities(Set<CxEntity> entities, CxEntity cxEntity) {
		entities.add(cxEntity);
		if (cxEntity instanceof Network) {
			Network network = (Network) cxEntity;
			for (Inst inst : network.getInstances()) {
				CxEntity subEntity = inst.getEntity() == null ? inst.getTask() : inst.getEntity();
				collectEntities(entities, subEntity);
			}
		}
	}

	/**
	 * Returns the Cx entity currently associated with the given URI.
	 * 
	 * @param uri
	 *            URI of a Cx entity
	 * @return a Cx entity (may be <code>null</code>)
	 */
	public CxEntity getCxEntity(URI uri) {
		return uriMap.get(uri);
	}

	/**
	 * Returns a collection of IR entities associated with the given Cx entity. If the Cx entity is
	 * specialized, this method returns a list of possibly many specialized IR entities
	 * corresponding to the original Cx entity; otherwise a single IR entity is returned.
	 * 
	 * @param cxEntity
	 *            a Cx entity
	 * @return a collection of IR entities
	 */
	public Collection<Entity> getEntities(CxEntity cxEntity) {
		Objects.requireNonNull(cxEntity, "cxEntity must not be null in getEntities");

		Entity entity = mapEntities.get(cxEntity);
		if (entity != null) {
			return Collections.singleton(entity);
		}

		Map<InstantiationContext, Entity> map = mapSpecialized.get(cxEntity);
		if (map == null) {
			return Collections.emptyList();
		}
		return map.values();
	}

	/**
	 * Returns the IR entity that is currently associated with the given Cx instantiable.
	 * 
	 * @param instantiable
	 *            an instantiable Cx entity
	 * @return an IR entity, or <code>null</code>
	 */
	public Entity getIrEntity(CxEntity instantiable) {
		return mapEntities.get(instantiable);
	}

	public <T extends EObject, U extends EObject> U getMapping(Entity entity, T cxObj) {
		Objects.requireNonNull(entity, "entity must not be null in getMapping");

		U irObj = basicGetMapping(entity, cxObj);
		if (irObj == null) {
			CxEntity cxEntity = EcoreUtil2.getContainerOfType(cxObj, CxEntity.class);
			// lookup in mapEntities, because Bundles are not specialized (yet)
			entity = mapEntities.get(cxEntity);
			if (entity != null) {
				irObj = basicGetMapping(entity, cxObj);
			}
		}
		return irObj;
	}

	/**
	 * Returns the specialization info associated with the given Cx entity.
	 * 
	 * @param cxEntity
	 *            a Cx entity
	 * @return a map
	 */
	public Map<InstantiationContext, Entity> getSpecialization(CxEntity cxEntity) {
		return mapSpecialized.get(cxEntity);
	}

	/**
	 * Checks whether the object at the given URI is specialized.
	 * 
	 * @param uri
	 *            URI
	 * @return true if the object at the given URI is specialized
	 */
	public boolean isSpecialized(URI uri) {
		CxEntity cxEntity = uriMap.get(uri);
		return mapSpecialized.containsKey(cxEntity);
	}

	/**
	 * Adds a mapping from the given Cx object to the given IR object in the given entity.
	 * 
	 * @param entity
	 *            an IR entity
	 * @param cxObj
	 *            a Cx object (entity, variable, port...)
	 * @param irObj
	 *            the IR object that corresponds to <code>cxObj</code> in the given entity
	 */
	public <T extends EObject, U extends EObject> void putMapping(Entity entity, T cxObj, U irObj) {
		Objects.requireNonNull(entity, "entity must not be null in putMapping");

		Map<EObject, EObject> map = mapCxToIr.get(entity);
		if (map == null) {
			map = new HashMap<>();
			mapCxToIr.put(entity, map);
		}
		map.put(cxObj, irObj);
	}

	/**
	 * Removes info about all specialized entities that can be reached from the given entity.
	 * 
	 * @param cxEntity
	 */
	public Iterable<InstantiationContext> removeSpecialized(CxEntity cxEntity) {
		if (cxEntity == null) {
			return ImmutableSet.of();
		}

		Set<CxEntity> entities = new LinkedHashSet<>();
		collectEntities(entities, cxEntity);

		List<InstantiationContext> contexts = new ArrayList<>();
		for (CxEntity aCxEntity : entities) {
			Map<InstantiationContext, Entity> map = mapSpecialized.remove(aCxEntity);
			if (map != null) {
				contexts.addAll(map.keySet());
				mapCxToIr.keySet().removeAll(map.values());
			}
		}

		return contexts;
	}

	public void updateMapping(CxEntity cxEntity, Entity entity, InstantiationContext ctx) {
		Objects.requireNonNull(cxEntity, "cxEntity must not be null in updateMapping");

		URI uri = EcoreUtil.getURI(cxEntity);
		CxEntity oldEntity = uriMap.get(uri);
		if (oldEntity != cxEntity) {
			uriMap.put(uri, cxEntity);
		}

		// updates mapEntities/mapSpecialized
		if (ctx == null) {
			// clean up anything associated with previous version of cxEntity
			// only does this for mapEntities, specialized mappings are cleared by instantiator
			if (oldEntity != null && oldEntity != cxEntity) {
				mapCxToIr.remove(mapEntities.remove(oldEntity));
			}

			mapEntities.put(cxEntity, entity);
		} else {
			Map<InstantiationContext, Entity> map = mapSpecialized.get(cxEntity);
			if (map == null) {
				map = new LinkedHashMap<>();
				mapSpecialized.put(cxEntity, map);
			}
			map.put(ctx, entity);
		}
	}

}
