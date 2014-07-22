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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.util.IResourceScopeCache;
import org.eclipse.xtext.util.Tuples;
import org.eclipse.xtext.xbase.lib.Pair;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.synflow.cflow.cflow.Connect;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.Instantiable;
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.cflow.cflow.Network;
import com.synflow.cflow.cflow.Task;
import com.synflow.cflow.cflow.VarRef;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.cflow.util.CflowSwitch;
import com.synflow.cflow.internal.CopyOf;
import com.synflow.cflow.internal.instantiation.properties.PropertiesSupport;
import com.synflow.cflow.internal.instantiation.v2.IInstantiator;
import com.synflow.cflow.internal.instantiation.v2.InstInfo;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.DpnFactory;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Instance;
import com.synflow.models.dpn.Port;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Var;

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

	@Inject
	private IInstantiator instantiator;

	/**
	 * the current connection maker.
	 */
	private ConnectionMaker maker;

	/**
	 * Provides new instances of connection maker.
	 */
	@Inject
	private Provider<ConnectionMaker> provider;

	public MapperImpl() {
		builtins = new ArrayList<>();
	}

	@Override
	public Entity caseNetwork(Network network) {
		DPN dpn = DpnFactory.eINSTANCE.createDPN();
		dpn.init();

		for (Inst inst : network.getInstances()) {
			getInstance(inst);
		}

		connect(network, dpn);

		return dpn;
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
		final Instantiable instantiable = task == null ? inst.getEntity() : task;

		Entity entity = getEntity(instantiable);
		instance.setEntity(entity);

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

	@Override
	public Entity getEntity(final NamedEntity entity) {
		return cache.get(Pair.of(MapperImpl.class.getName(), entity), entity.eResource(),
				new Provider<Entity>() {
					@Override
					public Entity get() {
						return doSwitch(entity);
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
	public Iterable<InstInfo> getMappings(NamedEntity entity) {
		return instantiator.getMappings(entity);
	}

	@Override
	public Port getPort(final Variable port) {
		Object key = Pair.of(MapperImpl.class.getName(), port);
		return cache.get(key, port.eResource(), null);
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
		return cache.get(Tuples.create(MapperImpl.class.getName(), inst, link), ref.eResource(),
				new Provider<Port>() {
					@Override
					public Port get() {
						// reference to a port from an instance
						return maker.getConnectedPort(link, inst, ref);
					}
				});
	}

	@Override
	public Procedure getProcedure(Variable function) {
		Resource irResource = null;
		Object key = Tuples.create(MapperImpl.class.getName(), irResource, function);
		return cache.get(key, function.eResource(), null);
	}

	@Override
	public Var getVar(Variable variable) {
		Resource irResource = null;
		Object key = Tuples.create(MapperImpl.class.getName(), irResource, variable);
		return cache.get(key, variable.eResource(), null);
	}

	@Override
	public void restoreMapping() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMapping(InstInfo info) {
		// TODO Auto-generated method stub

	}

}
