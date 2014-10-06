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

import java.util.Iterator;

import org.eclipse.xtext.linking.impl.ImportedNamesAdapter;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;

/**
 * This class extends the default imported names adapter to use case-sensitive imported names. Makes
 * it easier to find objects directly using their qualified name.
 * 
 * @author Matthieu Wipliez
 *
 */
public class CxImportedNamesAdapter extends ImportedNamesAdapter {

	public class CxWrappingScope extends WrappingScope {

		private IScope delegate;

		public CxWrappingScope(IScope scope) {
			super(scope);
			this.delegate = scope;
		}

		@Override
		public Iterable<IEObjectDescription> getElements(final QualifiedName name) {
			return new Iterable<IEObjectDescription>() {
				public Iterator<IEObjectDescription> iterator() {
					getImportedNames().add(name);
					final Iterable<IEObjectDescription> elements = delegate.getElements(name);
					return elements.iterator();
				}
			};
		}

		@Override
		public IEObjectDescription getSingleElement(QualifiedName name) {
			getImportedNames().add(name);
			return delegate.getSingleElement(name);
		}

	}

	@Override
	public IScope wrap(IScope scope) {
		return new CxWrappingScope(scope);
	}

}
