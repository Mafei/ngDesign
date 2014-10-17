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
package com.synflow.ngDesign.ui.internal.navigator.actions;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.actions.DeleteResourceAction;

import com.synflow.core.layout.ITreeElement;

/**
 * This class defines a delete action that cannot delete source folders.
 * 
 * @author Matthieu Wipliez
 *
 */
public class DeleteAction extends DeleteResourceAction {

	public DeleteAction(IShellProvider provider) {
		super(provider);
	}

	private boolean canDelete(IStructuredSelection selection) {
		// cannot delete source folder
		for (Iterator<?> e = getStructuredSelection().iterator(); e.hasNext();) {
			Object next = e.next();
			if (next instanceof ITreeElement) {
				ITreeElement element = (ITreeElement) next;
				if (element.isSourceFolder()) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection) && canDelete(selection);
	}

}
