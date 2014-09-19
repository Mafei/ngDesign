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
package com.synflow.cx.internal.instantiation;

import org.eclipse.emf.common.util.URI;

import com.synflow.cx.cx.CxEntity;

/**
 * This class holds information about a Cx entity, its specialized name and URI of the corresponding
 * IR resource.
 * 
 * @author Matthieu Wipliez
 *
 */
public class EntityInfo {

	private final CxEntity cxEntity;

	private final String name;

	private final boolean specialized;

	private final URI uri;

	public EntityInfo(CxEntity cxEntity, String name, URI uri, boolean specialized) {
		this.cxEntity = cxEntity;
		this.name = name;
		this.uri = uri;
		this.specialized = specialized;
	}

	public CxEntity getCxEntity() {
		return cxEntity;
	}

	public String getName() {
		return name;
	}

	public URI getURI() {
		return uri;
	}

	public boolean isSpecialized() {
		return specialized;
	}

	@Override
	public String toString() {
		return name + " " + uri;
	}

}
