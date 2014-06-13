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

import java.io.InputStream;

/**
 * This interface defines a file writer that is independent of the underlying
 * platform.
 * 
 * @author Matthieu Wipliez
 * 
 */
public interface IFileWriter {

	/**
	 * Returns <code>true</code> if a file with the given name exists.
	 * 
	 * @param fileName
	 *            name of destination file
	 */
	boolean exists(String fileName);

	/**
	 * Removes the file with the given name.
	 * 
	 * @param fileName
	 *            name of destination file
	 */
	void remove(String fileName);

	/**
	 * Sets the output folder to which file name are relative.
	 * 
	 * @param folder
	 *            an output folder
	 */
	void setOutputFolder(String folder);

	/**
	 * Writes the given contents to the file with the given name.
	 * 
	 * @param fileName
	 *            name of destination file
	 * @param sequence
	 *            a sequence of characters
	 */
	void write(String fileName, CharSequence sequence);

	/**
	 * Writes the given contents to the file with the given name.
	 * 
	 * @param fileName
	 *            name of destination file
	 * @param source
	 *            an input stream
	 */
	void write(String fileName, InputStream source);

}
