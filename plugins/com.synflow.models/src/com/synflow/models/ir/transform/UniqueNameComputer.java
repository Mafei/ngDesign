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
package com.synflow.models.ir.transform;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.synflow.models.ir.Var;

/**
 * This class defines a name computer that generates unique names.
 * 
 * @author Matthieu Wipliez
 *
 */
public class UniqueNameComputer {

	private Set<String> names;

	public UniqueNameComputer(List<Var> variables) {
		names = new HashSet<>();
		for (Var variable : variables) {
			names.add(variable.getName());
		}
	}

	/**
	 * Returns a unique name based on the given name.
	 * 
	 * @param name
	 *            name
	 * @return a unique name based on the given name
	 */
	public String getUniqueName(String name) {
		String uniqueName = name;
		int i = 1;
		while (names.contains(uniqueName)) {
			uniqueName = name + "_" + i;
			i++;
		}
		names.add(uniqueName);
		return uniqueName;
	}

}
