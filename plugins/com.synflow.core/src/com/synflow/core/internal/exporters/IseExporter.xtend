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

/**
 * This class defines a generator of Quartus II project file.
 * 
 * @author Matthieu Wipliez
 * 
 */
class IseExporter extends Exporter {

	/**
	 * Exports to Xilinx .ise file.
	 */
	override doExport() {
		val name = entity.simpleName
		val path = new Path(FOLDER_PROJECTS).append(name).toString
		setParameters(path, Target.SYNTHESIS)

		writer.write(path + "/" + name + ".xise", printIse)
	}

	def private printFile(String path)
		'''
		<file xil_pn:name="«path»" xil_pn:type="FILE_«language.toUpperCase»">
		  <association xil_pn:name="BehavioralSimulation" xil_pn:seqID="1"/>
		  <association xil_pn:name="Implementation" xil_pn:seqID="1"/>
		</file>
		'''

	def private printIse() {
		'''
		<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
		<project xmlns="http://www.xilinx.com/XMLSchema" xmlns:xil_pn="http://www.xilinx.com/XMLSchema">

		  <header>
		    <!-- ISE source project file created by Synflow Studio.             -->
		    <!--                                                                   -->
		    <!-- This file contains project source information including a list of -->
		    <!-- project source files, project and process properties.  This file, -->
		    <!-- along with the project source files, is sufficient to open and    -->
		    <!-- implement in ISE Project Navigator.                               -->
		    <!--                                                                   -->
		  </header>

		  <version xil_pn:ise_version="14.2" xil_pn:schema_version="2"/>

		  <files>
		    <!-- Source files -->
		    «FOR path : paths»
		    «printFile(path)»
		    «ENDFOR»
		  </files>

		  <properties>
		    <property xil_pn:name="Device" xil_pn:value="xc6vlx75t" xil_pn:valueState="non-default"/>
		    <property xil_pn:name="Evaluation Development Board" xil_pn:value="Virtex 6 ML605 Evaluation Platform" xil_pn:valueState="non-default"/>
		    <property xil_pn:name="Package" xil_pn:value="ff484" xil_pn:valueState="non-default"/>
		    <property xil_pn:name="Device Family" xil_pn:value="Virtex6" xil_pn:valueState="non-default"/>
		    <property xil_pn:name="Speed Grade" xil_pn:value="-1" xil_pn:valueState="non-default"/>

		    <property xil_pn:name="Preferred Language" xil_pn:value="«language»" xil_pn:valueState="non-default"/>
		    <property xil_pn:name="Property Specification in Project File" xil_pn:value="Store non-default values only" xil_pn:valueState="non-default"/>

		    <property xil_pn:name="Implementation Top" xil_pn:value="Architecture|«entity.simpleName»|rtl_«entity.simpleName»" xil_pn:valueState="non-default"/>
		    <property xil_pn:name="Implementation Top File" xil_pn:value="«computePath(entity)»" xil_pn:valueState="non-default"/>
		    <property xil_pn:name="Implementation Top Instance Path" xil_pn:value="/«entity.simpleName»" xil_pn:valueState="non-default"/>
		    
		    <property xil_pn:name="Verilog Include Directories" xil_pn:value="«includePath.join(File.pathSeparator)»" xil_pn:valueState="non-default" xil_pn:x_locked="true"/>
		  </properties>

		  <bindings/>

		  <libraries/>

		  <autoManagedFiles>
		    <!-- The following files are identified by `include statements in verilog -->
		    <!-- source files and are automatically managed by Project Navigator.     -->
		    <!--                                                                      -->
		    <!-- Do not hand-edit this section, as it will be overwritten when the    -->
		    <!-- project is analyzed based on files automatically identified as       -->
		    <!-- include files.                                                       -->
		  </autoManagedFiles>
		</project>
		'''
	}

}
