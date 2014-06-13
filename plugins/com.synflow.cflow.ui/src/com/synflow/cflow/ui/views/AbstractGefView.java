/*******************************************************************************
 * Copyright (c) 2012-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cflow.ui.views;

import static com.synflow.core.ISynflowConstants.FILE_EXT_CFLOW;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.FreeformGraphicalRootEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.IXtextModelListener;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import com.google.inject.Inject;
import com.synflow.cflow.cflow.Module;
import com.synflow.cflow.cflow.NamedEntity;
import com.synflow.cflow.ui.internal.views.graph.DirtyMarqueeTool;
import com.synflow.core.util.CoreUtil;
import com.synflow.models.dpn.Entity;
import com.synflow.models.util.EcoreHelper;

/**
 * This class defines a view of the FSM of a task.
 * 
 * @author Matthieu Wipliez
 * 
 */
public abstract class AbstractGefView extends ViewPart implements IPartListener,
		IXtextModelListener {

	private class CaretListenerImpl implements CaretListener {

		@Override
		public void caretMoved(CaretEvent event) {
			StyledText text = (StyledText) event.widget;
			int offset = event.caretOffset;
			selectLine(text, offset);
		}

	}

	private CaretListenerImpl caretListener;

	private EditDomain editDomain;

	private String entityName;

	@Inject
	private IQualifiedNameProvider nameProvider;

	private XtextResource resource;

	private SelectionSynchronizer synchronizer;

	protected GraphicalViewer viewer;

	private IXtextDocument xtextDocument;

	public AbstractGefView() {
		caretListener = new CaretListenerImpl();
		entityName = "";
	}

	protected void clearViewer() {
		viewer.setContents(null);
	}

	/**
	 * Creates the GraphicalViewer on the specified <code>Composite</code>.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	protected void createGraphicalViewer(Composite parent) {
		GraphicalViewer viewer = new ScrollingGraphicalViewer();
		Control control = viewer.createControl(parent);
		control.setBackground(ColorConstants.white);

		editDomain = new EditDomain();
		viewer.setEditDomain(editDomain);
		editDomain.addViewer(viewer);

		viewer.setRootEditPart(new FreeformGraphicalRootEditPart() {
			@Override
			public DragTracker getDragTracker(Request req) {
				return new DirtyMarqueeTool();
			}
		});
		viewer.setEditPartFactory(getEditPartFactory());

		getSelectionSynchronizer().addViewer(viewer);
		getSite().setSelectionProvider(viewer);

		setGraphicalViewer(viewer);
	}

	@Override
	public void createPartControl(Composite parent) {
		createGraphicalViewer(parent);

		// add selection listener
		getSite().getPage().addPartListener(this);
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(this);
	}

	private String getClassName(Module module, int offset) {
		if (module == null) {
			return null;
		}

		ICompositeNode rootNode = NodeModelUtils.findActualNodeFor(module);
		ILeafNode leafNode = NodeModelUtils.findLeafNodeAtOffset(rootNode, offset);

		EObject eObject = NodeModelUtils.findActualSemanticObjectFor(leafNode);
		NamedEntity entity = EcoreUtil2.getContainerOfType(eObject, NamedEntity.class);
		if (entity != null) {
			return nameProvider.getFullyQualifiedName(entity).toString();
		}

		return null;
	}

	protected abstract EditPartFactory getEditPartFactory();

	protected String getEntityName() {
		return entityName;
	}

	protected SelectionSynchronizer getSelectionSynchronizer() {
		if (synchronizer == null)
			synchronizer = new SelectionSynchronizer();
		return synchronizer;
	}

	/**
	 * Installs cursor listener and selects current line.
	 * 
	 * @param part
	 */
	private void initCursorListener(IWorkbenchPart part) {
		Control control = (Control) part.getAdapter(Control.class);
		if (control instanceof StyledText) {
			StyledText text = (StyledText) control;
			text.addCaretListener(caretListener);

			selectLine(text, text.getCaretOffset());
		}
	}

	protected abstract boolean irFileLoaded(Entity entity);

	/**
	 * Loads the .ir file that corresponds to the given className
	 * 
	 * @param className
	 */
	private void loadIrFile(String className) {
		if (className != null) {
			IFile cfFile = EcoreHelper.getFile(resource);
			IProject project = cfFile.getProject();
			IFile irFile = CoreUtil.getIrFile(project, className);
			if (irFile.exists()) {
				// use a new resource set each time so we make sure the resource is reloaded
				// we can probably do something more clever, e.g. by leveraging resourceChanged
				// will do if necessary
				ResourceSet set = new ResourceSetImpl();
				EObject eObject = EcoreHelper.getEObject(set, irFile);
				if (eObject instanceof Entity) {
					Entity entity = (Entity) eObject;
					entityName = entity.getName();
					if (irFileLoaded(entity)) {
						updateViewer();
						return;
					}
				}
			}
		}

		// if .cf file valid and header or task with no FSM clear viewer
		reset();
		clearViewer();
	}

	@Override
	public void modelChanged(XtextResource resource) {
		this.resource = resource;

		Display display = PlatformUI.getWorkbench().getDisplay();
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				loadIrFile(entityName);
			}

		});
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof XtextEditor) {
			XtextEditor xtextEditor = (XtextEditor) part;
			IEditorInput input = xtextEditor.getEditorInput();
			if (input instanceof IFileEditorInput) {
				IFileEditorInput fileInput = (IFileEditorInput) input;
				IFile file = fileInput.getFile();
				if (FILE_EXT_CFLOW.equals(file.getFileExtension())) {
					// set xtextDocument field
					xtextDocument = xtextEditor.getDocument();

					// initialize resource field
					xtextDocument.readOnly(new IUnitOfWork.Void<XtextResource>() {
						@Override
						public void process(XtextResource state) throws Exception {
							AbstractGefView.this.resource = state;
						}
					});

					// add listener to track updates to model and to track caret
					xtextDocument.addModelListener(this);
					initCursorListener(part);
				}
			}
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
		Control control = (Control) part.getAdapter(Control.class);
		if (control instanceof StyledText) {
			StyledText text = (StyledText) control;
			text.removeCaretListener(caretListener);
		}

		if (xtextDocument != null) {
			xtextDocument.removeModelListener(this);
			xtextDocument = null;
		}
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}

	protected void reset() {
	}

	protected void selectLine(StyledText text, int offset) {
		if (resource != null) {
			Module module = (Module) resource.getContents().get(0);

			String className = getClassName(module, offset);
			if (className == null) {
				entityName = null;
				reset();
			} else if (!className.equals(entityName)) {
				// className is different from current actorName, must reload
				loadIrFile(className);
			}
		}
	}

	@Override
	public void setFocus() {
	}

	private void setGraphicalViewer(GraphicalViewer viewer) {
		this.viewer = viewer;
	}

	protected abstract void updateViewer();

}
