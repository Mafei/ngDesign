/*******************************************************************************
 * Copyright (c) 2013-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.internal.scheduler.path;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.synflow.cx.cx.Branch;
import com.synflow.cx.cx.CExpression;
import com.synflow.cx.internal.scheduler.node.Node;
import com.synflow.cx.services.CflowPrinter;

/**
 * This class defines a code path as an iterable over StatementCond.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class Path {

	private Deque<Branch> conds;

	private Iterator<Branch> it;

	public Path() {
		conds = new ArrayDeque<>();
	}

	public void add(Node node) {
		Object content = node.getContent();
		if (content instanceof Branch) {
			Branch cond = (Branch) content;
			conds.addFirst(cond);
		}
	}

	/**
	 * Returns the next statement cond of this path.
	 * 
	 * @return a StatementCond
	 */
	public Branch getNext() {
		if (it == null) {
			it = conds.iterator();
		}
		return it.next();
	}

	@Override
	public String toString() {
		return Joiner.on(", ").join(Iterables.transform(conds, new Function<Branch, String>() {
			@Override
			public String apply(Branch cond) {
				CExpression condition = cond.getCondition();
				if (condition == null) {
					return "(else)";
				} else {
					return new CflowPrinter().toString(condition);
				}
			}
		}));
	}

}
