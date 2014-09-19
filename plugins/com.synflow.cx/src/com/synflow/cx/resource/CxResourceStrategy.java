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
package com.synflow.cx.resource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy;
import org.eclipse.xtext.util.IAcceptor;

import com.google.common.collect.ImmutableMap;
import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.CExpression;
import com.synflow.cx.cx.CType;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Obj;
import com.synflow.cx.cx.Task;
import com.synflow.cx.cx.Typedef;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.services.CxPrinter;

/**
 * This class describes a strategy that exports ports and bundle variables. It creates object
 * descriptions with user data that describes the type and value (if any) of variables.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CxResourceStrategy extends DefaultResourceDescriptionStrategy {

	@Override
	public boolean createEObjectDescriptions(EObject eObject,
			IAcceptor<IEObjectDescription> acceptor) {
		if (eObject instanceof Variable) {
			Variable variable = (Variable) eObject;
			if (CxUtil.isPort(variable)) {
				createVariable(variable, acceptor);
			} else {
				Task task = EcoreUtil2.getContainerOfType(variable, Task.class);
				if (task == null) {
					// bundle or network
					// so by definition the variable is constant and can be exported safely
					createVariable(variable, acceptor);
				}
			}
		} else if (eObject instanceof Typedef) {
			Typedef typedef = (Typedef) eObject;
			createTypedef(typedef, acceptor);
		} else if (eObject instanceof Inst) {
			Inst inst = (Inst) eObject;
			createInst(inst, acceptor);
		} else {
			return super.createEObjectDescriptions(eObject, acceptor);
		}

		// no need to visit contents of variable or typedef
		return false;
	}

	private void createInst(Inst inst, IAcceptor<IEObjectDescription> acceptor) {
		Map<String, String> userData = null;

		// if inst has arguments, adds it to user data
		Obj obj = inst.getArguments();
		if (obj != null) {
			userData = ImmutableMap.of("properties", new CxPrinter().toString(obj));
		}

		// create eobject description
		QualifiedName qualifiedName = getQualifiedNameProvider().getFullyQualifiedName(inst);
		if (qualifiedName != null) {
			acceptor.accept(EObjectDescription.create(qualifiedName, inst, userData));
		}
	}

	private void createTypedef(Typedef typedef, IAcceptor<IEObjectDescription> acceptor) {
		QualifiedName qualifiedName = getQualifiedNameProvider().getFullyQualifiedName(typedef);
		String type = new CxPrinter().toString(typedef.getType());
		Map<String, String> userData = ImmutableMap.of("type", type);
		acceptor.accept(EObjectDescription.create(qualifiedName, typedef, userData));
	}

	private void createVariable(Variable variable, IAcceptor<IEObjectDescription> acceptor) {
		StringBuilder builder = new StringBuilder();
		Iterator<Variable> it = variable.getParameters().iterator();
		if (it.hasNext()) {
			builder.append('(');
			getType(builder, it.next());
			while (it.hasNext()) {
				builder.append(',');
				getType(builder, it.next());
			}
			builder.append(")->");
		}
		getType(builder, variable);

		for (CExpression dim : variable.getDimensions()) {
			builder.append('[');
			new CxPrinter(builder).doSwitch(dim);
			builder.append(']');
		}

		// add type to user data
		Map<String, String> userData = new HashMap<>(2);
		userData.put("type", builder.toString());

		// if variable has value, adds it to user data
		EObject value = variable.getValue();
		if (value != null) {
			userData.put("value", new CxPrinter().toString(value));
		}

		// create eobject description
		QualifiedName qualifiedName = getQualifiedNameProvider().getFullyQualifiedName(variable);
		if (qualifiedName != null) {
			acceptor.accept(EObjectDescription.create(qualifiedName, variable, userData));
		}
	}

	private void getType(StringBuilder builder, Variable variable) {
		CType type = CxUtil.getType(variable);
		if (type != null) {
			new CxPrinter(builder).doSwitch(type);
		}
	}

}
