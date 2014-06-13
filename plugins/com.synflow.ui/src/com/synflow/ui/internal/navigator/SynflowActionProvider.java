/*******************************************************************************
 * Copyright (c) 2003, 2011, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Matthieu Wipliez (Synflow SAS) - modified
 *******************************************************************************/
package com.synflow.ui.internal.navigator;

import org.eclipse.jdt.ui.actions.CCPActionGroup;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * This class describes an action provider with only the CCP action group from JDT.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class SynflowActionProvider extends CommonActionProvider {

	private CCPActionGroup fCCPGroup;

	private boolean fInViewPart = false;

	@Override
	public void dispose() {
		if (fInViewPart) {
			fCCPGroup.dispose();
		}
		super.dispose();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		if (fInViewPart) {
			fCCPGroup.fillActionBars(actionBars);
		}
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		if (fInViewPart) {
			fCCPGroup.fillContextMenu(menu);
		}
	}

	@Override
	public void init(ICommonActionExtensionSite site) {
		ICommonViewerWorkbenchSite workbenchSite = null;
		if (site.getViewSite() instanceof ICommonViewerWorkbenchSite)
			workbenchSite = (ICommonViewerWorkbenchSite) site.getViewSite();

		if (workbenchSite != null) {
			if (workbenchSite.getPart() != null && workbenchSite.getPart() instanceof IViewPart) {
				IViewPart viewPart = (IViewPart) workbenchSite.getPart();

				fCCPGroup = new CCPActionGroup(viewPart);

				fInViewPart = true;
			}
		}
	}

	@Override
	public void setContext(ActionContext context) {
		super.setContext(context);
		if (fInViewPart) {
			fCCPGroup.setContext(context);
		}
	}

}
