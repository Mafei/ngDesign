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
package com.synflow.cflow.internal.scheduler;

import com.synflow.cflow.internal.scheduler.node.Node;

/**
 * This interface defines methods to handle the scheduling of if statements:
 * <ul>
 * <li>fork</li>
 * <li>join</li>
 * <li>startBranch</li>
 * </ul>
 * 
 * @author Matthieu Wipliez
 *
 */
public interface IfBehavior {

	/**
	 * Forks at the current node.
	 * 
	 * @return the current node.
	 */
	Node fork();

	/**
	 * Joins fork's branches together, collecting input/output patterns of fork's children into
	 * fork's action. This method also sets fork as the current node, but do not clear its children.
	 * 
	 * @param fork
	 *            the node that was saved before visiting branches
	 */
	void join(Node fork);

	/**
	 * Starts a new 'if' branch from the given fork node (obtained from the 'fork' method).
	 * 
	 * @param fork
	 *            the fork node
	 */
	void startBranch(Node fork);

}
