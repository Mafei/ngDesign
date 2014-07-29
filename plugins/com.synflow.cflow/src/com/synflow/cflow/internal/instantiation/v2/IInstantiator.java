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
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.models.dpn.Entity;
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
	void forEachMapping(NamedEntity cxEntity, Executable<Entity> executable);

	/**
	 * Returns the entities that have been instantiated.
	 * 
	 * @return an iterable over entities
	 */
	Iterable<Entity> getEntities();

	Iterable<Entity> getEntities(NamedEntity cxEntity);

	<T extends EObject, U extends EObject> U getMapping(T cxObj);

	<T extends EObject, U extends EObject> void putMapping(T cxObj, U irObj);

	void update(ResourceSet resourceSet);

}
