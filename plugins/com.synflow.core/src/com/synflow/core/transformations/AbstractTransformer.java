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
package com.synflow.core.transformations;

import static com.synflow.models.util.EcoreHelper.getContainerOfType;
import static com.synflow.models.util.SwitchUtil.DONE;
import static org.eclipse.emf.ecore.util.EcoreUtil.ExternalCrossReferencer.find;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.synflow.models.dpn.Actor;
import com.synflow.models.dpn.DPN;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Unit;
import com.synflow.models.dpn.util.DpnSwitch;
import com.synflow.models.graph.Vertex;
import com.synflow.models.graph.visit.Ordering;
import com.synflow.models.graph.visit.ReversePostOrder;
import com.synflow.models.ir.Procedure;
import com.synflow.models.ir.Var;
import com.synflow.models.util.Void;

/**
 * This class defines an abstract code transformer.
 * 
 * @author Matthieu Wipliez
 * 
 */
public abstract class AbstractTransformer extends DpnSwitch<Void> {

	/**
	 * Computes the import list of the given entity, and adds it to the "imports" attribute of the
	 * template data of the given entity.
	 * 
	 * @param entity
	 *            an entity
	 */
	private static void computeImportList(Entity entity) {
		JsonObject imports = new JsonObject();
		entity.getProperties().add("imports", imports);

		Map<EObject, Collection<Setting>> crossRefs = find(entity);
		for (Collection<Setting> settings : crossRefs.values()) {
			for (Setting setting : settings) {
				Object object = setting.get(true);
				if (object instanceof EObject) {
					EObject eObject = (EObject) object;
					Unit unit = getContainerOfType(eObject, Unit.class);
					if (unit == null) {
						continue;
					}

					JsonArray array = imports.getAsJsonArray(unit.getName());
					if (array == null) {
						array = new JsonArray();
						imports.add(unit.getName(), array);
					}
					String name;
					if (eObject instanceof Var) {
						name = ((Var) eObject).getName();
					} else if (eObject instanceof Procedure) {
						name = ((Procedure) eObject).getName();
					} else {
						throw new IllegalArgumentException("unexpected eObject type: " + eObject);
					}
					array.add(new JsonPrimitive(name));
				}
			}
		}
	}

	@Override
	public Void caseActor(Actor actor) {
		computeImportList(actor);

		return DONE;
	}

	@Override
	public Void caseDPN(DPN network) {
		computeImportList(network);

		// sort vertices of network
		sortVertices(network);

		return DONE;
	}

	@Override
	public Void caseUnit(Unit unit) {
		computeImportList(unit);

		return DONE;
	}

	/**
	 * Computes and returns the list of entries of the given network. The entries are all
	 * (connected) vertices with no incoming connections.
	 * 
	 * @param dpn
	 *            a dpn
	 * @return the list of entries
	 */
	private List<Vertex> getEntries(DPN dpn) {
		return Arrays.asList(dpn.getVertex());
	}

	/**
	 * Sorts the given vertices with the given ordering.
	 * 
	 * @param vertices
	 *            a list of vertices (typically, network.getVertices())
	 * @param ordering
	 *            an ordering (like DFS or post-order)
	 */
	protected void sort(EList<Vertex> vertices, Ordering ordering) {
		final Map<Vertex, Integer> position = new HashMap<Vertex, Integer>();
		int i = 0;
		for (Vertex vertex : ordering) {
			position.put(vertex, i);
			i++;
		}

		// sorts vertices according to topological order
		// vertices not in the "position" map are not sorted
		ECollections.sort(vertices, new Comparator<Vertex>() {
			@Override
			public int compare(Vertex o1, Vertex o2) {
				Integer p1 = position.get(o1);
				if (p1 == null) {
					p1 = 0;
				}

				Integer p2 = position.get(o2);
				if (p2 == null) {
					p2 = 0;
				}
				return p1.compareTo(p2);
			}
		});
	}

	/**
	 * Sorts vertices of the given network. This implementation uses topological order, but
	 * subclasses are free to override to use a different algorithm.
	 * 
	 * @param dpn
	 *            a network
	 */
	protected void sortVertices(DPN dpn) {
		// compute the list of entries
		// which contains input ports with outgoing connections
		List<Vertex> entries = getEntries(dpn);
		if (!entries.isEmpty()) {
			Ordering ordering = new ReversePostOrder(dpn.getGraph(), entries);
			sort(dpn.getGraph().getVertices(), ordering);
		}
	}

}
