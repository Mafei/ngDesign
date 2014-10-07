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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.IScopeProvider;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.synflow.core.util.CoreUtil;
import com.synflow.cx.CxUtil;
import com.synflow.cx.UriComputer;
import com.synflow.cx.cx.Bundle;
import com.synflow.cx.cx.CExpression;
import com.synflow.cx.cx.CxEntity;
import com.synflow.cx.cx.CxPackage.Literals;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Instantiable;
import com.synflow.cx.cx.Module;
import com.synflow.cx.cx.Network;
import com.synflow.cx.cx.Task;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.cx.util.CxSwitch;
import com.synflow.cx.internal.TransformerUtil;
import com.synflow.cx.internal.instantiation.properties.PropertiesSupport;
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
public class EntityMapper extends CxSwitch<Entity> {

	enum Options {
		DRY_RUN
	}

	@Inject
	private IQualifiedNameConverter converter;

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

	/**
	 * Configures the given IR entity from the Cx entity info and the instantiation context. Sets
	 * basic properties (name, file name, line number) and translates skeleton (state variables,
	 * ports...)
	 * 
	 * @param entity
	 *            IR entity
	 * @param info
	 *            Cx entity info
	 * @param ctx
	 *            instantiation context
	 */
	public void configureEntity(Entity entity, EntityInfo info, InstantiationContext ctx) {
		CxEntity cxEntity = info.getCxEntity();

		// set name
		if (CoreUtil.isBuiltin(entity) || CoreUtil.isExternal(entity)) {
			entity.setName(getName(cxEntity));
		} else {
			entity.setName(info.getName());
		}

		// set file name
		Module module = EcoreUtil2.getContainerOfType(cxEntity, Module.class);
		String fileName = CxUtil.getFileName(module);
		entity.setFileName(fileName);

		// set line number
		int lineNumber = TransformerUtil.getStartLine(cxEntity);
		entity.setLineNumber(lineNumber);

		// create skeleton (with proper values if ctx is given)
		if (ctx == null) {
			skeletonMaker.createSkeleton(cxEntity, entity);
		} else {
			Map<Variable, EObject> values = setValues(cxEntity, ctx);
			try {
				skeletonMaker.createSkeleton(cxEntity, entity);
			} finally {
				restoreValues(values);
			}
		}
	}

	/**
	 * Returns an EntityInfo about the given Cx entity.
	 * 
	 * @param cxEntity
	 *            a Cx entity
	 * @return an EntityInfo object
	 */
	public EntityInfo createEntityInfo(CxEntity cxEntity) {
		return createEntityInfo(cxEntity, null, null);
	}

	/**
	 * Returns an EntityInfo about the given entity.
	 * 
	 * @param cxEntity
	 *            Cx entity
	 * @param inst
	 *            instance, may be <code>null</code>
	 * @param specializedName
	 *            specialized name, if null this method uses the entity name as returned by
	 *            {@link #getName(CxEntity)}
	 * @return an EntityInfo object
	 */
	private EntityInfo createEntityInfo(CxEntity cxEntity, Inst inst, String specializedName) {
		boolean specialized = specializedName != null;
		String name = specialized ? specializedName : getName(cxEntity);
		if (name == null) {
			// happens when cxEntity is a proxy because it could not be resolved
			return new EntityInfo(cxEntity, null, null, specialized);
		}

		URI cxUri = cxEntity.eResource().getURI();
		URI uriInst = inst == null ? null : EcoreUtil.getURI(inst);
		URI uri = UriComputer.INSTANCE.computeUri(name, cxUri, uriInst);

		return new EntityInfo(cxEntity, name, uri, specialized);
	}

	/**
	 * Returns an EntityInfo for the Cx entity instantiated using the given instantiation context.
	 * 
	 * @param ctx
	 *            an instantiation context
	 * @return an EntityInfo
	 */
	public EntityInfo createEntityInfo(InstantiationContext ctx) {
		// if this instance declares an inner task, it is specialized
		Inst inst = ctx.getInst();
		boolean specialized = inst.getTask() != null;
		Instantiable cxEntity = specialized ? inst.getTask() : inst.getEntity();

		// if the instance depends on parent's properties, it is specialized
		Map<Variable, EObject> values = getVariablesMap(cxEntity, ctx);
		specialized |= !values.isEmpty();

		return createEntityInfo(cxEntity, inst, specialized ? ctx.getName() : null);
	}

	/**
	 * Returns the qualified name of the given entity.
	 * 
	 * @param entity
	 *            Cx entity
	 * @return a name
	 */
	private String getName(CxEntity entity) {
		QualifiedName qualifiedName = qualifiedNameProvider.getFullyQualifiedName(entity);
		if (qualifiedName == null) {
			return null;
		}
		return qualifiedName.toString();
	}

	/**
	 * Returns a map of variable - value association based on the given Cx entity's variables and
	 * the instantiation context. Equivalent to <code>visitProperties(cxEntity, ctx, false)</code>.
	 * 
	 * @param cxEntity
	 *            Cx entity
	 * @param ctx
	 *            instantiation context
	 * @return a map
	 */
	private Map<Variable, EObject> getVariablesMap(CxEntity cxEntity, InstantiationContext ctx) {
		return visitProperties(cxEntity, ctx, false);
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
	 * Sets the values of affected variables in the given Cx entity to the values given by the
	 * instantiation context. Equivalent to <code>visitProperties(cxEntity, ctx, true)</code>.
	 * 
	 * @param cxEntity
	 *            Cx entity
	 * @param ctx
	 *            instantiation context
	 * @return a map of variable - value association
	 */
	private Map<Variable, EObject> setValues(CxEntity cxEntity, InstantiationContext ctx) {
		return visitProperties(cxEntity, ctx, true);
	}

	/**
	 * Returns a map of variable - value association based on the given Cx entity's variables and
	 * the instantiation context. The <code>set</code> parameter controls whether the value of
	 * variables are updated to the value given in the context or not.
	 * 
	 * @param cxEntity
	 *            Cx entity
	 * @param ctx
	 *            instantiation context
	 * @param set
	 *            if true, update the value of affected variables
	 * @return a map
	 */
	private Map<Variable, EObject> visitProperties(CxEntity cxEntity, InstantiationContext ctx,
			boolean set) {
		Map<Variable, EObject> previous = new HashMap<>();
		if (ctx.getProperties().isEmpty()) {
			return Collections.emptyMap();
		}

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
					if (set) {
						variable.setValue(EcoreUtil.copy(entry.getValue()));
					}
				}
			}
		}

		return previous;
	}

}
