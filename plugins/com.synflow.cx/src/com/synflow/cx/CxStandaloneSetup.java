/*******************************************************************************
 * Copyright (c) 2012 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx;

import com.synflow.cx.CxStandaloneSetupGenerated;

/**
 * Initialization support for running Xtext languages without equinox extension
 * registry
 */
public class CxStandaloneSetup extends CxStandaloneSetupGenerated {

	public static void doSetup() {
		new CxStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}
