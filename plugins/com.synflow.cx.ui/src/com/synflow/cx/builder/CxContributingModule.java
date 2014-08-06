/*******************************************************************************
 * Copyright (c) 2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.builder;

import org.eclipse.xtext.builder.impl.IToBeBuiltComputerContribution;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * This class defines a shared-state contributing module referenced in plugin.xml that adds an
 * {@link IToBeBuiltComputerContribution} implemented by {@link CxToBeBuiltComputer}.
 * 
 * @author Matthieu Wipliez
 *
 */
@SuppressWarnings("restriction")
public class CxContributingModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(IToBeBuiltComputerContribution.class).to(CxToBeBuiltComputer.class);
	}

}
