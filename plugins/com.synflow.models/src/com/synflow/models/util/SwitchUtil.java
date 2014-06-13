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
package com.synflow.models.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.Switch;

/**
 * This class defines utility stuff for EMF-switch based code transformations.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class SwitchUtil {

	/**
	 * to use in cascading switch
	 */
	public static final Void CASCADE = null;

	/**
	 * to use for non-cascading switch;
	 */
	public static final Void DONE = new Void();

	/**
	 * Checks the given objects with the given EMF switch, and returns <code>true</code> as soon as
	 * the {@link Switch#doSwitch(EObject)} method returns true. Otherwise returns false. If an
	 * object is null, returns false.
	 * 
	 * @param emfSwitch
	 *            an EMF switch
	 * @param eObjects
	 *            a varargs array of objects
	 * @return a boolean
	 */
	public static boolean check(Switch<Boolean> emfSwitch, EObject... eObjects) {
		for (EObject eObject : eObjects) {
			if (doSwitchBoolean(emfSwitch, eObject)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks the given objects with the given EMF switch, and returns <code>true</code> as soon as
	 * the {@link Switch#doSwitch(EObject)} method returns true. Otherwise returns false. If an
	 * object is null, returns false.
	 * 
	 * @param emfSwitch
	 *            an EMF switch
	 * @param eObjects
	 *            an iterable of objects
	 * @return a boolean
	 */
	public static boolean check(Switch<Boolean> emfSwitch, Iterable<? extends EObject> eObjects) {
		for (EObject eObject : eObjects) {
			if (doSwitchBoolean(emfSwitch, eObject)) {
				return true;
			}
		}
		return false;
	}

	private static boolean doSwitchBoolean(Switch<Boolean> emfSwitch, EObject eObject) {
		if (eObject == null) {
			return false;
		}
		return emfSwitch.doSwitch(eObject);
	}

	public static Void visit(Switch<Void> emfSwitch, EObject... eObjects) {
		for (EObject eObject : eObjects) {
			if (eObject != null) {
				emfSwitch.doSwitch(eObject);
			}
		}
		return DONE;
	}

	public static Void visit(Switch<Void> emfSwitch, Iterable<? extends EObject> eObjects) {
		for (EObject eObject : eObjects) {
			if (eObject != null) {
				emfSwitch.doSwitch(eObject);
			}
		}
		return DONE;
	}

}
