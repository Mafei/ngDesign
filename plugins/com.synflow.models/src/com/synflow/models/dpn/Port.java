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

import com.synflow.models.ir.Var;

/**
 * <!-- begin-user-doc -->This class defines a port. A port has a location, a type, a name.<!--
 * end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link com.synflow.models.dpn.Port#getInterface <em>Interface</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.synflow.models.dpn.DpnPackage#getPort()
 * @model
 * @generated
 */
public interface Port extends Var {

	/**
	 * Returns the names of the additional inputs this port's interface requires.
	 * 
	 * @return an iterable over strings
	 */
	Iterable<String> getAdditionalInputs();

	/**
	 * Returns the names of the additional outputs this port's interface requires.
	 * 
	 * @return an iterable over strings
	 */
	Iterable<String> getAdditionalOutputs();

	/**
	 * Returns the names of the additional signals (inputs + outputs) this port's interface
	 * requires.
	 * 
	 * @return an iterable over strings
	 */
	Iterable<String> getAdditionalSignals();

	/**
	 * Returns the value of the '<em><b>Interface</b></em>' attribute. The literals are from the
	 * enumeration {@link com.synflow.models.dpn.InterfaceType}. <!-- begin-user-doc --><!--
	 * end-user-doc -->
	 * 
	 * @return the value of the '<em>Interface</em>' attribute.
	 * @see com.synflow.models.dpn.InterfaceType
	 * @see #setInterface(InterfaceType)
	 * @see com.synflow.models.dpn.DpnPackage#getPort_Interface()
	 * @model
	 * @generated
	 */
	InterfaceType getInterface();

	/**
	 * Returns <code>true</code> if this port's interface is sync.
	 */
	boolean isSync();

	/**
	 * Sets the value of the '{@link com.synflow.models.dpn.Port#getInterface <em>Interface</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Interface</em>' attribute.
	 * @see com.synflow.models.dpn.InterfaceType
	 * @see #getInterface()
	 * @generated
	 */
	void setInterface(InterfaceType value);

}
