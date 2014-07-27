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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
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
import com.synflow.cflow.cflow.CExpression;
import com.synflow.cflow.cflow.CflowPackage.Literals;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.Instantiable;
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.cflow.cflow.Network;
import com.synflow.cflow.cflow.Task;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.cflow.util.CflowSwitch;
import com.synflow.cflow.internal.instantiation.properties.PropertiesSupport;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.DpnFactory;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Unit;

/**
 * This class maps Cx entities to IR URIs.
 * 
 * @author Matthieu Wipliez
 *
 */
public class EntityMapper extends CflowSwitch<Entity> {

	@Inject
	private IQualifiedNameConverter converter;

	@Inject
	private IInstantiator instantiator;

	@Inject
	private IQualifiedNameProvider qualifiedNameProvider;

	@Inject
	private IScopeProvider scopeProvider;

	@Inject
	private SkeletonMaker skeletonMaker;

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

	public Entity createEntity(EntityInfo info, InstantiationContext ctx) {
		// add entity to resource
		Entity entity = doSwitch(info.getCxEntity());
		Entity oldEntity = instantiator.setEntity(entity);
		entity.setName(info.getName());

		Resource resource = info.getResource();
		resource.getContents().clear();
		resource.getContents().add(entity);

		// set values on entity
		Map<Variable, EObject> values = setValues(info.getCxEntity(), ctx);

		try {
			skeletonMaker.createSkeleton(info.getCxEntity(), entity);
		} finally {
			// restore values
			restoreValues(values);

			// restore current entity in instantiator
			instantiator.setEntity(oldEntity);
		}
		return entity;
	}

	/**
	 * Returns info for the IR entity instantiated by the given instance.
	 * 
	 * @param inst
	 *            a Cx instance
	 * @return info about the IR entity
	 */
	public EntityInfo getEntityInfo(Inst inst, InstantiationContext ctx) {
		Instantiable cxEntity;
		if (inst.getTask() == null) {
			cxEntity = inst.getEntity();
		} else {
			cxEntity = inst.getTask();
		}

		// get URI of .ir file
		URI cxUri = cxEntity.eResource().getURI();
		URI uriInst = EcoreUtil.getURI(inst);
		String name = getEntityName(inst, cxEntity, ctx);
		URI uri = UriComputer.INSTANCE.computeUri(uriInst, cxUri, name);

		// TODO if entity is builtin/external, we must use its non-specialized name

		return new EntityInfo(cxEntity, name, uri);
	}

	/**
	 * Returns info for the IR entity instantiated by the given instance.
	 * 
	 * @param inst
	 *            a Cx instance
	 * @return info about the IR entity
	 */
	public EntityInfo getEntityInfo(NamedEntity cxEntity) {
		// get URI of .ir file
		URI cxUri = cxEntity.eResource().getURI();
		String name = getName(cxEntity);
		URI uri = UriComputer.INSTANCE.computeUri(null, cxUri, name);

		return new EntityInfo(cxEntity, name, uri);
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
	private String getEntityName(Inst inst, Instantiable cxEntity, InstantiationContext ctx) {
		String name = getName(cxEntity);
		if (ctx.getProperties().isEmpty()) {
			return name;
		}

		IScope scope = scopeProvider.getScope(cxEntity, Literals.VAR_REF__VARIABLE);
		for (Entry<String, CExpression> entry : ctx.getProperties().entrySet()) {
			String varName = entry.getKey();
			QualifiedName qName = converter.toQualifiedName(varName);
			IEObjectDescription objDesc = scope.getSingleElement(qName);
			if (objDesc != null) {
				// properties configure at least one variable, returns specialized name
				return ctx.getName();
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
	 * Restore values using the given map.
	 * 
	 * @param values
	 *            a map variable to value
	 */
	private void restoreValues(Map<Variable, EObject> values) {
		for (Entry<Variable, EObject> binding : values.entrySet()) {
			binding.getKey().setValue(binding.getValue());
		}
	}

	/**
	 * Applies properties' values.
	 * 
	 * @param cxEntity
	 *            Cx entity
	 * @param ctx
	 *            instantiation context
	 */
	private Map<Variable, EObject> setValues(NamedEntity cxEntity, InstantiationContext ctx) {
		Map<Variable, EObject> previous = new HashMap<>();
		IScope scope = scopeProvider.getScope(cxEntity, Literals.VAR_REF__VARIABLE);
		for (Entry<String, CExpression> entry : ctx.getProperties().entrySet()) {
			String varName = entry.getKey();
			QualifiedName qName = converter.toQualifiedName(varName);
			IEObjectDescription objDesc = scope.getSingleElement(qName);
			if (objDesc != null) {
				EObject eObject = objDesc.getEObjectOrProxy();
				if (eObject instanceof Variable) {
					Variable variable = (Variable) eObject;
					previous.put(variable, variable.getValue());
					variable.setValue(EcoreUtil.copy(entry.getValue()));
				}
			}
		}

		return previous;
	}

}
