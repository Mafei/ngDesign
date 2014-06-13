/*******************************************************************************
 * Copyright (c) 2012-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.generators.vhdl;

import static com.synflow.models.util.SwitchUtil.DONE;

import org.eclipse.emf.ecore.EObject;

import com.synflow.core.transformations.AbstractTransformer;
import com.synflow.core.transformations.BodyTransformation;
import com.synflow.core.transformations.CodeCleaner;
import com.synflow.core.transformations.ModuleTransformation;
import com.synflow.core.transformations.ProcedureTransformation;
import com.synflow.core.transformations.StoreOnceTransformation;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Unit;
import com.synflow.models.ir.transform.PhiRemoval;
import com.synflow.models.ir.transform.SSATransformation;
import com.synflow.models.ir.transform.SSAVariableRenamer;
import com.synflow.models.util.Void;

/**
 * This class defines an abstract generator that uses Xtend for templates.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class VhdlTransformer extends AbstractTransformer {

	@Override
	public Void caseActor(Actor actor) {
		super.caseActor(actor);
		transform(actor);
		return DONE;
	}

	@Override
	public Void caseDPN(DPN network) {
		super.caseDPN(network);
		transform(network);
		return DONE;
	}

	@Override
	public Void caseUnit(Unit unit) {
		super.caseUnit(unit);
		transform(unit);
		return DONE;
	}

	private void transform(EObject eObject) {
		// transformations on the code
		ModuleTransformation[] transformations = {
				// makes sure there is at most one store per variable per cycle
				new BodyTransformation(new StoreOnceTransformation()),

				// cleans up code
				new ProcedureTransformation(new SSATransformation()),
				new ProcedureTransformation(new CodeCleaner()),
				new ProcedureTransformation(new PhiRemoval()),
				new ProcedureTransformation(new SSAVariableRenamer()) };

		// applies transformations
		for (ModuleTransformation transformation : transformations) {
			transformation.doSwitch(eObject);
		}
	}

}
