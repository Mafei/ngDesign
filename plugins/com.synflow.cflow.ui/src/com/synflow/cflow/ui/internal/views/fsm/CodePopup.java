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
package com.synflow.cflow.ui.internal.views.fsm;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.synflow.cflow.ui.CflowEditorCreator;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.State;
import com.synflow.models.dpn.Transition;
import com.synflow.models.graph.Edge;

/**
 * This class defines a code popup that shows C~ code.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CodePopup extends PopupDialog {

	private Composite composite;

	private IFile file;

	private Transition transition;

	private Point location;

	public CodePopup(Shell parent) {
		super(parent, SWT.TOOL, false, false, false, false, false, null, null);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		CflowEditorCreator creator = CflowEditorCreator.get();
		composite = new Composite(parent, SWT.NONE);
		creator.createEditor(file, transition, composite);
		composite.setLayout(new GridLayout());
		return composite;
	}

	public Composite getComposite() {
		return composite;
	}

	@Override
	protected Point getInitialLocation(Point initialSize) {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public void setModel(State state) {
		for (Edge edge : state.getOutgoing()) {
			Transition transition = (Transition) edge;
			setModel(transition);
			return;
		}
	}

	public void setModel(Transition transition) {
		Actor actor = (Actor) transition.eContainer().eContainer();
		file = actor.getFile();
		this.transition = transition;
	}

}
