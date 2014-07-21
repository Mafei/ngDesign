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

import static com.synflow.cflow.internal.TransformerUtil.getStartLine;
import static com.synflow.models.util.SwitchUtil.DONE;

import java.util.Map;

import org.eclipse.xtext.EcoreUtil2;

import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.Bundle;
import com.synflow.cflow.cflow.Module;
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.cflow.cflow.Network;
import com.synflow.cflow.cflow.Task;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Unit;
import com.synflow.models.dpn.util.DpnSwitch;
import com.synflow.models.util.Void;

/**
 * This class creates the skeleton of an IR entity: state variables, ports.
 * 
 * @author Matthieu Wipliez
 *
 */
public class SkeletonMaker extends DpnSwitch<Void> {

	private Map<Entity, NamedEntity> map;

	@Override
	public Void caseActor(Actor actor) {
		Task task = (Task) map.get(actor);
		setFileAndLine(actor, task);
		try {
			setValues();
			translateStateVars();
			translatePorts();
		} finally {
			restoreValues();
		}
		return DONE;
	}

	@Override
	public Void caseDPN(DPN dpn) {
		Network network = (Network) map.get(dpn);
		setFileAndLine(dpn, network);
		try {
			setValues();
			translateStateVars();
			translatePorts();
		} finally {
			restoreValues();
		}
		return DONE;
	}

	@Override
	public Void caseUnit(Unit unit) {
		Bundle bundle = (Bundle) map.get(unit);
		setFileAndLine(unit, bundle);
		try {
			setValues();
			translateStateVars();
		} finally {
			restoreValues();
		}
		return DONE;
	}

	public void createSkeleton(Map<Entity, NamedEntity> map, Entity entity) {
		this.map = map;
		try {
			doSwitch(entity);
		} finally {
			this.map = null;
		}
	}

	private void restoreValues() {
		// TODO Auto-generated method stub

	}

	/**
	 * Sets filename and line number of IR entity from Cx entity.
	 * 
	 * @param entity
	 *            IR entity
	 * @param cxEntity
	 *            Cx entity
	 */
	private void setFileAndLine(Entity entity, NamedEntity cxEntity) {
		// set file name
		Module module = EcoreUtil2.getContainerOfType(cxEntity, Module.class);
		String fileName = CflowUtil.getFileName(module);
		entity.setFileName(fileName);

		// set line number
		int lineNumber = getStartLine(cxEntity);
		entity.setLineNumber(lineNumber);
	}

	private void setValues() {
		// TODO Auto-generated method stub

	}

	private void translatePorts() {
		// TODO Auto-generated method stub

	}

	private void translateStateVars() {
		// TODO
		// for (Variable variable : xxx) {
		// cache.get(variable, resource, new Provider<>() {
		// void apply() {
		// transformVariable();
		// }
		// });
		// }
	}

}
