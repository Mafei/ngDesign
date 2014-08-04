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
package com.synflow.cflow.ui.views;

import org.eclipse.gef.EditPartFactory;
import org.eclipse.swt.custom.StyledText;

import com.synflow.cflow.ui.internal.views.graph.DpnEditPartFactory;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Entity;
import com.synflow.models.ir.util.IrUtil;

/**
 * This class defines a view of the graph of a DPN.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class GraphView extends AbstractGefView {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.synflow.cflow.ui.views.GraphView";

	private DPN dpn;

	@Override
	protected void clearViewer() {
		super.clearViewer();
		setPartName("(no graph)");
	}

	@Override
	protected EditPartFactory getEditPartFactory() {
		return new DpnEditPartFactory();
	}

	@Override
	protected boolean irFileLoaded(Entity entity) {
		if (entity instanceof DPN) {
			dpn = (DPN) entity;
			return true;
		}
		return false;
	}

	@Override
	protected void reset() {
		super.reset();
		dpn = null;
	}

	@Override
	protected void selectLine(StyledText text, int offset) {
		super.selectLine(text, offset);

		if (dpn == null) {
			// no FSM, clears view and leave
			clearViewer();
			return;
		}
	}

	@Override
	protected void updateViewer() {
		// set part name
		String name = IrUtil.getSimpleName(getEntityName());
		setPartName(name);

		viewer.setContents(dpn);
	}

}
