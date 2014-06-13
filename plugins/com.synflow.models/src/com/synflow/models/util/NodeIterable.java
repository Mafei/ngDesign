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
package com.synflow.models.util;

import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.UnmodifiableIterator;

/**
 * This class defines an iterable over Nodes.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class NodeIterable implements Iterable<Node> {

	private class NodeIterator extends UnmodifiableIterator<Node> {

		private int i;

		public NodeIterator() {
			i = 0;
		}

		@Override
		public boolean hasNext() {
			return i < nodes.getLength();
		}

		@Override
		public Node next() {
			return nodes.item(i++);
		}

	}

	private NodeList nodes;

	public NodeIterable(NodeList nodes) {
		this.nodes = nodes;
	}

	@Override
	public Iterator<Node> iterator() {
		return new NodeIterator();
	}

}
