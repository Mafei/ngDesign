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
package com.synflow.models.ir.impl;

import com.synflow.models.ir.Expression;
import com.synflow.models.ir.IrPackage;
import com.synflow.models.ir.TypeGen;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Type Gen</b></em>'. <!--
 * end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link com.synflow.models.ir.impl.TypeGenImpl#getSize <em>Size</em>}</li>
 * <li>{@link com.synflow.models.ir.impl.TypeGenImpl#getTypeName <em>Type Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TypeGenImpl extends TypeImpl implements TypeGen {
	/**
	 * The cached value of the '{@link #getSize() <em>Size</em>}' containment reference. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getSize()
	 * @generated
	 * @ordered
	 */
	protected Expression size;

	/**
	 * The default value of the '{@link #getTypeName() <em>Type Name</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getTypeName()
	 * @generated
	 * @ordered
	 */
	protected static final String TYPE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTypeName() <em>Type Name</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getTypeName()
	 * @generated
	 * @ordered
	 */
	protected String typeName = TYPE_NAME_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected TypeGenImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return IrPackage.Literals.TYPE_GEN;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Expression getSize() {
		return size;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public NotificationChain basicSetSize(Expression newSize, NotificationChain msgs) {
		Expression oldSize = size;
		size = newSize;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					IrPackage.TYPE_GEN__SIZE, oldSize, newSize);
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
	public void setSize(Expression newSize) {
		if (newSize != size) {
			NotificationChain msgs = null;
			if (size != null)
				msgs = ((InternalEObject) size).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
						- IrPackage.TYPE_GEN__SIZE, null, msgs);
			if (newSize != null)
				msgs = ((InternalEObject) newSize).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
						- IrPackage.TYPE_GEN__SIZE, null, msgs);
			msgs = basicSetSize(newSize, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IrPackage.TYPE_GEN__SIZE,
					newSize, newSize));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setTypeName(String newTypeName) {
		String oldTypeName = typeName;
		typeName = newTypeName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IrPackage.TYPE_GEN__TYPE_NAME,
					oldTypeName, typeName));
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
		case IrPackage.TYPE_GEN__SIZE:
			return basicSetSize(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case IrPackage.TYPE_GEN__SIZE:
			return getSize();
		case IrPackage.TYPE_GEN__TYPE_NAME:
			return getTypeName();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case IrPackage.TYPE_GEN__SIZE:
			setSize((Expression) newValue);
			return;
		case IrPackage.TYPE_GEN__TYPE_NAME:
			setTypeName((String) newValue);
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
	public void eUnset(int featureID) {
		switch (featureID) {
		case IrPackage.TYPE_GEN__SIZE:
			setSize((Expression) null);
			return;
		case IrPackage.TYPE_GEN__TYPE_NAME:
			setTypeName(TYPE_NAME_EDEFAULT);
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case IrPackage.TYPE_GEN__SIZE:
			return size != null;
		case IrPackage.TYPE_GEN__TYPE_NAME:
			return TYPE_NAME_EDEFAULT == null ? typeName != null : !TYPE_NAME_EDEFAULT
					.equals(typeName);
		}
		return super.eIsSet(featureID);
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
		result.append(" (typeName: ");
		result.append(typeName);
		result.append(')');
		return result.toString();
	}

} // TypeGenImpl
