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
package com.synflow.cflow.internal.instantiation;

import static com.synflow.models.util.SwitchUtil.DONE;

import com.synflow.models.dpn.Instance;
import com.synflow.models.ir.TypeInt;
import com.synflow.models.ir.util.IrSwitch;
import com.synflow.models.util.Void;

/**
 * This class defines a type resolver.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class TypeResolver extends IrSwitch<Void> {

	// private Instance instance;

	public TypeResolver(Instance instance) {
		// this.instance = instance;
	}

	@Override
	public Void caseTypeInt(TypeInt type) {
		// TODO do this for TypeGen type
		// Expression unresolvedSize = type.getSize();
		// Expression size = new ExprResolver(instance).doSwitch(unresolvedSize);
		//
		// // remove original expression so we clean up uses
		// IrUtil.delete(unresolvedSize);
		//
		// // set up new size
		// type.setSize(size);
		return DONE;
	}

}