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
package com.synflow.cx.ui.linking;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.diagnostics.DiagnosticMessage;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.linking.impl.LinkingDiagnosticMessageProvider;

import com.synflow.cx.cx.CxEntity;
import com.synflow.cx.cx.VarRef;

/**
 * This class provides custom messages for linking diagnostics.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CflowLinkingDiagnosticMessageProvider extends LinkingDiagnosticMessageProvider {

	@Override
	public DiagnosticMessage getUnresolvedProxyMessage(final ILinkingDiagnosticContext context) {
		EObject element = context.getContext();
		String link = context.getLinkText();
		if (element instanceof CxEntity) {
			return new DiagnosticMessage(link + " cannot be imported", Severity.ERROR, null);
		} else if (element instanceof VarRef) {
			return new DiagnosticMessage(link + " cannot be resolved", Severity.ERROR, null);
		}
		return super.getUnresolvedProxyMessage(context);
	}

}
