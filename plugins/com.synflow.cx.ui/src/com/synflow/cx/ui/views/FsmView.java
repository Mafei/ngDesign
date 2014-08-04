/*******************************************************************************
 * Copyright (c) 2012-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.StyledText;

import com.synflow.cx.ui.internal.views.fsm.FsmEditPartFactory;
import com.synflow.cx.ui.internal.views.fsm.layout.FsmLayout;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.FSM;
import com.synflow.models.dpn.Transition;
import com.synflow.models.ir.util.IrUtil;

/**
 * This class defines a view of the FSM of a task.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class FsmView extends AbstractGefView {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.synflow.cx.ui.views.FsmView";

	private FSM fsm;

	@Override
	protected void clearViewer() {
		super.clearViewer();
		setPartName("(no FSM)");
	}

	@Override
	protected EditPartFactory getEditPartFactory() {
		return new FsmEditPartFactory();
	}

	@Override
	protected boolean irFileLoaded(Entity entity) {
		if (entity instanceof Actor) {
			// fsm is actor's FSM (may be null)
			Actor actor = (Actor) entity;
			fsm = actor.getFsm();
			return (fsm != null);
		}

		return false;
	}

	@Override
	protected void reset() {
		super.reset();
		fsm = null;
	}

	@Override
	protected void selectLine(StyledText text, int offset) {
		super.selectLine(text, offset);

		if (fsm == null) {
			// no FSM, clears view and leave
			clearViewer();
			return;
		}

		int line = text.getLineAtOffset(offset) + 1;
		List<EditPart> parts = new ArrayList<>();
		for (Transition transition : fsm.getTransitions()) {
			if (transition.getLines().contains(line)) {
				EditPart part = (EditPart) viewer.getEditPartRegistry().get(transition);
				parts.add(part);
			}
		}

		// selects all appropriate edit parts
		viewer.setSelection(new StructuredSelection(parts));

		// reveals the first one (avoids scrolling when FSM is big)
		Iterator<EditPart> it = parts.iterator();
		if (it.hasNext()) {
			viewer.reveal(it.next());
		}
	}

	@Override
	protected void updateViewer() {
		// set part name
		String name = IrUtil.getSimpleName(getEntityName());
		setPartName(fsm.getStates().size() + " states, " + fsm.getTransitions().size()
				+ " transitions (" + name + ")");

		// layout FSM
		new FsmLayout().visit(fsm);

		viewer.setContents(fsm);
	}

}
