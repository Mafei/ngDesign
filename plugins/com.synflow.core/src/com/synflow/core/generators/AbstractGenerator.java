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
package com.synflow.core.generators;

import static com.synflow.core.IProperties.IMPL_BUILTIN;
import static com.synflow.core.IProperties.PROP_TYPE;
import static com.synflow.core.ISynflowConstants.FOLDER_TESTBENCH;
import static com.synflow.core.ISynflowConstants.SUFFIX_GEN;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synflow.core.ICodeGenerator;
import com.synflow.core.IFileWriter;
import com.synflow.core.SynflowCore;
import com.synflow.core.util.CoreUtil;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Instance;
import com.synflow.models.ir.util.IrUtil;

/**
 * This class defines an abstract generator that uses Xtend for templates.
 * 
 * @author Matthieu Wipliez
 * 
 */
public abstract class AbstractGenerator implements ICodeGenerator {

	protected IFileWriter writer;

	@Override
	public final String computePath(String name) {
		return computePath(name, getFileExtension());
	}

	@Override
	public String computePath(String fileName, String fileExt) {
		String name = getName().toLowerCase();
		return name + SUFFIX_GEN + '/' + fileName + '.' + fileExt;
	}

	@Override
	public String computePathTb(Entity entity) {
		String name = IrUtil.getFile(entity.getName()) + ".tb";
		return FOLDER_TESTBENCH + '/' + name + '.' + getFileExtension();
	}

	/**
	 * Copy the built-in entity and its dependencies to the target folder.
	 * 
	 * @param entity
	 *            an entity
	 */
	private void copyBuiltinEntity(Entity entity) {
		for (String name : CoreUtil.getFileList(entity)) {
			copyBuiltinFile(name);
		}
	}

	/**
	 * Copy the file that matches the given qualified name to the target folder.
	 * 
	 * @param name
	 *            qualified name of an entity
	 */
	private void copyBuiltinFile(String name) {
		// then copy this entity
		String genName = getName().toLowerCase();
		String fileName = IrUtil.getFile(name);
		String pathName = "/lib/" + genName + "/src/" + fileName + "." + getFileExtension();
		URI uri = URI.createPlatformPluginURI("/" + SynflowCore.PLUGIN_ID + pathName, false);

		try {
			InputStream is = new ExtensibleURIConverterImpl().createInputStream(uri, null);
			write(fileName, is);
			is.close();
		} catch (IOException e) {
			SynflowCore.log(e);
		}
	}

	/**
	 * Prints the given entity.
	 * 
	 * @param entity
	 *            an entity
	 */
	protected abstract void doPrint(Entity entity);

	/**
	 * Prints the testbench for the given entity.
	 * 
	 * @param entity
	 *            an entity
	 */
	protected abstract void doPrintTestbench(Entity entity);

	@Override
	public final IFileWriter getFileWriter() {
		return writer;
	}

	@Override
	public void initialize(IFileWriter writer) {
		this.writer = writer;
	}

	@Override
	public void print(Entity entity) {
		JsonObject implementation = CoreUtil.getImplementation(entity);
		if (implementation == null) {
			// default case: print entity
			if (entity instanceof DPN) {
				DPN dpn = (DPN) entity;
				for (Instance instance : dpn.getInstances()) {
					Entity ent = instance.getEntity();
					if (ent == null || ent.eIsProxy()) {
						// do not print this network if not all entities are resolved
						return;
					}
				}
			}
			doPrint(entity);
		} else {
			JsonElement type = implementation.get(PROP_TYPE);
			if (IMPL_BUILTIN.equals(type)) {
				// if type is 'builtin' copy entity and dependencies to target folder
				copyBuiltinEntity(entity);
			}
		}
	}

	@Override
	public void printTestbench(Entity entity) {
		JsonObject implementation = CoreUtil.getImplementation(entity);
		if (implementation == null) {
			// only print testbench for entities without 'implementation' property
			doPrintTestbench(entity);
		}
	}

	@Override
	public void remove(String name) {
		writer.remove(computePath(IrUtil.getFile(name)));
	}

	@Override
	public final void write(Entity entity, CharSequence contents) {
		if (contents == null) {
			return;
		}

		write(IrUtil.getFile(entity.getName()), contents);
	}

	protected final void write(String fileName, CharSequence contents) {
		String path = computePath(fileName);
		writer.write(path, contents);
	}

	protected final void write(String fileName, InputStream source) {
		String path = computePath(fileName);
		writer.write(path, source);
	}

}
