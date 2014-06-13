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
package com.synflow.core;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.osgi.framework.Bundle;

/**
 * This class defines a factory that injects objects.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class InjectableExtensionFactory implements IExecutableExtension,
		IExecutableExtensionFactory {

	private String clazzName;

	private IConfigurationElement config;

	@Override
	public Object create() throws CoreException {
		Object result = null;

		try {
			Class<?> clazz = getBundle().loadClass(clazzName);
			result = SynflowCore.getDefault().getInstance(clazz);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (result instanceof IExecutableExtension) {
			((IExecutableExtension) result).setInitializationData(config, null,
					null);
		}
		return result;
	}

	/**
	 * Returns the bundle to use to load classes.
	 * 
	 * @return the bundle to use to load classes
	 */
	protected Bundle getBundle() {
		return SynflowCore.getBundle();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		if (data instanceof String) {
			clazzName = (String) data;
		} else if (data instanceof Map<?, ?>) {
			clazzName = ((Map<String, String>) data).get("className");
		}
		this.config = config;
	}

}
