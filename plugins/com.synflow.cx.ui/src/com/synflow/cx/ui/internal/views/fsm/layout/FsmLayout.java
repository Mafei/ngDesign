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
package com.synflow.cx.ui.internal.views.fsm.layout;

import com.synflow.models.dpn.FSM;

/**
 * This class defines a layout creator for an FSM.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class FsmLayout {

	public void visit(FSM fsm) {
		Box top = new VertexLayout().layoutVertices(fsm);
		new EdgeLayout().layoutEdges(fsm, top);
	}

}
