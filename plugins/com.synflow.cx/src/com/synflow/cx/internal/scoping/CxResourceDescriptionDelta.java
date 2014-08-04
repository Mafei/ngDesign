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
package com.synflow.cx.internal.scoping;

import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionDelta;

import com.synflow.cx.cx.CxPackage.Literals;

/**
 * This class extends the default resource description delta with Cx-specific behavior.
 * 
 * @author Matthieu Wipliez
 *
 */
public class CxResourceDescriptionDelta extends DefaultResourceDescriptionDelta {

	public CxResourceDescriptionDelta(IResourceDescription old, IResourceDescription _new) {
		super(old, _new);
	}

	@Override
	protected boolean internalHasChanges() {
		if (super.internalHasChanges()) {
			return true;
		}

		for (IEObjectDescription objDesc : super.getNew()
				.getExportedObjectsByType(Literals.NETWORK)) {
			return true;
		}

		return false;
	}

}
