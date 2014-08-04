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
package com.synflow.cflow.internal.services;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import com.synflow.core.SynflowCore;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.FSM;
import com.synflow.models.dpn.State;
import com.synflow.models.dpn.Transition;
import com.synflow.models.graph.Edge;
import com.synflow.models.graph.Vertex;

/**
 * This class defines a simple edge-coloring BFS implementation.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class EdgeColoring {

	public static final String TYPE = "com.synflow.cflow.cycleIndicator";

	private IFile file;

	private int index;

	private Set<Vertex> visited;

	public EdgeColoring(IFile file) {
		this.file = file;
		visited = new HashSet<>();

		try {
			file.deleteMarkers(TYPE, false, 1);
		} catch (CoreException e) {
			SynflowCore.log(e);
		}
	}

	private String getColor() {
		if (index == 0) {
			return "4169E1";
		} else {
			return "87CEEB";
		}
	}

	public void visit(Actor actor) {
		FSM fsm = actor.getFsm();
		if (fsm == null) {
			return;
		}

		State state = fsm.getInitialState();
		visit(state);
	}

	public void visit(Edge edge) {
		Transition transition = (Transition) edge;

		try {
			for (int line : transition.getLines()) {
				IMarker marker = file.createMarker(TYPE);
				marker.setAttribute(IMarker.LINE_NUMBER, line);
				marker.setAttribute("color", getColor());
			}
		} catch (CoreException e) {
			SynflowCore.log(e);
		}
	}

	private void visit(Vertex vertex) {
		Deque<Vertex> visitList = new ArrayDeque<Vertex>();
		visitList.addLast(vertex);

		while (!visitList.isEmpty()) {
			Vertex next = visitList.removeFirst();

			// only adds the successors if they have not been visited yet.
			if (!visited.contains(next)) {
				visited.add(next);

				for (Edge edge : next.getOutgoing()) {
					visit(edge);
				}
				index = (index + 1) & 1;

				for (Vertex succ : next.getSuccessors()) {
					visitList.addLast(succ);
				}
			}
		}
	}

}
