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
package com.synflow.cflow.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class defines a thread that copies an input stream to an output stream.
 * This is intended to be used when running commands in tests.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class StreamCopier extends Thread {

	private InputStream source;

	private OutputStream target;

	public StreamCopier(InputStream source, OutputStream target) {
		this.source = source;
		this.target = target;
	}

	@Override
	public void run() {
		byte[] b = new byte[4096];
		try {
			int n = source.read(b);
			while (n != -1) {
				target.write(b, 0, n);
				target.flush();
				n = source.read(b);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
