/*******************************************************************************
 * Copyright (c) 2013-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.generators.vhdl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.util.EcoreUtil;

import com.synflow.generators.Namer;
import com.synflow.models.dpn.Endpoint;
import com.synflow.models.ir.Block;
import com.synflow.models.ir.BlockBasic;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.InstReturn;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.Var;

/**
 * This class defines utility methods for HDL generation.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class HdlGeneratorUtil {

	private static String computeName(Map<String, Var> inverseMap, String name) {
		int i = 0;
		String candidate;
		do {
			i++;
			candidate = name + "_unique_" + i;
		} while (inverseMap.containsKey(candidate));

		return candidate;
	}

	/**
	 * computes the local variable map that associates each local variable of each action's body
	 * with a name. The implementation uses two maps rather than a BiMap because BiMap does not
	 * support multiple values for a given key.
	 */
	public static Collection<Var> computeVarMap(List<Procedure> procs, Map<Var, String> varMap) {
		Map<String, Var> inverseMap = new LinkedHashMap<>();
		for (Procedure proc : procs) {
			for (Var local : proc.getLocals()) {
				Type type = local.getType();
				String name = local.getName();

				Var existing = inverseMap.get(name);
				if (existing == null || EcoreUtil.equals(type, existing.getType())) {
					varMap.put(local, name);
					inverseMap.put(name, local);
				} else {
					String typedName = computeName(inverseMap, name);
					varMap.put(local, typedName);
					inverseMap.put(typedName, local);
				}
			}
		}

		return inverseMap.values();
	}

	/**
	 * Returns the expression that this scheduler procedure reduces to. The scheduler must be
	 * inlinable.
	 * 
	 * @param scheduler
	 *            a scheduler
	 * @return an expression
	 */
	public static Expression getExpression(Procedure scheduler) {
		BlockBasic basic = (BlockBasic) scheduler.getBlocks().get(0);
		InstReturn instReturn = (InstReturn) basic.getInstructions().get(0);
		return instReturn.getValue();
	}

	/**
	 * Returns the number of hexadecimal digits required for the given number. Equivalent to
	 * <code>(int) Math.ceil((float) n / 4.0f)</code>.
	 * 
	 * @param n
	 *            a (positive) number
	 * @return the number of hexadecimal digits required for <code>n</code>
	 */
	public static int getNumberOfHexadecimalDigits(int n) {
		return (n % 4 == 0) ? n / 4 : n / 4 + 1;
	}

	public static String getSignal(Namer namer, Endpoint endpoint) {
		if (endpoint.hasInstance()) {
			return endpoint.getInstance().getName() + "_" + namer.getName(endpoint.getPort());
		} else {
			return namer.getName(endpoint.getPort());
		}
	}

	/**
	 * Returns <code>true</code> if the scheduler procedure can be inlined.
	 * 
	 * @param scheduler
	 *            a scheduler procedure
	 */
	public static boolean isInlinable(Procedure scheduler) {
		if (scheduler.getLocals().isEmpty() && scheduler.getBlocks().size() == 1) {
			final Block block = scheduler.getBlocks().get(0);
			BlockBasic basic = (BlockBasic) block;
			return basic.getInstructions().size() == 1;
		}

		return false;
	}

	/**
	 * Returns <code>true</code> if a signal/wire is needed, either:
	 * <ul>
	 * <li>there is more than one connection (broadcast), or</li>
	 * <li>there is one outgoing connection to an instance.</li>
	 * </ul>
	 * In any other cases this method returns false:
	 * <ul>
	 * <li>when the list of endpoints is empty,</li>
	 * <li>when there is one endpoint that is an output port.</li>
	 * </ul>
	 */
	public static boolean isSignalNeeded(List<Endpoint> endpoints) {
		if (endpoints.isEmpty()) {
			return false;
		} else if (endpoints.size() > 1) {
			return true;
		} else {
			return endpoints.get(0).hasInstance();
		}
	}

}
