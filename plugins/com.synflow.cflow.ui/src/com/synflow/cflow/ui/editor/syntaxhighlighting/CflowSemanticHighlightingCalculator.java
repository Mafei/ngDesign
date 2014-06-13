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
package com.synflow.cflow.ui.editor.syntaxhighlighting;

import static com.synflow.cflow.CflowConstants.NAME_LOOP;
import static com.synflow.cflow.CflowConstants.NAME_LOOP_DEPRECATED;
import static com.synflow.cflow.CflowConstants.NAME_SETUP;
import static com.synflow.cflow.CflowConstants.NAME_SETUP_DEPRECATED;
import static com.synflow.cflow.ui.editor.syntaxhighlighting.CflowHighlightingConfiguration.DEPRECATED_ID;
import static com.synflow.cflow.ui.editor.syntaxhighlighting.CflowHighlightingConfiguration.SPECIAL_ID;
import static com.synflow.cflow.ui.editor.syntaxhighlighting.CflowHighlightingConfiguration.TYPE_ID;
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

import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.CflowPackage.Literals;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.Module;
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.cflow.cflow.Network;
import com.synflow.cflow.cflow.Task;
import com.synflow.cflow.cflow.TypeRef;
import com.synflow.cflow.cflow.Typedef;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.cflow.util.CflowSwitch;
import com.synflow.models.util.Void;

/**
 * This class defines a highlighting calculator for typedefs.
 * 
 * @author Matthieu Wipliez
 */
public class CflowSemanticHighlightingCalculator implements ISemanticHighlightingCalculator {

	private class VariableSwitch extends CflowSwitch<Void> {

		private IHighlightedPositionAcceptor acceptor;

		public VariableSwitch(IHighlightedPositionAcceptor acceptor) {
			this.acceptor = acceptor;
		}

		@Override
		public Void caseInst(Inst inst) {
			return visit(this, inst.getTask());
		}

		@Override
		public Void caseNamedEntity(NamedEntity entity) {
			for (Typedef typeDef : entity.getTypes()) {
				addPosition(acceptor, TYPE_ID, typeDef, Literals.TYPEDEF__NAME);
			}

			return DONE;
		}

		@Override
		public Void caseNetwork(Network network) {
			visit(this, network.getInstances());
			return CASCADE;
		}

		@Override
		public Void caseTask(Task task) {
			for (Variable function : CflowUtil.getFunctions(task.getDecls())) {
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
