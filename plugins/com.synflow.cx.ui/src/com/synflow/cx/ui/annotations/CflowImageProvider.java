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
package com.synflow.cx.ui.annotations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * This class defines an image provider for the cycle indicator marker.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CflowImageProvider implements IAnnotationImageProvider {

	public static final String TYPE_ANNOT = "com.synflow.cx.ui.cycleIndicator";

	private Map<String, Image> images = new HashMap<>();

	@Override
	public Image getManagedImage(Annotation annotation) {
		IMarker marker = ((MarkerAnnotation) annotation).getMarker();

		Device device = Display.getCurrent();
		String value = marker.getAttribute("color", "ff00ff");
		Image image = images.get(value);
		if (image == null) {
			image = new Image(device, 16, 15);
			images.put(value, image);

			int red = Integer.parseInt(value.substring(0, 2), 16);
			int green = Integer.parseInt(value.substring(2, 4), 16);
			int blue = Integer.parseInt(value.substring(4, 6), 16);
			Color color = new Color(device, red, green, blue);

			GC gc = new GC(image);
			gc.setBackground(color);
			gc.setForeground(color);
			gc.fillRectangle(0, 0, 16, 15);
			gc.dispose();
			color.dispose();
		}

		return image;
	}

	@Override
	public String getImageDescriptorId(Annotation annotation) {
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor(String imageDescriptorId) {
		return null;
	}

}
