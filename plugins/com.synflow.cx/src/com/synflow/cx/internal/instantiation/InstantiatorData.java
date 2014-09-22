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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;

import com.synflow.cx.cx.Bundle;
import com.synflow.cx.cx.CxEntity;
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
				// lookup in mapEntities, because Bundles are not specialized (yet)
				entity = mapEntities.get(cxEntity);
				if (entity != null) {
					return getMapping(entity, cxObj);
				}
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

	public void updateMapping(CxEntity cxEntity, Entity entity, InstantiationContext ctx) {
		Objects.requireNonNull(cxEntity, "cxEntity must not be null in updateMapping");

		URI uri = EcoreUtil.getURI(cxEntity);
		CxEntity oldEntity = uriMap.get(uri);
		if (oldEntity != cxEntity) {
			uriMap.put(uri, cxEntity);

			// clean up anything associated with previous version of cxEntity
			if (oldEntity != null) {
				if (ctx == null) {
					mapCxToIr.remove(mapEntities.remove(oldEntity));
				} else {
					Map<InstantiationContext, Entity> map = mapSpecialized.remove(oldEntity);
					if (map != null) {
						mapCxToIr.entrySet().removeAll(map.values());
					}
				}
			}
		}

		// updates mapEntities/mapSpecialized
		if (ctx == null) {
			mapEntities.put(cxEntity, entity);
		} else {
			Map<InstantiationContext, Entity> map = mapSpecialized.get(cxEntity);
			if (map == null) {
				map = new LinkedHashMap<>();
				mapSpecialized.put(cxEntity, map);
			} else {
				// clean up old contexts
				Iterator<InstantiationContext> it = map.keySet().iterator();
				while (it.hasNext()) {
					InstantiationContext subCtx = it.next();
					if (subCtx.getParent() == null) {
						it.remove();
					}
				}
			}
			map.put(ctx, entity);
		}
	}

}
