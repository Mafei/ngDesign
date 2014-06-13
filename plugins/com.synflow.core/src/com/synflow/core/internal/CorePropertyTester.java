/*******************************************************************************
 * Copyright (c) 2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.core.internal;

import static org.eclipse.jdt.core.IJavaElement.PACKAGE_FRAGMENT;
import static org.eclipse.jdt.core.IJavaElement.PACKAGE_FRAGMENT_ROOT;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

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
			IJavaElement element = JavaCore.create(resource);
			if (element != null && element.getElementType() == PACKAGE_FRAGMENT) {
				return true;
			}
		} else if ("isSourceFolder".equals(property)) {
			IJavaElement element = JavaCore.create(resource);
			if (element != null && element.getElementType() == PACKAGE_FRAGMENT_ROOT) {
				return true;
			}
		}

		return false;
	}

}
