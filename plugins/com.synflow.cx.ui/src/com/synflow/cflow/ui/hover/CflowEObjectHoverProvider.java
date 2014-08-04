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
package com.synflow.cflow.ui.hover;

import static com.synflow.cflow.CflowConstants.DIR_IN;
import static com.synflow.cflow.CflowConstants.DIR_OUT;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.Switch;
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.synflow.cflow.CflowUtil;
import com.synflow.cflow.cflow.CExpression;
import com.synflow.cflow.cflow.CType;
import com.synflow.cflow.cflow.ExpressionInteger;
import com.synflow.cflow.cflow.Inst;
import com.synflow.cflow.cflow.Instantiable;
import com.synflow.cflow.cflow.Network;
import com.synflow.cflow.cflow.Task;
import com.synflow.cflow.cflow.Typedef;
import com.synflow.cflow.cflow.Variable;
import com.synflow.cflow.cflow.util.CflowSwitch;
import com.synflow.cflow.services.Evaluator;
import com.synflow.models.dpn.InterfaceType;
import com.synflow.models.ir.util.ValueUtil;

/**
 * This class extends the default hover provider to provide additional details, concerning ports or
 * state variables.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CflowEObjectHoverProvider extends DefaultEObjectHoverProvider {

	private class HoverSwitch extends CflowSwitch<String> {

		private Function<Variable, String> toStringPort = new Function<Variable, String>() {
			@Override
			public String apply(Variable port) {
				String repr = doSwitch(port);
				int index = repr.indexOf(' ');
				return repr.substring(index + 1);
			}
		};

		@Override
		public String caseExpressionInteger(ExpressionInteger expr) {
			return printValue(expr.getValue());
		}

		@Override
		public String caseInstantiable(Instantiable entity) {
			Iterable<Variable> portsIn = CflowUtil.getPorts(entity.getPortDecls(), DIR_IN);
			Iterable<Variable> portsOut = CflowUtil.getPorts(entity.getPortDecls(), DIR_OUT);
			Iterable<String> itIn = Iterables.transform(portsIn, toStringPort);
			Iterable<String> itOut = Iterables.transform(portsOut, toStringPort);

			String name = entity.getName();
			if (name == null) {
				EObject cter = entity.eContainer();
				if (cter instanceof Inst) {
					Inst inst = (Inst) cter;
					name = inst.getName();
				} else {
					name = "anonymous";
				}
			}

			StringBuilder builder = new StringBuilder(name);
			builder.append(" {<br/>");
			if (!Iterables.isEmpty(itIn)) {
				builder.append("&nbsp;&nbsp;" + DIR_IN + " ");
				Joiner.on(", ").appendTo(builder, itIn);
				builder.append(";<br/>");
			}

			if (!Iterables.isEmpty(itOut)) {
				builder.append("&nbsp;&nbsp;" + DIR_OUT + " ");
				Joiner.on(", ").appendTo(builder, itOut);
				builder.append(";<br/>");
			}

			builder.append("}");
			return builder.toString();
		}

		@Override
		public String caseNetwork(Network network) {
			return "network " + caseInstantiable(network);
		}

		@Override
		public String caseTask(Task task) {
			return "task " + caseInstantiable(task);
		}

		@Override
		public String caseTypedef(Typedef type) {
			return "typedef " + getLabel(type.getType()) + " " + type.getName();
		}

		@Override
		public String caseVariable(Variable variable) {
			StringBuilder builder = new StringBuilder();

			// constant
			if (CflowUtil.isConstant(variable)) {
				builder.append("const ");
			}

			// if port, prepends with port/direction
			if (CflowUtil.isPort(variable)) {
				String dir = CflowUtil.getDirection(variable);
				builder.append(dir);
				builder.append(' ');

				InterfaceType iface = CflowUtil.getInterface(variable);
				if (iface.isSyncAck()) {
					builder.append("sync ack");
				} else if (iface.isSyncReady()) {
					builder.append("sync ready");
				} else if (iface.isSync()) {
					builder.append("sync");
				}
				builder.append(' ');
			}

			// type and name
			CType type = CflowUtil.getType(variable);
			if (type == null) {
				builder.append("void");
			} else {
				builder.append(getLabel(type));
			}
			builder.append(" ");
			builder.append(variable.getName());

			if (CflowUtil.isFunction(variable)) {
				// parameters
				builder.append('(');
				Iterator<Variable> it = variable.getParameters().iterator();
				if (it.hasNext()) {
					builder.append(doSwitch(it.next()));
					while (it.hasNext()) {
						builder.append(", ");
						builder.append(doSwitch(it.next()));
					}
				}
				builder.append(')');
			} else {
				// dimensions
				for (CExpression value : variable.getDimensions()) {
					builder.append('[');
					builder.append(String.valueOf(Evaluator.getValue(value)));
					builder.append(']');
				}
			}

			// if state variable, append with value (if any)
			if (CflowUtil.isVarDecl(variable)) {
				EObject value = variable.getValue();
				if (value != null) {
					builder.append(" = ");
					builder.append(printValue(Evaluator.getValue(value)));
				}
			}

			return builder.toString();
		}

		@Override
		public String defaultCase(EObject o) {
			return CflowEObjectHoverProvider.super.getFirstLine(o);
		}

	}

	private final Switch<String> hoverSwitch = new HoverSwitch();

	@Override
	protected String getFirstLine(EObject o) {
		return "<b>" + hoverSwitch.doSwitch(o) + "</b>";
	}

	@Override
	protected boolean hasHover(EObject o) {
		return o instanceof ExpressionInteger || super.hasHover(o);
	}

	private String printValue(Object value) {
		if (value == null) {
			return "?";
		} else if (ValueUtil.isInt(value)) {
			BigInteger i = (BigInteger) value;
			StringBuilder builder = new StringBuilder(i.toString());
			builder.append(" [0x");
			builder.append(i.toString(16).toUpperCase());
			builder.append("]");
			return builder.toString();
		} else if (ValueUtil.isList(value)) {
			int size = Array.getLength(value);
			StringBuilder builder = new StringBuilder();
			builder.append('{');
			if (size > 0) {
				builder.append(printValue(Array.get(value, 0)));
				for (int i = 1; i < size; i++) {
					builder.append(", ");
					builder.append(printValue(Array.get(value, i)));
				}
			}
			builder.append('}');
			return builder.toString();
		} else {
			return value.toString();
		}
	}

}
