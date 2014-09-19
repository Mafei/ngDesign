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
package com.synflow.cx.resource;

import java.util.Collection;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.resource.IResourceDescription.Manager.AllChangeAware;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionManager;

import com.synflow.cx.cx.CxPackage.Literals;

/**
 * This class describes a resource description manager.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CxResourceDescriptionManager extends DefaultResourceDescriptionManager implements
		AllChangeAware {

	@Override
	public boolean isAffectedByAny(Collection<Delta> deltas, IResourceDescription candidate,
			IResourceDescriptions context) throws IllegalArgumentException {
		for (IEObjectDescription objDesc : candidate.getExportedObjectsByType(Literals.BUNDLE)) {
			// a candidate is a bundle, is it loaded by the deltas?
			for (Delta delta : deltas) {
				for (QualifiedName name : delta.getNew().getImportedNames()) {
					// if the description imports something from the bundle, we consider
					// that the bundle is "affected" so it will be loaded by the instantiator
					if (name.startsWithIgnoreCase(objDesc.getName())) {
						return true;
					}
				}
			}
		}

		return isAffected(deltas, candidate, context);
	}

}
