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
package com.synflow.cx.ui.wizards

import org.eclipse.jface.viewers.IStructuredSelection

/**
 * This class provides a page to create a new C~ network.
 * 
 * @author Matthieu Wipliez
 */
class NewNetworkPage extends NewFilePage {

	new(String pageName, IStructuredSelection selection) {
		super(pageName, "cf", selection);
	}

	override getStringContents(String author, int year)
		'''
		/*
		 * Copyright (c) «year» «author»
		 * All rights reserved.
		 */
		package «package»;

		network «name» {
			in u8 a, sync b; out u16 c;

			x = new task {
			};

			y = new A();
		}

		task A {
		}
		'''

}
