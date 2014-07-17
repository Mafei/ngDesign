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
package com.synflow.ngDesign.ui.internal.navigator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.synflow.cflow.cflow.NamedEntity;

/**
 * This class defines a simple sorter for our Project Explorer view.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CflowComparator extends ViewerComparator {

	private static final int OTHERS = 51;

	private static final int PACKAGEFRAGMENT = 3;

	private static final int PACKAGEFRAGMENTROOTS = 2;

	private static final int PROJECTS = 1;

	private static final int RESOURCEFOLDERS = 7;

	private static final int RESOURCES = 8;

	@Override
	public int category(Object element) {
		if (element instanceof IProject) {
			return PROJECTS;
		} else if (element instanceof IPackageFragmentRoot) {
			return PACKAGEFRAGMENTROOTS;
		} else if (element instanceof IPackageFragment) {
			return PACKAGEFRAGMENT;
		} else if (element instanceof IContainer) {
			return RESOURCEFOLDERS;
		} else if (element instanceof IFile) {
			return RESOURCES;
		}
		return OTHERS;
	}

	@Override
	@SuppressWarnings("unchecked")
	public int compare(Viewer viewer, Object e1, Object e2) {
		int cat1 = category(e1);
		int cat2 = category(e2);

		if (cat1 != cat2) {
			return cat1 - cat2;
		}

		String name1 = getElementName(e1);
		String name2 = getElementName(e2);

		// use the comparator to compare the strings
		int result = getComparator().compare(name1, name2);
		return result;
	}

	private String getElementName(Object element) {
		if (element instanceof IJavaElement) {
			return ((IJavaElement) element).getElementName();
		} else if (element instanceof NamedEntity) {
			return ((NamedEntity) element).getName();
		} else {
			return element.toString();
		}
	}

}
