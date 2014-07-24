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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.synflow.cflow.cflow.Connect;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.cflow.cflow.Network;
import com.synflow.cflow.cflow.VarRef;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.cflow.util.CflowSwitch;
import com.synflow.cflow.internal.CopyOf;
import com.synflow.cflow.internal.instantiation.v2.IInstantiator;
import com.synflow.models.dpn.DPN;
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

	@Override
	public Iterable<Resource> getBuiltins() {
		List<Resource> result = builtins;
		builtins = new ArrayList<>();
		return result;
	}

	@Override
	public Entity getEntity(final NamedEntity entity) {
		return instantiator.getMapping(entity);
	}

	@Override
	public Instance getInstance(final Inst inst) {
		return instantiator.getMapping(inst);
	}

	@Override
	public Port getPort(final Variable port) {
		return instantiator.getMapping(port);
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
		return instantiator.getMapping(function);
	}

	@Override
	public Var getVar(Variable variable) {
		return instantiator.getMapping(variable);
	}

}
