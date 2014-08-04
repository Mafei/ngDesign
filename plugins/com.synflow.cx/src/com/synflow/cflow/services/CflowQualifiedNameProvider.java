/*******************************************************************************
 * Copyright (c) 2012-2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cflow.services;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;

import com.synflow.cflow.cflow.Module;
import com.synflow.cflow.cflow.Task;

/**
 * This class defines a qualified name provider for C~ that computes the qualified name of a module
 * as its simple name (file name without extension).
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CflowQualifiedNameProvider extends DefaultDeclarativeQualifiedNameProvider {

	protected QualifiedName qualifiedName(Module module) {
		return getConverter().toQualifiedName(module.getPackage());
	}

	protected QualifiedName qualifiedName(Task task) {
		String name = task.getName();
		if (name == null) {
			// anonymous task
			EObject cter = task.eContainer();
			QualifiedName qName = getFullyQualifiedName(cter);
			int size = qName.getSegmentCount();
			String taskName = qName.getSegment(size - 2) + "_" + qName.getLastSegment();
			return qName.skipLast(2).append(taskName);
		}

		// will use task's "name" attribute and container
		return null;
	}

}
