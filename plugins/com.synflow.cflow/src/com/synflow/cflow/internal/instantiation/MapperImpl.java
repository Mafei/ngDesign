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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.VarRef;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.cflow.util.CflowSwitch;
import com.synflow.cflow.internal.instantiation.v2.IInstantiator;
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
	private IInstantiator instantiator;

	public MapperImpl() {
		builtins = new ArrayList<>();
	}

	@Override
	public Iterable<Resource> getBuiltins() {
		List<Resource> result = builtins;
		builtins = new ArrayList<>();
		return result;
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
		return instantiator.getPort(refOrCopyOfRef);
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
