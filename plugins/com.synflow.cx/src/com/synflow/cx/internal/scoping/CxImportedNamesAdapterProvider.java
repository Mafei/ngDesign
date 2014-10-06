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

import org.eclipse.xtext.linking.impl.ImportedNamesAdapter;

import com.google.inject.Provider;

/**
 * This class defines a provider for {@link ImportedNamesAdapter} to use the Cx-specific version.
 * 
 * @author Matthieu Wipliez
 *
 */
public class CxImportedNamesAdapterProvider implements Provider<ImportedNamesAdapter> {

	@Override
	public ImportedNamesAdapter get() {
		return new CxImportedNamesAdapter();
	}

}
