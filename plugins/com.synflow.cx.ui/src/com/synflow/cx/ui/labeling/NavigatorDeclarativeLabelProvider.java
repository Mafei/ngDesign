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
package com.synflow.cx.ui.labeling;

import static com.synflow.core.ISynflowConstants.FILE_EXT_CX;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.xtext.ui.label.DeclarativeLabelProvider;

import com.google.inject.Inject;
import com.synflow.core.SynflowCore;
import com.synflow.core.SynflowNature;
import com.synflow.core.layout.ITreeElement;
import com.synflow.core.layout.Package;
import com.synflow.core.layout.SourceFolder;

/**
 * This class defines a declarative label provider that extends {@link CxLabelProvider} with images
 * for projects, Cx files, Java source folders and packages, and text for resources and Java
 * elements.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class NavigatorDeclarativeLabelProvider extends DeclarativeLabelProvider {

	@Inject
	public NavigatorDeclarativeLabelProvider(CxLabelProvider delegate) {
		super(delegate);
	}

	public String image(IFile file) {
		if (FILE_EXT_CX.equals(file.getFileExtension())) {
			return "cx_obj.gif";
		}
		return null;
	}

	public String image(IProject project) {
		try {
			if (project.isAccessible() && project.hasNature(SynflowNature.NATURE_ID)) {
				return "sfprj_obj.gif";
			}
		} catch (CoreException e) {
			SynflowCore.log(e);
		}
		return null;
	}

	public String image(Package package_) {
		if (package_.isEmpty()) {
			return "empty_pack_obj.gif";
		}
		return "package_obj.gif";
	}

	public String image(SourceFolder folder) {
		return "packagefolder_obj.gif";
	}

	public String text(IResource resource) {
		return resource.getName();
	}

	public String text(ITreeElement element) {
		return element.getName();
	}

}