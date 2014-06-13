/*******************************************************************************
 * Copyright (c) 2012-2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.core;

import com.synflow.models.dpn.Entity;

/**
 * This interface defines a code generator. A code generator can be initialized, and defines a
 * doSwitch method that visits an object to generate code.
 * 
 * @author Matthieu Wipliez
 * 
 */
public interface ICodeGenerator {

	/**
	 * Returns <code>computePath(name, getFileExtension())</code>.
	 * 
	 * @param name
	 * @return
	 */
	String computePath(String name);

	/**
	 * Returns a path composed of this generator's name (possibly with a '-gen' suffix, depending on
	 * generators), the given file name and file extension.
	 * 
	 * @param fileName
	 * @param fileExt
	 * @return
	 */
	String computePath(String fileName, String fileExt);

	/**
	 * Returns <code>'testbench/' + file + '.' + fileExtension</code> where file is computed based
	 * on eObject's name.
	 * 
	 * @param entity
	 * @return
	 */
	String computePathTb(Entity entity);

	/**
	 * Returns the file extension of files this generator generates (e.g. "c").
	 * 
	 * @return the file extension
	 */
	String getFileExtension();

	IFileWriter getFileWriter();

	/**
	 * Returns the name of this generator.
	 * 
	 * @return the name of this generator
	 */
	String getName();

	/**
	 * Initializes this generator with the given template writer.
	 */
	void initialize(IFileWriter writer);

	/**
	 * Prints code for the given object, unless it has an 'implementation' property.
	 * 
	 * @param entity
	 *            entity
	 */
	void print(Entity entity);

	/**
	 * Prints a test bench for the given object.
	 * 
	 * @param entity
	 *            actor or network
	 */
	void printTestbench(Entity entity);

	void remove(String name);

	/**
	 * Transforms the given object.
	 * 
	 * @param entity
	 *            an object to transform
	 */
	void transform(Entity entity);

	/**
	 * This method writes the given contents to a file in &lt;name&gt;-gen constructed from the
	 * given object's name, and this generator's file extension.
	 * 
	 * @param entity
	 *            an actor/network/unit
	 * @param contents
	 *            contents (may be <code>null</code>, in which case nothing is written)
	 */
	void write(Entity entity, CharSequence contents);

}
