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
package com.synflow.cflow.generator;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IGenerator;

import com.google.inject.Inject;
import com.synflow.cflow.internal.compiler.IModuleCompiler;

/**
 * This class defines a generator for C~ source/header files.
 * 
 * @author Matthieu Wipliez
 */
public class CflowGenerator implements IGenerator {

	@Inject
	private IModuleCompiler compiler;

	@Override
	public void doGenerate(Resource resource, IFileSystemAccess fsa) {
		// do nothing if the resource does not contain anything
		if (resource.getContents().isEmpty()) {
			return;
		}

		EObject object = resource.getContents().get(0);

		// set file system and then transform entity to IR
		compiler.setFileSystemAccess(fsa);
		compiler.doSwitch(object);
		compiler.serializeBuiltins();

		// if (ResourcesPlugin.getPlugin() != null) {
		// IFile cfFile = EcoreHelper.getFile(resource);
		// new EdgeColoring(cfFile).visit(actor);
		// }
	}

}
