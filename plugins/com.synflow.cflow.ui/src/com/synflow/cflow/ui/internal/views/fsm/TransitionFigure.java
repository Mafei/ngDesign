/*******************************************************************************
 * Copyright (c) 2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cflow.ui.internal.views.fsm;

import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.swt.SWT;

/**
 * This class defines the figure for a transition.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class TransitionFigure extends PolylineConnection {

	public TransitionFigure() {
		// set anti-aliasing
		setAntialias(SWT.ON);

		// add arrow at the end
		PolygonDecoration decoration = new PolygonDecoration();
		decoration.setAntialias(SWT.ON);
		setTargetDecoration(decoration);
	}

}
