/*******************************************************************************
 * Copyright (c) 2012-2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cflow.ui.contentassist;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.synflow.cflow.cflow.CflowPackage;
import com.synflow.cflow.cflow.StatementWrite;

/**
 * see http://www.eclipse.org/Xtext/documentation.html#contentAssist on how to customize content
 * assistant
 */
public class CflowProposalProvider extends AbstractCflowProposalProvider {

	@Override
	public void complete_ValidID(EObject model, RuleCall ruleCall, ContentAssistContext context,
			ICompletionProposalAcceptor acceptor) {
		if (model instanceof StatementWrite) {
			StatementWrite stmt = (StatementWrite) model;
			IScope scope = getScopeProvider().getScope(stmt.getPort(),
					CflowPackage.Literals.VAR_REF__VARIABLE);
			for (IEObjectDescription candidate : scope.getAllElements()) {
				String proposal = getQualifiedNameConverter().toString(candidate.getName());
				StyledString displayString = getStyledDisplayString(candidate);
				EObject objectOrProxy = candidate.getEObjectOrProxy();
				Image image = getImage(objectOrProxy);
				acceptor.accept(createCompletionProposal(proposal, displayString, image, context));
			}
		} else {
			super.complete_ValidID(model, ruleCall, context, acceptor);
		}
	}

	@Override
	public void completeImported_Type(EObject model, Assignment assignment,
			ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		final URI uriRoot = EcoreUtil.getURI(EcoreUtil.getRootContainer(model));
		lookupCrossReference(((CrossReference) assignment.getTerminal()), context, acceptor,
				new Predicate<IEObjectDescription>() {
					@Override
					public boolean apply(IEObjectDescription candidate) {
						URI uriCandidate = candidate.getEObjectURI();
						return !uriRoot.equals(uriCandidate);
					}
				});
	}

	@Override
	public void completeKeyword(Keyword keyword, ContentAssistContext context,
			ICompletionProposalAcceptor acceptor) {
		if (isKeywordWorthyToPropose(keyword)) {
			super.completeKeyword(keyword, context, acceptor);
		}
	}

	@Override
	protected Function<IEObjectDescription, ICompletionProposal> getProposalFactory(
			String ruleName, ContentAssistContext contentAssistContext) {
		return new DefaultProposalCreator(contentAssistContext, "FullyQualifiedName",
				getQualifiedNameConverter());
	}

	/**
	 * Returns <code>true</code> if the given keyword should be proposed to the user. Taken from
	 * Xbase code.
	 * 
	 * @param keyword
	 * @return
	 */
	private boolean isKeywordWorthyToPropose(Keyword keyword) {
		return keyword.getValue().length() > 1 && Character.isLetter(keyword.getValue().charAt(0));
	}

}