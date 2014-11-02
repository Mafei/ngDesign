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
package com.synflow.cx.ui.wizards

import org.eclipse.jface.viewers.IStructuredSelection

/**
 * This class provides a page to create a new Cx bundle.
 * 
 * @author Matthieu Wipliez
 */
class NewBundlePage extends NewFilePage {

	new(IStructuredSelection selection) {
		super("NewBundle", selection);
	}

	override getStringContents(String author, int year)
		'''
		/*
		 * Copyright (c) «year» «author»
		 * All rights reserved.
		 */
		package «package»;

		bundle «entityName» {

			/** current year. Switch to u12 in year 2048 */
			u11 YEAR = «year»;

			int increment(int x) {
				return (int) (x + 1);
			}

		}
		'''

}
