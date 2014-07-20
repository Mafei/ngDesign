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
package com.synflow.cflow.internal.instantiation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.IScopeProvider;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.synflow.cflow.UriComputer;
import com.synflow.cflow.cflow.Bundle;
import com.synflow.cflow.cflow.CflowPackage.Literals;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.Instantiable;
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.cflow.cflow.Network;
import com.synflow.cflow.cflow.Obj;
import com.synflow.cflow.cflow.Pair;
import com.synflow.cflow.cflow.Task;
import com.synflow.cflow.cflow.util.CflowSwitch;
import com.synflow.cflow.internal.instantiation.properties.PropertiesSupport;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.DpnFactory;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Unit;

/**
 * This class converts a Cx entity to an IR entity.
 * 
 * @author Matthieu Wipliez
 *
 */
public class EntityMapper extends CflowSwitch<Entity> {

	@Inject
	private IQualifiedNameConverter converter;

	private Map<Entity, NamedEntity> map;

	@Inject
	private IQualifiedNameProvider qualifiedNameProvider;

	@Inject
	private IScopeProvider scopeProvider;

	@Inject
	private SkeletonMaker skeletonMaker;

	public EntityMapper() {
		map = new HashMap<>();
	}

	@Override
	public Entity caseBundle(Bundle bundle) {
		Unit unit = DpnFactory.eINSTANCE.createUnit();
		unit.setProperties(new JsonObject());
		return unit;
	}

	@Override
	public Entity caseNetwork(Network network) {
		DPN dpn = DpnFactory.eINSTANCE.createDPN();
		dpn.init();
		new PropertiesSupport(network).setProperties(dpn);
		return dpn;
	}

	@Override
	public Entity caseTask(Task task) {
		Actor actor = DpnFactory.eINSTANCE.createActor();
		new PropertiesSupport(task).setProperties(actor);
		return actor;
	}

	public Entity createEntity(Resource resource, NamedEntity cxEntity, String name) {
		// add entity to resource
		Entity entity = doSwitch(cxEntity);
		entity.setName(name);
		resource.getContents().add(entity);
		map.put(entity, cxEntity);

		skeletonMaker.createSkeleton(map, entity);
		return entity;
	}

	/**
	 * Returns either the entity name, or a specialized name, depending on the context (properties
	 * given to the instance).
	 * 
	 * @param inst
	 *            instance
	 * @param cxEntity
	 *            instantiated entity
	 * @return a name
	 */
	private String getEntityName(Inst inst, Instantiable cxEntity) {
		String name = getName(cxEntity);

		Obj obj = inst.getArguments();
		if (obj == null) {
			return name;
		}

		IScope scope = scopeProvider.getScope(cxEntity, Literals.VAR_REF__VARIABLE);
		for (Pair pair : obj.getMembers()) {
			String varName = pair.getKey();
			QualifiedName qName = converter.toQualifiedName(varName);
			IEObjectDescription objDesc = scope.getSingleElement(qName);
			if (objDesc != null) {
				// properties configure at least one variable, returns specialized name
				// TODO
				return name;
			}
		}

		return name;
	}

	/**
	 * Returns the qualified name of the given entity.
	 * 
	 * @param entity
	 *            Cx entity
	 * @return a name
	 */
	private String getName(NamedEntity entity) {
		QualifiedName qualifiedName = qualifiedNameProvider.getFullyQualifiedName(entity);
		if (qualifiedName == null) {
			return null;
		}
		return qualifiedName.toString();
	}

	/**
	 * Attempts to find the entity instantiated by the given instance.
	 * 
	 * @param inst
	 *            a Cx instance
	 * @return an entity
	 */
	public Entity getOrCreateEntity(Inst inst) {
		Instantiable cxEntity;
		if (inst.getTask() == null) {
			cxEntity = inst.getEntity();
		} else {
			cxEntity = inst.getTask();
		}

		// get URI of .ir file
		Resource cxResource = cxEntity.eResource();
		URI cxUri = cxResource.getURI();
		URI uriInst = EcoreUtil.getURI(inst);
		String name = getEntityName(inst, cxEntity);
		URI uri = UriComputer.INSTANCE.computeUri(uriInst, cxUri, name);

		// get or create IR resource
		ResourceSet set = cxResource.getResourceSet();
		Resource resource = set.getResource(uri, false);
		if (resource == null) {
			resource = set.createResource(uri);
		} else {
			if (resource.getTimeStamp() >= cxResource.getTimeStamp()) {
				// resource is up-to-date, returns existing entity
				EObject contents = resource.getContents().get(0);
				return (Entity) contents;
			}

			// resource is stale, will replace its contents
			resource.getContents().clear();
		}

		return createEntity(resource, cxEntity, name);
	}

}
