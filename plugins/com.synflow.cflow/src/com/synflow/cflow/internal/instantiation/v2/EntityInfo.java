package com.synflow.cflow.internal.instantiation.v2;

import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.models.dpn.Entity;

public class EntityInfo {

	private NamedEntity cxEntity;

	public String name;

	public URI uri;

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
