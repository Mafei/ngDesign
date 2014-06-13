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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EReference;

import com.synflow.models.graph.Edge;
import com.synflow.models.graph.Graph;
import com.synflow.models.graph.GraphPackage;
import com.synflow.models.graph.Vertex;

/**
 * This class defines a reverse post order based on a post-order DFS.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ReversePostOrder extends DFS {

	/**
	 * Creates the reverse post-ordering of the given graph, starting from the
	 * given roots. If <code>roots</code> is <code>null</code> or empty, this
	 * method visits the graph to find roots. If no entry vertex is found, the
	 * first vertex of the graph is used.
	 * 
	 * @param graph
	 *            a graph
	 * @param refEdges
	 *            the EReference that returns either the incoming or outgoing
	 *            edges of a vertex
	 * @param refVertex
	 *            the EReference that returns either the source or target of an
	 *            edge
	 * @param roots
	 *            a list of vertex
	 */
	@SuppressWarnings("unchecked")
	public ReversePostOrder(Graph graph, EReference refEdges,
			EReference refVertex, List<? extends Vertex> roots) {
		super(refEdges, refVertex, graph.getVertices().size());

		if (roots == null || roots.isEmpty()) {
			roots = new ArrayList<Vertex>();
			EReference opposite = refVertex.getEOpposite();
			for (Vertex vertex : graph.getVertices()) {
				List<Edge> edges = (List<Edge>) vertex.eGet(opposite);
				if (edges.isEmpty()) {
					((List<Vertex>) roots).add(vertex);
				}
			}
		}

		if (roots.isEmpty()) {
			// no entry point in the graph, take the first vertex
			if (!graph.getVertices().isEmpty()) {
				visitPost(graph.getVertices().get(0));
			}
		} else {
			for (Vertex vertex : roots) {
				visitPost(vertex);
			}
		}

		Collections.reverse(vertices);
	}

	/**
	 * Creates the reverse post-ordering of the given graph, starting from the
	 * given roots. If <code>roots</code> is <code>null</code> or empty, this
	 * method visits the graph to find roots. If no entry vertex is found, the
	 * first vertex of the graph is used.
	 * 
	 * @param graph
	 *            a graph
	 * @param refEdges
	 *            the EReference that returns either the incoming or outgoing
	 *            edges of a vertex
	 * @param refVertex
	 *            the EReference that returns either the source or target of an
	 *            edge
	 * @param entries
	 *            entry vertices given individually
	 */
	public ReversePostOrder(Graph graph, EReference refEdges,
			EReference refVertex, Vertex... entries) {
		this(graph, refEdges, refVertex, Arrays.asList(entries));
	}

	/**
	 * Creates the reverse post-ordering of the given graph, starting from the
	 * given entries. If <code>entries</code> is <code>null</code> or empty,
	 * this method visits the graph to find roots. If no entry vertex is found,
	 * the first vertex of the graph is used.
	 * 
	 * @param graph
	 *            a graph
	 * @param entries
	 *            a list of vertex
	 */
	public ReversePostOrder(Graph graph, List<? extends Vertex> entries) {
		this(graph, GraphPackage.Literals.VERTEX__OUTGOING,
				GraphPackage.Literals.EDGE__TARGET, entries);
	}

	/**
	 * Creates the reverse post-ordering of the given graph, starting from the
	 * given entries. This is a convenience constructor equivalent to
	 * <code>this(graph, Arrays.asList(entries));</code>.
	 * 
	 * @param graph
	 *            a graph
	 * @param entries
	 *            entry vertices given individually
	 */
	public ReversePostOrder(Graph graph, Vertex... entries) {
		this(graph, Arrays.asList(entries));
	}

}
