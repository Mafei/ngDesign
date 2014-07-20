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

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.IScopeProvider;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.synflow.cflow.UriComputer;
import com.synflow.cflow.cflow.Bundle;
import com.synflow.cflow.cflow.CflowPackage.Literals;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.cflow.cflow.Network;
import com.synflow.cflow.cflow.Task;
import com.synflow.cflow.cflow.util.CflowSwitch;
import com.synflow.cflow.internal.instantiation.properties.JsonMaker;
import com.synflow.cflow.internal.instantiation.properties.PropertiesSupport;
import com.synflow.core.util.CoreUtil;
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
public class EntityMaker extends CflowSwitch<Entity> {

	@Inject
	private IQualifiedNameConverter converter;

	private Map<Entity, NamedEntity> map;

	private JsonObject properties;

	@Inject
	private IQualifiedNameProvider qualifiedNameProvider;

	@Inject
	private IScopeProvider scopeProvider;

	private URI uriInst;

	/**
	 * Adds the given entity to a .ir resource whose path is computed from cxResource and the
	 * entity's name.
	 * 
	 * @param entity
	 *            IR entity
	 * @param cxResource
	 *            Cx resource
	 */
	private void addEntityToIrResource(Entity entity, Resource cxResource) {
		addEntityToIrResource(entity, cxResource, entity.getName());
	}

	/**
	 * Adds the given entity to a .ir resource whose path is computed from cxResource and name.
	 * 
	 * @param entity
	 *            IR entity
	 * @param cxResource
	 *            Cx resource
	 * @param name
	 *            qualified name
	 */
	private void addEntityToIrResource(Entity entity, Resource cxResource, String name) {
		// get URI of .ir file
		URI cfUri = cxResource.getURI();
		URI uri = UriComputer.INSTANCE.computeUri(uriInst, cfUri, name);

		// get or create IR resource
		ResourceSet set = cxResource.getResourceSet();
		Resource resource = set.getResource(uri, false);
		if (resource == null) {
			resource = set.createResource(uri);
		} else {
			// makes sure the resource does not contain anything else
			resource.getContents().clear();
		}

		// add entity to resource
		resource.getContents().add(entity);
	}

	@Override
	public Entity caseBundle(Bundle bundle) {
		Unit unit = DpnFactory.eINSTANCE.createUnit();
		unit.setName(getName(bundle));
		addEntityToIrResource(unit, bundle.eResource());

		unit.setProperties(new JsonObject());
		return unit;
	}

	@Override
	public Entity caseInst(Inst inst) {
		uriInst = EcoreUtil.getURI(inst);
		NamedEntity cxEntity;
		if (inst.getTask() == null) {
			cxEntity = inst.getEntity();
		} else {
			cxEntity = inst.getTask();
		}

		Entity entity = doSwitch(cxEntity);
		map.put(entity, cxEntity);
		return entity;
	}

	@Override
	public Entity caseNetwork(Network network) {
		DPN dpn = DpnFactory.eINSTANCE.createDPN();
		dpn.init();
		new PropertiesSupport(network).setProperties(dpn);

		dpn.setName(getName(network));
		addEntityToIrResource(dpn, network.eResource());

		return dpn;
	}

	@Override
	public Entity caseTask(Task task) {
		Actor actor = DpnFactory.eINSTANCE.createActor();
		new PropertiesSupport(task).setProperties(actor);

		String actorName = getName(task);
		JsonObject impl = CoreUtil.getImplementation(actor);
		if (impl != null) {
			// external or built-in
			// update name based on properties values
			// but do not call actor.setName
			IScope scope = scopeProvider.getScope(task, Literals.VAR_REF__VARIABLE);
			for (Entry<String, JsonElement> entry : properties.entrySet()) {
				String name = entry.getKey();
				QualifiedName qName = converter.toQualifiedName(name);
				IEObjectDescription objDesc = scope.getSingleElement(qName);
				if (objDesc != null) {

				}
			}
		} else {
			actor.setName(actorName);
		}

		addEntityToIrResource(actor, task.eResource(), actorName);

		return actor;
	}

	public Entity createEntity(Map<Entity, NamedEntity> map, Inst inst) {
		this.map = map;
		JsonMaker maker = new JsonMaker();
		this.properties = maker.toJson(inst.getArguments());

		try {
			Entity entity = doSwitch(inst);
			return entity;
		} finally {
			this.map = null;
			this.properties = null;
			this.uriInst = null;
		}
	}

	private String getName(NamedEntity entity) {
		QualifiedName qualifiedName = qualifiedNameProvider.getFullyQualifiedName(entity);
		if (qualifiedName == null) {
			return null;
		}
		return qualifiedName.toString();
	}

}
