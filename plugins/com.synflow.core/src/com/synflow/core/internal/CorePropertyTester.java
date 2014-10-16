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
package com.synflow.core.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;

import com.synflow.core.layout.LayoutUtil;

/**
 * This class defines a property tester.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CorePropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		IResource resource = (IResource) receiver;
		if ("isPackage".equals(property)) {
			return LayoutUtil.isPackage(resource);
		} else if ("isSourceFolder".equals(property)) {
			return LayoutUtil.isSourceFolder(resource);
		}

		return false;
	}

}
