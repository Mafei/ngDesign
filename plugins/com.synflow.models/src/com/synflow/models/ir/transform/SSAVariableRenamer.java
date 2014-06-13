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
package com.synflow.models.ir.transform;

import static com.synflow.models.ir.util.IrUtil.getNameSSA;
import static com.synflow.models.util.SwitchUtil.DONE;

import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Var;
import com.synflow.models.ir.util.AbstractIrVisitor;
import com.synflow.models.util.Void;

/**
 * This class defines an IR transformation that renames local variables when
 * using SSA.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class SSAVariableRenamer extends AbstractIrVisitor {

	@Override
	public Void caseProcedure(Procedure procedure) {
		for (Var local : procedure.getLocals()) {
			local.setName(getNameSSA(local));
		}

		return DONE;
	}

}
