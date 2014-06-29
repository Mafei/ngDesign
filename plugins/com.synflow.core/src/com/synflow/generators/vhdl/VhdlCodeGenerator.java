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
package com.synflow.generators.vhdl;

import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;
import com.synflow.generators.AbstractGenerator;
import com.synflow.generators.CoreGeneratorsModule;
import com.synflow.generators.Namer;
import com.synflow.models.dpn.Entity;

/**
 * This class implements a generator for VHDL.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class VhdlCodeGenerator extends AbstractGenerator {

	/**
	 * In VHDL, a valid identifier must not begin or end with an underscore, and must not contain
	 * two following underscores. This regular expression matches any such illegal identifier.
	 */
	private static Pattern RE_ILLEGAL_ID = Pattern.compile("(^_.*)|(.*_$)|(.*__.*)");

	private static Set<String> RESERVED = ImmutableSet.of("abs", "access", "after", "alias", "all",
			"and", "architecture", "array", "assert", "attribute", "begin", "block", "body",
			"buffer", "bus", "case", "component", "configuration", "constant", "disconnect",
			"downto", "else", "elsif", "end", "entity", "exit", "file", "for", "function",
			"generate", "generic", "group", "guarded", "if", "impure", "in", "inertial", "inout",
			"is", "label", "library", "linkage", "literal", "loop", "map", "mod", "nand", "new",
			"next", "nor", "not", "null", "of", "on", "open", "or", "others", "out", "package",
			"port", "postponed", "procedure", "process", "pure", "range", "record", "register",
			"reject", "rem", "report", "resize", "return", "rol", "ror", "select", "severity",
			"signal", "shared", "sla", "sll", "sra", "srl", "subtype", "then", "to", "transport",
			"type", "unaffected", "units", "until", "use", "variable", "wait", "when", "while",
			"with", "xnor", "xor");

	private Namer namer;

	public VhdlCodeGenerator() {
		namer = new Namer(RESERVED, RE_ILLEGAL_ID, "\\", "\\");
	}

	@Override
	protected void doPrint(Entity entity) {
		CharSequence contents = new VhdlDfPrinter(namer).doSwitch(entity);
		write(entity, contents);
	}

	@Override
	protected void doPrintTestbench(Entity entity) {
		CharSequence contents = new VhdlTestbenchPrinter(namer).doSwitch(entity);
		if (contents == null) {
			return;
		}

		writer.write(computePathTb(entity), contents);
	}

	@Override
	public String getFileExtension() {
		return "vhd";
	}

	@Override
	public String getName() {
		return CoreGeneratorsModule.VHDL;
	}

	@Override
	public void transform(Entity entity) {
		new VhdlTransformer().doSwitch(entity);
	}

}
