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
package com.synflow.ngDesign.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.xtext.naming.IQualifiedNameProvider;

import com.synflow.cflow.cflow.GenericEntity;
import com.synflow.cflow.ui.CflowExecutableExtensionFactory;
import com.synflow.core.SynflowCore;
import com.synflow.core.util.CoreUtil;
import com.synflow.models.dpn.Entity;
import com.synflow.models.util.EcoreHelper;

/**
 * This class defines a common handler.
 * 
 * @author Matthieu Wipliez
 * 
 */
public abstract class CommonHandler extends AbstractHandler {

	/**
	 * Returns the file selected by the handler.
	 * 
	 * @param context
	 *            context
	 * @return a file, or <code>null</code>
	 */
	protected IFile getFile(Object context) {
		if (context instanceof IEvaluationContext) {
			IEvaluationContext appContext = (IEvaluationContext) context;

			Object selection = appContext.getVariable("selection");
			Object elt = ((IStructuredSelection) selection).getFirstElement();
			IFile file = (IFile) elt;
			return file;
		}

		return null;
	}

	/**
	 * Returns the model file selected by the handler, or <code>null</code> if no file is selected,
	 * or if the file does not exist.
	 * 
	 * @param context
	 *            context
	 * @return a file, or <code>null</code>
	 */
	protected Entity getEntity(Object context) {
		if (context instanceof IEvaluationContext) {
			IEvaluationContext appContext = (IEvaluationContext) context;

			Object selection = appContext.getVariable("selection");
			Object elt = ((IStructuredSelection) selection).getFirstElement();
			GenericEntity genericEntity = (GenericEntity) elt;
			IFile file = EcoreHelper.getFile(genericEntity);

			CflowExecutableExtensionFactory factory = new CflowExecutableExtensionFactory();
			try {
				factory.setInitializationData(null, null, IQualifiedNameProvider.class.getName());
				Object obj = factory.create();
				IQualifiedNameProvider provider = (IQualifiedNameProvider) obj;
				String name = provider.getFullyQualifiedName(genericEntity).toString();
				IFile irFile = CoreUtil.getIrFile(file.getProject(), name);

				ResourceSet set = genericEntity.eResource().getResourceSet();
				Entity entity = EcoreHelper.getEObject(set, irFile);
				return entity;
			} catch (CoreException e) {
				SynflowCore.log(e);
			}
		}

		return null;
	}

}
