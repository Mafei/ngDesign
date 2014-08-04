/*******************************************************************************
 * Copyright (c) 2012 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.tests.interpreter;

import com.synflow.cx.tests.AbstractPassTests;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.Entity;

/**
 * This class defines Cx tests that are expected to succeed.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class InterpreterPassTests extends AbstractPassTests {

	/**
	 * Parses, validates, compiles, and interprets the actor defined in the file whose name is
	 * given. The input/output ports must have "stimulus" and "expected" attributes.
	 * 
	 * @param name
	 *            name of a .cf file that contains an entity
	 * @param expected
	 *            expected outcome of the execution: <code>true</code> if pass is expected,
	 *            <code>false</code> if fail is expected
	 */
	protected final void checkEntity(Entity entity, boolean expected) {
		if (!(entity instanceof Actor)) {
			return;
		}

		Actor actor = (Actor) entity;
		if (actor.getActions().isEmpty()) {
			// no actions => nothing to check
			return;
		}

		checkProperties(actor);

		// assert 1) the test was expected to succeed, and it did
		// 2) the test was expected to fail, and it did
		// TODO bring the interpreter back
		//boolean actual = new TestInterpreter(actor).runTest();
		//assertEquals(expected, actual);
	}

}
