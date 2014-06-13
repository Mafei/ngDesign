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
/*
 * Copyright (c) 2009-2011, IETR/INSA of Rennes
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
package com.synflow.models.ir;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * This class defines a procedure.
 * 
 * @author Matthieu Wipliez
 * @model
 */
public interface Procedure extends EObject {

	/**
	 * Returns the value of the '<em><b>Blocks</b></em>' containment reference list. The list
	 * contents are of type {@link com.synflow.models.ir.Block}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Blocks</em>' containment reference list isn't clear, there really
	 * should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Blocks</em>' containment reference list.
	 * @see com.synflow.models.ir.IrPackage#getProcedure_Blocks()
	 * @model containment="true"
	 * @generated
	 */
	EList<Block> getBlocks();

	/**
	 * Returns the first block in the list of blocks of the given procedure. A new block is created
	 * if there is no block in the given block list.
	 * 
	 * @param procedure
	 *            a procedure
	 * @return a block
	 */
	BlockBasic getFirst();

	/**
	 * Returns the last block in the list of blocks of the given procedure. A new block is created
	 * if there is no block in the given block list.
	 * 
	 * @param procedure
	 *            a procedure
	 * @return a block
	 */
	BlockBasic getLast();

	/**
	 * Returns the line number on which this procedure starts.
	 * 
	 * @return the line number on which this procedure starts
	 * @model
	 */
	public int getLineNumber();

	/**
	 * Returns the local variable of this procedure that has the given name.
	 * 
	 * @param name
	 *            name of the local variable
	 * 
	 * @return the local variable of this procedure that has the given name.
	 */
	Var getLocal(String name);

	/**
	 * Returns the local variables of this procedure as an ordered map.
	 * 
	 * @return the local variables of this procedure as an ordered map
	 * @model containment="true"
	 */
	EList<Var> getLocals();

	/**
	 * Returns the name of this procedure.
	 * 
	 * @return the name of this procedure
	 * @model dataType="org.eclipse.emf.ecore.EString"
	 */
	String getName();

	/**
	 * Returns the parameters of this procedure.
	 * 
	 * @return the parameters of this procedure
	 * @model containment="true"
	 */
	EList<Var> getParameters();

	/**
	 * Returns the return type of this procedure.
	 * 
	 * @return the return type of this procedure
	 * @model containment="true"
	 */
	Type getReturnType();

	/**
	 * Sets the line number on which this procedure starts.
	 * 
	 * @param newLineNumber
	 *            the line number on which this procedure starts
	 */
	void setLineNumber(int newLineNumber);

	/**
	 * Sets the name of this procedure.
	 * 
	 * @param name
	 *            the new name
	 */
	void setName(String name);

	/**
	 * Sets the return type of this procedure
	 * 
	 * @param returnType
	 *            a type
	 */
	void setReturnType(Type returnType);

}
