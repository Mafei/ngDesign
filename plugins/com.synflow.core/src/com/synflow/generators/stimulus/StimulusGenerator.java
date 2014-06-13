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
package com.synflow.generators.stimulus;

import static com.synflow.core.IProperties.PROP_TEST;
import static com.synflow.core.ISynflowConstants.FOLDER_SIM;

import com.google.gson.JsonObject;
import com.synflow.generators.AbstractGenerator;
import com.synflow.models.dpn.Entity;
import com.synflow.models.ir.util.IrUtil;

/**
 * This class defines a generator of stimulus
 */
public class StimulusGenerator extends AbstractGenerator {

	@Override
	public String computePath(String fileName, String fileExt) {
		return FOLDER_SIM + '/' + fileName + '.' + fileExt;
	}

	@Override
	protected void doPrint(Entity entity) {
		JsonObject test = entity.getProperties().getAsJsonObject(PROP_TEST);

		// prints the stimulus/expected data sets for the given entity
		StimulusPrinter printer = new StimulusPrinter(entity.getInputs());
		write("stimSigNames", printer.getPortNames());
		write("stimSigValues", printer.getValues(test));

		printer = new StimulusPrinter(entity.getOutputs());
		write("traceSigNames", printer.getPortNames());
		write("traceSigValues", printer.getValues(test));
	}

	@Override
	protected void doPrintTestbench(Entity entity) {
	}

	@Override
	public String getFileExtension() {
		return "txt";
	}

	@Override
	public String getName() {
		return FOLDER_SIM;
	}

	@Override
	public void print(Entity entity) {
		doPrint(entity);
	}

	@Override
	public void remove(String name) {
		String simpleName = IrUtil.getSimpleName(name);
		writer.remove(computePath(simpleName));
	}

	@Override
	public void transform(Entity entity) {
	}

}