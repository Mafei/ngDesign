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
package com.synflow.cx.scoping;

import static com.synflow.cx.CxConstants.DIR_IN;
import static com.synflow.cx.CxConstants.DIR_OUT;
import static com.synflow.cx.CxConstants.PROP_AVAILABLE;
import static com.synflow.cx.CxConstants.PROP_READ;
import static com.synflow.cx.CxConstants.TYPE_READS;
import static org.eclipse.xtext.EcoreUtil2.getContainerOfType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.Scopes;
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider;
import org.eclipse.xtext.scoping.impl.SimpleScope;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.Block;
import com.synflow.cx.cx.Bundle;
import com.synflow.cx.cx.Connect;
import com.synflow.cx.cx.ExpressionVariable;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Instantiable;
import com.synflow.cx.cx.Module;
import com.synflow.cx.cx.Network;
import com.synflow.cx.cx.PortDecl;
import com.synflow.cx.cx.Statement;
import com.synflow.cx.cx.StatementLoop;
import com.synflow.cx.cx.StatementVariable;
import com.synflow.cx.cx.Task;
import com.synflow.cx.cx.VarRef;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.cx.CxPackage.Literals;

/**
 * This class contains custom scoping description.
 * 
 */
public class CxScopeProvider extends AbstractDeclarativeScopeProvider {

	private void addLocalVars(List<Variable> variables, List<Variable> decls) {
		ListIterator<Variable> it = decls.listIterator(decls.size());
		while (it.hasPrevious()) {
			variables.add(it.previous());
		}
	}

	private Iterable<IEObjectDescription> getAllPortDescs(Network network, String direction) {
		Iterable<IEObjectDescription> descriptions = getPortDescs(network, direction);

		// invert direction when referencing ports in instances
		direction = direction == DIR_IN ? DIR_OUT : DIR_IN;
		for (final Inst inst : network.getInstances()) {
			descriptions = Iterables.concat(descriptions, getPortDescs(inst, direction));
		}
		return descriptions;
	}

	private Iterable<? extends IEObjectDescription> getPortDescs(final Inst inst, String direction) {
		Iterable<PortDecl> portDecls;
		Task task = inst.getTask();
		if (task == null) {
			Instantiable entity = inst.getEntity();
			if (entity == null) {
				return ImmutableSet.of();
			}
			portDecls = entity.getPortDecls();
		} else {
			portDecls = task.getPortDecls();
		}

		// names are computed as "instance.port"
		Iterable<Variable> ports = CxUtil.getPorts(portDecls, direction);
		return Iterables.transform(ports, new Function<Variable, IEObjectDescription>() {
			@Override
			public IEObjectDescription apply(Variable port) {
				QualifiedName name = QualifiedName.create(inst.getName(), port.getName());
				return new EObjectDescription(name, port, null);
			}
		});
	}

	private Iterable<IEObjectDescription> getPortDescs(Instantiable entity, String direction) {
		Iterable<Variable> ports = CxUtil.getPorts(entity.getPortDecls(), direction);
		return Iterables.transform(ports, new Function<Variable, IEObjectDescription>() {
			@Override
			public IEObjectDescription apply(Variable port) {
				QualifiedName name = QualifiedName.create(port.getName());
				return new EObjectDescription(name, port, null);
			}
		});
	}

	/**
	 * Returns the scope for ports.
	 * 
	 * @param task
	 *            task from which we are access a port
	 * @param direction
	 *            direction of ports in this task or in the parent design (if any)
	 * @return a scope
	 */
	private IScope getScopePorts(Task task, String direction) {
		Iterable<IEObjectDescription> descriptions = getPortDescs(task, direction);
		Network network = getContainerOfType(task, Network.class);
		if (network != null) {
			// network may be null if the task is not contained in an instance
			descriptions = Iterables.concat(descriptions, getAllPortDescs(network, direction));
		}
		return new SimpleScope(IScope.NULLSCOPE, descriptions);
	}

	/**
	 * Returns the scope for a variable referenced inside a bundle. Returns the scope of global
	 * variables.
	 */
	public IScope scope_VarRef_variable(Bundle bundle, EReference reference) {
		Iterable<Variable> variables = CxUtil.getStateVars(bundle.getDecls());
		IScope outer = delegateGetScope(bundle, reference);
		return Scopes.scopeFor(variables, outer);
	}

	/**
	 * Returns the scope for a variable referenced inside a task. Returns the scope of global
	 * variables.
	 */
	public IScope scope_VarRef_variable(Module module, EReference reference) {
		return delegateGetScope(module, reference);
	}

	/**
	 * Returns the scope for a variable referenced inside a network.
	 */
	public IScope scope_VarRef_variable(Network network, EReference reference) {
		return delegateGetScope(network, reference);
	}

	/**
	 * Returns the scope for a variable referenced inside a statement.
	 */
	public IScope scope_VarRef_variable(Statement statement, EReference reference) {
		// finds all variables declared (from inner to outer)
		List<Variable> variables = new ArrayList<Variable>();

		EObject cter = statement.eContainer();
		if (cter instanceof Block) {
			Block block = (Block) cter;
			List<Statement> stmts = block.getStmts();
			int upto = ECollections.indexOf(stmts, statement, 0);
			ListIterator<Statement> it = stmts.listIterator(upto);
			while (it.hasPrevious()) {
				Statement stmt = it.previous();
				if (stmt instanceof StatementVariable) {
					StatementVariable stmtVar = (StatementVariable) stmt;
					addLocalVars(variables, stmtVar.getVariables());
				} else if (stmt instanceof StatementLoop) {
					StatementLoop loop = (StatementLoop) stmt;
					Statement init = loop.getInit();
					if (init instanceof StatementVariable) {
						StatementVariable stmtVar = (StatementVariable) init;
						addLocalVars(variables, stmtVar.getVariables());
					}
				}
			}
		}

		// build scope (from outer to inner)
		IScope outer = getScope(statement.eContainer(), reference);
		ListIterator<Variable> it = variables.listIterator(variables.size());
		while (it.hasPrevious()) {
			Variable inner = it.previous();
			outer = Scopes.scopeFor(Collections.singleton(inner), outer);
		}
		return outer;
	}

	/**
	 * Returns the scope for a variable referenced inside a statement.
	 */
	public IScope scope_VarRef_variable(StatementLoop loop, EReference reference) {
		IScope outer = getScope(loop.eContainer(), reference);

		Statement init = loop.getInit();
		if (init instanceof StatementVariable) {
			StatementVariable stmtVar = (StatementVariable) init;
			return Scopes.scopeFor(stmtVar.getVariables(), outer);
		}
		return outer;
	}

	/**
	 * Returns the scope for a variable referenced inside a task. Returns the scope of global
	 * variables.
	 */
	public IScope scope_VarRef_variable(Task task, EReference reference) {
		Iterable<Variable> variables = CxUtil.getStateVars(task.getDecls());
		IScope outer = delegateGetScope(task, reference);
		return Scopes.scopeFor(variables, outer);
	}

	/**
	 * Returns the scope for a variable referenced inside a statement.
	 */
	public IScope scope_VarRef_variable(Variable function, EReference reference) {
		IScope outer = getScope(function.eContainer(), reference);
		return Scopes.scopeFor(function.getParameters(), outer);
	}

	/**
	 * Returns the scope for a variable referenced inside an expression. If used with the
	 * 'available' or 'read' property, returns the scope of input ports. Otherwise, resolves the
	 * scope with the expression's container.
	 */
	public IScope scope_VarRef_variable(VarRef ref, EReference reference) {
		EObject cter = ref.eContainer();
		EStructuralFeature feature = ref.eContainingFeature();
		if (feature == Literals.EXPRESSION_VARIABLE__SOURCE) {
			ExpressionVariable expr = (ExpressionVariable) ref.eContainer();
			String prop = expr.getProperty();
			if (PROP_AVAILABLE.equals(prop) || PROP_READ.equals(prop)) {
				// variable referenced is an input port
				Task task = getContainerOfType(expr, Task.class);
				return getScopePorts(task, DIR_IN);
			}

			return super.getScope(expr, reference);
		} else if (feature == Literals.STATEMENT_WRITE__PORT) {
			Task task = getContainerOfType(cter, Task.class);
			return getScopePorts(task, DIR_OUT);
		} else if (feature == Literals.CONNECT__PORTS) {
			Connect connect = (Connect) cter;
			Network network = (Network) connect.eContainer();
			String direction = TYPE_READS.equals(connect.getType()) ? DIR_IN : DIR_OUT;
			Iterable<IEObjectDescription> descriptions = getAllPortDescs(network, direction);
			return new SimpleScope(IScope.NULLSCOPE, descriptions);
		}

		return super.getScope(cter, reference);
	}

}
