package com.synflow.cflow.internal.instantiation.v2;

import org.eclipse.xtext.util.IResourceScopeCache;

import com.google.inject.Provider;
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.cflow.internal.scheduler.node.Node;
import com.synflow.models.dpn.Entity;

public class InstModel {

	private Node node;

	private IResourceScopeCache cache;

	public InstInfo getInfo(NamedEntity entity) {
		return new InstInfo();
	}

	public Iterable<Entity> getMappings(final NamedEntity cxEntity) {
		Object key = null;
		return cache.get(key, cxEntity.eResource(), new Provider<Iterable<Entity>>() {
			@Override
			public Iterable<Entity> get() {
				return null;
			}
		});
	}

}
