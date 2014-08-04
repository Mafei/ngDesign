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
package com.synflow.cx.ui.outline;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.IImageHelper;
import org.eclipse.xtext.ui.editor.outline.IOutlineNode;
import org.eclipse.xtext.ui.editor.outline.impl.DefaultOutlineTreeProvider;
import org.eclipse.xtext.ui.editor.outline.impl.DocumentRootNode;
import org.eclipse.xtext.ui.editor.outline.impl.EObjectNode;
import org.eclipse.xtext.ui.editor.outline.impl.EStructuralFeatureNode;
import org.eclipse.xtext.ui.editor.utils.TextStyle;
import org.eclipse.xtext.ui.label.StylerFactory;

import com.google.inject.Inject;
import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.CType;
import com.synflow.cx.cx.Connect;
import com.synflow.cx.cx.CxEntity;
import com.synflow.cx.cx.Import;
import com.synflow.cx.cx.Imported;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Module;
import com.synflow.cx.cx.MultiPortDecl;
import com.synflow.cx.cx.Obj;
import com.synflow.cx.cx.PortDef;
import com.synflow.cx.cx.SinglePortDecl;
import com.synflow.cx.cx.Typedef;
import com.synflow.cx.cx.VarDecl;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.cx.CxPackage.Literals;

/**
 * This class provides an outline tree for a Cx file. Most methods are declared protected so that
 * JDT does not complain of unused methods.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CxOutlineTreeProvider extends DefaultOutlineTreeProvider {

	@Inject
	private IImageHelper imageHelper;

	@Inject
	private StylerFactory stylerFactory;

	protected void _createNode(DocumentRootNode parentNode, Module module) {
		// package name
		String packageName = module.getPackage();
		createEStructuralFeatureNode(parentNode, module, Literals.MODULE__PACKAGE,
				imageHelper.getImage("package_obj.gif"), packageName, true);

		// imports
		if (!module.getImports().isEmpty()) {
			Image image = imageHelper.getImage("impc_obj.gif");
			EStructuralFeatureNode importsNode = createEStructuralFeatureNode(parentNode, module,
					Literals.MODULE__IMPORTS, image, "imports", false);
			_createChildren(importsNode, module);
		}

		// create node
		for (CxEntity entity : module.getEntities()) {
			EObjectNode node = createEObjectNode(parentNode, entity);
			createNode(node, entity);
		}
	}

	protected void _createNode(EObjectNode parent, CxEntity entity) {
		// entity imports
		if (!entity.getImports().isEmpty()) {
			Image image = imageHelper.getImage("impc_obj.gif");
			createEStructuralFeatureNode(parent, entity, Literals.CX_ENTITY__IMPORTS, image,
					"imports", false);
		}

		createChildren(parent, entity);
	}

	protected void _createNode(EObjectNode parent, Import import_) {
		// do not show imports like this, they are already handled below
	}

	protected void _createNode(EObjectNode parent, Inst inst) {
		EObjectNode node = createEObjectNode(parent, inst);
		if (inst.getTask() != null) {
			createNode(node, inst.getTask());
		}
	}

	protected void _createNode(EObjectNode parent, MultiPortDecl portDecls) {
		for (SinglePortDecl portDecl : portDecls.getDecls()) {
			createNode(parent, portDecl);
		}
	}

	protected void _createNode(EObjectNode parent, Obj object) {
		// do not show properties
	}

	protected void _createNode(EObjectNode parent, SinglePortDecl portDecls) {
		for (PortDef portDecl : portDecls.getPorts()) {
			createNode(parent, portDecl.getVar());
		}
	}

	protected void _createNode(EObjectNode parent, VarDecl stateVars) {
		for (Variable stateVar : stateVars.getVariables()) {
			createNode(parent, stateVar);
		}
	}

	protected void _createNode(EStructuralFeatureNode parent, Import import_) {
		for (Imported imported : import_.getImported()) {
			CxEntity type = imported.getType();
			if (type != null) {
				Object text = textDispatcher.invoke(imported);
				Image image = imageDispatcher.invoke(imported);
				createEObjectNode(parent, imported, image, text, true);
			}
		}
	}

	protected boolean _isLeaf(Connect connect) {
		return true;
	}

	protected boolean _isLeaf(Inst inst) {
		return inst.getTask() == null;
	}

	protected boolean _isLeaf(Obj obj) {
		return true;
	}

	protected boolean _isLeaf(Typedef typeDef) {
		return true;
	}

	protected boolean _isLeaf(Variable variable) {
		return true;
	}

	protected String _text(Imported imported) {
		List<INode> nodes = NodeModelUtils.findNodesForFeature(imported, Literals.IMPORTED__TYPE);
		String name = NodeModelUtils.getTokenText(nodes.get(0));
		if (imported.isWildcard()) {
			name += ".*";
		}
		return name;
	}

	/**
	 * Returns the textual representation of a type definition.
	 * 
	 * @param typeDef
	 *            a type definition
	 * @return a StyledString
	 */
	protected Object _text(Typedef typeDef) {
		StyledString styledText = (StyledString) super._text(typeDef);
		return appendSimpleName(styledText, typeDef.getType());
	}

	/**
	 * Returns the textual representation of a variable (port/state variable).
	 * 
	 * @param variable
	 *            a variable
	 * @return a StyledString
	 */
	protected Object _text(Variable variable) {
		StyledString styledText = (StyledString) super._text(variable);
		CType type = CxUtil.getType(variable);
		return appendSimpleName(styledText, type);
	}

	/**
	 * Appends " : " and the string representation of the given type, and returns a StyledString, or
	 * <code>null</code> if any of the two arguments is null.
	 * 
	 * @param styledText
	 *            a styled string
	 * @param type
	 *            AST type
	 * @return a StyledString or <code>null</code>
	 */
	private Object appendSimpleName(StyledString styledText, CType type) {
		if (styledText == null) {
			return null;
		}

		String typeName = " : ";
		typeName += (type == null) ? "void" : labelProvider.getText(type);
		return styledText.append(new StyledString(typeName, stylerFactory
				.createXtextStyleAdapterStyler(getTypeTextStyle())));
	}

	/**
	 * Handles null elements.
	 */
	@Override
	protected void createNode(IOutlineNode parent, EObject modelElement) {
		if (modelElement != null) {
			super.createNode(parent, modelElement);
		}
	}

	/**
	 * Returns the text style for the "type" part.
	 * 
	 * @return a TextStyle instance
	 */
	private TextStyle getTypeTextStyle() {
		TextStyle textStyle = new TextStyle();
		textStyle.setColor(new RGB(149, 125, 71));
		return textStyle;
	}

}
