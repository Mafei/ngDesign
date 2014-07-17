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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.synflow.core.ICodeGenerator;
import com.synflow.core.IFileWriter;
import com.synflow.core.SynflowCore;
import com.synflow.models.dpn.Entity;

/**
 * This class defines a handler for commands that generate a testbench/unit test for a .cf file.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class GenerateTestHandler extends CommonHandler {

	@Inject
	@Named("Stimulus")
	private ICodeGenerator stimulusGenerator;

	@Inject
	@Named("Eclipse")
	private IFileWriter writer;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Entity entity = getEntity(event.getApplicationContext());
		if (entity == null) {
			return null;
		}

		final IProject project = entity.getFile().getProject();

		// creates writer
		writer.setOutputFolder(project.getName());

		// print stimulus
		stimulusGenerator.initialize(writer);
		stimulusGenerator.print(entity);

		// either Verilog or VHDL
		ICodeGenerator generator = SynflowCore.getGenerator(project);
		if (generator != null) {
			generateTest(generator, entity);
		}

		return null;
	}

	private void generateTest(ICodeGenerator generator, Entity entity) {
		// initialize generator with writer and print testbench
		generator.initialize(writer);
		generator.printTestbench(entity);
	}

}
