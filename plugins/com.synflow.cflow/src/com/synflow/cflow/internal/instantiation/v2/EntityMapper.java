package com.synflow.cflow.internal.instantiation.v2;

import org.eclipse.emf.common.util.URI;
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

	public Entity createEntity(EntityInfo info) {
		// add entity to resource
		Entity entity = doSwitch(info.getCxEntity());
		Entity oldEntity = instantiator.setEntity(entity);
		entity.setName(info.getName());

		Resource resource = info.getResource();
		resource.getContents().clear();
		resource.getContents().add(entity);

		skeletonMaker.createSkeleton(info.getCxEntity(), entity);
		instantiator.setEntity(oldEntity);
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
		String name = getEntityName(inst, cxEntity, ctx.getName());
		URI uri = UriComputer.INSTANCE.computeUri(uriInst, cxUri, name);

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
	private String getEntityName(Inst inst, Instantiable cxEntity, String instName) {
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
				return instName;
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

}
