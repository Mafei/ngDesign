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
package com.synflow.cx.ui.editor.syntaxhighlighting;

import static com.synflow.cx.CxConstants.NAME_LOOP;
import static com.synflow.cx.CxConstants.NAME_LOOP_DEPRECATED;
import static com.synflow.cx.CxConstants.NAME_SETUP;
import static com.synflow.cx.CxConstants.NAME_SETUP_DEPRECATED;
import static com.synflow.cx.ui.editor.syntaxhighlighting.CxHighlightingConfiguration.DEPRECATED_ID;
import static com.synflow.cx.ui.editor.syntaxhighlighting.CxHighlightingConfiguration.SPECIAL_ID;
import static com.synflow.cx.ui.editor.syntaxhighlighting.CxHighlightingConfiguration.TYPE_ID;
import static com.synflow.models.util.SwitchUtil.CASCADE;
import static com.synflow.models.util.SwitchUtil.DONE;
import static com.synflow.models.util.SwitchUtil.visit;
import static org.eclipse.xtext.EcoreUtil2.getAllContentsOfType;
import static org.eclipse.xtext.nodemodel.util.NodeModelUtils.findActualNodeFor;
import static org.eclipse.xtext.nodemodel.util.NodeModelUtils.findNodesForFeature;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightedPositionAcceptor;
import org.eclipse.xtext.ui.editor.syntaxcoloring.ISemanticHighlightingCalculator;

import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.CxEntity;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Module;
import com.synflow.cx.cx.Network;
import com.synflow.cx.cx.Task;
import com.synflow.cx.cx.TypeRef;
import com.synflow.cx.cx.Typedef;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.cx.CxPackage.Literals;
import com.synflow.cx.cx.util.CxSwitch;
import com.synflow.models.util.Void;

/**
 * This class defines a highlighting calculator for typedefs.
 * 
 * @author Matthieu Wipliez
 */
public class CxSemanticHighlightingCalculator implements ISemanticHighlightingCalculator {

	private class VariableSwitch extends CxSwitch<Void> {

		private IHighlightedPositionAcceptor acceptor;

		public VariableSwitch(IHighlightedPositionAcceptor acceptor) {
			this.acceptor = acceptor;
		}

		@Override
		public Void caseCxEntity(CxEntity entity) {
			for (Typedef typeDef : entity.getTypes()) {
				addPosition(acceptor, TYPE_ID, typeDef, Literals.TYPEDEF__NAME);
			}

			return DONE;
		}

		@Override
		public Void caseInst(Inst inst) {
			return visit(this, inst.getTask());
		}

		@Override
		public Void caseNetwork(Network network) {
			visit(this, network.getInstances());
			return CASCADE;
		}

		@Override
		public Void caseTask(Task task) {
			for (Variable function : CxUtil.getFunctions(task.getDecls())) {
				String name = function.getName();
				if (NAME_SETUP.equals(name) || NAME_LOOP.equals(name)) {
					addPosition(acceptor, SPECIAL_ID, function, Literals.VARIABLE__NAME);
				} else if (NAME_SETUP_DEPRECATED.equals(name) || NAME_LOOP_DEPRECATED.equals(name)) {
					addPosition(acceptor, DEPRECATED_ID, function, Literals.VARIABLE__NAME);
				}
			}
			return CASCADE;
		}

	}

	private void addPosition(IHighlightedPositionAcceptor acceptor, String id, EObject eObject) {
		addPosition(acceptor, id, findActualNodeFor(eObject));
	}

	private void addPosition(IHighlightedPositionAcceptor acceptor, String id, EObject eObject,
			EStructuralFeature feature) {
		List<INode> nodes = findNodesForFeature(eObject, feature);
		if (!nodes.isEmpty()) {
			addPosition(acceptor, id, nodes.get(0));
		}
	}

	private void addPosition(IHighlightedPositionAcceptor acceptor, String id, INode node) {
		int offset = node.getOffset();
		int length = node.getLength();
		acceptor.addPosition(offset, length, id);
	}

	@Override
	public void provideHighlightingFor(XtextResource resource, IHighlightedPositionAcceptor acceptor) {
		if (resource == null || resource.getParseResult() == null) {
			return;
		}

		INode root = resource.getParseResult().getRootNode();
		Module module = (Module) root.getSemanticElement();
		if (module != null) {
			VariableSwitch sw = new VariableSwitch(acceptor);
			visit(sw, module.getEntities());

			for (TypeRef typeRef : getAllContentsOfType(module, TypeRef.class)) {
				addPosition(acceptor, TYPE_ID, typeRef);
			}
		}
	}

}
