/*******************************************************************************
 * Copyright (c) 2012-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez
 *    Nicolas Siret
 *******************************************************************************/
package com.synflow.cx.tests.codegen;

import static com.synflow.core.ISynflowConstants.FOLDER_SIM;
import static com.synflow.core.ISynflowConstants.FOLDER_TESTBENCH;
import static com.synflow.core.ISynflowConstants.SUFFIX_GEN;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Paths;

import org.eclipse.xtext.junit4.InjectWith;
import org.junit.Assert;

import com.google.gson.JsonObject;
import com.synflow.core.IExportConfiguration;
import com.synflow.core.IExportConfiguration.Target;
import com.synflow.core.util.CoreUtil;
import com.synflow.cx.CxInjectorProvider;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Instance;
import com.synflow.models.ir.util.IrUtil;

/**
 * This class defines Cx tests that are expected to succeed.
 * 
 * @author Matthieu Wipliez
 * @author Nicolas Siret
 * 
 */
@InjectWith(CxInjectorProvider.class)
public abstract class HdlPassTests extends CodegenPassTests {

	/**
	 * Compiles the given object. If the object is a network, compiles all its children recursively.
	 * 
	 * @param object
	 *            an actor or a network
	 * @throws Exception
	 */
	protected final void compile(Entity entity) throws Exception {
		if (entity instanceof DPN) {
			DPN network = (DPN) entity;
			for (Instance instance : network.getInstances()) {
				compile(instance.getEntity());
			}
		}

		Iterable<String> names = CoreUtil.getFileList(entity);
		boolean external = CoreUtil.isExternal(entity);
		for (String name : names) {
			compile(name, external);
		}
	}

	/**
	 * Compiles the file with the given name.
	 * 
	 * @param name
	 *            name
	 * @param external
	 *            flag if implementation is external
	 * @throws Exception
	 */
	private void compile(String name, boolean external) throws Exception {
		File sim = new File(outputPath, FOLDER_SIM);

		String path;
		if (external) {
			path = Paths.get(name).toAbsolutePath().toString();
		} else {
			final String genName = getCodeGenerator().getName().toLowerCase();
			File target = new File(outputPath, genName + SUFFIX_GEN);
			target.mkdirs();

			final String fileExt = getCodeGenerator().getFileExtension();
			path = new File(target, IrUtil.getFile(name) + "." + fileExt).getPath();
		}

		// compiles the file
		int compil = runCompileCommand(sim, path);
		Assert.assertEquals("expected code generation to be correct for " + path, 0, compil);
	}

	/**
	 * Compiles the testbench of the given entity (unless the entity has an 'implementation'
	 * property)
	 * 
	 * @param entity
	 *            an entity
	 */
	protected void compileTb(Entity entity) throws Exception {
		JsonObject implementation = CoreUtil.getImplementation(entity);
		if (implementation != null) {
			return;
		}

		File sim = new File(outputPath, FOLDER_SIM);
		File tb = new File(outputPath, FOLDER_TESTBENCH);

		String name = entity.getName();
		final String fileExt = getCodeGenerator().getFileExtension();
		String file = new File(tb, IrUtil.getFile(name) + ".tb." + fileExt).getPath();
		int compil = runCompileCommand(sim, file);
		Assert.assertEquals("expected code generation to be correct for " + name + "testbench", 0,
				compil);
	}

	protected abstract IExportConfiguration getConfiguration();

	protected abstract String getLibraryPath();

	protected abstract int runCompileCommand(File sim, String string) throws Exception;

	/**
	 * Runs vsim in the given folder with the given file name.
	 * 
	 * @param directory
	 * @param name
	 * @return
	 * @throws Exception
	 */
	private int runVsim(File directory, String name) throws Exception {
		Process process = executeCommand(directory, "vsim", "-novopt", "work." + name);

		// Process process = executeCommand(directory, "vsim", "-c", "-novopt",
		// "-lib", "work", name);

		try {
			OutputStream os = process.getOutputStream();
			os.write("run 10 us\nquit -sim\nquit\n".getBytes());
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// read stdout and set status to -1 if "Error" is encountered
		// copy output to System.out
		int status = 0;
		InputStream source = process.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(source));
		try {
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				if (line.contains("Error") || line.contains("Fatal") || line.contains("Failure")
						|| line.contains("Assertion failed")) {
					status = -1;
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		process.waitFor();
		return status;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();

		File sim = new File(outputPath, FOLDER_SIM);
		if (sim.exists()) {
			return;
		}

		// create work library
		sim.mkdirs();
		executeCommand(sim, "vlib", "work").waitFor();

		// compile libraries
		String path = getLibraryPath();
		String pathLib = new File(path).getCanonicalPath();
		String fileExt = getCodeGenerator().getFileExtension();
		for (String lib : getConfiguration().getRootDependencies(Target.SIMULATION)) {
			String file = IrUtil.getFile(lib);
			int simul = runCompileCommand(sim, pathLib + "/" + file + "." + fileExt);
			Assert.assertEquals("expected code generation to be correct for libraries", 0, simul);
		}
	}

	/**
	 * Simulates the given entity (unless the entity has an 'implementation' property)<
	 * 
	 * @param entity
	 *            an entity
	 * @throws Exception
	 */
	protected void simulate(Entity entity) throws Exception {
		JsonObject implementation = CoreUtil.getImplementation(entity);
		if (implementation != null) {
			return;
		}

		String name = entity.getSimpleName();
		File sim = new File(outputPath, FOLDER_SIM);
		int simu = runVsim(sim, name + "_tb");
		Assert.assertEquals("expected simulation to be correct for " + name, 0, simu);
	}

}
