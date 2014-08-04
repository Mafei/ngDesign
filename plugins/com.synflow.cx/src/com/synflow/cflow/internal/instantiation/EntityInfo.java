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
package com.synflow.cflow.internal.instantiation;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.synflow.cflow.cflow.CxEntity;
import com.synflow.models.dpn.Entity;

/**
 * This class holds information about a Cx entity, its specialized name and URI of the corresponding
 * IR resource.
 * 
 * @author Matthieu Wipliez
 *
 */
public class EntityInfo {

	private final CxEntity cxEntity;

	private final String name;

	private final URI uri;

	public EntityInfo(CxEntity cxEntity, String name, URI uri) {
		this.cxEntity = cxEntity;
		this.name = name;
		this.uri = uri;
	}

	/**
	 * Creates a resource with the URI given to the constructor, and adds the given entity to it.
	 * 
	 * @param entity
	 *            an entity
	 */
	public void createResource(Entity entity) {
		ResourceSet set = cxEntity.eResource().getResourceSet();
		Resource resource = set.createResource(uri);
		resource.getContents().add(entity);
	}

	public CxEntity getCxEntity() {
		return cxEntity;
	}

	public String getName() {
		return name;
	}

}
