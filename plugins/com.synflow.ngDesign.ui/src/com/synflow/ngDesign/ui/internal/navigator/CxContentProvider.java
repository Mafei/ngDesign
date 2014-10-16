/*******************************************************************************
 * Copyright (c) 2003, 2011, 2013-2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Matthieu Wipliez (Synflow SAS) - adapted to Cx
 *******************************************************************************/
package com.synflow.ngDesign.ui.internal.navigator;

import static com.synflow.core.ISynflowConstants.FILE_EXT_CX;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider2;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.synflow.core.SynflowCore;
import com.synflow.core.layout.ITreeElement;
import com.synflow.core.layout.Package;
import com.synflow.core.layout.ProjectLayout;
import com.synflow.core.layout.SourceFolder;
import com.synflow.cx.cx.Module;
import com.synflow.models.util.EcoreHelper;

/**
 * This class defines a Cx content provider. Many methods are copied from JDT's
 * org.eclipse.jdt.internal.ui.navigator.JavaNavigatorContentProvider and are Copyright (c) IBM
 * Corporation and others (see above).
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CxContentProvider implements IPipelinedTreeContentProvider2, IResourceChangeListener,
		ITreeContentProvider {

	private static class RefreshComputerVisitor implements IResourceDeltaVisitor {

		private Object shouldRefresh;

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			int kind = delta.getKind();
			IResource resource = delta.getResource();
			if (kind == IResourceDelta.ADDED || kind == IResourceDelta.CHANGED) {
				if (resource.getType() == IResource.FILE
						&& FILE_EXT_CX.equals(resource.getFileExtension())) {
					shouldRefresh = resource;
				}
			} else if (kind == IResourceDelta.REMOVED) {
				shouldRefresh = resource;
			}
			return true;
		}

	}

	private ColumnViewer viewer;

	/**
	 * Converts the shape modification to use Java elements.
	 * 
	 * 
	 * @param modification
	 *            the shape modification to convert
	 * @return returns true if the conversion took place
	 */
	@SuppressWarnings("unchecked")
	private boolean convertToJavaElements(PipelinedShapeModification modification) {
		Object parent = modification.getParent();
		// As of 3.3, we no longer re-parent additions to IProject.
		if (parent instanceof IContainer) {
			ITreeElement element = ProjectLayout.getTreeElement((IResource) parent);
			if (element != null) {
				boolean converted = convertToJavaElements(modification.getChildren());
				if (converted) {
					boolean packages = true;
					for (Object obj : modification.getChildren()) {
						if (obj instanceof ITreeElement) {
							ITreeElement child = (ITreeElement) obj;
							packages &= child.isPackage();
						} else {
							packages = false;
						}
					}

					if (packages) {
						element = ProjectLayout.getSourceFolder(element.getResource().getProject());
					}
				}

				// we don't convert the root
				modification.setParent(element);
				return convertToJavaElements(modification.getChildren());
			}
		}
		return false;
	}

	/**
	 * Converts the shape modification to use Java elements.
	 * 
	 * 
	 * @param currentChildren
	 *            The set of current children that would be contributed or refreshed in the viewer.
	 * @return returns true if the conversion took place
	 */
	private boolean convertToJavaElements(Set<Object> currentChildren) {
		LinkedHashSet<Object> convertedChildren = new LinkedHashSet<Object>();
		for (Iterator<Object> childrenItr = currentChildren.iterator(); childrenItr.hasNext();) {
			Object child = childrenItr.next();
			// only convert IFolders and IFiles
			if (child instanceof IResource) {
				if (convertToJavaElements(convertedChildren, (IResource) child)) {
					childrenItr.remove();
				}
			}
		}

		if (!convertedChildren.isEmpty()) {
			currentChildren.addAll(convertedChildren);
			return true;
		}
		return false;
	}

	private boolean convertToJavaElements(Set<Object> currentChildren, IResource member) {
		ITreeElement newChild = ProjectLayout.getTreeElement(member);
		boolean validChild = newChild != null;
		if (validChild) {
			currentChildren.add(newChild);

			if (member instanceof IFolder) {
				IFolder folder = (IFolder) member;
				try {
					if (folder.exists()) {
						for (IResource child : folder.members()) {
							convertToJavaElements(currentChildren, child);
						}
					}
				} catch (CoreException e) {
					SynflowCore.log(e);
				}
			}
		}
		return validChild;
	}

	/**
	 * Adapted from the Common Navigator Content Provider
	 * 
	 * @param javaElements
	 *            the java elements
	 * @param proposedChildren
	 *            the proposed children
	 */
	private void customize(Object[] javaElements, Set<Object> proposedChildren) {
		List<?> elementList = Arrays.asList(javaElements);
		for (Iterator<?> iter = proposedChildren.iterator(); iter.hasNext();) {
			Object element = iter.next();
			IResource resource = null;
			if (element instanceof IResource) {
				resource = (IResource) element;
			} else if (element instanceof IAdaptable) {
				resource = (IResource) ((IAdaptable) element).getAdapter(IResource.class);
			}
			if (resource != null) {
				int i = elementList.indexOf(resource);
				if (i >= 0) {
					javaElements[i] = null;
				}
			}
		}
		for (int i = 0; i < javaElements.length; i++) {
			Object element = javaElements[i];
			if (element instanceof ITreeElement) {
				ITreeElement cElement = (ITreeElement) element;
				IResource resource = cElement.getResource();
				if (resource != null) {
					proposedChildren.remove(resource);
				}
				proposedChildren.add(element);
			} else if (element != null) {
				proposedChildren.add(element);
			}
		}
	}

	@Override
	public void dispose() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(this);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IProject) {
			IProject project = (IProject) parentElement;
			return ProjectLayout.getChildren(project);
		}

		if (parentElement instanceof SourceFolder) {
			SourceFolder root = (SourceFolder) parentElement;
			return root.getPackages();
		}

		if (parentElement instanceof Package) {
			Package package_ = (Package) parentElement;
			return package_.getFiles();
		}

		if (parentElement instanceof IFolder) {
			IFolder folder = (IFolder) parentElement;
			try {
				return folder.members();
			} catch (CoreException e) {
				SynflowCore.log(e);
			}
		} else if (parentElement instanceof IFile) {
			IFile file = (IFile) parentElement;
			if (FILE_EXT_CX.equals(file.getFileExtension())) {
				ResourceSet set = new XtextResourceSet();
				Module module = EcoreHelper.getEObject(set, file);
				if (module != null) {
					return getChildren(module);
				}
			}
		} else if (parentElement instanceof Module) {
			Module module = (Module) parentElement;
			return module.getEntities().toArray();
		}
		return new Object[0];
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IWorkspaceRoot) {
			IWorkspaceRoot root = (IWorkspaceRoot) inputElement;
			return root.getProjects();
		}
		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		// copied and modified from JDT
		if (element == null) {
			return false;
		}

		// try to map resources to the containing package fragment
		if (element instanceof IResource) {
			IResource parent = ((IResource) element).getParent();
			if (parent == null) {
				return null;
			}

			ITreeElement tree = ProjectLayout.getTreeElement(parent);
			if (tree != null) {
				return tree;
			}
			return parent;
		} else if (element instanceof Package) {
			Package package_ = (Package) element;
			return package_.getSourceFolder();
		} else if (element instanceof SourceFolder) {
			SourceFolder folder = (SourceFolder) element;
			return folder.getProject();
		}

		return null;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getPipelinedChildren(Object parent, Set currentChildren) {
		customize(getChildren(parent), currentChildren);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getPipelinedElements(Object input, Set currentElements) {
		customize(getElements(input), currentElements);
	}

	@Override
	public Object getPipelinedParent(Object object, Object suggestedParent) {
		return getParent(object);
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IProject) {
			return ((IProject) element).isAccessible();
		}

		return getChildren(element).length > 0;
	}

	@Override
	public boolean hasPipelinedChildren(Object element, boolean currentHasChildren) {
		return hasChildren(element);
	}

	@Override
	public void init(ICommonContentExtensionSite aConfig) {
		IMemento memento = aConfig.getMemento();
		restoreState(memento);

		// nothing else to do at the moment
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (ColumnViewer) viewer;

		if (newInput instanceof IWorkspaceRoot) {
			IWorkspaceRoot root = (IWorkspaceRoot) newInput;
			root.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		}
	}

	@Override
	public PipelinedShapeModification interceptAdd(PipelinedShapeModification addModification) {
		convertToJavaElements(addModification);
		return addModification;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean interceptRefresh(PipelinedViewerUpdate refreshSynchronization) {
		return convertToJavaElements(refreshSynchronization.getRefreshTargets());
	}

	@Override
	@SuppressWarnings("unchecked")
	public PipelinedShapeModification interceptRemove(PipelinedShapeModification removeModification) {
		convertToJavaElements(removeModification.getChildren());
		return removeModification;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean interceptUpdate(PipelinedViewerUpdate updateSynchronization) {
		return convertToJavaElements(updateSynchronization.getRefreshTargets());
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		try {
			final RefreshComputerVisitor visitor = new RefreshComputerVisitor();
			delta.accept(visitor);
			if (visitor.shouldRefresh != null) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						viewer.refresh();
					}
				});
			}
		} catch (CoreException e) {
			SynflowCore.log(e);
		}
	}

	@Override
	public void restoreState(IMemento aMemento) {
		// nothing to do here
	}

	@Override
	public void saveState(IMemento aMemento) {
		// nothing to do here
	}

}
