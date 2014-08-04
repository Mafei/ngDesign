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
package com.synflow.cx.internal;

import static java.math.BigInteger.ZERO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.TerminalRule;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.synflow.cx.cx.Statement;
import com.synflow.models.ir.ExprBool;
import com.synflow.models.ir.ExprInt;
import com.synflow.models.ir.Expression;

/**
 * This class defines utility functions for transformation of C~.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class TransformerUtil {

	public static List<Expression> emptyList() {
		return Collections.<Expression> emptyList();
	}

	/**
	 * Returns the line at which the given object ends.
	 * 
	 * @param object
	 *            an AST object
	 * @return the line at which the given object ends
	 */
	public static int getEndLine(EObject object) {
		ICompositeNode node = NodeModelUtils.getNode(object);
		if (node == null) {
			object = object.eContainer();
			if (object == null) {
				return 0;
			} else {
				return getEndLine(object);
			}
		} else {
			return node.getEndLine() + 1;
		}
	}

	/**
	 * Returns the line at which the given object starts.
	 * 
	 * @param object
	 *            an AST object
	 * @return the line at which the given object starts
	 */
	public static int getStartLine(EObject object) {
		ICompositeNode node = NodeModelUtils.getNode(object);
		if (node == null) {
			object = object.eContainer();
			if (object == null) {
				return 0;
			} else {
				return getStartLine(object);
			}
		} else {
			return node.getStartLine();
		}
	}

	/**
	 * Returns <code>true</code> if <code>expr</code> is an ExprBool whose value is
	 * <code>false</code>.
	 * 
	 * @param expr
	 *            an expression
	 * @return a boolean indicating whether <code>expr</code> is an ExprBool whose value is
	 *         <code>false</code>
	 */
	public static boolean isFalse(Expression expr) {
		return expr instanceof ExprBool && !((ExprBool) expr).isValue();
	}

	/**
	 * Returns <code>true</code> if <code>expr</code> is an ExprInt whose value is different from
	 * <code>0</code>.
	 * 
	 * @param expr
	 *            an expression
	 * @return a boolean indicating whether <code>expr</code> is an ExprInt whose value is different
	 *         from <code>0</code>
	 */
	public static boolean isOne(Expression expr) {
		return expr instanceof ExprInt && ((ExprInt) expr).getValue().compareTo(ZERO) != 0;
	}

	/**
	 * Returns <code>true</code> if <code>expr</code> is an ExprBool whose value is
	 * <code>true</code>.
	 * 
	 * @param expr
	 *            an expression
	 * @return a boolean indicating whether <code>expr</code> is an ExprBool whose value is
	 *         <code>true</code>
	 */
	public static boolean isTrue(Expression expr) {
		return expr instanceof ExprBool && ((ExprBool) expr).isValue();
	}

	/**
	 * Returns <code>true</code> if <code>expr</code> is an ExprInt whose value equals
	 * <code>0</code>.
	 * 
	 * @param expr
	 *            an expression
	 * @return a boolean indicating whether <code>expr</code> is an ExprInt whose value equals
	 *         <code>0</code>
	 */
	public static boolean isZero(Expression expr) {
		return expr instanceof ExprInt && ((ExprInt) expr).getValue().compareTo(ZERO) == 0;
	}

	public static void mkComment(Statement statement) {
		List<String> lines = new ArrayList<>();
		ICompositeNode node = NodeModelUtils.findActualNodeFor(statement);
		for (ILeafNode leaf : node.getLeafNodes()) {
			if (leaf.isHidden()) {
				String text = leaf.getText();
				EObject eObject = leaf.getGrammarElement();
				if (eObject instanceof TerminalRule) {
					TerminalRule rule = (TerminalRule) eObject;
					String name = rule.getName();
					if ("SL_COMMENT".equals(name)) {
						lines.add(text.substring(2).trim());
					} else if ("ML_COMMENT".equals(name)) {
						String contents = text.substring(2, text.length() - 2);
						String[] lineArray = contents.split("\\r?\\n");
						lines.addAll(Arrays.asList(lineArray));
					} else {
						continue;
					}
				}
			}
		}

		// List<Attribute> attributes = builder.getProcedure().getAttributes();
		// for (String line : lines) {
		// Attribute attribute = UtilFactory.eINSTANCE.createAttribute(ATTR_COMMENTS);
		// attribute.setStringValue(line);
		// attributes.add(attribute);
		// }
	}

}
