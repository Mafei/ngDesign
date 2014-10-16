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
package com.synflow.core.layout;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

/**
 * This interface defines an element of a project tree.
 * 
 * @author Matthieu Wipliez
 *
 */
public interface ITreeElement extends IAdaptable {

	String getName();

	IResource getResource();

	boolean isPackage();

	boolean isSourceFolder();

}
