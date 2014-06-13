/*******************************************************************************
 * Copyright (c) 2012 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.models.ir.util;

import static com.synflow.models.util.SwitchUtil.DONE;

import com.synflow.models.ir.Type;
import com.synflow.models.ir.TypeArray;
import com.synflow.models.ir.TypeBool;
import com.synflow.models.ir.TypeFloat;
import com.synflow.models.ir.TypeInt;
import com.synflow.models.ir.TypeString;
import com.synflow.models.ir.TypeVoid;
import com.synflow.models.util.Void;

/**
 * This class defines a type printer for Cflow-like types.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class TypePrinter extends IrSwitch<Void> {

	private StringBuilder builder;

	@Override
	public Void caseTypeArray(TypeArray type) {
		doSwitch(type.getElementType());
		for (int dim : type.getDimensions()) {
			builder.append('[');
			builder.append(dim);
			builder.append(']');
		}
		return DONE;
	}

	@Override
	public Void caseTypeBool(TypeBool type) {
		builder.append("bool");
		return DONE;
	}

	@Override
	public Void caseTypeFloat(TypeFloat type) {
		builder.append("float");
		return DONE;
	}

	@Override
	public Void caseTypeInt(TypeInt type) {
		if (type.isSigned()) {
			builder.append('i');
		} else {
			builder.append('u');
		}

		int size = type.getSize();
		builder.append(size);

		return DONE;
	}

	@Override
	public Void caseTypeString(TypeString type) {
		builder.append("String");
		return DONE;
	}

	@Override
	public Void caseTypeVoid(TypeVoid type) {
		builder.append("void");
		return DONE;
	}

	public String toString(Type type) {
		builder = new StringBuilder();
		doSwitch(type);
		return builder.toString();
	}

}
