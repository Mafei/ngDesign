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
package com.synflow.cx.ui.labeling;

import org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider;

import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.Bundle;
import com.synflow.cx.cx.Import;
import com.synflow.cx.cx.Imported;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Network;
import com.synflow.cx.cx.Task;
import com.synflow.cx.cx.TypeDecl;
import com.synflow.cx.cx.TypeGen;
import com.synflow.cx.cx.TypeRef;
import com.synflow.cx.cx.Typedef;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.services.CxPrinter;

/**
 * Provides labels for a EObjects.
 * 
 * see http://www.eclipse.org/Xtext/documentation/latest/xtext.html#labelProvider
 */
public class CxLabelProvider extends DefaultEObjectLabelProvider {

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
		if (CxUtil.isFunction(variable)) {
			return "methpri_obj.gif";
		} else if (CxUtil.isPort(variable)) {
			String dir = CxUtil.getDirection(variable);
			if ("in".equals(dir)) {
				return "input_port.png";
			} else {
				return "output_port.png";
			}
		}

		return "state_var.png";
	}

	public String text(TypeDecl type) {
		return new CxPrinter().toString(type);
	}

	public String text(TypeGen type) {
		return new CxPrinter().toString(type);
	}

	public String text(TypeRef type) {
		return getText(type.getTypeDef());
	}

	public String text(Variable variable) {
		return new CxPrinter().toString(variable);
	}

}
