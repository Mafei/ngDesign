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
 * <li>{@link com.synflow.models.ir.ExprCast#getCastedSize <em>Casted Size</em>}</li>
 * <li>{@link com.synflow.models.ir.ExprCast#getExpr <em>Expr</em>}</li>
 * <li>{@link com.synflow.models.ir.ExprCast#getTargetTypeName <em>Target Type Name</em>}</li>
 * <li>{@link com.synflow.models.ir.ExprCast#isToSigned <em>To Signed</em>}</li>
 * <li>{@link com.synflow.models.ir.ExprCast#isToUnsigned <em>To Unsigned</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.synflow.models.ir.IrPackage#getExprCast()
 * @model
 * @generated
 */
public interface ExprCast extends Expression {

	/**
	 * Returns the value of the '<em><b>Casted Size</b></em>' containment reference. <!--
	 * begin-user-doc --><!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Casted Size</em>' containment reference.
	 * @see #setCastedSize(Expression)
	 * @see com.synflow.models.ir.IrPackage#getExprCast_CastedSize()
	 * @model
	 * @generated
	 */
	int getCastedSize();

	/**
	 * Sets the value of the '{@link com.synflow.models.ir.ExprCast#getCastedSize
	 * <em>Casted Size</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Casted Size</em>' attribute.
	 * @see #getCastedSize()
	 * @generated
	 */
	void setCastedSize(int value);

	/**
	 * Returns the value of the '<em><b>Expr</b></em>' containment reference. <!-- begin-user-doc
	 * -->
	 * <p>
	 * If the meaning of the '<em>Expr</em>' containment reference isn't clear, there really should
	 * be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Expr</em>' containment reference.
	 * @see #setExpr(Expression)
	 * @see com.synflow.models.ir.IrPackage#getExprCast_Expr()
	 * @model containment="true"
	 * @generated
	 */
	Expression getExpr();

	/**
	 * Returns the value of the '<em><b>To Signed</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>To Signed</em>' attribute isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>To Signed</em>' attribute.
	 * @see #setToSigned(boolean)
	 * @see com.synflow.models.ir.IrPackage#getExprCast_ToSigned()
	 * @model
	 * @generated
	 */
	boolean isToSigned();

	/**
	 * Returns the value of the '<em><b>To Unsigned</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>To Unsigned</em>' attribute isn't clear, there really should be
	 * more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>To Unsigned</em>' attribute.
	 * @see #setToUnsigned(boolean)
	 * @see com.synflow.models.ir.IrPackage#getExprCast_ToUnsigned()
	 * @model
	 * @generated
	 */
	boolean isToUnsigned();

	/**
	 * Sets the value of the '{@link com.synflow.models.ir.ExprCast#getExpr <em>Expr</em>}'
	 * containment reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Expr</em>' containment reference.
	 * @see #getExpr()
	 * @generated
	 */
	void setExpr(Expression value);

	/**
	 * Returns the value of the '<em><b>Target Type Name</b></em>' attribute. <!-- begin-user-doc
	 * -->
	 * <p>
	 * If the meaning of the '<em>Target Type Name</em>' attribute isn't clear, there really should
	 * be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Target Type Name</em>' attribute.
	 * @see #setTargetTypeName(String)
	 * @see com.synflow.models.ir.IrPackage#getExprCast_TargetTypeName()
	 * @model
	 * @generated
	 */
	String getTargetTypeName();

	/**
	 * Sets the value of the '{@link com.synflow.models.ir.ExprCast#getTargetTypeName
	 * <em>Target Type Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Target Type Name</em>' attribute.
	 * @see #getTargetTypeName()
	 * @generated
	 */
	void setTargetTypeName(String value);

	/**
	 * Sets the value of the '{@link com.synflow.models.ir.ExprCast#isToSigned <em>To Signed</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>To Signed</em>' attribute.
	 * @see #isToSigned()
	 * @generated
	 */
	void setToSigned(boolean value);

	/**
	 * Sets the value of the '{@link com.synflow.models.ir.ExprCast#isToUnsigned
	 * <em>To Unsigned</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>To Unsigned</em>' attribute.
	 * @see #isToUnsigned()
	 * @generated
	 */
	void setToUnsigned(boolean value);

} // ExprCast
