package com.synflow.cflow.internal.instantiation.v2;

import org.eclipse.emf.common.util.URI;

import com.google.gson.JsonObject;
import com.synflow.models.dpn.Entity;

public class InstInfo {

	private URI cxUri;

	private Entity entity;

	private URI irUri;

	private JsonObject properties;

	public InstInfo(Entity entity) {
		this.entity = entity;
	}

	public Entity getEntity() {
		return entity;
	}

}
