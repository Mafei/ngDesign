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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.google.inject.ImplementedBy;
import com.synflow.cflow.cflow.CxEntity;
import com.synflow.cflow.cflow.VarRef;
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
	 * For each IR entity associated to the given Cx entity, calls the executable. setEntity is
	 * called before and after the runnable to set the current entity.
	 * 
	 * @param cxEntity
	 *            Cx entity
	 * @param executable
	 *            an executable
	 */
	void forEachMapping(CxEntity cxEntity, Executable<Entity> executable);

	/**
	 * Returns the entities that have been instantiated.
	 * 
	 * @return an iterable over entities
	 */
	Iterable<Entity> getEntities();

	/**
	 * Returns the IR object that corresponds to the given Cx object.
	 * 
	 * @param cxObj
	 *            a Cx object (entity, variable, port...)
	 * @return the IR mapping
	 */
	<T extends EObject, U extends EObject> U getMapping(T cxObj);

	/**
	 * Returns the IR port that corresponds to the given reference.
	 * 
	 * @param ref
	 *            a reference to a Cx port
	 * @return an IR port
	 */
	Port getPort(VarRef ref);

	<T extends EObject, U extends EObject> void putMapping(T cxObj, U irObj);

	void update(ResourceSet resourceSet);

}
