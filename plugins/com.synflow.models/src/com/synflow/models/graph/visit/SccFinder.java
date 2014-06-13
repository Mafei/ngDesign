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
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.synflow.models.graph.Edge;
import com.synflow.models.graph.Graph;
import com.synflow.models.graph.Vertex;

/**
 * This class implements Tarjan's strongly connected components algorithm.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class SccFinder {

	private int index;

	private final Map<Vertex, Integer> indexMap;

	private final Map<Vertex, Integer> lowlinkMap;

	private List<List<Vertex>> sccs;

	private final Deque<Vertex> stack;

	public SccFinder() {
		indexMap = new HashMap<Vertex, Integer>();
		lowlinkMap = new HashMap<Vertex, Integer>();
		stack = new ArrayDeque<Vertex>();
	}

	public List<List<Vertex>> visitGraph(Graph graph) {
		sccs = new ArrayList<List<Vertex>>();
		index = 0;
		for (Vertex vertex : graph.getVertices()) {
			if (!indexMap.containsKey(vertex)) {
				strongConnect(vertex);
			}
		}

		return sccs;
	}

	private void strongConnect(Vertex v) {
		// Set the depth index for v to the smallest unused index
		indexMap.put(v, index);
		lowlinkMap.put(v, index);
		index++;
		stack.push(v);

		// Consider successors of v
		for (Edge edge : v.getOutgoing()) {
			Vertex w = edge.getTarget();
			if (!indexMap.containsKey(w)) {
				// Successor w has not yet been visited; recurse on it
				strongConnect(w);
				lowlinkMap.put(v,
						Math.min(lowlinkMap.get(v), lowlinkMap.get(w)));
			} else if (stack.contains(w)) {
				// Successor w is in stack S and hence in the current SCC
				lowlinkMap.put(v, Math.min(lowlinkMap.get(v), indexMap.get(w)));
			}
		}

		// If v is a root node, pop the stack and generate an SCC
		if (lowlinkMap.get(v) == indexMap.get(v)) {
			List<Vertex> scc = new ArrayList<Vertex>();
			Vertex w;
			do {
				w = stack.pop();
				scc.add(w);
			} while (w != v);
			sccs.add(scc);
		}
	}

}
