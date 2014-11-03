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
package com.synflow.cx.references;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.findReferences.ReferenceFinder;
import org.eclipse.xtext.findReferences.TargetURIs;

import com.google.inject.Inject;
import com.synflow.cx.CxUtil;
import com.synflow.cx.cx.CxPackage.Literals;
import com.synflow.cx.cx.Inst;
import com.synflow.cx.cx.Instantiable;
import com.synflow.cx.cx.VarRef;
import com.synflow.cx.cx.Variable;
import com.synflow.cx.internal.instantiation.ConnectorHelper;

/**
 * This class extends the default reference finder to find references to ports made on an instance.
 * 
 * @author Matthieu Wipliez
 *
 */
@SuppressWarnings("restriction")
public class CxReferenceFinder extends ReferenceFinder {

	@Inject
	private ConnectorHelper helper;

	@Override
	protected void findLocalReferencesFromElement(TargetURIs targetURIs, EObject sourceCandidate,
			Resource localResource, Acceptor acceptor) {
		URI sourceURI = null;
		if (doProcess(sourceCandidate, targetURIs)) {
			for (EReference ref : sourceCandidate.eClass().getEAllReferences()) {
				if (sourceCandidate.eIsSet(ref)) {
					if (ref.isContainment()) {
						Object content = sourceCandidate.eGet(ref, false);
						if (ref.isMany()) {
							@SuppressWarnings("unchecked")
							InternalEList<EObject> contentList = (InternalEList<EObject>) content;
							for (int i = 0; i < contentList.size(); ++i) {
								EObject childElement = contentList.basicGet(i);
								if (!childElement.eIsProxy()) {
									findLocalReferencesFromElement(targetURIs, childElement,
											localResource, acceptor);
								}
							}
						} else {
							EObject childElement = (EObject) content;
							if (!childElement.eIsProxy()) {
								findLocalReferencesFromElement(targetURIs, childElement,
										localResource, acceptor);
							}
						}
					} else if (!ref.isContainer()) {
						if (doProcess(ref, targetURIs)) {
							Object value = sourceCandidate.eGet(ref, false);
							if (ref.isMany()) {
								@SuppressWarnings("unchecked")
								InternalEList<EObject> values = (InternalEList<EObject>) value;
								for (int i = 0; i < values.size(); ++i) {
									EObject instanceOrProxy = toValidInstanceOrNull(localResource,
											targetURIs, values.basicGet(i));
									if (instanceOrProxy != null) {
										URI refURI = EcoreUtil2
												.getPlatformResourceOrNormalizedURI(instanceOrProxy);
										if (targetURIs.contains(refURI)) {
											sourceURI = (sourceURI == null) ? EcoreUtil2
													.getPlatformResourceOrNormalizedURI(sourceCandidate)
													: sourceURI;
											acceptor.accept(sourceCandidate, sourceURI, ref, i,
													instanceOrProxy, refURI);
										}
									}
								}
							} else {
								EObject instanceOrProxy = toValidInstanceOrNull(localResource,
										targetURIs, (EObject) value);
								if (instanceOrProxy != null) {
									boolean accept = false;
									if (ref == Literals.INST__ENTITY) {
										Inst inst = (Inst) sourceCandidate;
										Instantiable instantiable = inst.getEntity();
										if (instantiable.eResource() != localResource) {
											findLocalReferencesFromElement(targetURIs,
													instantiable, instantiable.eResource(),
													acceptor);
										}
									} else if (ref == Literals.VAR_REF__VARIABLE) {
										Variable variable = (Variable) instanceOrProxy;
										if (CxUtil.isPort(variable)) {
											Inst inst = helper.getInst((VarRef) sourceCandidate);
											if (inst != null) {
												URI instUri = EcoreUtil.getURI(inst);
												if (targetURIs.contains(instUri)) {
													accept = true;
												}
											}
										}
									}

									URI refURI = EcoreUtil2
											.getPlatformResourceOrNormalizedURI(instanceOrProxy);
									if (targetURIs.contains(refURI) || accept) {
										sourceURI = (sourceURI == null) ? EcoreUtil2
												.getPlatformResourceOrNormalizedURI(sourceCandidate)
												: sourceURI;
										acceptor.accept(sourceCandidate, sourceURI, ref, -1,
												instanceOrProxy, refURI);
									}
								}
							}
						}
					}
				}
			}
		}
	}

}
