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
package com.synflow.core.internal.builder;

import static com.synflow.core.ISynflowConstants.FILE_EXT_CFLOW;
import static com.synflow.core.ISynflowConstants.FILE_EXT_IR;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;

/**
 * This class defines a file resource visitor.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class FileResourceVisitor implements IResourceVisitor,
		IResourceDeltaVisitor {

	private final List<IFile> derived;

	private final List<IFile> removed;

	private final List<IFile> sources;

	public FileResourceVisitor() {
		derived = new ArrayList<>();
		removed = new ArrayList<>();
		sources = new ArrayList<>();
	}

	public List<IFile> getDerived() {
		return derived;
	}

	public List<IFile> getRemoved() {
		return removed;
	}

	public List<IFile> getSources() {
		return sources;
	}

	@Override
	public boolean visit(IResource resource) {
		visitResource(resource);
		return true;
	}

	@Override
	public boolean visit(IResourceDelta delta) {
		int kind = delta.getKind();
		if (kind == IResourceDelta.ADDED || kind == IResourceDelta.CHANGED) {
			visitResource(delta.getResource());
		} else if (kind == IResourceDelta.REMOVED) {
			IResource resource = delta.getResource();
			if (resource.getType() == IResource.FILE) {
				IFile file = (IFile) resource;
				String fileExt = resource.getFileExtension();

				if (fileExt != null) {
					switch (fileExt) {
					case FILE_EXT_CFLOW:
						removed.add(file);
					}
				}
			}
		}

		return true;
	}

	/**
	 * Visits a resource and adds the given resource to the appropriate list (if
	 * any).
	 * 
	 * @param resource
	 *            the resource being added/changed or visited
	 */
	private void visitResource(IResource resource) {
		if (resource.getType() == IResource.FILE) {
			IFile file = (IFile) resource;
			String fileExt = resource.getFileExtension();

			if (fileExt != null) {
				switch (fileExt) {
				case FILE_EXT_CFLOW:
					sources.add(file);
					break;
				case FILE_EXT_IR:
					derived.add(file);
					break;
				}
			}
		}
	}

}
