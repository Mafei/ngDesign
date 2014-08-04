/*******************************************************************************
 * Copyright (c) 2003, 2011, 2013-2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Matthieu Wipliez (Synflow SAS) - adapted to C~
 *******************************************************************************/
package com.synflow.ngDesign.ui.internal.navigator;

import static com.synflow.core.ISynflowConstants.FILE_EXT_CFLOW;
import static org.eclipse.jdt.core.IJavaElement.PACKAGE_FRAGMENT;

import java.util.ArrayList;
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
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.synflow.core.SynflowCore;
import com.synflow.cx.cx.Module;
import com.synflow.models.util.EcoreHelper;

/**
 * This class defines a C~ content provider. Many methods are copied from JDT's
 * org.eclipse.jdt.internal.ui.navigator.JavaNavigatorContentProvider and are Copyright (c) IBM
 * Corporation and others (see above).
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CflowContentProvider implements IPipelinedTreeContentProvider2,
		IResourceChangeListener, ITreeContentProvider {

	private static class RefreshComputerVisitor implements IResourceDeltaVisitor {

		private Object shouldRefresh;

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			int kind = delta.getKind();
			IResource resource = delta.getResource();
			if (kind == IResourceDelta.ADDED || kind == IResourceDelta.CHANGED) {
				if (resource.getType() == IResource.FILE
						&& FILE_EXT_CFLOW.equals(resource.getFileExtension())) {
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
			IJavaElement element = JavaCore.create((IContainer) parent);
			if (element != null && element.exists()) {
				// we don't convert the root
				if (!(element instanceof IJavaModel) && !(element instanceof IJavaProject))
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
		IJavaElement newChild;
		for (Iterator<Object> childrenItr = currentChildren.iterator(); childrenItr.hasNext();) {
			Object child = childrenItr.next();
			// only convert IFolders and IFiles
			if (child instanceof IFolder || child instanceof IFile) {
				if ((newChild = JavaCore.create((IResource) child)) != null && newChild.exists()) {
					childrenItr.remove();
					convertedChildren.add(newChild);
				}
			} else if (child instanceof IJavaProject) {
				childrenItr.remove();
				convertedChildren.add(((IJavaProject) child).getProject());
			}
		}
		if (!convertedChildren.isEmpty()) {
			currentChildren.addAll(convertedChildren);
			return true;
		}
		return false;

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
			if (element instanceof IJavaElement) {
				IJavaElement cElement = (IJavaElement) element;
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

	@SuppressWarnings("unchecked")
	private void deconvertJavaProjects(PipelinedShapeModification modification) {
		Set<IProject> convertedChildren = new LinkedHashSet<IProject>();
		for (Iterator<IAdaptable> iterator = modification.getChildren().iterator(); iterator
				.hasNext();) {
			Object added = iterator.next();
			if (added instanceof IJavaProject) {
				iterator.remove();
				convertedChildren.add(((IJavaProject) added).getProject());
			}
		}
		modification.getChildren().addAll(convertedChildren);
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
			if (!project.isAccessible()) {
				return new Object[0];
			}

			try {
				return getPackageFragmentRoots(JavaCore.create(project));
			} catch (CoreException e) {
				SynflowCore.log(e);
			}
		}

		if (parentElement instanceof IPackageFragmentRoot) {
			IPackageFragmentRoot root = (IPackageFragmentRoot) parentElement;
			try {
				return getPackageFragments(root);
			} catch (CoreException e) {
				SynflowCore.log(e);
			}
		}

		if (parentElement instanceof IPackageFragment) {
			IPackageFragment fragment = (IPackageFragment) parentElement;
			try {
				return fragment.getNonJavaResources();
			} catch (CoreException e) {
				SynflowCore.log(e);
			}
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
			if (FILE_EXT_CFLOW.equals(file.getFileExtension())) {
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

	private Object[] getPackageFragmentRoots(IJavaProject project) throws JavaModelException {
		List<Object> result = new ArrayList<Object>();

		IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			IClasspathEntry classpathEntry = root.getRawClasspathEntry();
			int entryKind = classpathEntry.getEntryKind();
			if (entryKind == IClasspathEntry.CPE_SOURCE) {
				result.add(root);
			}
		}

		Object[] resources = project.getNonJavaResources();
		result.addAll(Arrays.asList(resources));
		return result.toArray();
	}

	private Object[] getPackageFragments(IPackageFragmentRoot root) throws JavaModelException {
		IJavaElement[] children = root.getChildren();
		Iterable<IJavaElement> iterable;
		iterable = Iterables.filter(Arrays.asList(children), new Predicate<IJavaElement>() {
			@Override
			public boolean apply(IJavaElement input) {
				// no filters on other elements
				if (input.getElementType() != PACKAGE_FRAGMENT) {
					return true;
				}

				// don't show default package
				IPackageFragment fragment = (IPackageFragment) input;
				if (fragment.isDefaultPackage()) {
					return false;
				}

				// do not show empty parent packages
				try {
					if (fragment.hasSubpackages() && SynflowCore.isEmpty(fragment)) {
						return false;
					}
				} catch (JavaModelException e) {
					SynflowCore.log(e);
				}

				return true;
			}
		});
		return Iterables.toArray(iterable, Object.class);
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
			if (element instanceof IProject) {
				return parent;
			}

			// http://bugs.eclipse.org/bugs/show_bug.cgi?id=31374
			IJavaElement jParent = JavaCore.create(parent);
			if (jParent != null && jParent.exists()) {
				return jParent;
			}
			return parent;
		} else if (element instanceof IJavaElement) {
			IJavaElement jElement = (IJavaElement) element;
			if (jElement.exists()) {
				if (element instanceof IPackageFragmentRoot) {
					IPackageFragmentRoot root = (IPackageFragmentRoot) element;
					return root.getJavaProject().getProject();
				}

				return jElement.getParent();
			}
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
		Object parent = addModification.getParent();

		if (parent instanceof IJavaProject) {
			addModification.setParent(((IJavaProject) parent).getProject());
		}

		if (parent instanceof IWorkspaceRoot) {
			deconvertJavaProjects(addModification);
		}

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
		deconvertJavaProjects(removeModification);
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
