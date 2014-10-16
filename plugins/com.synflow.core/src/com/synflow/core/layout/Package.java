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

public class Package extends AbstractTreeElement {

	public Package(IResource resource) {
		super(resource);
	}

	public Object[] getFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPackage() {
		return true;
	}

}
