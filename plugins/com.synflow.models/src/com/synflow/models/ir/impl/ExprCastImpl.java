/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.synflow.models.ir.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

import com.synflow.models.ir.ExprCast;
import com.synflow.models.ir.Expression;
import com.synflow.models.ir.IrPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Expr Cast</b></em>'. <!--
 * end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link com.synflow.models.ir.impl.ExprCastImpl#getCastedSize <em>Casted Size</em>}</li>
 * <li>{@link com.synflow.models.ir.impl.ExprCastImpl#getExpr <em>Expr</em>}</li>
 * <li>{@link com.synflow.models.ir.impl.ExprCastImpl#isToSigned <em>To Signed</em>}</li>
 * <li>{@link com.synflow.models.ir.impl.ExprCastImpl#isToUnsigned <em>To Unsigned</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ExprCastImpl extends ExpressionImpl implements ExprCast {

	/**
	 * The default value of the '{@link #getCastedSize() <em>Casted Size</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getCastedSize()
	 * @generated
	 * @ordered
	 */
	protected static final int CASTED_SIZE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getCastedSize() <em>Casted Size</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getCastedSize()
	 * @generated
	 * @ordered
	 */
	protected int castedSize = CASTED_SIZE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getExpr() <em>Expr</em>}' containment reference. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getExpr()
	 * @generated
	 * @ordered
	 */
	protected Expression expr;

	/**
	 * The default value of the '{@link #isToSigned() <em>To Signed</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #isToSigned()
	 * @generated
	 * @ordered
	 */
	protected static final boolean TO_SIGNED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isToSigned() <em>To Signed</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #isToSigned()
	 * @generated
	 * @ordered
	 */
	protected boolean toSigned = TO_SIGNED_EDEFAULT;

	/**
	 * The default value of the '{@link #isToUnsigned() <em>To Unsigned</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #isToUnsigned()
	 * @generated
	 * @ordered
	 */
	protected static final boolean TO_UNSIGNED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isToUnsigned() <em>To Unsigned</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #isToUnsigned()
	 * @generated
	 * @ordered
	 */
	protected boolean toUnsigned = TO_UNSIGNED_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ExprCastImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public NotificationChain basicSetExpr(Expression newExpr, NotificationChain msgs) {
		Expression oldExpr = expr;
		expr = newExpr;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					IrPackage.EXPR_CAST__EXPR, oldExpr, newExpr);
			if (msgs == null)
				msgs = notification;
			else
				msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case IrPackage.EXPR_CAST__CASTED_SIZE:
			return getCastedSize();
		case IrPackage.EXPR_CAST__EXPR:
			return getExpr();
		case IrPackage.EXPR_CAST__TO_SIGNED:
			return isToSigned();
		case IrPackage.EXPR_CAST__TO_UNSIGNED:
			return isToUnsigned();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID,
			NotificationChain msgs) {
		switch (featureID) {
		case IrPackage.EXPR_CAST__EXPR:
			return basicSetExpr(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case IrPackage.EXPR_CAST__CASTED_SIZE:
			return castedSize != CASTED_SIZE_EDEFAULT;
		case IrPackage.EXPR_CAST__EXPR:
			return expr != null;
		case IrPackage.EXPR_CAST__TO_SIGNED:
			return toSigned != TO_SIGNED_EDEFAULT;
		case IrPackage.EXPR_CAST__TO_UNSIGNED:
			return toUnsigned != TO_UNSIGNED_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case IrPackage.EXPR_CAST__CASTED_SIZE:
			setCastedSize((Integer) newValue);
			return;
		case IrPackage.EXPR_CAST__EXPR:
			setExpr((Expression) newValue);
			return;
		case IrPackage.EXPR_CAST__TO_SIGNED:
			setToSigned((Boolean) newValue);
			return;
		case IrPackage.EXPR_CAST__TO_UNSIGNED:
			setToUnsigned((Boolean) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return IrPackage.Literals.EXPR_CAST;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case IrPackage.EXPR_CAST__CASTED_SIZE:
			setCastedSize(CASTED_SIZE_EDEFAULT);
			return;
		case IrPackage.EXPR_CAST__EXPR:
			setExpr((Expression) null);
			return;
		case IrPackage.EXPR_CAST__TO_SIGNED:
			setToSigned(TO_SIGNED_EDEFAULT);
			return;
		case IrPackage.EXPR_CAST__TO_UNSIGNED:
			setToUnsigned(TO_UNSIGNED_EDEFAULT);
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public int getCastedSize() {
		return castedSize;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setCastedSize(int newCastedSize) {
		int oldCastedSize = castedSize;
		castedSize = newCastedSize;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IrPackage.EXPR_CAST__CASTED_SIZE,
					oldCastedSize, castedSize));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Expression getExpr() {
		return expr;
	}

	@Override
	public boolean isCast() {
		return true;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public boolean isToSigned() {
		return toSigned;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public boolean isToUnsigned() {
		return toUnsigned;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setExpr(Expression newExpr) {
		if (newExpr != expr) {
			NotificationChain msgs = null;
			if (expr != null)
				msgs = ((InternalEObject) expr).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
						- IrPackage.EXPR_CAST__EXPR, null, msgs);
			if (newExpr != null)
				msgs = ((InternalEObject) newExpr).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
						- IrPackage.EXPR_CAST__EXPR, null, msgs);
			msgs = basicSetExpr(newExpr, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IrPackage.EXPR_CAST__EXPR,
					newExpr, newExpr));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setToSigned(boolean newToSigned) {
		boolean oldToSigned = toSigned;
		toSigned = newToSigned;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IrPackage.EXPR_CAST__TO_SIGNED,
					oldToSigned, toSigned));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setToUnsigned(boolean newToUnsigned) {
		boolean oldToUnsigned = toUnsigned;
		toUnsigned = newToUnsigned;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IrPackage.EXPR_CAST__TO_UNSIGNED,
					oldToUnsigned, toUnsigned));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy())
			return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (castedSize: ");
		result.append(castedSize);
		result.append(", toSigned: ");
		result.append(toSigned);
		result.append(", toUnsigned: ");
		result.append(toUnsigned);
		result.append(')');
		return result.toString();
	}

} // ExprCastImpl
