/*******************************************************************************
 * Copyright (c) 2014 Synflow SAS, 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Matthieu Wipliez (Synflow SAS) - simplified code
 *******************************************************************************/
package com.synflow.ui.internal.navigator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * This class defines a simple sorter for our Project Explorer view.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ProjectExplorerSorter extends ViewerSorter {

	private final CflowComparator fComparator;

	/**
	 * Constructor.
	 */
	public ProjectExplorerSorter() {
		super(null); // delay initialization of collator
		fComparator = new CflowComparator();
	}

	@Override
	public int category(Object element) {
		return fComparator.category(element);
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		return fComparator.compare(viewer, e1, e2);
	}

}
