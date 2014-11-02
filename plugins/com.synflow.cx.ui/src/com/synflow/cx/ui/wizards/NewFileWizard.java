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
/*
 * Copyright (c) 2010, IETR/INSA of Rennes
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the IETR/INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.synflow.cx.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * This class provides a wizard to create a new file.
 * 
 * @author Matthieu Wipliez
 */
public abstract class NewFileWizard extends Wizard implements INewWizard {

	private NewFilePage newFilePage;

	protected IStructuredSelection selection;

	private IWorkbench workbench;

	@Override
	public void addPages() {
		newFilePage = new NewFilePage(getType(), selection);
		addPage(newFilePage);
	}

	/**
	 * Returns a stream containing the initial contents to be given to new file resource instances.
	 * 
	 * @return initial contents to be given to new file resource instances
	 */
	private InputStream getInitialContents() {
		final String author = System.getProperty("user.name");
		final int year = Calendar.getInstance().get(Calendar.YEAR);
		String pack = newFilePage.getPackage();
		String entityName = newFilePage.getEntityName();

		CharSequence charSeq = getStringContents(author, year, pack, entityName);
		return new ByteArrayInputStream(charSeq.toString().getBytes());
	}

	abstract protected CharSequence getStringContents(String author, int year, String package_,
			String entityName);

	protected abstract String getType();

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		this.workbench = workbench;

		setNeedsProgressMonitor(true);
		setWindowTitle("New Cx " + getType());
	}

	@Override
	public boolean performFinish() {
		NewFilePage page = (NewFilePage) getPages()[0];
		IFile file = page.createNewFile(getInitialContents());
		if (file == null) {
			return false;
		}

		// Open editor on new file.
		IWorkbenchWindow dw = workbench.getActiveWorkbenchWindow();
		try {
			if (dw != null) {
				BasicNewResourceWizard.selectAndReveal(file, dw);
				IWorkbenchPage activePage = dw.getActivePage();
				if (activePage != null) {
					IDE.openEditor(activePage, file, true);
				}
			}
		} catch (PartInitException e) {
			MessageDialog.openError(dw.getShell(), "Problem opening editor", e.getMessage());
		}

		return true;
	}

}
