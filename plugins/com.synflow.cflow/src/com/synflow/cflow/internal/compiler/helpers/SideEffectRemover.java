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
package com.synflow.cflow.internal.compiler.helpers;

import static com.synflow.models.util.SwitchUtil.DONE;

import com.synflow.models.ir.InstCall;
import com.synflow.models.ir.InstStore;
import com.synflow.models.ir.util.AbstractIrVisitor;
import com.synflow.models.util.Void;

/**
 * This class defines an IR transformation that removes stores and prints.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class SideEffectRemover extends AbstractIrVisitor {

	@Override
	public Void caseInstCall(InstCall call) {
		if (call.isPrint()) {
			delete(call);
		}

		return DONE;
	}

	@Override
	public Void caseInstStore(InstStore store) {
		delete(store);
		return DONE;
	}

}
