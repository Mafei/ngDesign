package com.synflow.cflow.internal.instantiation.v2;

import org.eclipse.emf.common.util.URI;

import com.google.gson.JsonObject;
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.cflow.internal.scheduler.node.Node;
import com.synflow.models.dpn.Entity;

public class InstInfo {

	private URI cxUri;

	private URI irUri;

	private Node node;

	private JsonObject properties;
	
	private InstModel model;

	public Iterable<Entity> getMappings(NamedEntity entity) {
		return model.getMappings(entity);
	}

}
