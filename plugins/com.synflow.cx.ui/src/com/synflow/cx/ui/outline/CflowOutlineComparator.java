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

import org.eclipse.xtext.ui.editor.outline.IOutlineNode;
import org.eclipse.xtext.ui.editor.outline.actions.SortOutlineContribution;
import org.eclipse.xtext.ui.editor.outline.impl.EObjectNode;

import com.synflow.cx.cx.CflowPackage;

/**
 * This class provides a comparator for entries of the outline tree.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CflowOutlineComparator extends SortOutlineContribution.DefaultComparator {

	@Override
	public int getCategory(IOutlineNode node) {
		if (node instanceof EObjectNode) {
			EObjectNode objNode = (EObjectNode) node;
			switch (objNode.getEClass().getClassifierID()) {
			case CflowPackage.MODULE:
				return Integer.MIN_VALUE;
			case CflowPackage.VAR_DECL:
				return -10;
			default:
				return 0;
			}
		}

		return 0;
	}

}
