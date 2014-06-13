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

import static com.synflow.cflow.validation.IssueCodes.ERR_CANNOT_ASSIGN_CONST;
import static com.synflow.cflow.validation.IssueCodes.ERR_CMP_ALWAYS_FALSE;
import static com.synflow.cflow.validation.IssueCodes.ERR_CMP_ALWAYS_TRUE;
import static com.synflow.cflow.validation.IssueCodes.ERR_DIV_MOD_NOT_CONST_POW_Of_TWO;
import static com.synflow.cflow.validation.IssueCodes.ERR_EXPECTED_CONST;
import static com.synflow.cflow.validation.IssueCodes.ERR_ILLEGAL_FENCE;
import static com.synflow.cflow.validation.IssueCodes.ERR_LOCAL_NOT_INITIALIZED;
import static com.synflow.cflow.validation.IssueCodes.ERR_MAIN_FUNCTION_BAD_TYPE;
import static com.synflow.cflow.validation.IssueCodes.ERR_MISSING_MAIN_FUNCTION;
import static com.synflow.cflow.validation.IssueCodes.ERR_MULTIPLE_READS;
import static com.synflow.cflow.validation.IssueCodes.ERR_SIDE_EFFECTS_FUNCTION;
import static com.synflow.cflow.validation.IssueCodes.ERR_TYPE_MISMATCH;
import static com.synflow.cflow.validation.IssueCodes.ERR_VAR_DECL;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.validation.AssertableDiagnostics.DiagnosticPredicate;
import org.eclipse.xtext.validation.AbstractValidationDiagnostic;
import org.junit.Test;

import com.synflow.cflow.CflowInjectorProvider;
import com.synflow.cflow.cflow.Module;
import com.synflow.cflow.validation.IssueCodes;

/**
 * This class defines C~ tests that are expected to fail.
 * 
 * @author Matthieu Wipliez
 * 
 */
@InjectWith(CflowInjectorProvider.class)
public class XFailTests extends AbstractCflowTest {

	/**
	 * Asserts that the validator produces at least one predicate that matches the given error code
	 * when validating the given object.
	 * 
	 * @param object
	 *            an object
	 * @param code
	 *            expected error code
	 */
	protected final void assertError(Module object, final String code) {
		DiagnosticPredicate predicate = new DiagnosticPredicate() {

			@Override
			public boolean apply(Diagnostic d) {
				if (d instanceof AbstractValidationDiagnostic) {
					AbstractValidationDiagnostic diag = (AbstractValidationDiagnostic) d;
					return code.equals(diag.getIssueCode());
				}
				return false;
			}

		};
		tester.validate(object).assertAny(predicate);
	}

	/**
	 * Asserts that the validator produces all given error codes when validating the given object.
	 * 
	 * @param object
	 *            an object
	 * @param code
	 *            expected error code
	 */
	protected final void assertErrors(Module object, String... codes) {
		for (String code : codes) {
			assertError(object, code);
		}
	}

	/**
	 * Asserts that the resource from which the given object was loaded has parse errors.
	 * 
	 * @param object
	 *            an object (top-level node of the AST)
	 */
	protected final void assertHasParseErrors(Module object) {
		assertNotNull(object);
		assertFalse(object.eResource().getErrors().isEmpty());
	}

	@Test
	public void checkAvailable1() throws Exception {
		assertError(getModule("com/synflow/test/xfail/Available1.cf"), IssueCodes.ERR_AVAILABLE);
	}

	@Test
	public void checkAvailable2() throws Exception {
		assertError(getModule("com/synflow/test/xfail/Available2.cf"), IssueCodes.ERR_AVAILABLE);
	}

	@Test
	public void checkAvailable3() throws Exception {
		assertError(getModule("com/synflow/test/xfail/Available3.cf"), IssueCodes.ERR_AVAILABLE);
	}

	@Test
	public void checkAvailable4() throws Exception {
		assertError(getModule("com/synflow/test/xfail/Available4.cf"), IssueCodes.ERR_AVAILABLE);
	}

	@Test
	public void checkCompareAlwaysFalse1() throws Exception {
		assertError(getModule("com/synflow/test/xfail/CompareAlwaysFalse1.cf"),
				ERR_CMP_ALWAYS_FALSE);
	}

	@Test
	public void checkCompareAlwaysFalse2() throws Exception {
		assertError(getModule("com/synflow/test/xfail/CompareAlwaysFalse2.cf"),
				ERR_CMP_ALWAYS_FALSE);
	}

	@Test
	public void checkCompareAlwaysTrue1() throws Exception {
		assertError(getModule("com/synflow/test/xfail/CompareAlwaysTrue1.cf"), ERR_CMP_ALWAYS_TRUE);
	}

	@Test
	public void checkCompareAlwaysTrue2() throws Exception {
		assertError(getModule("com/synflow/test/xfail/CompareAlwaysTrue2.cf"), ERR_CMP_ALWAYS_TRUE);
	}

	@Test
	public void checkConst1() throws Exception {
		assertError(getModule("com/synflow/test/xfail/Const1.cf"), ERR_VAR_DECL);
	}

	@Test
	public void checkConst3() throws Exception {
		assertError(getModule("com/synflow/test/xfail/Const3.cf"), ERR_CANNOT_ASSIGN_CONST);
	}

	@Test
	public void checkConst4() throws Exception {
		assertError(getModule("com/synflow/test/xfail/Const4.cf"), ERR_CANNOT_ASSIGN_CONST);
	}

	@Test
	public void checkDivByNonConst() throws Exception {
		assertError(getModule("com/synflow/test/xfail/DivByNonConst.cf"),
				ERR_DIV_MOD_NOT_CONST_POW_Of_TWO);
	}

	@Test
	public void checkDivByNonPowerOfTwo() throws Exception {
		assertError(getModule("com/synflow/test/xfail/DivByNonPowerOfTwo.cf"),
				ERR_DIV_MOD_NOT_CONST_POW_Of_TWO);
	}

	@Test
	public void checkDuplicate1() throws Exception {
		assertError(getModule("com/synflow/test/xfail/Duplicate1.cf"),
				IssueCodes.ERR_DUPLICATE_DECLARATIONS);
	}

	@Test
	public void checkDuplicate2() throws Exception {
		assertError(getModule("com/synflow/test/xfail/Duplicate2.cf"),
				IssueCodes.ERR_DUPLICATE_DECLARATIONS);
	}

	@Test
	public void checkDuplicate3() throws Exception {
		assertError(getModule("com/synflow/test/xfail/Duplicate3.cf"),
				IssueCodes.ERR_DUPLICATE_DECLARATIONS);
	}

	@Test
	public void checkExpectedConstantIdle() throws Exception {
		assertError(getModule("com/synflow/test/xfail/ExpectedConstantIdle.cf"), ERR_EXPECTED_CONST);
	}

	@Test
	public void checkExpectedConstantIdle2() throws Exception {
		assertError(getModule("com/synflow/test/xfail/ExpectedConstantIdle2.cf"),
				ERR_EXPECTED_CONST);
	}

	@Test
	public void checkExpectedConstantInitValue() throws Exception {
		assertError(getModule("com/synflow/test/xfail/ExpectedConstantInitValue.cf"),
				ERR_EXPECTED_CONST);
	}

	@Test
	public void checkExpectedConstantShift() throws Exception {
		assertError(getModule("com/synflow/test/xfail/ExpectedConstantShift.cf"),
				ERR_EXPECTED_CONST);
	}

	@Test
	public void checkFunctionCall1() throws Exception {
		assertError(getModule("com/synflow/test/xfail/FunctionCall1.cf"), ERR_TYPE_MISMATCH);
	}

	@Test
	public void checkFunctionCall2() throws Exception {
		assertError(getModule("com/synflow/test/xfail/FunctionCall1.cf"), ERR_TYPE_MISMATCH);
	}

	@Test
	public void checkIllegalFence1() throws Exception {
		assertError(getModule("com/synflow/test/xfail/IllegalFence1.cf"), ERR_ILLEGAL_FENCE);
	}

	@Test
	public void checkIllegalFence2() throws Exception {
		assertError(getModule("com/synflow/test/xfail/IllegalFence2.cf"), ERR_ILLEGAL_FENCE);
	}

	@Test
	public void checkIllegalFence3() throws Exception {
		assertError(getModule("com/synflow/test/xfail/IllegalFence3.cf"), ERR_ILLEGAL_FENCE);
	}

	@Test
	public void checkIllegalFence4() throws Exception {
		assertError(getModule("com/synflow/test/xfail/IllegalFence4.cf"), ERR_ILLEGAL_FENCE);
	}

	@Test
	public void checkMainFunctionIncorrectType() throws Exception {
		assertError(getModule("com/synflow/test/xfail/MainFunctionIncorrectType.cf"),
				ERR_MAIN_FUNCTION_BAD_TYPE);
	}

	@Test
	public void checkModByNonConst() throws Exception {
		assertError(getModule("com/synflow/test/xfail/ModByNonConst.cf"),
				ERR_DIV_MOD_NOT_CONST_POW_Of_TWO);
	}

	@Test
	public void checkModByNonPowerOfTwo() throws Exception {
		assertError(getModule("com/synflow/test/xfail/ModByNonPowerOfTwo.cf"),
				ERR_DIV_MOD_NOT_CONST_POW_Of_TWO);
	}

	@Test
	public void checkModuleWithNoFunctions() throws Exception {
		assertError(getModule("com/synflow/test/xfail/ModuleWithNoFunctions.cf"),
				ERR_MISSING_MAIN_FUNCTION);
	}

	@Test
	public void checkMultipleReads() throws Exception {
		assertError(getModule("com/synflow/test/xfail/MultipleReads.cf"), ERR_MULTIPLE_READS);
	}

	@Test
	public void checkMultipleReads2() throws Exception {
		assertError(getModule("com/synflow/test/xfail/MultipleReads2.cf"), ERR_MULTIPLE_READS);
	}

	@Test
	public void checkMultipleReads3() throws Exception {
		assertError(getModule("com/synflow/test/xfail/MultipleReads3.cf"), ERR_MULTIPLE_READS);
	}

	@Test
	public void checkNonPowerOfTwoMultiDimensionalArray() throws Exception {
		assertError(getModule("com/synflow/test/xfail/NonPowerOfTwoMultiDimensionalArray.cf"),
				IssueCodes.ERR_ARRAY_MULTI_NON_POWER_OF_TWO);
	}

	@Test
	public void checkNoSideEffectCallFunction() throws Exception {
		assertError(getModule("com/synflow/test/xfail/NoSideEffectCallFunction.cf"),
				IssueCodes.ERR_NO_SIDE_EFFECTS);
	}

	@Test
	public void checkNoSideEffectCallFunction2() throws Exception {
		assertError(getModule("com/synflow/test/xfail/NoSideEffectCallFunction2.cf"),
				IssueCodes.ERR_NO_SIDE_EFFECTS);
	}

	@Test
	public void checkNoSideEffectFunction1() throws Exception {
		assertError(getModule("com/synflow/test/xfail/NoSideEffectFunction1.cf"),
				ERR_SIDE_EFFECTS_FUNCTION);
	}

	@Test
	public void checkNoSideEffectFunction2() throws Exception {
		assertError(getModule("com/synflow/test/xfail/NoSideEffectFunction2.cf"),
				ERR_SIDE_EFFECTS_FUNCTION);
	}

	@Test
	public void checkNoSideEffectFunction3() throws Exception {
		assertError(getModule("com/synflow/test/xfail/NoSideEffectFunction3.cf"),
				ERR_SIDE_EFFECTS_FUNCTION);
	}

	@Test
	public void checkNoType_i1() throws Exception {
		assertError(getModule("com/synflow/test/xfail/NoType_i1.cf"), IssueCodes.ERR_TYPE_ONE_BIT);
	}

	@Test
	public void checkNoType_u1() throws Exception {
		assertError(getModule("com/synflow/test/xfail/NoType_u1.cf"), IssueCodes.ERR_TYPE_ONE_BIT);
	}

	@Test
	public void checkSyntaxErrorPort() throws Exception {
		assertHasParseErrors(getModule("com/synflow/test/xfail/FailSyntaxPort.cf"));
	}

	@Test
	public void defined() throws Exception {
		test("no_init", ERR_LOCAL_NOT_INITIALIZED, "NoInit1", "NoInit2");
	}

	@Test
	public void intepreterReallyChecks() throws Exception {
		// checkExecution("com/synflow/test/xfail/XfailBadCounter.cf", false);
	}

	private void test(String pack, String code, String... tests) throws Exception {
		int failed = 0;
		for (String test : tests) {
			String name = "com/synflow/test/xfail/" + pack + "/" + test + ".cf";
			System.out.println("testing " + name);
			try {
				assertError(getModule(name), code);
			} catch (Throwable t) {
				printFailure(name, t);
				failed++;
			}
		}

		testEnded(failed, tests.length);
	}

	@Test
	public void type() throws Exception {
		test("type", ERR_TYPE_MISMATCH, "TypeMismatch1", "TypeMismatch2", "TypeMismatchAssign1",
				"TypeMismatchAssign2", "TypeMismatchAssign3", "TypeMismatchBitSelect1",
				"TypeMismatchBitSelect2", "TypeMismatchDecl", "TypeMismatchIf",
				"TypeMismatchIndex", "TypeMismatchRead", "TypeMismatchStore1",
				"TypeMismatchStore2", "TypeMismatchTernary1", "TypeMismatchTernary2",
				"TypeMismatchWrite");
	}

}
