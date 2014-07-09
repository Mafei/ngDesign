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
package com.synflow.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class SynflowUi extends AbstractUIPlugin {

	public static final String COMMAND_TYPE_EXPORT_TYPE = "com.synflow.ui.exportType";

	public static final String IMG_LIC_ERR = "icons/license_err.png";

	public static final String IMG_LIC_OK = "icons/license_ok.png";

	// The shared instance
	private static SynflowUi plugin;

	// The plug-in WIZARD_ID
	public static final String PLUGIN_ID = "com.synflow.ui"; //$NON-NLS-1$

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static SynflowUi getDefault() {
		return plugin;
	}

	/**
	 * Creates and returns a new image descriptor for an image file located
	 * within this plug-in.
	 * 
	 * @param path
	 *            the relative path of the image file, relative to the root of
	 *            the plug-in; the path must be legal
	 * @return an image descriptor, or <code>null</code> if no image could be
	 *         found
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * The constructor
	 */
	public SynflowUi() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

}
