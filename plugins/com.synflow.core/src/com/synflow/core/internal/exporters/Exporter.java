/*******************************************************************************
 * Copyright (c) 2012-2014 Synflow SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nicolas Siret - initial API
 *    Matthieu Wipliez - implementation and refactoring
 *******************************************************************************/
package com.synflow.core.internal.exporters;

import static com.synflow.core.ISynflowConstants.SUFFIX_GEN;
import static org.eclipse.emf.ecore.util.EcoreUtil.getRootContainer;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.synflow.core.ICodeGenerator;
import com.synflow.core.IExportConfiguration;
import com.synflow.core.IExporter;
import com.synflow.core.IFileWriter;
import com.synflow.core.SynflowCore;
import com.synflow.core.util.CoreUtil;
import com.synflow.models.dpn.Entity;
import com.synflow.models.dpn.Unit;
import com.synflow.models.ir.util.IrUtil;

/**
 * This abstract class defines an Exporter, which exports one or more files from a Network. A
 * concrete implementation of an exporter must implement the doExport method that is responsible of
 * the actual export.
 * 
 * @author Matthieu Wipliez
 * @author Nicolas Siret
 * 
 */
public abstract class Exporter implements IExporter {

	private ICodeGenerator currentGenerator;

	/**
	 * date formatted as a String
	 */
	protected String date;

	protected Entity entity;

	private IFolder folder;

	/**
	 * equivalent to <code>language.toLowerCase()</code>
	 */
	private String langLower;

	private List<String> paths;

	protected IProject project;

	private Map<EObject, IProject> projectMap;

	private IExportConfiguration.Target target;

	@Inject
	@Named("Eclipse")
	protected IFileWriter writer;

	private void addPaths(Collection<? extends Entity> entities) {
		for (Entity entity : entities) {
			addPaths(entity);
		}
	}

	private void addPaths(Entity entity) {
		Iterable<String> names = CoreUtil.getFileList(entity);
		if (CoreUtil.isExternal(entity)) {
			Iterables.addAll(paths, names);
		} else {
			IProject project = projectMap.get(entity);
			for (String name : names) {
				paths.add(computePath(project, name));
			}
		}
	}

	/**
	 * Returns the path (relative to the current folder) of the given object (may be entity, unit,
	 * actor, network).
	 * 
	 * @param eObject
	 *            an EObject
	 * @return file name relative to the current folder
	 */
	protected final String computePath(Entity entity) {
		return computePath(projectMap.get(entity), entity.getName());
	}

	/**
	 * Returns a path that is relative to the current folder from the given qualified name belonging
	 * to the given project.
	 * 
	 * @param project
	 *            a project
	 * @param name
	 *            qualified name of an entity
	 * @return file name relative to the current folder
	 */
	private String computePath(IProject project, String name) {
		String path = currentGenerator.computePath(IrUtil.getFile(name));
		IFile target = project.getFile(path);
		return CoreUtil.getRelative(folder, target);
	}

	/**
	 * Computes the paths of built-in entities, units, actors, networks (in the proper order) to be
	 * compiled.
	 * 
	 */
	private void computePathList() {
		paths = new ArrayList<>();

		// first add root dependencies
		final String language = currentGenerator.getName();
		IExportConfiguration config = SynflowCore.getDefault().getInstance(
				IExportConfiguration.class, language);
		for (String root : config.getRootDependencies(target)) {
			copyDependency(root);

			paths.add(computePath(project, root));
		}

		// collect all entities
		NetworkVisitor visitor = new NetworkVisitor(project);
		visitor.doSwitch(entity);
		projectMap = visitor.getProjectMap();

		// find units
		List<Unit> units = findUnits(visitor.getEntities());

		// headers in Verilog must not be compiled/part of a project
		// because they are only valid when included by tasks/networks
		if (!"Verilog".equals(language)) {
			addPaths(units);
		}

		// adds entities
		addPaths(visitor.getEntities());
	}

	/**
	 * Copy the file from the library with the given qualified name.
	 * 
	 * @param name
	 */
	private void copyDependency(String name) {
		String file = IrUtil.getFile(name);
		String path = currentGenerator.computePath(file);
		if (!currentGenerator.getFileWriter().exists(path)) {
			try {
				String sourcePath = "/lib/" + currentGenerator.getName().toLowerCase() + "/src/"
						+ file + "." + currentGenerator.getFileExtension();
				InputStream source = SynflowCore.openStream(new Path(sourcePath));
				currentGenerator.getFileWriter().write(path, source);
			} catch (IOException e) {
				SynflowCore.log(e);
			}
		}
	}

	/**
	 * Do the actual export. Must be overridden by subclasses.
	 */
	protected abstract void doExport();

	@Override
	public void export(Entity entity) {
		initializeExport(entity);
		if (currentGenerator != null) {
			doExport();
		}
	}

	private List<Unit> findUnits(Collection<Entity> entities) {
		List<Unit> units = new ArrayList<>();
		for (Entity entity : entities) {
			Map<EObject, Collection<Setting>> map = EcoreUtil.ExternalCrossReferencer.find(entity);
			for (Collection<Setting> settings : map.values()) {
				for (Setting setting : settings) {
					Object object = setting.get(true);
					if (object instanceof EObject) {
						EObject cter = getRootContainer((EObject) object);
						if (cter instanceof Unit) {
							Unit unit = (Unit) cter;
							if (!units.contains(unit)) {
								units.add(unit);

								IProject project = unit.getFile().getProject();
								projectMap.put(unit, project);
							}
						}
					}
				}
			}
		}

		return units;
	}

	protected ICodeGenerator getGenerator() {
		return currentGenerator;
	}

	/**
	 * Returns the include path for this project's build path as a list of strings.
	 * 
	 * @return a list of strings
	 */
	protected List<String> getIncludePath() {
		Collection<IProject> projects = CoreUtil.getBuildPath(project);
		List<String> paths = new ArrayList<>();
		for (IProject project : projects) {
			IFolder gen = project.getFolder(langLower + SUFFIX_GEN);
			String path = CoreUtil.getRelative(folder, gen);
			paths.add(path);
		}

		return paths;
	}

	protected String getLanguage() {
		return currentGenerator.getName();
	}

	protected final List<String> getPaths() {
		if (paths == null) {
			// compute list of paths
			computePathList();
		}
		return paths;
	}

	/**
	 * Initializes this exporter by loading the given file as a network, instantiating it, loading
	 * the path to libraries, and formatting the current date.
	 * 
	 * @param file
	 *            a .xdf file
	 */
	private void initializeExport(Entity entity) {
		// set project and updates file writer
		project = entity.getFile().getProject();
		writer.setOutputFolder(project.getName());

		// set code generator
		currentGenerator = SynflowCore.getGenerator(project);
		if (currentGenerator == null) {
			return;
		}

		// initialize code generator with writer
		currentGenerator.initialize(writer);

		// lower-case versions of language
		langLower = currentGenerator.getName().toLowerCase();

		// sets attribute and transform entity
		this.entity = entity;
		currentGenerator.transform(this.entity);

		// reset paths
		paths = null;

		// format date
		DateFormat format = new SimpleDateFormat("HH:mm:ss MMMM dd, yyyy", Locale.US);
		date = format.format(new Date());
	}

	protected void setParameters(String path, IExportConfiguration.Target target) {
		folder = project.getFolder(path);
		this.target = target;
	}

}
