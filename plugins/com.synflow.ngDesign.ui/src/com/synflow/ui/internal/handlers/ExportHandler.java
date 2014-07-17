/*******************************************************************************
 * Copyright (c) 2012 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *    Nicolas Siret - added simulator support
 *******************************************************************************/
package com.synflow.ui.internal.handlers;

import static com.synflow.ui.internal.NgDesignUi.COMMAND_TYPE_EXPORT_TYPE;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.google.inject.Inject;
import com.synflow.core.IExporter;
import com.synflow.core.SynflowCore;
import com.synflow.models.dpn.Entity;

/**
 * This class defines a handler for commands that export to third-party tools (Modelsim, Quartus,
 * Diamond, ISE).
 * 
 * @author Matthieu Wipliez
 * @author Nicolas Siret
 * 
 */
public class ExportHandler extends CommonHandler {

	@Inject
	private GenerateTestHandler generateTest;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Entity entity = getEntity(event.getApplicationContext());
		if (entity != null) {
			String type = event.getParameter(COMMAND_TYPE_EXPORT_TYPE);
			IExporter exporter = SynflowCore.getDefault().getInstance(IExporter.class, type);
			exporter.export(entity);

			if ("Vsim".equals(type)) {
				// also generate test for the entity
				generateTest.execute(event);
			}
		}

		return null;
	}

}
