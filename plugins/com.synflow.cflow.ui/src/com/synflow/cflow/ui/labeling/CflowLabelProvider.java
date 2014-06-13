/*******************************************************************************
 * Copyright (c) 2012-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cflow.ui.labeling;

import org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider;

import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.Bundle;
import com.synflow.cflow.cflow.Import;
import com.synflow.cflow.cflow.Imported;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.Network;
import com.synflow.cflow.cflow.Task;
import com.synflow.cflow.cflow.TypeDecl;
import com.synflow.cflow.cflow.TypeGen;
import com.synflow.cflow.cflow.TypeRef;
import com.synflow.cflow.cflow.Typedef;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.services.CflowPrinter;
import com.synflow.cflow.services.Evaluator;

/**
 * Provides labels for a EObjects.
 * 
 * see http://www.eclipse.org/Xtext/documentation/latest/xtext.html#labelProvider
 */
public class CflowLabelProvider extends DefaultEObjectLabelProvider {

	public Object image(Bundle bundle) {
		return "type_bundle.png";
	}

	public String image(Import import_) {
		return "imp_obj.gif";
	}

	public Object image(Imported imported) {
		return getImage(imported.getType());
	}

	public Object image(Inst inst) {
		return "instance.png";
	}

	public Object image(Network network) {
		return "type_network.png";
	}

	public Object image(Task task) {
		return "type_task.png";
	}

	public String image(Typedef typeDef) {
		return "typedef.png";
	}

	public String image(Variable variable) {
		if (CflowUtil.isFunction(variable)) {
			return "methpri_obj.gif";
		} else if (CflowUtil.isPort(variable)) {
			String dir = CflowUtil.getDirection(variable);
			if ("in".equals(dir)) {
				return "input_port.png";
			} else {
				return "output_port.png";
			}
		}

		return "state_var.png";
	}

	public String text(TypeDecl type) {
		boolean unsigned = type.isUnsigned();
		StringBuilder builder = new StringBuilder(type.getSpec());
		if (unsigned) {
			builder.insert(0, "unsigned ");
		}

		return builder.toString();
	}

	public String text(TypeGen type) {
		int size = Evaluator.getIntValue(type.getSize());
		if (size != -1) {
			return type.getSpec() + size;
		}
		return type.getSpec() + "<" + new CflowPrinter().toString(type.getSize()) + ">";
	}

	public String text(TypeRef type) {
		return getText(type.getTypeDef());
	}

	public String text(Variable variable) {
		return new CflowPrinter().toString(variable);
	}

}
