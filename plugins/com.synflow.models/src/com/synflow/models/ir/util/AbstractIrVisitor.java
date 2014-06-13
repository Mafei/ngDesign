/*
 * Copyright (c) 2009, IETR/INSA of Rennes
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the IETR/INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.synflow.models.ir.util;

import static com.synflow.models.util.SwitchUtil.DONE;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import com.synflow.models.ir.BlockBasic;
import com.synflow.models.ir.BlockIf;
import com.synflow.models.ir.BlockWhile;
import com.synflow.models.ir.ExprBinary;
import com.synflow.models.ir.ExprUnary;
import com.synflow.models.ir.Instruction;
import com.synflow.models.ir.Procedure;
import com.synflow.models.util.Void;

/**
 * This abstract class implements a no-op visitor on IR procedures, blocks,
 * instructions, and (if visitFull is <code>true</code>) expressions. This class
 * should be extended by classes that implement intra-procedural IR visitors and
 * transformations.
 * 
 * @author Matthieu Wipliez
 * @since 1.2
 */
public abstract class AbstractIrVisitor extends IrSwitch<Void> {
	
	/**
	 * current procedure being visited
	 */
	protected Procedure procedure;

	@Override
	public Void caseBlockBasic(BlockBasic block) {
		return visit(block.getInstructions());
	}

	@Override
	public Void caseBlockIf(BlockIf blockIf) {
		visit(blockIf.getThenBlocks());
		visit(blockIf.getElseBlocks());
		doSwitch(blockIf.getJoinBlock());
		return DONE;
	}

	@Override
	public Void caseBlockWhile(BlockWhile blockWhile) {
		visit(blockWhile.getBlocks());
		doSwitch(blockWhile.getJoinBlock());
		return DONE;
	}

	@Override
	public Void caseExprBinary(ExprBinary expr) {
		doSwitch(expr.getE1());
		doSwitch(expr.getE2());
		return DONE;
	}

	@Override
	public Void caseExprUnary(ExprUnary expr) {
		doSwitch(expr.getExpr());
		return DONE;
	}

	@Override
	public Void caseProcedure(Procedure procedure) {
		this.procedure = procedure;
		return visit(procedure.getBlocks());
	}

	protected void delete(EObject eObject) {
		IrUtil.delete(eObject);
	}

	@Override
	public final Void doSwitch(EObject eObject) {
		if (eObject == null) {
			return null;
		}
		return doSwitch(eObject.eClass(), eObject);
	}

	@Override
	public Void doSwitch(int classifierID, EObject eObject) {
		// just so we can use it in DfVisitor
		return super.doSwitch(classifierID, eObject);
	}

	@Override
	public boolean isSwitchFor(EPackage ePackage) {
		// just so we can use it in DfVisitor
		return super.isSwitchFor(ePackage);
	}

	protected void replace(Instruction instr, Instruction by) {
		BlockBasic block = (BlockBasic) instr.eContainer();
		int index = block.indexOf(instr);
		block.getInstructions().set(index, by);
	}

	protected <T extends EObject> Void visit(EList<T> objects) {
		int i = 0; 
		while (i < objects.size()) {
			T object = objects.get(i);
			int size = objects.size();
			doSwitch(object);
			if (size == objects.size() && object == objects.get(i)) {
				i++;
			}
		}
		return DONE;
	}

}
