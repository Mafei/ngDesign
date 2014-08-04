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

import java.util.Collection;

import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionManager;

/**
 * This class defines a resource description manager that overrides the default resource description
 * manager to handle hierarchical dependency management.
 * 
 * @author Matthieu
 *
 */
public class CxResourceDescriptionManager extends DefaultResourceDescriptionManager {

	@Override
	public boolean isAffected(Collection<Delta> deltas, IResourceDescription candidate,
			IResourceDescriptions context) {
		return super.isAffected(deltas, candidate, context);
	}

}
