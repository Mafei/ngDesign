/*******************************************************************************
 * Copyright (c) 2013-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.ui.wizards;

/**
 * This class provides a wizard to create a new network design.
 * 
 * @author Matthieu Wipliez
 */
class NewNetworkWizard extends NewFileWizard {

	public static val WIZARD_ID = "com.synflow.cx.ui.wizards.newNetwork"

	override getType() {
		"network"
	}

	override getStringContents(String author, int year, String package_, String entityName)
		'''
		/*
		 * Copyright (c) «year» «author»
		 * All rights reserved.
		 */
		package «package_»;

		network «entityName» {
			in u8 a, sync b; out u16 c;

			// instantiate task/network A
			// x = new A();

			// instantiate inner task
			// y  = new task {
			// 	void loop() {
			//		x.result.read();
			//	}
			// };
		}
		'''

}
