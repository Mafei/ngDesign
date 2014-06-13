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
package com.synflow.cflow.internal.scoping;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;

/**
 * This class describes an import normalizer that allows import of x.y.Bundle and properly resolves
 * references to Bundle.A, Bundle.B etc.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CflowImportNormalizer extends ImportNormalizer {

	public CflowImportNormalizer(QualifiedName importedNamespace, boolean wildCard) {
		super(importedNamespace, wildCard, false);
	}

	@Override
	public QualifiedName deresolve(QualifiedName fullyQualifiedName) {
		if (getImportedNamespacePrefix().equals(fullyQualifiedName)) {
			return QualifiedName.create(fullyQualifiedName.getLastSegment());
		} else if (fullyQualifiedName.startsWith(getImportedNamespacePrefix())
				&& fullyQualifiedName.getSegmentCount() != getImportedNamespacePrefix()
						.getSegmentCount()) {
			return fullyQualifiedName.skipFirst(getImportedNamespacePrefix().getSegmentCount());
		}
		return null;
	}

	@Override
	public QualifiedName resolve(QualifiedName relativeName) {
		if (relativeName.isEmpty()) {
			return null;
		} else if (hasWildCard()) {
			return getImportedNamespacePrefix().append(relativeName);
		} else if (getImportedNamespacePrefix().getLastSegment().equals(
				relativeName.getFirstSegment())) {
			return getImportedNamespacePrefix().skipLast(1).append(relativeName);
		}
		return null;
	}

}
