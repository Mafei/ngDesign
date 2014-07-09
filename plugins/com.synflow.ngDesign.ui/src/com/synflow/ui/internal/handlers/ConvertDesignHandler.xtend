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
package com.synflow.ui.internal.handlers;

import com.google.common.collect.ArrayListMultimap
import com.google.inject.Inject
import com.google.inject.name.Named
import com.synflow.core.IFileWriter
import com.synflow.core.ISynflowConstants
import com.synflow.core.SynflowCore
import com.synflow.models.util.DomUtil
import com.synflow.models.util.NodeIterable
import com.synflow.models.util.SimpleNamespaceContext
import java.util.ArrayList
import java.util.List
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.core.commands.ExecutionException
import org.eclipse.core.resources.IContainer
import org.eclipse.core.resources.IFile
import org.eclipse.core.runtime.Path
import org.eclipse.emf.common.util.URI
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList

/**
 * This class defines a handler for the "convert design" command.
 * 
 * @author Matthieu Wipliez
 * 
 */
class ConvertDesignHandler extends CommonHandler {
	
	IContainer folder
	
	List<String> vertices

	@Inject
	@Named("Eclipse")
	IFileWriter writer

	override execute(ExecutionEvent event) throws ExecutionException {
		val file = getFile(event.getApplicationContext())
		if (file == null) {
			return null
		}

		folder = file.parent
		val project = file.project

		// creates writer
		writer.setOutputFolder(project.name)

		val is = file.contents
		val doc = DomUtil.parseDocument(is)

		val seq = getNetwork(file, doc)
		val path = file.fullPath.removeFirstSegments(1).removeFileExtension.addFileExtension(ISynflowConstants.FILE_EXT_CFLOW).toString
		writer.write(path, seq)

		null
	}

	def private getClassName(XPath xpath, Element vertex) {
		val href = xpath.evaluate('source/@href', vertex)
		val uri = URI.createURI(href).trimFragment
		val file = folder.getFile(new Path(uri.toString))
		file.fullPath.removeFirstSegments(2).removeFileExtension.toString.replace('/', '.')
	}

	def private getNetwork(IFile file, Document document) {
		val name = file.fullPath.removeFileExtension.lastSegment
		val pack = SynflowCore.getPackage(folder.fullPath)

		val xpath = XPathFactory.newInstance.newXPath
		xpath.setNamespaceContext(new SimpleNamespaceContext(document))
		val design = xpath.evaluate('/xmi:XMI/com.synflow.core.model:Design', document, XPathConstants.NODE) as Element
		val instances = new ArrayList
		val ports = new ArrayList
		visitVertices(instances, ports, xpath, design)
		
		val reads = new ArrayList
		visitConnections(reads, xpath, design)

		'''
		package «pack»;

		network «name»() {
			«IF !ports.empty»port «ports.join(', ')»;«ENDIF»

			«instances.join('\n')»

			«reads.join('\n')»
		}
		'''
	}
	
	def private getVertex(String name) {
		if (!name.empty) {
			val index = Integer.parseInt(name.substring(name.indexOf('.') + 1))
			vertices.get(index)
		}
	}

	def private visitConnections(List<CharSequence> reads, XPath xpath, Element design) {
		val readMap = ArrayListMultimap.create
		val nodes = xpath.evaluate('edges', design, XPathConstants.NODESET) as NodeList
		for (node : new NodeIterable(nodes)) {
			val edge = node as Element
			val source = getVertex(edge.getAttribute('source'))
			val sourcePort = edge.getAttribute('sourcePort')
			val target = getVertex(edge.getAttribute('target'))
			val targetPort = edge.getAttribute('targetPort')

			if (targetPort.empty) {
				// sourcePort can never be null here because direct connections from input ports
				// to output ports are not allowed
				readMap.put('this', '''«source».«sourcePort»''')
			} else {
				readMap.put(target, '''«source»«IF !sourcePort.empty».«sourcePort»«ENDIF»''')
			}
		}

		readMap.keySet.forEach[key|reads.add('''«key».reads(«readMap.get(key).join(', ')»);''')]
	}

	def private visitVertices(List<CharSequence> instances, List<CharSequence> ports, XPath xpath, Element design) {
		vertices = new ArrayList
		val nodes = xpath.evaluate('vertices', design, XPathConstants.NODESET) as NodeList
		for (node : new NodeIterable(nodes)) {
			val vertex = node as Element
			val name = vertex.getAttribute('label')

			val xsiType = vertex.getAttribute("xsi:type")
			if ("com.synflow.core.model:Reference".equals(xsiType)) {
				val className = vertex.getAttribute('className')
				val qName = if (className.empty) getClassName(xpath, vertex) else className
	
				val propClocks = new ArrayList
				val clocks = xpath.evaluate('clocks', vertex, XPathConstants.NODESET) as NodeList
				for (clock : new NodeIterable(clocks)) {
					propClocks += clock
				}
	
				val prop = if (propClocks.size > 1) '''[«propClocks.join(', ')»]'''
				instances += '''«name» = new «qName»(«prop»);'''
			} else if ('net.sf.orcc.df:Port'.equals(xsiType)) {
				val direction = xpath.evaluate("attributes[@name='direction']/@stringValue", vertex)
				ports += '''«direction» «name»_t «name»'''
			}

			vertices.add(name)
		}
	}

}
