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
package com.synflow.cx.ui.contentassist;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.xtext.ui.editor.templates.ContextTypeIdHelper;
import org.eclipse.xtext.ui.editor.templates.DefaultTemplateProposalProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.synflow.cx.ui.internal.CxActivator;

/**
 * This class extends the default template proposal provider to provide a better image.
 * 
 * @author Matthieu Wipliez
 *
 */
@Singleton
public class CxTemplateProposalProvider extends DefaultTemplateProposalProvider {

	private Image image;

	@Inject
	public CxTemplateProposalProvider(TemplateStore templateStore, ContextTypeRegistry registry,
			ContextTypeIdHelper helper) {
		super(templateStore, registry, helper);
	}

	@Override
	public Image getImage(Template template) {
		if (image == null) {
			ImageDescriptor imageDescriptor = CxActivator.imageDescriptorFromPlugin(
					"com.synflow.cx.ui", "icons/template_obj.gif"); //$NON-NLS-1$
			image = imageDescriptor.createImage();
		}
		return image;
	}

}
