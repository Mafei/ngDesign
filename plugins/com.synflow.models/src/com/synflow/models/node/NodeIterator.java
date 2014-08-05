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

import java.util.ListIterator;

/**
 * This class defines an iterator over a node's children.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class NodeIterator implements ListIterator<Node> {

	private Node currentNode;

	private Node referenceNode;

	public NodeIterator(NodeIterator it) {
		currentNode = it.currentNode;
		referenceNode = it.referenceNode;
	}

	public NodeIterator(Node node) {
		this.referenceNode = node;
	}

	@Override
	public void add(Node e) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the current node.
	 * 
	 * @return a node
	 */
	public Node current() {
		if (currentNode == null) {
			currentNode = referenceNode.getFirstChild();
		}
		return currentNode;
	}

	/**
	 * Returns what the next node would be, but does not advance this iterator.
	 * 
	 * @return a node
	 */
	public Node getNext() {
		if (currentNode == null) {
			return referenceNode.getFirstChild();
		} else {
			return currentNode.getNextSibling();
		}
	}

	/**
	 * Returns what the previous node would be, but does not advance this iterator.
	 * 
	 * @return a node
	 */
	private Node getPrevious() {
		if (currentNode == null) {
			return referenceNode.getLastChild();
		} else {
			return currentNode.getPreviousSibling();
		}
	}

	@Override
	public boolean hasNext() {
		return getNext() != null;
	}

	@Override
	public boolean hasPrevious() {
		return getPrevious() != null;
	}

	@Override
	public Node next() {
		currentNode = getNext();
		return currentNode;
	}

	@Override
	public int nextIndex() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Node previous() {
		currentNode = getPrevious();
		return currentNode;
	}

	@Override
	public int previousIndex() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Resets this node iterator.
	 */
	public void reset() {
		currentNode = null;
	}

	@Override
	public void set(Node e) {
		throw new UnsupportedOperationException();
	}

}
