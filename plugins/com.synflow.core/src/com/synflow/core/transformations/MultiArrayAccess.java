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
package com.synflow.core.transformations;

import static com.synflow.models.ir.IrFactory.eINSTANCE;
import static com.synflow.models.ir.OpBinary.SHIFT_LEFT;
import static com.synflow.models.util.SwitchUtil.DONE;

import java.util.Iterator;

import org.eclipse.emf.common.util.EList;

import com.synflow.models.ir.Def;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.InstLoad;
import com.synflow.models.ir.InstStore;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.TypeArray;
import com.synflow.models.ir.Use;
import com.synflow.models.ir.Var;
import com.synflow.models.ir.util.AbstractIrVisitor;
import com.synflow.models.ir.util.TypeUtil;
import com.synflow.models.util.EcoreHelper;
import com.synflow.models.util.Void;

/**
 * This class defines an IR transformation that replaces multi-array accesses by flat array
 * accesses.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class MultiArrayAccess extends AbstractIrVisitor {

	@Override
	public Void caseDef(Def def) {
		Var array = def.getVariable();
		TypeArray type = (TypeArray) array.getType();

		InstStore store = EcoreHelper.getContainerOfType(def, InstStore.class);
		visitIndexes(type, store.getIndexes());

		return DONE;
	}

	@Override
	public Void caseUse(Use use) {
		Var array = use.getVariable();
		TypeArray type = (TypeArray) array.getType();

		InstLoad load = EcoreHelper.getContainerOfType(use, InstLoad.class);
		visitIndexes(type, load.getIndexes());

		return DONE;
	}

	@Override
	public Void caseVar(Var var) {
		if (var.getType().isArray()) {
			TypeArray type = (TypeArray) var.getType();
			if (type.getDimensions().size() > 1) {
				visit(var.getUses());
				visit(var.getDefs());
			}
		}
		return DONE;
	}

	private void visitIndexes(TypeArray type, EList<Expression> indexes) {
		Iterator<Integer> itD = type.getDimensions().iterator();
		itD.next();

		Expression result = indexes.get(0);
		while (!indexes.isEmpty()) {
			int size = itD.next();
			int amount = TypeUtil.getSize(size - 1);
			Expression shifted = eINSTANCE.createExprBinary(result, SHIFT_LEFT,
					eINSTANCE.createExprInt(amount));
			result = eINSTANCE.createExprBinary(shifted, OpBinary.BITOR, indexes.get(0));
		}
		indexes.add(result);
	}

}
