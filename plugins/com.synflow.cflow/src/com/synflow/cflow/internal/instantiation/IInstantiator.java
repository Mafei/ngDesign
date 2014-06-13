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

import org.eclipse.emf.ecore.resource.Resource;

import com.google.inject.ImplementedBy;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.cflow.cflow.VarRef;
import com.synflow.cflow.cflow.Variable;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Instance;
import com.synflow.models.dpn.Port;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Var;

/**
 * This interface defines an instantiator.
 * 
 * @author Matthieu Wipliez
 * 
 */
@ImplementedBy(InstantiatorImpl.class)
public interface IInstantiator {

	/**
	 * Returns the built-in resources created during instantiation, and clears the internal copy
	 * maintained by this instantiator.
	 * 
	 * @return an iterable over resources
	 */
	Iterable<Resource> getBuiltins();

	/**
	 * Returns the IR entity associated with the given named entity. This also translates properties
	 * and (if the entity is a network) connects it (and does so recursively for all entities
	 * instantiated). The mapping between the C~ model and the IR instantiated model is cached, so
	 * in practice this method returns very fast most of the time, except when resources are dirty.
	 * 
	 * @param entity
	 *            a C~ entity
	 * @return an IR entity
	 */
	Entity getEntity(NamedEntity entity);

	/**
	 * Returns the IR instance associated with the given instance.
	 * 
	 * @param inst
	 *            a C~ instance
	 * @return an IR instance
	 */
	Instance getInstance(Inst inst);

	/**
	 * Returns the IR port associated with the given variable.
	 * 
	 * @param variable
	 *            a C~ port variable
	 * @return an IR port
	 */
	Port getPort(Variable variable);

	/**
	 * Returns the IR port associated with the given variable reference. If referenced from an
	 * anonymous task, this method will add a new port to the IR instance with resolved type. In
	 * other cases, the port returned may have a generic type.
	 * 
	 * @param ref
	 *            reference to a C~ port variable
	 * @return an IR port
	 */
	Port getPort(VarRef ref);

	Procedure getProcedure(Variable function);

	Var getVar(Variable variable);

}
