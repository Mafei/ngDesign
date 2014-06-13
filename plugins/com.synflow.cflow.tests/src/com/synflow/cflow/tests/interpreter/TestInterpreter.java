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
package com.synflow.cflow.tests.interpreter;

import java.math.BigInteger;
import java.util.List;

import org.junit.Assert;

import com.synflow.cflow.tests.TestUtil;
import com.synflow.models.OrccRuntimeException;
import com.synflow.models.dpn.Action;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.Pattern;
import com.synflow.models.dpn.Port;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.InstCall;
import com.synflow.models.ir.InstLoad;
import com.synflow.models.ir.InstStore;
import com.synflow.models.ir.IrFactory;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.TypeArray;
import com.synflow.models.ir.Var;
import com.synflow.models.ir.util.ActorInterpreter;
import com.synflow.models.ir.util.TypeUtil;
import com.synflow.models.ir.util.ValueUtil;

/**
 * This class defines an interpreter for actors that can be used for unit tests.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class TestInterpreter extends ActorInterpreter {

	private boolean passed;

	/**
	 * Creates a new test intepreter.
	 * 
	 * @param actor
	 *            an actor to test
	 */
	public TestInterpreter(Actor actor) {
		super(actor);

		passed = true;
	}

	@Override
	public Object caseInstCall(InstCall call) {
		if (call.isAssert()) {
			List<Expression> arguments = call.getArguments();
			Expression expr = arguments.get(0);
			Object value = exprInterpreter.doSwitch(expr);
			assert ValueUtil.isTrue(value);
			return null;
		} else if (call.isPrint()) {
			List<Expression> arguments = call.getArguments();
			for (Expression expr : arguments) {
				Object value = exprInterpreter.doSwitch(expr);
				System.out.print(value);
			}
			System.out.println();
			return null;
		} else {
			return super.caseInstCall(call);
		}
	}

	@Override
	public Object caseInstLoad(InstLoad instr) {
		Var target = instr.getTarget().getVariable();
		Var source = instr.getSource().getVariable();
		if (instr.getIndexes().isEmpty()) {
			target.setValue(source.getValue());
		} else {
			TypeArray type = (TypeArray) source.getType();
			try {
				Object array = source.getValue();
				List<Integer> dimensions = type.getDimensions();
				Object[] indexes = new Object[instr.getIndexes().size()];
				int i = 0;
				for (Expression index : instr.getIndexes()) {
					Object value = exprInterpreter.doSwitch(index);
					int dimSize = TypeUtil.getSize(dimensions.get(i) - 1);
					Type dimType = IrFactory.eINSTANCE.createTypeInt(dimSize, false);
					value = clipValue(dimType, value, instr);
					indexes[i++] = value;
				}

				Type eltType = type.getElementType();
				Object value = ValueUtil.get(eltType, array, indexes);
				target.setValue(value);
			} catch (IndexOutOfBoundsException e) {
				throw new OrccRuntimeException("Array index out of bounds at line "
						+ instr.getLineNumber());
			}
		}
		return null;
	}

	@Override
	public Object caseInstStore(InstStore instr) {
		Var target = instr.getTarget().getVariable();
		Object value = exprInterpreter.doSwitch(instr.getValue());
		if (instr.getIndexes().isEmpty()) {
			value = clipValue(target.getType(), value, instr);
			target.setValue(value);
		} else {
			TypeArray type = (TypeArray) target.getType();
			try {
				Object array = target.getValue();
				List<Integer> dimensions = type.getDimensions();
				Object[] indexes = new Object[instr.getIndexes().size()];
				int i = 0;
				for (Expression index : instr.getIndexes()) {
					Object idx = exprInterpreter.doSwitch(index);
					int dimSize = TypeUtil.getSize(dimensions.get(i) - 1);
					Type dimType = IrFactory.eINSTANCE.createTypeInt(dimSize, false);
					idx = clipValue(dimType, idx, instr);
					indexes[i++] = idx;
				}

				Type eltType = type.getElementType();
				value = clipValue(eltType, value, instr);
				ValueUtil.set(eltType, array, value, indexes);
			} catch (IndexOutOfBoundsException e) {
				throw new OrccRuntimeException("Array index out of bounds at line "
						+ instr.getLineNumber());
			}
		}
		return null;
	}

	@Override
	public Object caseProcedure(Procedure procedure) {
		for (Var local : procedure.getLocals()) {
			Type type = local.getType();
			Object value;
			if (type.isArray()) {
				value = ValueUtil.createArray((TypeArray) type);
			} else if (type.isBool()) {
				value = false;
			} else {
				value = BigInteger.ZERO;
			}
			local.setValue(value);
		}

		return doSwitch(procedure.getBlocks());
	}

	/**
	 * Checks that the given port's expected token match the actual token.
	 * 
	 * @param port
	 *            an output port
	 * @param var
	 *            the variable associated to the port in the output pattern
	 */
	private void check(Port port) {
		List<Expression> tokens = TestUtil.getTokens(port, "expected");
		if (tokens.isEmpty()) {
			if (!actor.getInputs().isEmpty()) {
				// we consider that if an actor runs out of expected tokens
				// and has input ports, it's an error
				// to avoid this, use #pragma once
				passed = false;
			}

			return;
		}

		Object token = port.getValue();

		// check if we have a token
		Expression exprToken = tokens.get(0);
		if (isNull(exprToken)) {
			return;
		}

		Object expectedToken = ValueUtil.getValue(exprToken);
		Assert.assertNotNull(expectedToken);

		// comparison using ValueUtil method (take types into account)
		Object obj = ValueUtil.equals(expectedToken, token);
		boolean equals = ValueUtil.isTrue(obj);
		passed &= equals;
	}

	/**
	 * Consumes one token on each input port.
	 */
	private void consumeNullTokens() {
		for (Port port : actor.getInputs()) {
			List<Expression> tokens = TestUtil.getTokens(port, "stimulus");
			if (!tokens.isEmpty()) {
				tokens.remove(0);
			}
		}

		for (Port port : actor.getOutputs()) {
			List<Expression> tokens = TestUtil.getTokens(port, "expected");
			if (!tokens.isEmpty()) {
				tokens.remove(0);
			}
		}
	}

	@Override
	public void execute(Action action) {
		// allocate patterns
		Pattern inputPattern = action.getInputPattern();
		Pattern outputPattern = action.getOutputPattern();
		allocatePattern(inputPattern);
		allocatePattern(outputPattern);

		// Interpret the whole action
		doSwitch(action.getBody());

		for (Port port : outputPattern.getPorts()) {
			check(port);
		}
	}

	/**
	 * Returns <code>true</code> if the token list stored in the attribute with the given name of
	 * the given port is empty.
	 * 
	 * @param port
	 *            a port (input or output)
	 * @param name
	 *            name of the token list ("stimulus" or "expected")
	 * @return <code>true</code> if all tokens from the given port have been consumed
	 */
	private boolean isEmpty(Port port, String name) {
//		Attribute attribute = port.getAttribute(name);
//		Assert.assertNotNull(attribute);
//
//		EObject value = attribute.getContainedValue();
//		Assert.assertTrue(value instanceof ExprList);
//
//		// check all remaining tokens are "null"
//		ExprList expected = (ExprList) value;
//		List<Expression> tokens = expected.getValue();
//		for (Expression token : tokens) {
//			if (isNull(token)) {
//				continue;
//			}
//			return false;
//		}
		return true;
	}

	private boolean isNull(Expression token) {
		return token == null;
	}

	@Override
	protected boolean isSchedulable(Action action) {
		Pattern pattern = action.getInputPattern();

		// check tokens
		for (Port port : pattern.getPorts()) {
			if (!read(port)) {
				// if no attribute or not enough tokens
				return false;
			}
		}

		Object result = doSwitch(action.getScheduler());
		return ValueUtil.isTrue(result);
	}

	/**
	 * Reads the tokens from port and put them into the given pattern. If removeTokens is true, the
	 * tokens are removed; otherwise they are just copied.
	 * 
	 * @param port
	 *            an input port
	 * @param var
	 *            the variable that may be associated to the given port (may be <code>null</code>)
	 * @param removeTokens
	 *            a boolean
	 * @return true if the given port has a stimulus attribute, which holds an ExprList, with enough
	 *         tokens (greater than or equal to the number of tokens associated to the port in the
	 *         pattern); otherwise, false.
	 */
	private boolean read(Port port) {
		List<Expression> tokens = TestUtil.getTokens(port, "stimulus");
		if (tokens.isEmpty()) {
			// no more tokens to read
			return false;
		}

		Expression token = tokens.get(0);
		if (isNull(token)) {
			return false;
		}
		setToken(port, token);
		return true;
	}

	/**
	 * Interprets and checks the given actor. Stops when no more actions can be fired or if the test
	 * failed, i.e. if an actual value was not equal to an expected value.
	 * 
	 * @return <code>true</code> if the outputs matched expected data, <code>false</code> otherwise
	 */
	public boolean runTest() {
		initialize();

		for (int i = 0; i < 40; i++) {
			schedule();
			consumeNullTokens();
		}

		// check all tokens have been consumed
		for (Port port : actor.getInputs()) {
			passed &= isEmpty(port, "stimulus");
		}

		for (Port port : actor.getOutputs()) {
			passed &= isEmpty(port, "expected");
		}

		return passed;
	}

	/**
	 * Copies the given token into the given port.
	 * 
	 * @param port
	 *            a port
	 * @param token
	 *            a token
	 */
	private void setToken(Port port, Expression token) {
		Object value = ValueUtil.getValue(token);
		port.setValue(value);
	}

}
