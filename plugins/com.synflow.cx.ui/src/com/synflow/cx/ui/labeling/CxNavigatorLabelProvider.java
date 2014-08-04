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

import org.eclipse.jdt.ui.ProblemsLabelDecorator;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

import com.google.inject.Inject;

/**
 * This class defines a label provider for the Cx navigator (Project Explorer). This delegates calls
 * to the {@link NavigatorDeclarativeLabelProvider}, and decorates images and text with JDT's
 * {@link ProblemsLabelDecorator}.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CxNavigatorLabelProvider implements ICommonLabelProvider {

	private ILabelDecorator decorator;

	private ILabelProvider delegateLabelProvider;

	@Inject
	public CxNavigatorLabelProvider(NavigatorDeclarativeLabelProvider provider) {
		delegateLabelProvider = provider;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		delegateLabelProvider.addListener(listener);
	}

	@Override
	public void dispose() {
		delegateLabelProvider.dispose();
	}

	@Override
	public String getDescription(Object element) {
		// no description
		return null;
	}

	@Override
	public Image getImage(Object element) {
		Image image = delegateLabelProvider.getImage(element);
		return decorator.decorateImage(image, element);
	}

	@Override
	public String getText(Object element) {
		String text = delegateLabelProvider.getText(element);
		return decorator.decorateText(text, element);
	}

	@Override
	public void init(ICommonContentExtensionSite aConfig) {
		decorator = new ProblemsLabelDecorator();
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return delegateLabelProvider.isLabelProperty(element, property);
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		delegateLabelProvider.removeListener(listener);
	}

	@Override
	public void restoreState(IMemento aMemento) {
	}

	@Override
	public void saveState(IMemento aMemento) {
	}

}
