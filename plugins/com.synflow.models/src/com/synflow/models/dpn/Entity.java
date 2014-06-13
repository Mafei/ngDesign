/*******************************************************************************
 * Copyright (c) 2013-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - cleaned up
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
package com.synflow.models.dpn;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Var;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Entity</b></em>'. <!--
 * end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link com.synflow.models.dpn.Entity#getFileName <em>File Name</em>}</li>
 * <li>{@link com.synflow.models.dpn.Entity#getInputs <em>Inputs</em>}</li>
 * <li>{@link com.synflow.models.dpn.Entity#getLineNumber <em>Line Number</em>}</li>
 * <li>{@link com.synflow.models.dpn.Entity#getName <em>Name</em>}</li>
 * <li>{@link com.synflow.models.dpn.Entity#getOutputs <em>Outputs</em>}</li>
 * <li>{@link com.synflow.models.dpn.Entity#getParameters <em>Parameters</em>}</li>
 * <li>{@link com.synflow.models.dpn.Entity#getProperties <em>Properties</em>}</li>
 * <li>{@link com.synflow.models.dpn.Entity#getProcedures <em>Procedures</em>}</li>
 * <li>{@link com.synflow.models.dpn.Entity#getVariables <em>Variables</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.synflow.models.dpn.DpnPackage#getEntity()
 * @model
 * @generated
 */
public interface Entity extends EObject {

	/**
	 * Returns the file this network is defined in.
	 * 
	 * @return the file this network is defined in
	 */
	IFile getFile();

	/**
	 * Returns the value of the '<em><b>File Name</b></em>' attribute. <!-- begin-user-doc --><!--
	 * end-user-doc -->
	 * 
	 * @return the value of the '<em>File Name</em>' attribute.
	 * @see #setFileName(String)
	 * @see com.synflow.models.dpn.DpnPackage#getEntity_FileName()
	 * @model
	 * @generated
	 */
	String getFileName();

	/**
	 * Returns the input port whose name matches the given name.
	 * 
	 * @param name
	 *            the port name
	 * @return an input port whose name matches the given name
	 */
	Port getInput(String name);

	/**
	 * Returns the value of the '<em><b>Inputs</b></em>' containment reference list. The list
	 * contents are of type {@link com.synflow.models.dpn.Port}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Inputs</em>' reference list isn't clear, there really should be
	 * more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Inputs</em>' containment reference list.
	 * @see com.synflow.models.dpn.DpnPackage#getEntity_Inputs()
	 * @model containment="true"
	 * @generated
	 */
	EList<Port> getInputs();

	/**
	 * Returns the value of the '<em><b>Line Number</b></em>' attribute. <!-- begin-user-doc --><!--
	 * end-user-doc -->
	 * 
	 * @return the value of the '<em>Line Number</em>' attribute.
	 * @see #setLineNumber(int)
	 * @see com.synflow.models.dpn.DpnPackage#getEntity_LineNumber()
	 * @model
	 * @generated
	 */
	int getLineNumber();

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see com.synflow.models.dpn.DpnPackage#getEntity_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Returns the output port whose name matches the given name.
	 * 
	 * @param name
	 *            the port name
	 * @return an output port whose name matches the given name
	 */
	Port getOutput(String name);

	/**
	 * Returns the value of the '<em><b>Outputs</b></em>' containment reference list. The list
	 * contents are of type {@link com.synflow.models.dpn.Port}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Outputs</em>' reference list isn't clear, there really should be
	 * more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Outputs</em>' containment reference list.
	 * @see com.synflow.models.dpn.DpnPackage#getEntity_Outputs()
	 * @model containment="true"
	 * @generated
	 */
	EList<Port> getOutputs();

	String getPackage();

	/**
	 * Returns the parameter with the given name.
	 * 
	 * @param name
	 *            name of a parameter
	 * @return the parameter with the given name
	 */
	Var getParameter(String name);

	/**
	 * Returns the value of the '<em><b>Parameters</b></em>' containment reference list. The list
	 * contents are of type {@link com.synflow.models.ir.Var}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parameters</em>' reference list isn't clear, there really should
	 * be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Parameters</em>' containment reference list.
	 * @see com.synflow.models.dpn.DpnPackage#getEntity_Parameters()
	 * @model containment="true"
	 * @generated
	 */
	EList<Var> getParameters();

	/**
	 * Returns a procedure of this actor whose name matches the given name.
	 * 
	 * @param name
	 *            the procedure name
	 * @return a procedure whose name matches the given name
	 */
	Procedure getProcedure(String name);

	/**
	 * Returns the value of the '<em><b>Procedures</b></em>' containment reference list. The list
	 * contents are of type {@link com.synflow.models.ir.Procedure}. <!-- begin-user-doc --><!--
	 * end-user-doc -->
	 * 
	 * @return the value of the '<em>Procedures</em>' containment reference list.
	 * @see com.synflow.models.dpn.DpnPackage#getEntity_Procedures()
	 * @model containment="true"
	 * @generated
	 */
	EList<Procedure> getProcedures();

	/**
	 * Returns the value of the '<em><b>Properties</b></em>' attribute. <!-- begin-user-doc --><!--
	 * end-user-doc -->
	 * 
	 * @return the value of the '<em>Properties</em>' attribute.
	 * @see #setProperties(JsonObject)
	 * @see com.synflow.models.dpn.DpnPackage#getEntity_Properties()
	 * @model dataType="com.synflow.models.dpn.JsonObject"
	 * @generated
	 */
	JsonObject getProperties();

	/**
	 * Returns the last component of the qualified name returned by {@link #getName()}.
	 * 
	 * @return an unqualified name
	 */
	String getSimpleName();

	/**
	 * Returns the variable with the given name.
	 * 
	 * @param name
	 *            name of a variable
	 * @return the variable with the given name
	 */
	Var getVariable(String name);

	/**
	 * Returns the value of the '<em><b>Variables</b></em>' containment reference list. The list
	 * contents are of type {@link com.synflow.models.ir.Var}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parameters</em>' reference list isn't clear, there really should
	 * be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Variables</em>' containment reference list.
	 * @see com.synflow.models.dpn.DpnPackage#getEntity_Variables()
	 * @model containment="true"
	 * @generated
	 */
	EList<Var> getVariables();

	/**
	 * Sets the value of the '{@link com.synflow.models.dpn.Entity#getFileName <em>File Name</em>}'
	 * attribute. <!-- begin-user-doc --><!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>File Name</em>' attribute.
	 * @see #getFileName()
	 * @generated
	 */
	void setFileName(String value);

	/**
	 * Sets the value of the '{@link com.synflow.models.dpn.Entity#getLineNumber
	 * <em>Line Number</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Line Number</em>' attribute.
	 * @see #getLineNumber()
	 * @generated
	 */
	void setLineNumber(int value);

	/**
	 * Sets the value of the '{@link com.synflow.models.dpn.Entity#getName <em>Name</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Sets the value of the '{@link com.synflow.models.dpn.Entity#getProperties
	 * <em>Properties</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Properties</em>' attribute.
	 * @see #getProperties()
	 * @generated
	 */
	void setProperties(JsonObject value);

} // Entity
