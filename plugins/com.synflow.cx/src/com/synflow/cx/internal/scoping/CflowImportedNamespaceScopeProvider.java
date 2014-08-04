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
package com.synflow.cx.internal.scoping;

import static org.eclipse.xtext.nodemodel.util.NodeModelUtils.findNodesForFeature;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;
import org.eclipse.xtext.scoping.impl.ImportedNamespaceAwareLocalScopeProvider;

import com.google.common.collect.Lists;
import com.synflow.cx.cx.CxEntity;
import com.synflow.cx.cx.Import;
import com.synflow.cx.cx.Imported;
import com.synflow.cx.cx.Module;
import com.synflow.cx.cx.CflowPackage.Literals;

/**
 * This class defines a simple namespace aware scope provider for Java-like imports in C~.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class CflowImportedNamespaceScopeProvider extends ImportedNamespaceAwareLocalScopeProvider {

	/**
	 * Creates a new imported namespace resolver and adds it to the normalizers list.
	 * 
	 * @param normalizers
	 *            a list of normalizers
	 * @param name
	 *            name
	 * @param ignoreCase
	 *            case (in)sensitive flag
	 */
	private void addImportNormalizer(List<ImportNormalizer> normalizers, String name,
			boolean ignoreCase) {
		ImportNormalizer normalizer = createImportedNamespaceResolver(name, ignoreCase);
		if (normalizer != null) {
			normalizers.add(normalizer);
		}
	}

	@Override
	protected ImportNormalizer doCreateImportNormalizer(QualifiedName importedNamespace,
			boolean wildcard, boolean ignoreCase) {
		return new CflowImportNormalizer(importedNamespace, wildcard);
	}

	/**
	 * Returns a list of import normalizers.
	 * 
	 * @param imports
	 *            a list of import directives
	 * @param ignoreCase
	 *            case (in)sensitive flag
	 * @return
	 */
	private List<ImportNormalizer> getImportNormalizers(EList<Import> imports, boolean ignoreCase) {
		List<ImportNormalizer> resolvers = Lists.newArrayList();
		for (Import imp : imports) {
			for (Imported imported : imp.getImported()) {
				String name = getName(imported);
				if (imported.isWildcard()) {
					addImportNormalizer(resolvers, name + ".*", ignoreCase);
				} else {
					addImportNormalizer(resolvers, name, ignoreCase);
				}
			}
		}
		return resolvers;
	}

	/**
	 * Returns the name of the given object.
	 * 
	 * @param to
	 *            an object
	 * @return a name
	 */
	private String getName(Imported imp) {
		List<INode> nodes = findNodesForFeature(imp, Literals.IMPORTED__TYPE);
		if (!nodes.isEmpty()) {
			INode node = nodes.get(0);
			return NodeModelUtils.getTokenText(node);
		}
		return null;
	}

	@Override
	protected List<ImportNormalizer> internalGetImportedNamespaceResolvers(final EObject context,
			boolean ignoreCase) {
		if (context instanceof Module) {
			Module module = (Module) context;
			return getImportNormalizers(module.getImports(), ignoreCase);
		} else if (context instanceof CxEntity) {
			CxEntity entity = (CxEntity) context;
			return getImportNormalizers(entity.getImports(), ignoreCase);
		} else {
			return Collections.emptyList();
		}
	}

}
