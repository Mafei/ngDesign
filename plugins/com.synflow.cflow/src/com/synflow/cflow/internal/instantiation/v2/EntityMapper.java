package com.synflow.cflow.internal.instantiation.v2;

import java.util.HashMap;
import java.util.Map;

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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
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
import com.synflow.cflow.internal.scheduler.node.Node;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.DpnFactory;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Unit;

public class EntityMapper extends CflowSwitch<Entity> {

	@Inject
	private IQualifiedNameConverter converter;

	Multimap<NamedEntity, Node> mapCxToNodes;

	private Map<Entity, NamedEntity> mapIrToCx;

	private Map<URI, Node> mapIrUriToNode;

	@Inject
	private IQualifiedNameProvider qualifiedNameProvider;

	@Inject
	private IScopeProvider scopeProvider;

	@Inject
	private SkeletonMaker skeletonMaker;

	public EntityMapper() {
		mapCxToNodes = HashMultimap.create();
		mapIrToCx = new HashMap<>();
		mapIrUriToNode = new HashMap<>();
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
		mapIrToCx.put(entity, cxEntity);

		skeletonMaker.createSkeleton(mapIrToCx, entity);
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
		URI cxUri = cxEntity.eResource().getURI();
		URI uriInst = EcoreUtil.getURI(inst);
		String name = getEntityName(inst, cxEntity);
		URI uri = UriComputer.INSTANCE.computeUri(uriInst, cxUri, name);

		return getOrCreateEntity(cxEntity, name, uri);
	}

	/**
	 * Attempts to find the entity that corresponds to the given entity.
	 * 
	 * @param cxEntity
	 *            a Cx entity
	 * @return an entity
	 */
	public Entity getOrCreateEntity(NamedEntity cxEntity) {
		// get URI of .ir file
		URI cxUri = cxEntity.eResource().getURI();
		String name = getName(cxEntity);
		URI uri = UriComputer.INSTANCE.computeUri(null, cxUri, name);

		return getOrCreateEntity(cxEntity, name, uri);
	}

	private Entity getOrCreateEntity(NamedEntity cxEntity, String name, URI uri) {
		Node node = mapIrUriToNode.get(uri);
		if (node != null) {
			InstInfo info = (InstInfo) node.getContent();
			return info.getEntity();
		}

		// get or create IR resource
		ResourceSet set = cxEntity.eResource().getResourceSet();
		Resource resource = set.getResource(uri, false);
		if (resource == null) {
			resource = set.createResource(uri);
		}

		// add entity to resource
		Entity entity = doSwitch(cxEntity);
		entity.setName(name);

		resource.getContents().clear();
		resource.getContents().add(entity);

		// adds to map
		mapIrToCx.put(entity, cxEntity);
		node = new Node();
		mapIrUriToNode.put(uri, node);

		skeletonMaker.createSkeleton(mapIrToCx, entity);
		return entity;
	}

}
