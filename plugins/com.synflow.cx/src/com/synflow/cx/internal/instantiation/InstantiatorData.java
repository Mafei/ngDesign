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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
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
	 * multi map from Cx entity to IR entities
	 */
	private Multimap<CxEntity, Entity> mapEntities;

	private Map<URI, CxEntity> uriMap;

	public InstantiatorData() {
		mapCxToIr = new HashMap<>();
		mapEntities = LinkedHashMultimap.create();
		uriMap = new HashMap<>();
	}

	public void associate(CxEntity cxEntity, Entity entity) {
		mapEntities.put(cxEntity, entity);
	}

	public Collection<Entity> getEntities(CxEntity cxEntity) {
		return mapEntities.get(cxEntity);
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
				entity = Iterables.getFirst(mapEntities.get(cxEntity), null);
				if (entity != null) {
					irObj = (U) mapCxToIr.get(entity).get(cxObj);
				}
			}
		}
		return irObj;
	}

	public <T extends EObject, U extends EObject> void putMapping(Entity entity, T cxObj, U irObj) {
		Objects.requireNonNull(entity, "entity must not be null in putMapping");

		Map<EObject, EObject> map = mapCxToIr.get(entity);
		if (map == null) {
			map = new HashMap<>();
			mapCxToIr.put(entity, map);
		}
		map.put(cxObj, irObj);
	}

	public void updateUri(CxEntity cxEntity) {
		URI uri = EcoreUtil.getURI(cxEntity);
		CxEntity candidate = uriMap.get(uri);
		if (cxEntity != candidate && candidate != null) {
			for (Entity entity : getEntities((CxEntity) candidate)) {
				mapCxToIr.remove(entity);
			}
			mapEntities.removeAll(candidate);
		}
		uriMap.put(uri, cxEntity);
	}

}
