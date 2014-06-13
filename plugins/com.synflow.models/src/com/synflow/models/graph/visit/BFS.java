/*******************************************************************************
 * Copyright (c) 2012 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.models.graph.visit;

import java.util.ArrayDeque;
import java.util.Deque;

import com.synflow.models.graph.Vertex;

/**
 * This class defines Breadth-First Search (BFS) for a graph.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class BFS extends Ordering {

	/**
	 * Builds the list of vertices that can be reached from the given vertex
	 * using breadth-first search.
	 * 
	 * @param vertex
	 *            a vertex
	 */
	public BFS(Vertex vertex) {
		visitVertex(vertex);
	}

	/**
	 * Builds the search starting from the given vertex.
	 * 
	 * @param vertex
	 *            a vertex
	 */
	public void visitVertex(Vertex vertex) {
		Deque<Vertex> visitList = new ArrayDeque<Vertex>();
		visitList.addLast(vertex);

		while (!visitList.isEmpty()) {
			Vertex next = visitList.removeFirst();

			// only adds the successors if they have not been visited yet.
			if (!visited.contains(next)) {
				visited.add(next);
				vertices.add(next);
				for (Vertex succ : next.getSuccessors()) {
					visitList.addLast(succ);
				}
			}
		}
	}

}
