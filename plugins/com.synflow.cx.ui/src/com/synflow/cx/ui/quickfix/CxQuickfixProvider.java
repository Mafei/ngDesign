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
package com.synflow.cx.ui.quickfix;

import static com.synflow.cx.validation.IssueCodes.ERR_ENTRY_FUNCTION_BAD_TYPE;
import static com.synflow.cx.validation.IssueCodes.ERR_UNRESOLVED_FUNCTION;
import static com.synflow.cx.validation.IssueCodes.SHOULD_REPLACE_NAME;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.edit.IModification;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;
import org.eclipse.xtext.ui.editor.quickfix.DefaultQuickfixProvider;
import org.eclipse.xtext.ui.editor.quickfix.Fix;
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionAcceptor;
import org.eclipse.xtext.validation.Issue;

import com.synflow.cx.cx.Variable;

/**
 * This class provides quick fixes for several issues.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CxQuickfixProvider extends DefaultQuickfixProvider {

	@Fix(ERR_ENTRY_FUNCTION_BAD_TYPE)
	public void changeTypeToVoid(final Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, "Change type to void", "Change function type to void", null,
				new ISemanticModification() {

					@Override
					public void apply(EObject element, IModificationContext context)
							throws Exception {
						Variable function = (Variable) element;
						INode nodeType = NodeModelUtils.findActualNodeFor(function.getType());

						IXtextDocument document = context.getXtextDocument();
						document.replace(nodeType.getOffset(), nodeType.getLength(), "void");
					}

				});
	}

	@Fix(ERR_UNRESOLVED_FUNCTION)
	public void fixUnresolvedFunction(final Issue issue, IssueResolutionAcceptor acceptor) {
		final String funcName = issue.getData()[0];
		acceptor.accept(issue, "Create function " + funcName + "()", "Create function " + funcName
				+ "()", null, new ISemanticModification() {
			public void apply(final EObject element, IModificationContext context)
					throws BadLocationException {
				Variable module = EcoreUtil2.getContainerOfType(element, Variable.class);
				ICompositeNode node = NodeModelUtils.getNode(module);
				int offset = node.getOffset() + node.getLength();
				StringBuilder builder = new StringBuilder("\n\nvoid ");
				String newFunc = builder.append(funcName).append("() {\n\t// TODO\n}").toString();
				context.getXtextDocument().replace(offset, 0, newFunc);
			}
		});
		createLinkingIssueResolutions(issue, acceptor);
	}

	@Fix(SHOULD_REPLACE_NAME)
	public void replaceName(final Issue issue, IssueResolutionAcceptor acceptor) {
		final String name = issue.getData()[0];
		final String replacement = issue.getData()[1];
		final int offset = issue.getOffset();
		final int length = issue.getLength();

		String description;
		if (issue.getSeverity() == Severity.ERROR) {
			description = "Updates the name to correct the error.";
		} else {
			description = "Updates the name to respect the recommended coding style.";
		}

		acceptor.accept(issue, "Replace '" + name + "' by '" + replacement + "'", description,
				null, new IModification() {
					public void apply(IModificationContext context) throws BadLocationException {
						IXtextDocument document = context.getXtextDocument();
						document.replace(offset, length, replacement);
					}
				});
	}

}
