/*******************************************************************************
 * Copyright (c) 2012-2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *    Nicolas Siret
 *******************************************************************************/
package com.synflow.cx.tests.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.xtext.junit4.InjectWith;
import org.junit.BeforeClass;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.synflow.core.ICodeGenerator;
import com.synflow.core.IFileWriter;
import com.synflow.cx.CxInjectorProvider;
import com.synflow.cx.tests.AbstractPassTests;
import com.synflow.cx.tests.StreamCopier;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Instance;

/**
 * This class defines C~ tests that are expected to succeed.
 * 
 * @author Matthieu Wipliez
 * @author Nicolas Siret
 * 
 */
@InjectWith(CxInjectorProvider.class)
public abstract class CodegenPassTests extends AbstractPassTests {

	@BeforeClass
	public static void cleanOutput() {
		boolean cleanOutput = false;

		String tmpDir = System.getProperty("java.io.tmpdir");
		String path = Paths.get(tmpDir, OUTPUT_NAME).toString();
		try {
			if (cleanOutput) {
				FileUtils.deleteDirectory(new File(path));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Inject
	@Named("Stimulus")
	private ICodeGenerator stimulusGenerator;

	@Inject
	@Named("File")
	private IFileWriter writer;

	@Override
	protected final void checkEntity(Entity entity, boolean expected) throws Exception {
		generateCode(entity);
		compileAndSimulate(entity);
	}

	/**
	 * Compiles and simulates the given object.
	 * 
	 * @param entity
	 *            an entity
	 * @throws Exception
	 */
	protected abstract void compileAndSimulate(Entity entity) throws Exception;

	/**
	 * Executes a command in the path given by the location, and returns its exit code.
	 * 
	 * @param directory
	 *            a directory
	 * @param command
	 *            a list of String
	 * @return the return code of the process
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected final Process executeCommand(File directory, List<String> command)
			throws IOException, InterruptedException {
		return executeCommand(directory, command.toArray(new String[0]));
	}

	/**
	 * Executes a command in the path given by the location, and returns its exit code.
	 * 
	 * @param directory
	 *            a directory
	 * @param command
	 *            a list of String
	 * @return the return code of the process
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected final Process executeCommand(File directory, String... command) throws IOException,
			InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(command).directory(directory);
		Process process = pb.start();
		new StreamCopier(process.getErrorStream(), System.err).start();
		return process;
	}

	/**
	 * Generates code, compiles it, and simulates.
	 * 
	 * @param object
	 *            a Unit, an Actor, or a Network
	 * @throws Exception
	 */
	private void generateCode(Entity entity) throws Exception {
		if (entity instanceof DPN) {
			DPN network = (DPN) entity;
			for (Instance instance : network.getInstances()) {
				generateCode(instance.getEntity());
			}
		}

		ICodeGenerator generator = getCodeGenerator();
		generator.initialize(writer);
		generator.transform(entity);
		generator.print(entity);
		generator.printTestbench(entity);

		// generate stimulus and expected(.txt)
		stimulusGenerator.initialize(writer);
		stimulusGenerator.print(entity);
	}

	/**
	 * Returns the code generator that this class should use
	 * 
	 * @return an instance of a ICodeGenerator
	 */
	protected abstract ICodeGenerator getCodeGenerator();

	@Override
	public void setUp() throws Exception {
		super.setUp();

		// set output folder
		writer.setOutputFolder(outputPath);
	}

}
