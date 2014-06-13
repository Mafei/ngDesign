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
package com.synflow.models.ir;

import java.lang.Integer;
import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc --> An implementation of the model object ' <em><b>Var</b></em>'. <!--
 * end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link com.synflow.models.ir.TypeArray#getDimensions <em>Dimensions</em>}</li>
 * <li>{@link com.synflow.models.ir.TypeArray#getElementType <em>Element Type</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.synflow.models.ir.IrPackage#getTypeArray()
 * @model
 * @generated
 */
public interface TypeArray extends Type {

	/**
	 * Returns the value of the '<em><b>Dimensions</b></em>' attribute list. The list contents are
	 * of type {@link java.lang.Integer}. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Dimensions</em>' attribute list.
	 * @see com.synflow.models.ir.IrPackage#getTypeArray_Dimensions()
	 * @model
	 * @generated
	 */
	EList<Integer> getDimensions();

	/**
	 * Returns the value of the '<em><b>Element Type</b></em>' containment reference. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Element Type</em>' containment reference.
	 * @see #setElementType(Type)
	 * @see com.synflow.models.ir.IrPackage#getTypeArray_ElementType()
	 * @model containment="true"
	 * @generated
	 */
	Type getElementType();

	/**
	 * Sets the value of the '{@link com.synflow.models.ir.TypeArray#getElementType
	 * <em>Element Type</em>}' containment reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Element Type</em>' containment reference.
	 * @see #getElementType()
	 * @generated
	 */
	void setElementType(Type value);

}
