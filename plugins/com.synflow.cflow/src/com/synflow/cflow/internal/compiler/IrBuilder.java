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
package com.synflow.cflow.internal.compiler;

import static com.synflow.cflow.internal.TransformerUtil.emptyList;
import static com.synflow.cflow.internal.TransformerUtil.getStartLine;
import static com.synflow.cflow.internal.TransformerUtil.isFalse;
import static com.synflow.cflow.internal.TransformerUtil.isOne;
import static com.synflow.cflow.internal.TransformerUtil.isTrue;
import static com.synflow.cflow.internal.TransformerUtil.isZero;
import static com.synflow.models.ir.IrFactory.eINSTANCE;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.CExpression;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.internal.instantiation.IInstantiator;
import com.synflow.cflow.internal.services.Typer;
import com.synflow.cflow.services.Evaluator;
import com.synflow.models.dpn.Entity;
import com.synflow.models.ir.Block;
import com.synflow.models.ir.BlockIf;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.InstLoad;
import com.synflow.models.ir.InstStore;
import com.synflow.models.ir.Instruction;
import com.synflow.models.ir.IrFactory;
import com.synflow.models.ir.OpBinary;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Type;
import com.synflow.models.ir.TypeArray;
import com.synflow.models.ir.TypeInt;
import com.synflow.models.ir.Var;
import com.synflow.models.ir.util.IrUtil;
import com.synflow.models.ir.util.TypeUtil;

/**
 * This class defines an IR builder. The build state is composed of the current entity, procedure,
 * blocks, etc.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class IrBuilder {

	private List<Block> blocks;

	private final Deque<List<Block>> deque;

	protected final Entity entity;

	protected final IInstantiator instantiator;

	private Map<Variable, Var> localMap;

	/**
	 * current procedure
	 */
	private Procedure procedure;

	protected Transformer transformer;

	protected final Typer typer;

	/**
	 * Creates a new function transformer with the given entity.
	 * 
	 * @param entity
	 *            target IR entity
	 */
	public IrBuilder(IInstantiator instantiator, Typer typer, Entity entity) {
		this.instantiator = instantiator;
		this.typer = typer;
		this.entity = entity;

		deque = new ArrayDeque<>();
		localMap = new HashMap<>();
	}

	final void add(Block block) {
		blocks.add(block);
	}

	public final void add(Instruction instruction) {
		IrUtil.getLast(blocks).add(instruction);
	}

	final void addAll(Collection<Block> blocks) {
		this.blocks.addAll(blocks);
	}

	final Var addLocal(Variable variable) {
		Var var = createVar(variable);
		var.setName(var.getName() + "_l");
		procedure.getLocals().add(var);
		return var;
	}

	final void addParameter(Variable variable) {
		Var param = createVar(variable);
		procedure.getParameters().add(param);
	}

	/**
	 * Creates a new temporary local variable based on the given hint. The local variable is
	 * guaranteed to have a unique name.
	 * 
	 * @param lineNumber
	 *            line number
	 * @param type
	 *            type
	 * @param hint
	 *            suggestion for a name
	 * @return a new local variable
	 */
	public Var createVar(int lineNumber, Type type, String hint) {
		Var var = IrFactory.eINSTANCE.newTempLocalVariable(procedure, type, hint);
		var.setLineNumber(lineNumber);
		return var;
	}

	/**
	 * Creates a new IR Var based on the AST variable.
	 * 
	 * @param variable
	 *            a variable
	 * @return an IR Var
	 */
	private Var createVar(Variable variable) {
		int lineNumber = getStartLine(variable);
		Type type = typer.getType(variable);
		String name = variable.getName();
		boolean assignable = !CflowUtil.isConstant(variable);

		// create local variable with the given name
		Var var = eINSTANCE.createVar(lineNumber, type, name, assignable);
		localMap.put(variable, var);
		return var;
	}

	/**
	 * Returns the IR variable that corresponds to the given C~ variable.
	 * 
	 * @param variable
	 *            a C~ variable
	 * @return the IR variable that corresponds to the given variable
	 */
	final Var getMapping(Variable variable) {
		if (!CflowUtil.isGlobal(variable)) {
			return localMap.get(variable);
		}

		return instantiator.getVar(variable);
	}

	public final Procedure getProcedure() {
		return procedure;
	}

	/**
	 * Returns the IR procedure that corresponds to the given C~ function.
	 * 
	 * @param function
	 *            a C~ function
	 * @return the IR procedure that corresponds to the given function
	 */
	final Procedure getProcedure(Variable function) {
		return instantiator.getProcedure(function);
	}

	/**
	 * Creates a new target scalar variable, and loads the given source into it. Do not perform bit
	 * selection though.
	 * 
	 * @param lineNumber
	 *            line number
	 * @param source
	 *            source variable
	 * @param indexes
	 *            indexes
	 * @return the new target scalar variable
	 */
	final Var loadVariable(int lineNumber, Var source, List<CExpression> indexes) {
		Type type = source.getType();
		int dimensions = Typer.getNumDimensions(type);

		// creates local target variable (will be cleaned up later if necessary)
		Type varType;
		if (type.isArray()) {
			varType = ((TypeArray) type).getElementType();
		} else {
			varType = type;
		}
		Var target = createVar(lineNumber, varType, "local_" + source.getName());

		// loads (but do not perform bit selection)
		List<CExpression> subIndexes = indexes.subList(0, dimensions);
		List<Expression> expressions = transformIndexes(type, subIndexes);

		InstLoad load = eINSTANCE.createInstLoad(lineNumber, target, source, expressions);
		add(load);

		return target;
	}

	public final void put(Variable variable, Var var) {
		localMap.put(variable, var);
	}

	/**
	 * Restores the value of blocks that was previously saved.
	 */
	public final void restoreBlocks() {
		blocks = deque.pollFirst();
	}

	/**
	 * Saves the current value of 'blocks'.
	 */
	public final void saveBlocks() {
		if (blocks != null) {
			deque.addFirst(blocks);
		}
	}

	final void setBlocks(List<Block> blocks) {
		this.blocks = blocks;
	}

	public final void setProcedure(Procedure procedure) {
		this.procedure = procedure;
		if (procedure == null) {
			this.blocks = null;
		} else {
			this.blocks = procedure.getBlocks();
		}
	}

	/**
	 * Sets the current procedure of this builder from the given function.
	 * 
	 * @param function
	 */
	final void setProcedure(Variable function) {
		procedure = instantiator.getProcedure(function);
		blocks = procedure.getBlocks();
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}

	/**
	 * Depending on the IR expression that value evaluates to, this method:
	 * <ul>
	 * <li>calls {@link #storeBitSet(int, Var, List, Var, int)} if value equals "true" or is an
	 * integer != 0.</li>
	 * <li>calls {@link #storeBitClear(int, Var, List, Var, int)} if value equals "false" or "0".</li>
	 * <li>otherwise it creates an if block with storeBitSet in the then block, and storeBitClear in
	 * the else block.</li>
	 * </ul>
	 * 
	 * @param lineNumber
	 *            line number
	 * @param target
	 *            target variable
	 * @param indexes
	 *            indexes
	 * @param local
	 *            local variable
	 * @param index
	 *            index of the bit to set
	 * @param value
	 *            value of the bit
	 */
	private void storeBit(int lineNumber, Var target, List<Expression> indexes, Var local,
			int index, Expression expr) {
		// get value
		if (isTrue(expr)) {
			storeBitSet(lineNumber, target, indexes, local, index);
		} else if (isFalse(expr)) {
			storeBitClear(lineNumber, target, indexes, local, index);
		} else {
			saveBlocks();

			// create and add block
			BlockIf block = eINSTANCE.createBlockIf();
			block.setJoinBlock(eINSTANCE.createBlockBasic());
			block.setLineNumber(lineNumber);

			block.setCondition(expr);
			add(block);

			// "then" block: set bit
			setBlocks(block.getThenBlocks());
			storeBitSet(lineNumber, target, indexes, local, index);

			// "else" block: clear bit
			setBlocks(block.getElseBlocks());
			storeBitClear(lineNumber, target, indexes, local, index);

			restoreBlocks();
		}
	}

	/**
	 * Creates Store(target, indexes, local & 0b110111) (the index of the '0' is given by the index
	 * variable).
	 * 
	 * @param lineNumber
	 *            line number
	 * @param target
	 *            target variable
	 * @param indexes
	 *            indexes
	 * @param local
	 *            local variable (loaded from the target)
	 * @param index
	 *            index of the bit to set
	 */
	private void storeBitClear(int lineNumber, Var target, List<Expression> indexes, Var local,
			int index) {
		Type type = local.getType();
		int size = ((TypeInt) type).getSize();

		BigInteger mask = ONE.shiftLeft(size).subtract(ONE).clearBit(index);
		Expression value = eINSTANCE.createExprBinary(eINSTANCE.createExprVar(local),
				OpBinary.BITAND, eINSTANCE.createExprInt(mask));

		InstStore store = eINSTANCE.createInstStore(lineNumber, target, indexes, value);
		add(store);
	}

	/**
	 * Creates Store(target, indexes, local | 0b001000) (the index of the '1' is given by the index
	 * variable).
	 * 
	 * @param lineNumber
	 *            line number
	 * @param target
	 *            target variable
	 * @param indexes
	 *            indexes
	 * @param local
	 *            local variable (loaded from the target)
	 * @param index
	 *            index of the bit to set
	 */
	private void storeBitSet(int lineNumber, Var target, List<Expression> indexes, Var local,
			int index) {
		BigInteger mask = ZERO.setBit(index);
		Expression value = eINSTANCE.createExprBinary(eINSTANCE.createExprVar(local),
				OpBinary.BITOR, eINSTANCE.createExprInt(mask));

		InstStore store = eINSTANCE.createInstStore(lineNumber, target, indexes, value);
		add(store);
	}

	/**
	 * Creates a store to the given target variable, with the given indexes, and the given value.
	 * 
	 * @param lineNumber
	 *            line number
	 * @param target
	 *            target IR variable
	 * @param indexes
	 *            list of IR expressions (may be <code>null</code>)
	 * @param value
	 *            C~ value
	 */
	public final void storeExpr(int lineNumber, Var target, List<CExpression> indexes,
			CExpression value) {
		Type type = target.getType();
		boolean hasIndexes = indexes != null && !indexes.isEmpty();
		int dimensions = Typer.getNumDimensions(type);
		boolean storeBit = hasIndexes && dimensions < indexes.size();

		// transform expression (with implicit cast to boolean)
		if (storeBit) {
			type = eINSTANCE.createTypeBool();
		} else if (hasIndexes) {
			type = ((TypeArray) type).getElementType();
		}
		Expression expr = transformExpr(value, type);

		// bit store
		if (storeBit) {
			// loads variable (but do not perform bit selection)
			Var local = loadVariable(lineNumber, target, indexes);

			// select indexes (but not bit selection)
			List<CExpression> subIndexes = indexes.subList(0, dimensions);
			List<Expression> expressions = transformIndexes(target.getType(), subIndexes);

			// select bit index
			CExpression exprIndex = indexes.get(indexes.size() - 1);
			int index = Evaluator.getIntValue(exprIndex);

			// store the bit
			storeBit(lineNumber, target, expressions, local, index, expr);
			return;
		}

		// transform indexes
		List<Expression> expressions;
		expressions = hasIndexes ? transformIndexes(target.getType(), indexes) : emptyList();

		// "normal" store
		InstStore store = eINSTANCE.createInstStore(lineNumber, target, expressions, expr);
		add(store);
	}

	/**
	 * Transforms the given expression, and cast to boolean if <code>target</code> is a boolean
	 * type.
	 * 
	 * @param expression
	 *            an AST expression
	 * @param target
	 *            target type
	 * @return an IR expression
	 */
	protected Expression transformExpr(CExpression expression, Type target) {
		Expression expr = transformer.transformExpr(expression);
		if (target.isBool()) {
			Type type = TypeUtil.getType(expr);
			if (isOne(expr)) {
				return eINSTANCE.createExprBool(true);
			} else if (isZero(expr)) {
				return eINSTANCE.createExprBool(false);
			} else if (!type.isBool()) {
				return eINSTANCE.createExprBinary(expr, OpBinary.NE, eINSTANCE.createExprInt(0));
			}
		}

		return expr;
	}

	/**
	 * Transforms the given AST expressions to a list of IR expressions. In the process nodes may be
	 * created and added to the current {@link #procedure}, since many expressions are expressed
	 * with IR statements.
	 * 
	 * @param expressions
	 *            a list of AST expressions
	 * @return a list of IR expressions
	 */
	final List<Expression> transformExpressions(List<CExpression> expressions) {
		int length = expressions.size();
		List<Expression> irExpressions = new ArrayList<Expression>(length);
		for (CExpression expression : expressions) {
			irExpressions.add(transformer.transformExpr(expression));
		}

		return irExpressions;
	}

	/**
	 * Transforms the given AST expressions to a list of IR expressions and convert them to
	 * unsigned.
	 * 
	 * @param expressions
	 *            a list of AST expressions
	 * @return a list of IR expressions
	 */
	private List<Expression> transformIndexes(Type type, List<CExpression> expressions) {
		List<Expression> irExpressions = transformExpressions(expressions);
		List<Expression> casted = new ArrayList<>(irExpressions.size());

		// cast indexes (only if type is an array => there are indexes to cast)
		if (type.isArray()) {
			Iterator<Integer> itD = ((TypeArray) type).getDimensions().iterator();

			// cast indexes
			for (Expression index : irExpressions) {
				int size = itD.next();
				int amount = TypeUtil.getSize(size - 1);
				casted.add(eINSTANCE.castToUnsigned(amount, index));
			}
		}
		return casted;
	}

}
