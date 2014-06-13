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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import com.synflow.models.graph.Edge;

/**
 * This class defines a transition of a Finite State Machine.
 * 
 * @author Matthieu Wipliez
 * @model
 */
public interface Transition extends Edge {

	<T> T get(String name);

	/**
	 * Returns the value of the '<em><b>Action</b></em>' reference. <!-- begin-user-doc -->Returns
	 * the action that is associated with this transition, or <code>null</code> if there is no
	 * action associated with this transition. <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Action</em>' reference.
	 * @see #setAction(Action)
	 * @see com.synflow.models.dpn.DpnPackage#getTransition_Action()
	 * @model
	 * @generated
	 */
	Action getAction();

	/**
	 * Returns the value of the '<em><b>Body</b></em>' reference list. The list contents are of type
	 * {@link org.eclipse.emf.ecore.EObject}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Body</em>' reference list isn't clear, there really should be more
	 * of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Body</em>' reference list.
	 * @see com.synflow.models.dpn.DpnPackage#getTransition_Body()
	 * @model
	 * @generated
	 */
	EList<EObject> getBody();

	/**
	 * Returns the value of the '<em><b>Lines</b></em>' attribute list. The list contents are of
	 * type {@link java.lang.Integer}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Lines</em>' attribute list isn't clear, there really should be
	 * more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Lines</em>' attribute list.
	 * @see com.synflow.models.dpn.DpnPackage#getTransition_Lines()
	 * @model
	 * @generated
	 */
	EList<Integer> getLines();

	/**
	 * Returns the value of the '<em><b>Scheduler</b></em>' reference list. The list contents are of
	 * type {@link org.eclipse.emf.ecore.EObject}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Scheduler</em>' reference list isn't clear, there really should be
	 * more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Scheduler</em>' reference list.
	 * @see com.synflow.models.dpn.DpnPackage#getTransition_Scheduler()
	 * @model
	 * @generated
	 */
	EList<EObject> getScheduler();

	@Override
	State getSource();

	@Override
	State getTarget();

	<T> void put(String name, T value);

	/**
	 * Sets the value of the '{@link com.synflow.models.dpn.Transition#getAction <em>Action</em>}'
	 * reference. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Action</em>' reference.
	 * @see #getAction()
	 * @generated
	 */
	void setAction(Action value);

}
