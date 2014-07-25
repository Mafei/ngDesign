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
package com.synflow.cflow.internal.scoping;

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
import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.CExpression;
import com.synflow.cflow.cflow.CType;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.Task;
import com.synflow.cflow.cflow.Typedef;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.services.CflowPrinter;

/**
 * This class describes a strategy that exports ports and bundle variables. It creates object
 * descriptions with user data that describes the type and value (if any) of variables.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CflowResourceStrategy extends DefaultResourceDescriptionStrategy {

	@Override
	public boolean createEObjectDescriptions(EObject eObject,
			IAcceptor<IEObjectDescription> acceptor) {
		if (eObject instanceof Variable) {
			Variable variable = (Variable) eObject;
			if (CflowUtil.isPort(variable)) {
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
			return false;
		} else {
			return super.createEObjectDescriptions(eObject, acceptor);
		}

		// no need to visit contents of variable or typedef
		return false;
	}

	private void createTypedef(Typedef typedef, IAcceptor<IEObjectDescription> acceptor) {
		QualifiedName qualifiedName = getQualifiedNameProvider().getFullyQualifiedName(typedef);
		String type = new CflowPrinter().toString(typedef.getType());
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
			new CflowPrinter(builder).doSwitch(dim);
			builder.append(']');
		}

		// add type to user data
		Map<String, String> userData = new HashMap<>();
		userData.put("type", builder.toString());

		// if variable has value, adds it to user data
		EObject value = variable.getValue();
		if (value != null) {
			userData.put("value", new CflowPrinter().toString(value));
		}

		// create eobject description
		QualifiedName qualifiedName = getQualifiedNameProvider().getFullyQualifiedName(variable);
		if (qualifiedName != null) {
			acceptor.accept(EObjectDescription.create(qualifiedName, variable, userData));
		}
	}

	private void getType(StringBuilder builder, Variable variable) {
		CType type = CflowUtil.getType(variable);
		if (type != null) {
			new CflowPrinter(builder).doSwitch(type);
		}
	}

}
