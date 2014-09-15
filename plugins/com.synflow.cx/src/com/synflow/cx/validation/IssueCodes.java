/*******************************************************************************
 * Copyright (c) 2012-2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.validation;

/**
 * This interface defines all codes for issues.
 * 
 * @author Matthieu Wipliez
 * 
 */
public interface IssueCodes {

	String ERR_ARRAY_MULTI_NON_POWER_OF_TWO = "errArrayMultiDimNonPowerOfTwo";

	/**
	 * when available is used outside of if/for/while
	 */
	String ERR_AVAILABLE = "errAvailable";

	String ERR_CANNOT_ASSIGN_CONST = "errCannotAssignConstant";

	String ERR_CMP_ALWAYS_FALSE = "errCmpAlwaysFalse";

	String ERR_CMP_ALWAYS_TRUE = "errCmpAlwaysTrue";

	/**
	 * / and % operators are only allowed with constants that are power of two.
	 */
	String ERR_DIV_MOD_NOT_CONST_POW_Of_TWO = "errDivModByNonPowerOfTwoConstant";

	String ERR_DUPLICATE_DECLARATIONS = "errDuplicateDeclarations";

	String ERR_ENTRY_FUNCTION_BAD_TYPE = "errEntryFunctionBadType";

	String ERR_EXPECTED_CONST = "errExpectedConstant";

	String ERR_FUNCTION_CALL = "errFunctionCall";

	String ERR_ILLEGAL_FENCE = "errIllegalFence";

	String ERR_LOCAL_NOT_INITIALIZED = "errLocalNotInitialized";

	/**
	 * multiple reads are forbidden in expressions
	 */
	String ERR_MULTIPLE_READS = "errMultipleReads";

	String ERR_NO_SIDE_EFFECTS = "errNoSideEffects";

	String ERR_SIDE_EFFECTS_FUNCTION = "errNonVoidHasSideEffects";

	String ERR_TYPE_MISMATCH = "errTypeMismatch";

	String ERR_TYPE_ONE_BIT = "errTypeOneBit";

	String ERR_UNRESOLVED_FUNCTION = "errUnresolvedFunction";

	String ERR_VAR_DECL = "errVariableDeclaration";

	String WARN_SHOULD_START_LOWER = "warnShouldStartWithLowercase";

	String WARN_SHOULD_START_UPPER = "warnShouldStartWithUppercase";

}
