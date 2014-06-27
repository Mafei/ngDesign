/**
 */
package com.synflow.models.ir;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Expr Resize</b></em>'. <!--
 * end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link com.synflow.models.ir.ExprResize#getExpr <em>Expr</em>}</li>
 * <li>{@link com.synflow.models.ir.ExprResize#getTargetSize <em>Target Size</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.synflow.models.ir.IrPackage#getExprResize()
 * @model
 * @generated
 */
public interface ExprResize extends Expression {
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
	 * @see com.synflow.models.ir.IrPackage#getExprResize_Expr()
	 * @model containment="true"
	 * @generated
	 */
	Expression getExpr();

	/**
	 * Sets the value of the '{@link com.synflow.models.ir.ExprResize#getExpr <em>Expr</em>}'
	 * containment reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Expr</em>' containment reference.
	 * @see #getExpr()
	 * @generated
	 */
	void setExpr(Expression value);

	/**
	 * Returns the value of the '<em><b>Target Size</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Target Size</em>' attribute isn't clear, there really should be
	 * more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Target Size</em>' attribute.
	 * @see #setTargetSize(int)
	 * @see com.synflow.models.ir.IrPackage#getExprResize_TargetSize()
	 * @model
	 * @generated
	 */
	int getTargetSize();

	/**
	 * Sets the value of the '{@link com.synflow.models.ir.ExprResize#getTargetSize
	 * <em>Target Size</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Target Size</em>' attribute.
	 * @see #getTargetSize()
	 * @generated
	 */
	void setTargetSize(int value);

} // ExprResize
