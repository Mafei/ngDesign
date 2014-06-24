/*******************************************************************************
 * Copyright (c) 2012-2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.core.transformations.impl;

import static com.synflow.models.ir.IrFactory.eINSTANCE;
import static com.synflow.models.util.SwitchUtil.DONE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.util.EcoreUtil;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.synflow.models.ir.BlockBasic;
import com.synflow.models.ir.Def;
import com.synflow.models.ir.InstAssign;
import com.synflow.models.ir.InstLoad;
import com.synflow.models.ir.InstStore;
import com.synflow.models.ir.IrFactory;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Use;
import com.synflow.models.ir.Var;
import com.synflow.models.ir.util.AbstractIrVisitor;
import com.synflow.models.util.Void;

/**
 * This class defines an actor transformation that transforms code so that at most it contains at
 * most one store per variable per cycle.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class StoreOnceTransformation extends AbstractIrVisitor {

	/**
	 * This class removes all loads of global variables.
	 * 
	 * @author Matthieu Wipliez
	 * 
	 */
	private class LoadStoreRemover extends AbstractIrVisitor {

		@Override
		public Void caseInstLoad(InstLoad load) {
			if (!load.getIndexes().isEmpty()) {
				return DONE;
			}

			delete(load);

			return DONE;
		}

		@Override
		public Void caseInstStore(InstStore store) {
			if (!store.getIndexes().isEmpty()) {
				return DONE;
			}

			Var target = store.getTarget().getVariable();
			if (needTransformation(target)) {
				// get local
				Var local = localMap.get(target);

				// add assign
				InstAssign assign = eINSTANCE.createInstAssign(local, store.getValue());
				replace(store, assign);

				// remove this store
				store.getTarget().setVariable(null);
			}

			return DONE;
		}

	}

	/**
	 * This class fills the multiLocalMap table, which maps from each global variable to the local
	 * variables that loads it.
	 * 
	 * @author Matthieu Wipliez
	 * 
	 */
	private class MapFiller extends AbstractIrVisitor {

		/**
		 * mapping from global variable to the local variables that loads it
		 */
		private Multimap<Var, Var> multiLocalMap;

		public MapFiller() {
			multiLocalMap = LinkedHashMultimap.create();
		}

		@Override
		public Void caseInstLoad(InstLoad load) {
			if (load.getIndexes().isEmpty()) {
				Var target = load.getTarget().getVariable();
				Var source = load.getSource().getVariable();

				// registers mapping
				multiLocalMap.put(source, target);
			}

			return DONE;
		}

		@Override
		public Void caseInstStore(InstStore store) {
			if (store.getIndexes().isEmpty()) {
				Var global = store.getTarget().getVariable();
				if (needTransformation(global)) {
					if (!multiLocalMap.containsKey(global)) {
						// adds new local variable
						Var local = IrFactory.eINSTANCE.newTempLocalVariable(procedure, global.getType(), "local_"
								+ global.getName());
						multiLocalMap.put(global, local);
					}
				}
			}

			return DONE;
		}

		/**
		 * Returns the multi map from global variable to local variables.
		 * 
		 * @return the multi map from global variable to local variables
		 */
		public Multimap<Var, Var> getMultiLocalMap() {
			return multiLocalMap;
		}

	}

	/**
	 * This class counts the number of Load/Store of each global variable.
	 * 
	 * @author Matthieu Wipliez
	 * 
	 */
	private class UseCounter extends AbstractIrVisitor {

		@Override
		public Void caseInstLoad(InstLoad load) {
			if (load.getIndexes().isEmpty()) {
				// increase numLoads
				loaded.add(load.getSource().getVariable());
			}

			return DONE;
		}

		@Override
		public Void caseInstStore(InstStore store) {
			if (store.getIndexes().isEmpty()) {
				// increase numStores
				stored.add(store.getTarget().getVariable());
			}

			return DONE;
		}

	}

	/**
	 * mapping from global variable to the number of times it is loaded
	 */
	private Multiset<Var> loaded;

	/**
	 * mapping from global variable to the local variable in which it is stored
	 */
	private Map<Var, Var> localMap;

	/**
	 * mapping from global variable to the number of times it is stored
	 */
	private Multiset<Var> stored;

	/**
	 * Adds load instructions at the beginning of the given procedure.
	 * 
	 * @param procedure
	 *            procedure
	 */
	private void addLoads(Procedure procedure) {
		BlockBasic basic = procedure.getFirst();
		int i = 0;
		for (Entry<Var, Var> entry : localMap.entrySet()) {
			Var global = entry.getKey();
			Var local = entry.getValue();

			InstLoad load = eINSTANCE.createInstLoad(local, global);
			basic.add(i++, load);
		}
	}

	/**
	 * Adds Store instructions at the end of the given procedure. One Store is added for each global
	 * variable referenced in the multiLocalMap map, which is stored more than once, or stored at
	 * least once and loaded at least once.
	 * 
	 * @param procedure
	 *            procedure
	 */
	private void addStores(Procedure procedure) {
		BlockBasic basic = procedure.getLast();
		for (Entry<Var, Var> entry : localMap.entrySet()) {
			Var global = entry.getKey();
			Var local = entry.getValue();

			if (needTransformation(global)) {
				InstStore store = eINSTANCE.createInstStore(global, local);
				basic.add(store);
			}
		}
	}

	@Override
	public Void caseProcedure(Procedure procedure) {
		loaded = HashMultiset.create();
		stored = HashMultiset.create();

		// computes loaded and stored multisets
		new UseCounter().doSwitch(procedure);

		// fills multimap
		MapFiller filler = new MapFiller();
		filler.doSwitch(procedure);

		// removes duplicate local variables
		localMap = removeDuplicates(filler.getMultiLocalMap());

		// removes all loads/stores
		new LoadStoreRemover().doSwitch(procedure);

		if (!localMap.isEmpty()) {
			// if there are at least one local mapping
			// add loads at the beginning of the procedure
			addLoads(procedure);

			// and stores at the bottom of the procedure
			addStores(procedure);
		}

		return DONE;
	}

	/**
	 * Returns <code>true</code> if Load/Store of the given variable should be transformed.
	 * 
	 * @param global
	 *            a global variable
	 * @return <code>true</code> if a transformation is needed
	 */
	private boolean needTransformation(Var global) {
		int numLoads = loaded.count(global);
		int numStores = stored.count(global);

		// at least one store
		// and at least two accesses (two stores or one load and one store)
		return numStores > 0 && numLoads + numStores >= 2;
	}

	private Map<Var, Var> removeDuplicates(Multimap<Var, Var> multiMap) {
		Map<Var, Var> localMap = new LinkedHashMap<Var, Var>();
		for (Var global : multiMap.keySet()) {
			Iterator<Var> it = multiMap.get(global).iterator();
			if (!it.hasNext()) {
				continue;
			}

			Var local = it.next();
			while (it.hasNext()) {
				Var duplicate = it.next();
				for (Def def : new ArrayList<>(duplicate.getDefs())) {
					def.setVariable(local);
				}

				for (Use use : new ArrayList<>(duplicate.getUses())) {
					use.setVariable(local);
				}

				// just remove from container
				EcoreUtil.remove(duplicate);
			}

			localMap.put(global, local);
		}
		return localMap;
	}

}
