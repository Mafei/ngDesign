/*******************************************************************************
 * Copyright (c) 2013-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.ui.internal.views.graph;

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.swt.SWT;

/**
 * This class defines the figure for a connection.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ConnectionFigure extends PolylineConnection {

	public ConnectionFigure() {
		// set anti-aliasing
		setAntialias(SWT.ON);
	}

}
