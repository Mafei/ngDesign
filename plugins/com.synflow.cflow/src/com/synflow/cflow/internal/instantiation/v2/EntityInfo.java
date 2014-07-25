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

import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.models.dpn.Entity;

/**
 * This class holds information about a Cx entity, its specialized name and URI of the corresponding
 * IR resource.
 * 
 * @author Matthieu Wipliez
 *
 */
public class EntityInfo {

	private NamedEntity cxEntity;

	private String name;

	private URI uri;

	public EntityInfo(NamedEntity cxEntity, String name, URI uri) {
		this.cxEntity = cxEntity;
		this.name = name;
		this.uri = uri;
	}

	public NamedEntity getCxEntity() {
		return cxEntity;
	}

	public String getName() {
		return name;
	}

	public Resource getResource() {
		// get or create IR resource
		ResourceSet set = cxEntity.eResource().getResourceSet();
		Resource resource = set.getResource(uri, false);
		if (resource == null) {
			resource = set.createResource(uri);
		}
		return resource;
	}

	public Entity loadEntity() {
		Resource resource = getResource();
		try {
			resource.load(null);
		} catch (IOException e) {
			return null;
		}
		return (Entity) resource.getContents().get(0);
	}

}
