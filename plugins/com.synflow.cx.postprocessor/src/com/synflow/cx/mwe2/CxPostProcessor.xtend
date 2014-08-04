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
package com.synflow.cx.mwe2

import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage
import org.eclipse.emf.common.util.BasicEMap
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EDataType
import org.eclipse.emf.ecore.EGenericType
import org.eclipse.emf.ecore.EOperation
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.EcoreFactory
import org.eclipse.emf.ecore.EcorePackage.Literals
import org.eclipse.xtext.GeneratedMetamodel

import static org.eclipse.emf.ecore.ETypedElement.*

/**
 * This class defines a post-processor for the Cx model.
 * 
 * @author Matthieu Wipliez
 */
class CxPostProcessor {

	def static augment(GeneratedMetamodel metamodel) {
		new CxPostProcessor().process(metamodel)
	}

	new() {
		System.out.println('running CxPostProcessor v20140804')
	}

	/**
	 * Adds a new data attribute with the given name and type to the given EClass.
	 */
	def private addAttribute(EClass eClass, String name, EDataType type) {
		addAttribute(eClass, name, type, false)
	}

	/**
	 * Adds a new data attribute with the given name, type, and transient flag to the given EClass.
	 */
	def private addAttribute(EClass eClass, String name, EDataType type, boolean isTransient) {
		val attribute = EcoreFactory.eINSTANCE.createEAttribute
		attribute.EType = type
		attribute.name = name
		attribute.transient = isTransient
		eClass.EStructuralFeatures.add(attribute)
		attribute
	}

	/**
	 * Adds a new reference with the given name and type to the given EClass.
	 */
	def private EReference addReference(EClass eClass, String name, EClass type, int upper) {
		val reference = EcoreFactory.eINSTANCE.createEReference
		reference.EType = type
		reference.name = name
		reference.upperBound = upper
		eClass.EStructuralFeatures.add(reference)
		reference
	}

	/**
	 * Adds a new class with the given name and super type.
	 */
	def private EClass addClass(EPackage p, String name) {
		val cls = EcoreFactory.eINSTANCE.createEClass
		cls.name = name
		p.EClassifiers += cls
		cls
	}

	/**
	 * Adds classes to the given package
	 */
	def private addClasses(EPackage p) {
		// first get classes, because adding a class to the package clears its name to class map
		val classCExpression = p.getEClassifier('CExpression') as EClass
		val classVarRef = p.getEClassifier('VarRef') as EClass

		val classEnter = p.addClass('Enter')
		classEnter.addReference('function', classVarRef, 1)
		classEnter.addAttribute('lineNumber', Literals.EINT)
		classEnter.addReference('parameters', classCExpression, UNBOUNDED_MULTIPLICITY)

		p.addClass('Leave')
	}

	/**
	 * This method adds data types (ErrorMarker and List) to the given package.
	 */
	def private addDataTypes(EPackage p) {
		// adds type for java.util.List
		val typeList = EcoreFactory.eINSTANCE.createEDataType
		typeList.instanceClassName = 'java.util.List'
		typeList.name = 'EList'
		
		val typeParam = EcoreFactory.eINSTANCE.createETypeParameter
		typeParam.name = 'T'
		typeList.ETypeParameters += typeParam

		p.EClassifiers += typeList

		// adds type for com.synflow.cx.internal.ErrorMarker
		val typeErrorMarker = EcoreFactory.eINSTANCE.createEDataType
		typeErrorMarker.instanceClassName = 'com.synflow.cx.internal.ErrorMarker'
		typeErrorMarker.name = 'ErrorMarker'
		p.EClassifiers += typeErrorMarker
	}

	/**
	 * Adds an operation to the eClass.
	 */
	def private EOperation addOperation(EClass eClass, String name, EGenericType returnType) {
		val op = EcoreFactory.eINSTANCE.createEOperation
		op.name = name
		op.EGenericType = returnType

		eClass.EOperations += op
		op
	}

	/**
	 * Visits the generated meta-model.
	 */
	def private process(GeneratedMetamodel metamodel) {
		metamodel.EPackage.visit
	}

	def private visit(EPackage p) {
		// adds 'initialized' and 'used' boolean attribute flags
		val eClassVariable = p.getEClassifier('Variable') as EClass
		eClassVariable.addAttribute('initialized', Literals.EBOOLEAN, true)
		eClassVariable.addAttribute('used', Literals.EBOOLEAN, true)

		addClasses(p)
		addDataTypes(p)
		updateInstantiable(p)
	}

	def private void setBody(EOperation op, String contents) {
		val body = EcoreFactory.eINSTANCE.createEAnnotation
		body.source = GenModelPackage.eNS_URI

		val map = EcoreFactory.eINSTANCE.create(Literals.ESTRING_TO_STRING_MAP_ENTRY) as BasicEMap.Entry<String,String>
		map.key = "body"
		map.value = contents

		body.details.add(map)
		op.EAnnotations += body
	}
	
	/**
	 * Updates the Instantiable class by adding an attribute 'internalErrors' and a 'getErrors' method.
	 * 
	 * 'getErrors' initializes internalErrors with a BasicEList so adding errors to the entity
	 * does not cause its containing resource to be considered modified (otherwise this breaks the
	 * instantiator).
	 */
	def private updateInstantiable(EPackage p) {
		val classEntity = p.getEClassifier('Instantiable') as EClass
		val typeErrorMarker = p.getEClassifier('ErrorMarker') as EDataType
		val typeList = p.getEClassifier('EList') as EDataType

		val errors = classEntity.addAttribute('internalErrors', typeErrorMarker, true)
		errors.upperBound = UNBOUNDED_MULTIPLICITY

		// create generic type List<ErrorMarker>
		val typeListErrors = EcoreFactory.eINSTANCE.createEGenericType
		typeListErrors.EClassifier = typeList
		val typeParamMarker = EcoreFactory.eINSTANCE.createEGenericType
		typeParamMarker.EClassifier = typeErrorMarker
		typeListErrors.ETypeArguments += typeParamMarker

		// add 'getErrors' method
		val opGetJson = classEntity.addOperation('getErrors', typeListErrors)
		opGetJson.setBody(
			'''
			if (internalErrors == null) {
				internalErrors = new org.eclipse.emf.common.util.BasicEList<ErrorMarker>();
			}
			return internalErrors;'''
		)
	}

}
