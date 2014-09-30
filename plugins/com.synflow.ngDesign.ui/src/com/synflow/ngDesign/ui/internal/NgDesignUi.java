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
package com.synflow.ngDesign.ui.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class NgDesignUi extends AbstractUIPlugin {

	public static final String COMMAND_TYPE_EXPORT_TYPE = "com.synflow.ngDesign.ui.exportType";

	// The shared instance
	private static NgDesignUi plugin;

	// The plug-in WIZARD_ID
	public static final String PLUGIN_ID = "com.synflow.ngDesign.ui"; //$NON-NLS-1$

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static NgDesignUi getDefault() {
		return plugin;
	}

	/**
	 * Creates and returns a new image descriptor for an image file located within this plug-in.
	 * 
	 * @param path
	 *            the relative path of the image file, relative to the root of the plug-in; the path
	 *            must be legal
	 * @return an image descriptor, or <code>null</code> if no image could be found
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * The constructor
	 */
	public NgDesignUi() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// set default theme for Windows 8 (otherwise the product looks ugly)
		ServiceReference<IThemeManager> reference;
		reference = context.getServiceReference(IThemeManager.class);
		try {
			IThemeManager manager = context.getService(reference);
			Display display = PlatformUI.getWorkbench().getDisplay();
			IThemeEngine engine = manager.getEngineForDisplay(display);
			ITheme themeActive = engine.getActiveTheme();
			if (themeActive == null) {
				if (Platform.OS_WIN32.equals(Platform.getOS())) {
					final String versionWin7 = "6.1";
					String osVersion = System.getProperty("os.version");
					if (osVersion != null && osVersion.compareTo(versionWin7) > 0) {
						// if OS is more recent than Windows 7, make theme API use Windows 7 theme
						System.setProperty("os.version", versionWin7);
					}

					// set default theme
					engine.setTheme("org.eclipse.e4.ui.css.theme.e4_default", true);
				}
			}
		} finally {
			context.ungetService(reference);
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

}
