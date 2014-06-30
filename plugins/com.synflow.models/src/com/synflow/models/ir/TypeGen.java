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
package com.synflow.models.ir;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Type Gen</b></em>'. <!--
 * end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link com.synflow.models.ir.TypeGen#getSize <em>Size</em>}</li>
 * <li>{@link com.synflow.models.ir.TypeGen#getTypeName <em>Type Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.synflow.models.ir.IrPackage#getTypeGen()
 * @model
 * @generated
 */
public interface TypeGen extends Type {

	/**
	 * Returns the value of the '<em><b>Size</b></em>' containment reference. <!-- begin-user-doc
	 * -->
	 * <p>
	 * If the meaning of the '<em>Size</em>' containment reference isn't clear, there really should
	 * be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Size</em>' containment reference.
	 * @see #setSize(Expression)
	 * @see com.synflow.models.ir.IrPackage#getTypeGen_Size()
	 * @model containment="true"
	 * @generated
	 */
	Expression getSize();

	/**
	 * Returns the value of the '<em><b>Type Name</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type Name</em>' attribute isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Type Name</em>' attribute.
	 * @see #setTypeName(String)
	 * @see com.synflow.models.ir.IrPackage#getTypeGen_TypeName()
	 * @model
	 * @generated
	 */
	String getTypeName();

	/**
	 * Sets the value of the '{@link com.synflow.models.ir.TypeGen#getSize <em>Size</em>}'
	 * containment reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Size</em>' containment reference.
	 * @see #getSize()
	 * @generated
	 */
	void setSize(Expression value);

	/**
	 * Sets the value of the '{@link com.synflow.models.ir.TypeGen#getTypeName <em>Type Name</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Type Name</em>' attribute.
	 * @see #getTypeName()
	 * @generated
	 */
	void setTypeName(String value);

} // TypeGen
