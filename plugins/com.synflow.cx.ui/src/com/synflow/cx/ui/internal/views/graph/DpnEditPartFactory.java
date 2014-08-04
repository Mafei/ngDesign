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
package com.synflow.cx.ui.internal.views.graph;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import com.synflow.models.dpn.Connection;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Instance;
import com.synflow.models.dpn.Port;

/**
 * This class defines the edit part factory for the DPN model.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class DpnEditPartFactory implements EditPartFactory {

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		AbstractGraphicalEditPart part;

		if (model instanceof DPN) {
			part = new DPNPart();
		} else if (model instanceof Instance) {
			part = new InstancePart();
		} else if (model instanceof Port) {
			part = new PortPart();
		} else if (model instanceof Connection) {
			part = new ConnectionPart();
		} else {
			return null;
		}

		part.setModel(model);
		return part;
	}

}
