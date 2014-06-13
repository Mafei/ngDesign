/*******************************************************************************
 * Copyright (c) 2012-2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cflow.internal.compiler.helpers;

import static com.synflow.models.util.SwitchUtil.DONE;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.synflow.core.transformations.ProcedureTransformation;
import com.synflow.models.dpn.Actor;
import com.synflow.models.ir.InstLoad;
import com.synflow.models.ir.InstStore;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Var;
import com.synflow.models.ir.util.AbstractIrVisitor;
import com.synflow.models.util.Void;

/**
 * This class visits all references to local variables, and each local variable
 * which is found to be used across more than one procedure is promoted to a
 * state variable.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class VariablePromoter extends AbstractIrVisitor {

	private static class VarNamePredicate implements Predicate<Var> {

		private String name;

		@Override
		public boolean apply(Var var) {
			return var.getName().equals(name);
		}

		public void setName(String name) {
			this.name = name;
		}

	}

	private final VarNamePredicate predName = new VarNamePredicate();

	private final Multimap<Var, Procedure> procMap;

	private final List<Var> stateVars;

	public VariablePromoter(List<Var> stateVars) {
		this.stateVars = stateVars;
		procMap = LinkedHashMultimap.create();
	}

	@Override
	public Void caseInstLoad(InstLoad load) {
		Var variable = load.getSource().getVariable();
		if (variable.isLocal()) {
			procMap.put(variable, procedure);
		}

		return DONE;
	}

	@Override
	public Void caseInstStore(InstStore store) {
		Var variable = store.getTarget().getVariable();
		if (variable.isLocal()) {
			procMap.put(variable, procedure);
		}

		return DONE;
	}

	/**
	 * Returns a unique name based on the given name.
	 * 
	 * @param name
	 *            name
	 * @return a unique name based on the given name
	 */
	public String getUniqueName(String name) {
		String uniqueName = name;
		int i = 1;
		predName.setName(uniqueName);
		while (Iterables.any(stateVars, predName)) {
			uniqueName = name + "_" + i++;
			predName.setName(uniqueName);
		}
		return uniqueName;
	}

	/**
	 * Visits the given actor, and promotes any local variable that is
	 * defined/used across more than one procedure.
	 * 
	 * @param actor
	 *            an actor
	 */
	public void visit(Actor actor) {
		new ProcedureTransformation(this).doSwitch(actor);

		for (Var variable : procMap.keySet()) {
			Collection<Procedure> procedures = procMap.get(variable);
			if (procedures.size() > 1) {
				// change name
				String procName = procedures.iterator().next().getName();
				String name = procName + "_" + variable.getName();
				variable.setName(getUniqueName(name));

				// promotes to state variable
				stateVars.add(variable);
			}
		}
	}

}
