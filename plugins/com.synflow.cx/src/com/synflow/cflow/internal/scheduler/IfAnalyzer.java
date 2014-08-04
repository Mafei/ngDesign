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
package com.synflow.cflow.internal.scheduler;

import static com.synflow.cflow.internal.AstUtil.cond;
import static com.synflow.models.util.SwitchUtil.DONE;
import static com.synflow.models.util.SwitchUtil.visit;

import java.util.List;

import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.Branch;
import com.synflow.cflow.cflow.StatementIf;
import com.synflow.cflow.internal.scheduler.node.Node;
import com.synflow.cflow.internal.services.VoidCflowSwitch;
import com.synflow.models.util.Void;

/**
 * This class builds an n-ary hierarchical tree that matches the conditions of the C~ code.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class IfAnalyzer extends VoidCflowSwitch {

	private Node node;

	public IfAnalyzer() {
		node = new Node();
	}

	@Override
	public Void caseBranch(Branch stmt) {
		node = new Node(node, stmt);
		visit(this, stmt.getBody());
		node = node.getParent();
		return DONE;
	}

	@Override
	public Void caseStatementIf(StatementIf stmt) {
		if (CflowUtil.isIfSimple(stmt)) {
			return DONE;
		}

		node = new Node(node, stmt);
		List<Branch> branches = stmt.getBranches();
		for (Branch branch : branches) {
			doSwitch(branch);
		}

		// if this 'if' has no 'else', we add an artificial 'else' branch
		if (branches.get(branches.size() - 1).getCondition() != null) {
			new Node(node, cond(null));
		}
		node = node.getParent();

		return DONE;
	}

	public Node getRoot() {
		return node;
	}

}
