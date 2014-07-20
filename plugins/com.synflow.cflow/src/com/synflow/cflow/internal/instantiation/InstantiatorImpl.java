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

import static com.synflow.models.util.SwitchUtil.DONE;
import static com.synflow.models.util.SwitchUtil.visit;

import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.Instantiable;
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.cflow.cflow.Network;
import com.synflow.cflow.cflow.util.CflowSwitch;
import com.synflow.models.dpn.Entity;
import com.synflow.models.util.Void;

/**
 * This class defines the default implementation of the instantiator.
 * 
 * @author Matthieu Wipliez
 * 
 */
@Singleton
public class InstantiatorImpl extends CflowSwitch<Void> implements IInstantiator {

	private List<Entity> entities;

	@Inject
	private EntityMaker entityMaker;

	private Map<Entity, NamedEntity> map;

	@Inject
	private SkeletonMaker skeletonMaker;

	@Override
	public Void caseInst(Inst inst) {
		translateEntity(inst);
		return DONE;
	}

	@Override
	public Void caseNetwork(Network network) {
		visit(this, network.getInstances());
		return DONE;
	}

	@Override
	public void instantiate(Instantiable entity) {
		doSwitch(entity);
	}

	private Entity translateEntity(Inst inst) {
		Entity entity = entityMaker.createEntity(map, inst);
		entities.add(entity);
		skeletonMaker.createSkeleton(map, entity);
		return entity;
	}

}
