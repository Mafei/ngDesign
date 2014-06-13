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

/**
 * <!-- begin-user-doc --> This class defines an actor. An actor has parameters, input and output
 * ports, state variables, procedures, actions and an FSM.<!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link com.synflow.models.dpn.Actor#getActions <em>Actions</em>}</li>
 * <li>{@link com.synflow.models.dpn.Actor#getActionsOutsideFsm <em>Actions Outside Fsm</em>}</li>
 * <li>{@link com.synflow.models.dpn.Actor#getFsm <em>Fsm</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.synflow.models.dpn.DpnPackage#getActor()
 * @model
 * @generated
 */
public interface Actor extends Entity {

	/**
	 * Returns the value of the '<em><b>Actions</b></em>' containment reference list. The list
	 * contents are of type {@link com.synflow.models.dpn.Action}. <!-- begin-user-doc --><!--
	 * end-user-doc -->
	 * 
	 * @return the value of the '<em>Actions</em>' containment reference list.
	 * @see com.synflow.models.dpn.DpnPackage#getActor_Actions()
	 * @model containment="true"
	 * @generated
	 */
	EList<Action> getActions();

	/**
	 * Returns the value of the '<em><b>Actions Outside Fsm</b></em>' reference list. The list
	 * contents are of type {@link com.synflow.models.dpn.Action}. <!-- begin-user-doc -->Returns
	 * the actions that are outside of an FSM. If this actor has no FSM, all actions of the actor
	 * are returned. The actions are sorted by decreasing priority.<!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Actions Outside Fsm</em>' reference list.
	 * @see com.synflow.models.dpn.DpnPackage#getActor_ActionsOutsideFsm()
	 * @model
	 * @generated
	 */
	EList<Action> getActionsOutsideFsm();

	/**
	 * Returns the value of the '<em><b>Fsm</b></em>' containment reference. <!-- begin-user-doc
	 * --><!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Fsm</em>' containment reference.
	 * @see #setFsm(FSM)
	 * @see com.synflow.models.dpn.DpnPackage#getActor_Fsm()
	 * @model containment="true"
	 * @generated
	 */
	FSM getFsm();

	/**
	 * Returns true if this actor has an FSM.
	 * 
	 * @return true if this actor has an FSM
	 */
	boolean hasFsm();

	/**
	 * Sets the value of the '{@link com.synflow.models.dpn.Actor#getFsm <em>Fsm</em>}' containment
	 * reference. <!-- begin-user-doc --><!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Fsm</em>' containment reference.
	 * @see #getFsm()
	 * @generated
	 */
	void setFsm(FSM value);

}
