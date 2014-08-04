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
package com.synflow.cx.tests.codegen.vhdl;

import com.synflow.cx.tests.CustomCxInjectorProvider;
import com.synflow.generators.CoreGeneratorsModule;

/**
 * This class defines a custom CxInjectorProvider that uses the core generators module.
 * 
 * @author Matthieu Wipliez
 *
 */
public class CxInjectorProviderVhdl extends CustomCxInjectorProvider {

	public CxInjectorProviderVhdl() {
		super(new CoreGeneratorsModule());
	}

}
