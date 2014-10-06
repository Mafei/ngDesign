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
package com.synflow.models.graph.visit;

import java.util.ArrayList;
import java.util.List;

import com.synflow.models.graph.Graph;
import com.synflow.models.graph.Vertex;

public class Finder {

	public static List<Vertex> findFirst(Graph graph) {
		List<Vertex> vertices = new ArrayList<>();
		for (Vertex vertex : graph.getVertices()) {
			if (vertex.getIncoming().isEmpty()) {
				vertices.add(vertex);
			}
		}
		return vertices;
	}

}
