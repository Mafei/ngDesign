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
package com.synflow.models.node;

import java.util.Iterator;

/**
 * This class defines an iterable over nodes.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class NodeIterable implements Iterable<Node> {

	private Node node;

	public NodeIterable(Node node) {
		this.node = node;
	}

	@Override
	public Iterator<Node> iterator() {
		return new NodeIterator(node);
	}

}
