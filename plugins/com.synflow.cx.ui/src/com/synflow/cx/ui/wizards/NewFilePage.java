/*******************************************************************************
 * Copyright (c) 2012 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import com.synflow.core.SynflowCore;

/**
 * This class provides a page to create a new text file.
 * 
 * @author Matthieu Wipliez
 */
public abstract class NewFilePage extends WizardNewFileCreationPage {

	private String fileExtension;

	public NewFilePage(String pageName, String fileExt, IStructuredSelection selection) {
		super(pageName, selection);
		this.fileExtension = fileExt;
	}

	protected final String getEntityName() {
		return new Path(getFileName()).removeFileExtension().lastSegment();
	}

	@Override
	public InputStream getInitialContents() {
		final String author = System.getProperty("user.name");
		final int year = Calendar.getInstance().get(Calendar.YEAR);

		return new ByteArrayInputStream(getStringContents(author, year).toString().getBytes());
	}

	protected final String getPackage() {
		return SynflowCore.getPackage(getContainerFullPath());
	}

	protected abstract CharSequence getStringContents(String author, int year);

	@Override
	public void setFileName(String value) {
		super.setFileName(value + "." + fileExtension);
	}

	@Override
	protected boolean validatePage() {
		if (!super.validatePage()) {
			return false;
		}

		// check first letter is uppercase
		char first = getFileName().charAt(0);
		if (!Character.isUpperCase(first)) {
			setErrorMessage("The file name must begin with a uppercase letter");
			return false;
		}

		// check file extension
		String fileExt = new Path(getFileName()).getFileExtension();
		if (!fileExtension.equals(fileExt)) {
			setErrorMessage("The file name must have a ." + fileExtension + " extension");
			return false;
		}

		// check file is in project
		IPath path = getContainerFullPath();
		IFile file = createFileHandle(path.append(getFileName()));
		IJavaProject project = JavaCore.create(file.getProject());
		if (project == null) {
			setErrorMessage("The file must be located in a Synflow project");
			return false;
		}

		// check file is in package
		try {
			IPackageFragment fragment = project.findPackageFragment(path);
			if (fragment == null) {
				setErrorMessage("The container must be a package");
				return false;
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		setErrorMessage(null);

		return true;
	}

}
