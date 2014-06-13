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
package com.synflow.core.internal.exporters;

import static com.synflow.models.util.SwitchUtil.CASCADE;
import static com.synflow.models.util.SwitchUtil.DONE;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.EObject;

import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Instance;
import com.synflow.models.dpn.util.DpnSwitch;
import com.synflow.models.util.Void;

/**
 * This class computes an order for compilation of entities.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class NetworkVisitor extends DpnSwitch<Void> {

	private final Set<Entity> entities;

	private IProject project;

	private final Map<EObject, IProject> projectMap;

	public NetworkVisitor(IProject project) {
		entities = new LinkedHashSet<Entity>();
		projectMap = new HashMap<>();
		this.project = project;
	}

	@Override
	public Void caseDPN(DPN network) {
		for (Instance instance : network.getInstances()) {
			Entity entity = instance.getEntity();
			if (entity instanceof DPN) {
				DPN subNetwork = (DPN) entity;

				// update project for this network and its children
				IProject oldProject = project;
				project = subNetwork.getFile().getProject();
				doSwitch(subNetwork);
				project = oldProject;
			} else {
				doSwitch(entity);
			}
		}

		return CASCADE;
	}

	@Override
	public Void caseEntity(Entity entity) {
		entities.add(entity);
		projectMap.put(entity, project);
		return DONE;
	}

	protected final Set<Entity> getEntities() {
		return entities;
	}

	public Map<EObject, IProject> getProjectMap() {
		return projectMap;
	}

}