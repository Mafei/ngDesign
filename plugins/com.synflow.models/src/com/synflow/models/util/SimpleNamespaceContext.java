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
package com.synflow.models.util;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Iterators;

/**
 * This class describes a simple namespace context implementation.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class SimpleNamespaceContext implements NamespaceContext {

	private Element docElement;

	public SimpleNamespaceContext(Document document) {
		this.docElement = document.getDocumentElement();
	}

	@Override
	public String getNamespaceURI(String prefix) {
		return docElement.lookupNamespaceURI(prefix);
	}

	@Override
	public String getPrefix(String namespaceURI) {
		return docElement.lookupPrefix(namespaceURI);
	}

	@Override
	public Iterator<?> getPrefixes(String namespaceURI) {
		return Iterators.singletonIterator(getPrefix(namespaceURI));
	}

}
