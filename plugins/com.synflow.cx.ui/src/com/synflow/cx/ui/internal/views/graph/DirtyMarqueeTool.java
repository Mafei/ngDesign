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
package com.synflow.cx.ui.internal.views.graph;

import java.util.Collection;

import org.eclipse.gef.tools.MarqueeDragTracker;

/**
 * This class defines a dirty marquee tool.
 * 
 * @author Matthieu Wipliez
 *
 */
public class DirtyMarqueeTool extends MarqueeDragTracker {

	public static boolean duringMarquee;

	@SuppressWarnings("rawtypes")
	protected Collection calculateMarqueeSelectedEditParts() {
		duringMarquee = true;
		try {
			return super.calculateMarqueeSelectedEditParts();
		} finally {
			duringMarquee = false;
		}
	}

}
