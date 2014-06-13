/*******************************************************************************
 * Copyright (c) 2013 Synflow SAS.
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
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Expr Cast</b></em>'. <!--
 * end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link com.synflow.models.ir.ExprTernary#getE1 <em>E1</em>}</li>
 * <li>{@link com.synflow.models.ir.ExprTernary#getE2 <em>E2</em>}</li>
 * <li>{@link com.synflow.models.ir.ExprTernary#getE3 <em>E3</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.synflow.models.ir.IrPackage#getExprTernary()
 * @model
 * @generated
 */
public interface ExprTernary extends Expression {

	/**
	 * Returns the value of the '<em><b>Casted Size</b></em>' containment reference. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Size</em>' attribute isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Casted Size</em>' containment reference.
	 * @see #setCastedSize(Expression)
	 * @see com.synflow.models.ir.IrPackage#getExprCast_CastedSize()
	 * @model containment="true"
	 * @generated
	 */
	Expression getE1();

	/**
	 * Returns the value of the '<em><b>Casted Size</b></em>' containment reference. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Size</em>' attribute isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Casted Size</em>' containment reference.
	 * @see #setCastedSize(Expression)
	 * @see com.synflow.models.ir.IrPackage#getExprCast_CastedSize()
	 * @model containment="true"
	 * @generated
	 */
	Expression getE2();

	/**
	 * Returns the value of the '<em><b>Casted Size</b></em>' containment reference. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Size</em>' attribute isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Casted Size</em>' containment reference.
	 * @see #setCastedSize(Expression)
	 * @see com.synflow.models.ir.IrPackage#getExprCast_CastedSize()
	 * @model containment="true"
	 * @generated
	 */
	Expression getE3();

	/**
	 * Sets the value of the '{@link com.synflow.models.ir.ExprTernary#getE1 <em>E1</em>}'
	 * containment reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>E1</em>' containment reference.
	 * @see #getE1()
	 * @generated
	 */
	void setE1(Expression value);

	/**
	 * Sets the value of the '{@link com.synflow.models.ir.ExprTernary#getE2 <em>E2</em>}'
	 * containment reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>E2</em>' containment reference.
	 * @see #getE2()
	 * @generated
	 */
	void setE2(Expression value);

	/**
	 * Sets the value of the '{@link com.synflow.models.ir.ExprTernary#getE3 <em>E3</em>}'
	 * containment reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>E3</em>' containment reference.
	 * @see #getE3()
	 * @generated
	 */
	void setE3(Expression value);

}
