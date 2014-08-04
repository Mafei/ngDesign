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
package com.synflow.cx.internal.compiler.helpers;

import static com.synflow.models.util.SwitchUtil.DONE;

import java.util.ArrayList;

import org.eclipse.emf.ecore.util.EcoreUtil;

import com.synflow.models.ir.InstAssign;
import com.synflow.models.ir.InstLoad;
import com.synflow.models.ir.InstStore;
import com.synflow.models.ir.IrFactory;
import com.synflow.models.ir.Use;
import com.synflow.models.ir.Var;
import com.synflow.models.ir.util.AbstractIrVisitor;
import com.synflow.models.util.Void;

/**
 * Replaces loads of local variables by direct references (use), and replaces
 * stores to scalar local variables by assignments.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class LoadStoreReplacer extends AbstractIrVisitor {

	@Override
	public Void caseInstLoad(InstLoad load) {
		Var source = load.getSource().getVariable();
		if (!source.isLocal() || !load.getIndexes().isEmpty()) {
			// a load of a global variable, or with indexes
			// must not be replaced
			return DONE;
		}

		// replace uses of target by source
		Var target = load.getTarget().getVariable();
		for (Use use : new ArrayList<>(target.getUses())) {
			use.setVariable(source);
		}

		// remove target
		EcoreUtil.remove(target);

		delete(load);

		return DONE;
	}

	@Override
	public Void caseInstStore(InstStore store) {
		Var target = store.getTarget().getVariable();
		if (!target.isLocal() || !store.getIndexes().isEmpty()) {
			// a store to a global variable, or with indexes
			// must not be replaced by an assign
			return DONE;
		}

		// create assign
		InstAssign assign = IrFactory.eINSTANCE.createInstAssign();
		assign.setTarget(store.getTarget());
		assign.setValue(store.getValue());

		// replace store by assign
		replace(store, assign);

		return DONE;
	}

}
