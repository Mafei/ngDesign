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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.synflow.models.graph.Vertex;

/**
 * This class defines an ordering.
 * 
 * @author Matthieu Wipliez
 * 
 */
public abstract class Ordering implements Iterable<Vertex> {

	protected final List<Vertex> vertices;

	protected final Set<Vertex> visited;

	/**
	 * Creates a new topological sorter.
	 */
	public Ordering() {
		vertices = new ArrayList<Vertex>();
		visited = new HashSet<Vertex>();
	}

	/**
	 * Creates a new topological sorter.
	 * 
	 * @param n
	 *            the expected number of vertices
	 */
	protected Ordering(int n) {
		vertices = new ArrayList<Vertex>(n);
		visited = new HashSet<Vertex>(n);
	}

	/**
	 * Returns the list of vertices in the specified order.
	 * 
	 * @return the list of vertices in the specified order
	 */
	public List<Vertex> getVertices() {
		return vertices;
	}

	@Override
	public Iterator<Vertex> iterator() {
		return vertices.iterator();
	}

}
