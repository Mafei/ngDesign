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
package com.synflow.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.synflow.core.internal.EclipseFileWriter;
import com.synflow.core.internal.NativeFileWriter;
import com.synflow.core.internal.exporters.DiamondExporter;
import com.synflow.core.internal.exporters.IseExporter;
import com.synflow.core.internal.exporters.QuartusExporter;
import com.synflow.core.internal.exporters.SimFilesExporter;

/**
 * This class defines the Synflow core plug-in, as well as various constants.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class SynflowModule extends AbstractModule {

	@Override
	protected void configure() {
		// file writers
		bind(IFileWriter.class).annotatedWith(Names.named("File")).to(NativeFileWriter.class);
		bind(IFileWriter.class).annotatedWith(Names.named("Eclipse")).to(EclipseFileWriter.class);

		// exporters
		bind(IExporter.class).annotatedWith(Names.named("Diamond")).to(DiamondExporter.class);
		bind(IExporter.class).annotatedWith(Names.named("Ise")).to(IseExporter.class);
		bind(IExporter.class).annotatedWith(Names.named("Quartus")).to(QuartusExporter.class);
		bind(IExporter.class).annotatedWith(Names.named("Vsim")).to(SimFilesExporter.class);

		// configure additional modules with this binder
		if (Platform.isRunning()) {
			IExtensionRegistry reg = Platform.getExtensionRegistry();
			IConfigurationElement[] elements = reg
					.getConfigurationElementsFor("com.synflow.core.injection");
			for (IConfigurationElement element : elements) {
				if ("module".equals(element.getName())) {
					try {
						Object obj = element.createExecutableExtension("class");
						((Module) obj).configure(binder());
					} catch (CoreException e) {
						SynflowCore.log(e);
					}
				}
			}
		}
	}

}
