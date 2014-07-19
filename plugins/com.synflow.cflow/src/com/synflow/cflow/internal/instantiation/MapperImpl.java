/*******************************************************************************
 * Copyright (c) 2013-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cflow.internal.instantiation;

import static com.synflow.cflow.internal.TransformerUtil.getStartLine;
import static com.synflow.models.ir.IrFactory.eINSTANCE;
import static org.eclipse.xtext.EcoreUtil2.getContainerOfType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.util.IResourceScopeCache;
import org.eclipse.xtext.util.Tuples;
import org.eclipse.xtext.xbase.lib.Pair;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.UriComputer;
import com.synflow.cflow.cflow.Bundle;
import com.synflow.cflow.cflow.Connect;
import com.synflow.cflow.cflow.GenericEntity;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.Module;
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.cflow.cflow.Network;
import com.synflow.cflow.cflow.PortDef;
import com.synflow.cflow.cflow.Task;
import com.synflow.cflow.cflow.VarRef;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.cflow.util.CflowSwitch;
import com.synflow.cflow.internal.CopyOf;
import com.synflow.cflow.internal.instantiation.properties.PropertiesSupport;
import com.synflow.cflow.internal.services.Typer;
import com.synflow.cflow.services.Evaluator;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.DpnFactory;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Instance;
import com.synflow.models.dpn.InterfaceType;
import com.synflow.models.dpn.Port;
import com.synflow.models.dpn.Unit;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.Var;
import com.synflow.models.ir.util.ValueUtil;

/**
 * This class defines the default implementation of the mapper.
 * 
 * @author Matthieu Wipliez
 * 
 */
@Singleton
public class MapperImpl extends CflowSwitch<Entity> implements IMapper {

	private List<Resource> builtins;

	@Inject
	private IResourceScopeCache cache;

	private URI context;

	private final Map<NamedEntity, Entity> localCache = new HashMap<>();

	/**
	 * the current connection maker.
	 */
	private ConnectionMaker maker;

	/**
	 * Provides new instances of connection maker.
	 */
	@Inject
	private Provider<ConnectionMaker> provider;

	@Inject
	private IQualifiedNameProvider qualifiedNameProvider;

	@Inject
	private Typer typer;

	public MapperImpl() {
		builtins = new ArrayList<>();
	}

	@Override
	public Entity caseBundle(Bundle bundle) {
		Unit unit = DpnFactory.eINSTANCE.createUnit();
		transformNamedEntity(unit, bundle);
		unit.setProperties(new JsonObject());
		return unit;
	}

	@Override
	public Entity caseNetwork(Network network) {
		DPN dpn = DpnFactory.eINSTANCE.createDPN();
		dpn.init();

		transformGenericEntity(dpn, network);
		for (Inst inst : network.getInstances()) {
			getInstance(inst);
		}

		connect(network, dpn);

		return dpn;
	}

	@Override
	public Entity caseTask(Task task) {
		Actor actor = DpnFactory.eINSTANCE.createActor();
		transformGenericEntity(actor, task);
		return actor;
	}

	/**
	 * Creates a new connection maker and connects the given network. Visits inner tasks first, and
	 * then connect statements.
	 * 
	 * @param network
	 * @param dpn
	 */
	private void connect(Network network, DPN dpn) {
		ConnectionMaker oldMaker = maker;
		maker = provider.get();
		maker.initialize(dpn);

		try {
			// solves references to implicit ports
			new ImplicitPortSwitch(this).doSwitch(network);

			for (Connect connect : network.getConnects()) {
				maker.makeConnection(connect);
			}
		} finally {
			// restore old maker
			maker = oldMaker;
		}
	}

	/**
	 * Creates an instance from the given Inst object.
	 * 
	 * @param inst
	 *            a C~ instance
	 * @return an IR instance
	 */
	private Instance createInstance(Inst inst) {
		Network network = (Network) inst.eContainer();

		// create instance and adds to DPN
		final Instance instance = DpnFactory.eINSTANCE.createInstance();
		instance.setName(inst.getName());
		final DPN dpn = (DPN) getEntity(network);
		dpn.add(instance);

		// get Entity from inst
		final Task task = inst.getTask();
		final GenericEntity genericEntity = task == null ? inst.getEntity() : task;

		try {
			this.context = EcoreUtil.getURI(inst);
			Entity entity = getEntity(genericEntity);
			instance.setEntity(entity);
		} finally {
			this.context = null;
		}

		// set properties. For anonymous tasks, use the task's properties for the instance
		PropertiesSupport support;
		if (task == null) {
			support = new PropertiesSupport(inst);
		} else {
			support = new PropertiesSupport(task);
		}
		support.setProperties(instance);

		return instance;
	}

	@Override
	public Iterable<Resource> getBuiltins() {
		List<Resource> result = builtins;
		builtins = new ArrayList<>();
		return result;
	}

	private Entity getEntity(EObject eObject) {
		NamedEntity entity = getContainerOfType(eObject, NamedEntity.class);
		return getEntity(entity);
	}

	@Override
	public Entity getEntity(final NamedEntity entity) {
		Entity result = localCache.get(entity);
		if (result != null) {
			return result;
		}

		return cache.get(Pair.of(MapperImpl.class.getName(), entity), entity.eResource(),
				new Provider<Entity>() {
					@Override
					public Entity get() {
						try {
							return doSwitch(entity);
						} finally {
							localCache.remove(entity);
						}
					}
				});
	}

	@Override
	public Instance getInstance(final Inst inst) {
		return cache.get(Pair.of(MapperImpl.class.getName(), inst), inst.eResource(),
				new Provider<Instance>() {
					@Override
					public Instance get() {
						return createInstance(inst);
					}
				});
	}

	@Override
	public Port getPort(final Variable port) {
		return cache.get(Pair.of(MapperImpl.class.getName(), port), port.eResource(),
				new Provider<Port>() {
					@Override
					public Port get() {
						InterfaceType ifType = CflowUtil.getInterface(port);
						Type type = typer.doSwitch(port);
						String name = port.getName();

						Port dpnPort = DpnFactory.eINSTANCE.createPort(type, name, ifType);
						PortDef portDef = (PortDef) port.eContainer();
						GenericEntity genericEntity = EcoreUtil2.getContainerOfType(portDef,
								GenericEntity.class);
						Entity entity = getEntity(genericEntity);
						if (CflowUtil.isInput(port)) {
							entity.getInputs().add(dpnPort);
						} else {
							entity.getOutputs().add(dpnPort);
						}
						return dpnPort;
					}
				});
	}

	@Override
	public Port getPort(VarRef refOrCopyOfRef) {
		final VarRef ref = CopyOf.getOriginal(refOrCopyOfRef);

		Variable port = ref.getVariable();
		final Inst inst = EcoreUtil2.getContainerOfType(ref, Inst.class);
		if (inst == null || EcoreUtil.isAncestor(inst, port)) {
			// if the reference is contained in a named task
			// or it is contained in an anonymous task and refers to one of its own ports
			return getPort(port);
		}

		INode node = NodeModelUtils.getNode(ref);
		final String link = NodeModelUtils.getTokenText(node);
		return cache.get(Tuples.create(MapperImpl.class.getName(), inst, link),
				ref.eResource(), new Provider<Port>() {
					@Override
					public Port get() {
						// reference to a port from an instance
						return maker.getConnectedPort(link, inst, ref);
					}
				});
	}

	@Override
	public Procedure getProcedure(Variable function) {
		return getVariable(function, Procedure.class);
	}

	@Override
	public Var getVar(Variable variable) {
		return getVariable(variable, Var.class);
	}

	/**
	 * Returns an IR mapping (Procedure or Var) for the given C~ variable.
	 * 
	 * @param variable
	 *            C~ variable
	 * @param type
	 *            class type to return
	 * @return
	 */
	private <T> T getVariable(final Variable variable, final Class<T> type) {
		final Entity entity = getEntity(variable);

		return cache.get(Pair.of(MapperImpl.class.getName(), variable), variable.eResource(),
				new Provider<T>() {
					@Override
					public T get() {
						return transformVariable(variable, entity, type);
					}
				});
	}

	/**
	 * Registers the C~ entity and adds it to a .ir resource.
	 * 
	 * @param entity
	 *            IR entity
	 * @param cfEntity
	 *            C~ entity
	 */
	private void registerNamedEntity(Entity entity, NamedEntity cfEntity) {
		QualifiedName qualifiedName = qualifiedNameProvider.getFullyQualifiedName(cfEntity);
		if (qualifiedName == null) {
			return;
		}
		String name = qualifiedName.toString();
		entity.setName(name);

		// get URI of .ir file
		URI cfUri = cfEntity.eResource().getURI();
		URI uri = UriComputer.INSTANCE.computeUri(context, cfUri, name);

		// get or create IR resource
		ResourceSet set = cfEntity.eResource().getResourceSet();
		Resource resource = set.getResource(uri, false);
		if (resource == null) {
			resource = set.createResource(uri);
		} else {
			// makes sure the resource does not contain anything else
			resource.getContents().clear();
		}

		// add entity to resource
		resource.getContents().add(entity);

		// if entity is built-in, must serialize it
		if (cfUri.isPlatformPlugin()) {
			builtins.add(resource);
		}
	}

	/**
	 * Initializes the C~ entity from the C~ entity, and adds it to a .ir resource.
	 * 
	 * @param entity
	 *            IR entity
	 * @param genericEntity
	 *            C~ entity
	 */
	private void transformGenericEntity(Entity entity, GenericEntity genericEntity) {
		transformNamedEntity(entity, genericEntity);

		// transform ports
		for (Variable variable : CflowUtil.getPorts(genericEntity.getPortDecls())) {
			getPort(variable);
		}

		// transform properties
		new PropertiesSupport(genericEntity).setProperties(entity);
	}

	/**
	 * Initializes the C~ entity from the C~ entity, and adds it to a .ir resource.
	 * 
	 * @param entity
	 *            IR entity
	 * @param cfEntity
	 *            C~ entity
	 */
	private void transformNamedEntity(Entity entity, NamedEntity cfEntity) {
		// add to cache
		localCache.put(cfEntity, entity);

		// register entity in the resource set
		registerNamedEntity(entity, cfEntity);

		// set file name
		Module module = EcoreUtil2.getContainerOfType(cfEntity, Module.class);
		String fileName = CflowUtil.getFileName(module);
		entity.setFileName(fileName);

		// set line number
		int lineNumber = getStartLine(cfEntity);
		entity.setLineNumber(lineNumber);

		// transform constants
		for (Variable variable : CflowUtil.getStateVars(cfEntity.getDecls())) {
			if (CflowUtil.isConstant(variable)) {
				if (CflowUtil.isFunction(variable)) {
					getProcedure(variable);
				} else {
					getVar(variable);
				}
			}
		}
	}

	/**
	 * Translates the given C~ variable into an IR Procedure or Var.
	 * 
	 * @param variable
	 * @return
	 */
	private <T> T transformVariable(Variable variable, Entity entity, Class<T> cls) {
		int lineNumber = getStartLine(variable);
		Type type = typer.doSwitch(variable);
		String name = variable.getName();

		if (CflowUtil.isFunction(variable)) {
			Procedure procedure = eINSTANCE.createProcedure(name, lineNumber, type);
			entity.getProcedures().add(procedure);
			return cls.cast(procedure);
		} else {
			boolean assignable = !CflowUtil.isConstant(variable);

			// retrieve initial value (may be null)
			Object value = Evaluator.getValue(variable.getValue());
			Expression init = ValueUtil.getExpression(value);

			// create var
			Var var = eINSTANCE.createVar(lineNumber, type, name, assignable, init);

			// add to variables list of containing entity
			entity.getVariables().add(var);
			return cls.cast(var);
		}
	}

}
