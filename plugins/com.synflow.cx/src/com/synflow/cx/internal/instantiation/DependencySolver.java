/*******************************************************************************
 * Copyright (c) 2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthieu Wipliez - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.synflow.cx.internal.instantiation;

import static com.synflow.models.util.SwitchUtil.DONE;
import static com.synflow.models.util.SwitchUtil.visit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.google.common.collect.Iterables;
import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.CxType;
import com.synflow.cx.cx.ExpressionVariable;
import com.synflow.cx.cx.TypeRef;
import com.synflow.cx.cx.Typedef;
import com.synflow.cx.cx.VarRef;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.internal.services.VoidCxSwitch;
import com.synflow.models.graph.Edge;
import com.synflow.models.graph.Graph;
import com.synflow.models.graph.GraphFactory;
import com.synflow.models.graph.Vertex;
import com.synflow.models.graph.visit.DFS;
import com.synflow.models.graph.visit.Finder;
import com.synflow.models.util.Void;

/**
 * This class defines a solver of dependency between variables.
 * 
 * @author Matthieu Wipliez
 *
 */
public class DependencySolver extends VoidCxSwitch {

	private static class VertexAdapter extends AdapterImpl {

		private final EObject contents;

		public VertexAdapter(EObject contents) {
			Objects.requireNonNull(contents);
			this.contents = contents;
		}

		@SuppressWarnings("unchecked")
		public <T extends EObject> T getContent() {
			return (T) contents;
		}

		@Override
		public boolean isAdapterForType(Object type) {
			return type == getClass();
		}

		@Override
		public String toString() {
			return contents.toString();
		}

	}

	private static VertexAdapter getVertexAdapter(EObject eObject) {
		return (VertexAdapter) EcoreUtil.getAdapter(eObject.eAdapters(), VertexAdapter.class);
	}

	private Vertex declaration;

	private List<EObject> eObjects;

	private Graph graph;

	public DependencySolver() {
		eObjects = new ArrayList<>();
	}

	public void add(Variable variable) {
		eObjects.add(variable);
	}

	public void addAll(Iterable<? extends EObject> iterable) {
		Iterables.addAll(eObjects, iterable);
	}

	@Override
	public Void caseExpressionVariable(ExpressionVariable expr) {
		super.caseExpressionVariable(expr);
		return visit(this, expr.getSource());
	}

	@Override
	public Void caseTypeRef(TypeRef ref) {
		Typedef typedef = ref.getTypeDef();
		return handle(typedef);
	}

	@Override
	public Void caseVariable(Variable variable) {
		CxType type = CxUtil.getType(variable);
		visit(this, type);
		visit(this, variable.getDimensions());
		return visit(this, variable.getValue());
	}

	@Override
	public Void caseVarRef(VarRef ref) {
		Variable variable = ref.getVariable();
		return handle(variable);
	}

	public void computeOrder() {
		graph = GraphFactory.eINSTANCE.createGraph();
		for (EObject eObject : eObjects) {
			Vertex vertex = GraphFactory.eINSTANCE.createVertex();
			vertex.eAdapters().add(new VertexAdapter(eObject));
			eObject.eAdapters().add(new VertexAdapter(vertex));
			graph.add(vertex);
		}

		for (EObject eObject : eObjects) {
			declaration = getVertexAdapter(eObject).getContent();
			visit(this, eObject);
		}
	}

	public Iterable<EObject> getObjects() {
		DFS dfs = new DFS(graph.getVertices().size());
		for (Vertex vertex : Finder.findFirst(graph)) {
			dfs.visitPre(vertex);
		}

		List<EObject> ordered = new ArrayList<>(dfs.getVertices().size());
		for (Vertex vertex : dfs.getVertices()) {
			ordered.add(getVertexAdapter(vertex).getContent());
		}

		// remove adapters from objects
		// very important in the case of bundles, whose constants are used in many places
		for (EObject eObject : eObjects) {
			VertexAdapter adapter = getVertexAdapter(eObject);
			eObject.eAdapters().remove(adapter);
		}

		return ordered;
	}

	private Void handle(EObject eObject) {
		VertexAdapter adapter = getVertexAdapter(eObject);
		if (adapter == null) {
			// type not in current entity
			return DONE;
		}

		Edge edge = GraphFactory.eINSTANCE.createEdge();

		Vertex vertex = adapter.getContent();
		edge.setSource(vertex);
		edge.setTarget(declaration);
		graph.add(edge);
		return DONE;
	}

}
