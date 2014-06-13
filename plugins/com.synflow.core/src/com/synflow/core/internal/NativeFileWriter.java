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
package com.synflow.core.internal;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.synflow.core.IFileWriter;

/**
 * This class defines an implementation of a IFileWriter based on the native Java file classes. The
 * name of a file must be relative to the output directory.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class NativeFileWriter extends AbstractFileWriter implements IFileWriter {

	@Override
	public boolean exists(String fileName) {
		Path path = Paths.get(outputFolder, fileName);
		return Files.exists(path);
	}

	@Override
	public void remove(String fileName) {
		Path path = Paths.get(outputFolder, fileName);
		if (Files.exists(path)) {
			try {
				Files.delete(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void write(String fileName, CharSequence sequence) {
		Path path = Paths.get(outputFolder, fileName);
		try {
			Files.createDirectories(path.getParent());
			Files.write(path, sequence.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void write(String fileName, InputStream source) {
		Path path = Paths.get(outputFolder, fileName);
		try {
			Files.createDirectories(path.getParent());
			Files.copy(source, path, REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
