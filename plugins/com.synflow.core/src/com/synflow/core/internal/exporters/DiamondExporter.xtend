/*******************************************************************************
 * Copyright (c) 2012-2013 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nicolas Siret - initial API and implementation and/or initial documentation
 *    Matthieu Wipliez - refactoring and misc changes
 *******************************************************************************/
package com.synflow.core.internal.exporters

import java.io.File
import org.eclipse.core.runtime.Path

import static com.synflow.core.ISynflowConstants.*

/**
 * This class defines a generator of Diamond project file.
 * 
 * @author Nicolas Siret
 * 
 */
class DiamondExporter extends Exporter {

	/**
	 * Exports to Diamond .ldf file.
	 */
	override doExport() {
		val name = entity.simpleName
		val path = new Path(FOLDER_PROJECTS).append(name).toString
		setParameters(path, TARGET_SYNTHESIS)

		writer.write(path + "/" + name + ".pty", printXty(""))
		writer.write(path + "/" + name + ".ldf", printLdf)

		preservingWrite(path + "/Strategy1.sty", printXty("Strategy1"))
		preservingWrite(path + "/" + name + ".lpf", printLpf)
		preservingWrite(path + "/" + name + ".xcf", printXcf)
	}

	def private printLdf() {
		'''
		<?xml version="1.0" encoding="UTF-8"?>
		<BaliProject version="2.0" title="«entity.simpleName»" device="LFE3-35EA-8FN484C" default_implementation="«entity.simpleName»">
		    <Options/>
		    <Implementation title="«entity.simpleName»" dir="«entity.simpleName»" description="«entity.simpleName»" default_strategy="Strategy1">
		        <Options>
		            <Option name="include path" value="«includePath.join(File.pathSeparator)»"/>
		        </Options>

		        <!-- Source files -->
		        «FOR path : paths»
		        «printLdfFile(path)»
		        «ENDFOR»
		        <Source name="«entity.simpleName».lpf" type="Logic Preference" type_short="LPF">
		            <Options/>
		        </Source>
		        <Source name="«entity.simpleName».xcf" type="ispVM Download Project" type_short="ispVM">
		            <Options/>
		        </Source>
		    </Implementation>
		    <Strategy name="Strategy1" file="Strategy1.sty"/>  
		</BaliProject>
		'''
	}

	def private printLdfFile(String path)
		'''
		<Source name="«path»" type="«language»" type_short="«language»">
		    <Options/>
		</Source>
		'''

	def private printLpf()
		'''
		BLOCK RESETPATHS ;
		BLOCK ASYNCPATHS ;
		LOCATE COMP "clock" SITE "L5" ;
		LOCATE COMP "reset_n" SITE "A21" ;
		IOBUF PORT "clock" IO_TYPE=LVDS25 ;
		IOBUF PORT "reset_n" IO_TYPE=LVCMOS33 ;
		'''

	def private printXty(String label)
		'''
		<?xml version="1.0" encoding="UTF-8"?>
		<!DOCTYPE strategy>
		<Strategy version="1.0" predefined="0" description="" label="«label»"/>
		'''

	def private printXcf()
		'''
		<?xml version='1.0' encoding='utf-8' ?>
		<!DOCTYPE		ispXCF	SYSTEM	"IspXCF.dtd" >
		<ispXCF version="1.4">
			<Comment></Comment>
			<Chain>
				<Comm>JTAG</Comm>
				<Device>
					<SelectedProg value="TRUE"/>
					<Pos>1</Pos>
					<Vendor>Lattice</Vendor>
					<Family>LatticeECP3</Family>
					<Name>LFE3-35EA</Name>
					<IDCode>0x01012043</IDCode>
					<Package>All</Package>
					<PON>LFE3-35EA</PON>
					<Bypass>
						<InstrLen>8</InstrLen>
						<InstrVal>11111111</InstrVal>
						<BScanLen>1</BScanLen>
						<BScanVal>0</BScanVal>
					</Bypass>
					<File>«entity.simpleName»/«entity.simpleName»_«entity.simpleName».bit</File>
					<FileTime>6/26/2012 22:50:15</FileTime>
					<Operation>Fast Program</Operation>
					<Option>
						<SVFVendor>JTAG STANDARD</SVFVendor>
						<IOState>HighZ</IOState>
						<PreloadLength>675</PreloadLength>
						<IOVectorData>0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF</IOVectorData>
						<OverideUES value="TRUE"/>
						<TCKFrequency>1.000000 MHz</TCKFrequency>
						<SVFProcessor>ispVM</SVFProcessor>
						<Usercode>0x00000000</Usercode>
						<AccessMode>JTAG</AccessMode>
					</Option>
				</Device>
			</Chain>
			<ProjectOptions>
				<Program>SEQUENTIAL</Program>
				<Process>ENTIRED CHAIN</Process>
				<OperationOverride>No Override</OperationOverride>
				<StartTAP>TLR</StartTAP>
				<EndTAP>TLR</EndTAP>
				<VerifyUsercode value="FALSE"/>
			</ProjectOptions>
			<CableOptions>
				<CableName>USB2</CableName>
				<PortAdd>FTUSB-0</PortAdd>
			</CableOptions>
		</ispXCF>
		'''

	/**
	 * Only writes to the given file if it does not already exist.
	 */
	def private preservingWrite(String file, CharSequence seq) {
		if (!writer.exists(file)) {
			writer.write(file, seq)
		}
	}

}
