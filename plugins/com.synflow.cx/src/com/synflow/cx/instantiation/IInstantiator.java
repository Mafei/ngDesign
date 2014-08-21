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
package com.synflow.cx.instantiation;

import org.eclipse.emf.ecore.EObject;

import com.google.inject.ImplementedBy;
import com.synflow.cx.cx.CxEntity;
import com.synflow.cx.cx.Module;
import com.synflow.cx.cx.VarRef;
import com.synflow.cx.internal.instantiation.InstantiatorImpl;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Port;
import com.synflow.models.util.Executable;

/**
 * This interface defines an instantiator.
 * 
 * @author Matthieu Wipliez
 * 
 */
@ImplementedBy(InstantiatorImpl.class)
public interface IInstantiator {

	/**
	 * Clears all data retained by the instantiator.
	 */
	void clearData();

	/**
	 * Retrieves all IR entities associated to the given Cx entity, and for each <code>entity</code>
	 * , calls <code>executable.exec(entity)</code>.
	 * 
	 * @param cxEntity
	 *            Cx entity
	 * @param executable
	 *            an executable
	 * @see #execute(Entity, Executable)
	 */
	void forEachMapping(CxEntity cxEntity, Executable<Entity> executable);

	/**
	 * Returns the built-in entities that have been instantiated, and clears the internal copy
	 * maintained by this instantiator.
	 * 
	 * @return an iterable over entities
	 */
	Iterable<Entity> getBuiltins();

	/**
	 * Returns the IR object that corresponds to the given Cx object in the given entity.
	 * 
	 * @param entity
	 *            the entity to use to find the mapping
	 * @param cxObj
	 *            a Cx object (entity, variable, port...)
	 * @return the IR mapping
	 */
	<T extends EObject, U extends EObject> U getMapping(Entity entity, T cxObj);

	/**
	 * Returns the IR port that corresponds to the given reference.
	 * 
	 * @param entity
	 *            the entity in which the mapping exists
	 * @param ref
	 *            a reference to a Cx port
	 * @return an IR port
	 */
	Port getPort(Entity entity, VarRef ref);

	/**
	 * Adds a mapping from the given Cx object to the given IR object in the given entity.
	 * 
	 * @param entity
	 *            an IR entity
	 * @param cxObj
	 *            a Cx object (entity, variable, port...)
	 * @param irObj
	 *            the IR object that corresponds to <code>cxObj</code> in the given entity
	 */
	<T extends EObject, U extends EObject> void putMapping(Entity entity, T cxObj, U irObj);

	/**
	 * Updates the instantiation tree with entities of the given module. Most of the time this
	 * method only performs a partial update of the tree.
	 * 
	 * @param module
	 *            a module
	 */
	void update(Module module);

}
