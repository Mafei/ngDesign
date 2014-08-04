/*******************************************************************************
 * Copyright (c) 2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.internal.compiler;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.generator.IFileSystemAccess;

import com.google.inject.ImplementedBy;
import com.synflow.models.dpn.Entity;
import com.synflow.models.util.Void;

/**
 * This class transforms a C~ module to IR entities (actors, networks, units).
 * 
 * @author Matthieu Wipliez
 * 
 */
@ImplementedBy(ModuleCompilerImpl.class)
public interface IModuleCompiler {

	Void doSwitch(EObject eObject);

	void serialize(Entity entity);

	void serializeBuiltins();

	void setFileSystemAccess(IFileSystemAccess fsa);

}
