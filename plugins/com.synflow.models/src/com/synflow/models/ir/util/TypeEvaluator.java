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
package com.synflow.models.ir.util;

import com.synflow.models.ir.Type;
import com.synflow.models.ir.TypeArray;
import com.synflow.models.ir.TypeBool;
import com.synflow.models.ir.TypeFloat;
import com.synflow.models.ir.TypeInt;
import com.synflow.models.ir.TypeString;

/**
 * This class defines an IR transformation that tries to return a resolved type from a type when
 * possible. A resolved type is a type in which all variables have been replaced by their value. If
 * this is not possible, the doSwitch method returns null.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class TypeEvaluator extends IrSwitch<Type> {

	@Override
	public Type caseTypeArray(TypeArray type) {
		return IrUtil.copy(type);
	}

	@Override
	public TypeBool caseTypeBool(TypeBool type) {
		return IrUtil.copy(type);
	}

	@Override
	public TypeFloat caseTypeFloat(TypeFloat type) {
		return IrUtil.copy(type);
	}

	@Override
	public Type caseTypeInt(TypeInt type) {
		return IrUtil.copy(type);
	}

	@Override
	public TypeString caseTypeString(TypeString type) {
		return IrUtil.copy(type);
	}

}
