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
package com.synflow.ui.internal.launching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.synflow.core.SynflowCore;
import com.synflow.models.dpn.Entity;
import com.synflow.models.util.EcoreHelper;

/**
 * This class defines a resource visitor that collects all .ir and .xdf files.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ModelResourceVisitor implements IResourceVisitor, IResourceDeltaVisitor {

	private List<Entity> eObjects;

	private List<String> filter;

	private ResourceSet set;

	public ModelResourceVisitor(ResourceSet set, String... fileExts) {
		eObjects = new ArrayList<>();
		this.set = set;
		if (fileExts != null) {
			this.filter = Arrays.asList(fileExts);
		}
	}

	public List<Entity> getEObjects() {
		return eObjects;
	}

	private boolean isFileExtensionAccepted(String fileExt) {
		if (filter == null) {
			return true;
		}
		return filter.contains(fileExt);
	}

	/**
	 * Returns true if this file should be skipped. By default always returns false (no files are
	 * skipped). Subclasses may override to implement caching.
	 * 
	 * @param file
	 * @return
	 */
	protected boolean shouldSkipFile(IFile file) {
		return false;
	}

	@Override
	public boolean visit(IResource resource) {
		visitResource(resource);
		return true;
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		int kind = delta.getKind();
		if (kind == IResourceDelta.ADDED || kind == IResourceDelta.CHANGED) {
			visitResource(delta.getResource());
		}

		return true;
	}

	/**
	 * Visits a resource and loads the given resource.
	 * 
	 * @param resource
	 *            the resource being added/changed or visited
	 */
	private void visitResource(IResource resource) {
		if (resource.getType() == IResource.FILE) {
			IFile file = (IFile) resource;
			String fileExt = file.getFileExtension();
			if (!isFileExtensionAccepted(fileExt)) {
				return;
			}

			if (shouldSkipFile(file)) {
				return;
			}

			try {
				Entity entity = EcoreHelper.getEObject(set, file);
				eObjects.add(entity);
			} catch (RuntimeException e) {
				// could not load an actor/network
				SynflowCore.log(e);
			}
		}
	}

}
