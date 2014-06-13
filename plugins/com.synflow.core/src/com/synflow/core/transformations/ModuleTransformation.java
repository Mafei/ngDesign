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
package com.synflow.core.transformations;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.util.DpnSwitch;
import com.synflow.models.ir.util.AbstractIrVisitor;
import com.synflow.models.util.Void;

/**
 * This class defines a task transformation: it does not visit networks.
 * 
 * @author Matthieu Wipliez
 * 
 */
public abstract class ModuleTransformation extends DpnSwitch<Void> {

	protected AbstractIrVisitor irVisitor;

	protected ModuleTransformation() {
	}

	protected ModuleTransformation(AbstractIrVisitor irVisitor) {
		this.irVisitor = irVisitor;
	}

	@Override
	public Void caseDPN(DPN network) {
		return null;
	}

	@Override
	public Void defaultCase(EObject eObject) {
		if (irVisitor != null) {
			EClass eClass = eObject.eClass();
			if (irVisitor.isSwitchFor(eClass.getEPackage())) {
				return irVisitor.doSwitch(eClass.getClassifierID(), eObject);
			}
		}
		return null;
	}

}
