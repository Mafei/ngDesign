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
package com.synflow.core.internal.exporters

import com.synflow.core.IExportConfiguration.Target
import java.io.File
import org.eclipse.core.runtime.Path

import static com.synflow.core.ISynflowConstants.*

/**
 * This class defines a generator of Quartus II project file.
 * 
 * @author Matthieu Wipliez
 * 
 */
class QuartusExporter extends Exporter {

	/**
	 * Exports to Altera .qpf and .qsf files.
	 */
	override doExport() {
		val name = entity.simpleName
		val path = new Path(FOLDER_PROJECTS).append(name).toString
		setParameters(path, Target.SYNTHESIS)

		writer.write(path + "/" + name + ".qpf", printQpf)
		writer.write(path + "/" + name + ".qsf", printQsf)
	}

	def private printQpf()
		'''
		QUARTUS_VERSION = "11.1"
		DATE = "«date»"

		# Revisions

		PROJECT_REVISION = "«entity.simpleName»"
		'''

	def private printQsf() {
		'''
		# Generated from «entity.simpleName»
		set_global_assignment -name FAMILY "Cyclone IV GX"
		set_global_assignment -name DEVICE AUTO
		set_global_assignment -name TOP_LEVEL_ENTITY «entity.simpleName»
		set_global_assignment -name ORIGINAL_QUARTUS_VERSION 12.0
		set_global_assignment -name PROJECT_CREATION_TIME_DATE "«date»"
		set_global_assignment -name LAST_QUARTUS_VERSION 12.0

		# Source files
		«FOR path : paths»
		«printAssignment(path)»
		«ENDFOR»

		set_global_assignment -name SEARCH_PATH "«includePath.join(File.pathSeparator)»"
		'''
	}

	def private printAssignment(String path)
		'''
		set_global_assignment -name «language.toUpperCase»_FILE «path»
		'''

}
