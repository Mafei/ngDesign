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
package com.synflow.cx.ui.internal.views.fsm;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import com.synflow.models.dpn.FSM;
import com.synflow.models.dpn.State;
import com.synflow.models.dpn.Transition;

/**
 * This class defines the edit part factory for the FSM model.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class FsmEditPartFactory implements EditPartFactory {

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		AbstractGraphicalEditPart part;

		if (model instanceof FSM) {
			part = new FsmPart();
		} else if (model instanceof State) {
			part = new StatePart();
		} else if (model instanceof Transition) {
			part = new TransitionPart();
		} else {
			return null;
		}

		part.setModel(model);
		return part;
	}

}
