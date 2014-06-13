/*******************************************************************************
 * Copyright (c) 2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.core.transformations;

import static com.synflow.models.util.SwitchUtil.DONE;

import com.synflow.models.dpn.Action;
import com.synflow.models.dpn.Actor;
import com.synflow.models.ir.util.AbstractIrVisitor;
import com.synflow.models.util.Void;

/**
 * This class defines a module transformation that transforms the body of actions.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class BodyTransformation extends ModuleTransformation {

	public BodyTransformation(AbstractIrVisitor irVisitor) {
		super(irVisitor);
	}

	@Override
	public Void caseAction(Action action) {
		doSwitch(action.getBody());
		return DONE;
	}

	@Override
	public Void caseActor(Actor actor) {
		for (Action action : actor.getActions()) {
			doSwitch(action);
		}

		return DONE;
	}

}
