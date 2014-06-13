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
package com.synflow.core.internal;

import com.synflow.core.IFileWriter;

/**
 * This class defines an abstract implementation of a IFileWriter.
 * 
 * @author Matthieu Wipliez
 * 
 */
public abstract class AbstractFileWriter implements IFileWriter {

	protected String outputFolder;

	@Override
	public void setOutputFolder(String folder) {
		outputFolder = folder;
	}

}
