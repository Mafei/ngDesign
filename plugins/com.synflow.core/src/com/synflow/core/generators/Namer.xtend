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
package com.synflow.core.generators

import com.synflow.models.dpn.Port
import com.synflow.models.ir.Var
import java.util.Set
import java.util.regex.Pattern
import com.synflow.models.dpn.Entity

/**
 * This class defines a Namer, that knows how to print ports and variables, and
 * escape them if necessary.
 * 
 * @author Matthieu Wipliez
 */
class Namer {

	val String first

	val String last

	val Pattern pattern

	val Set<String> reserved

	/**
	 * Creates a new namer.
	 * 
	 * @param reserved
	 *            set of reserved identifiers
	 * @param first
	 *            first character of the escape sequence
	 * @param last
	 *            first character of the escape sequence
	 */
	new(Set<String> reserved, String first, String last) {
		this.reserved = reserved
		this.pattern = null
		this.first = first
		this.last = last
	}

	/**
	 * Creates a new namer.
	 * 
	 * @param reserved
	 *            set of reserved identifiers
	 * @param pattern
	 *            pattern that identifies reserved character sequences (may be
	 *            <code>null</code>)
	 * @param first
	 *            first character of the escape sequence
	 * @param last
	 *            first character of the escape sequence
	 */
	new(Set<String> reserved, Pattern pattern, String first, String last) {
		this.reserved = reserved
		this.pattern = pattern
		this.first = first
		this.last = last
	}

	def private String escape(String name) {
		'''«first»«name»«last»'''
	}

	def getName(Entity entity) {
		val name = entity.simpleName
		if (needsEscaping(name)) {
			name.escape
		} else {
			name
		}
	}

	def getName(Port port) {
		val name = port.name
		if (needsEscaping(name)) {
			name.escape
		} else {
			name
		}
	}

	def getName(Var variable) {
		val name = variable.name
		if (needsEscaping(name)) {
			name.escape
		} else {
			name
		}
	}

	/**
	 * Returns <code>true</code> if the given name needs escaping.
	 * 
	 * @param name
	 *            name of a variable, parameter, port, procedure...
	 * @return <code>true</code> if the given name needs escaping
	 */
	def private needsEscaping(String name) {
		reserved.contains(name.toLowerCase()) || (pattern != null && pattern.matcher(name).find())
	}

}
