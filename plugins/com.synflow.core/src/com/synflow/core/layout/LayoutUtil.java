package com.synflow.core.layout;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.synflow.core.SynflowCore;
import com.synflow.core.SynflowNature;

public class LayoutUtil {

	public static final String FOLDER_SRC = "src";

	public static boolean isPackage(IResource resource) {
		IContainer parent = resource.getParent();
		if (parent == null) {
			return false;
		}

		return isPackage(parent) || isSourceFolder(parent);
	}

	public static boolean isSourceFolder(IResource resource) {
		if (!resource.isAccessible()) {
			return false;
		}

		IContainer container = resource.getParent();
		if (container == null || container.getType() != IResource.PROJECT) {
			return false;
		}

		IProject project = (IProject) container;
		try {
			if (!project.hasNature(SynflowNature.NATURE_ID)) {
				return false;
			}
		} catch (CoreException e) {
			SynflowCore.log(e);
		}

		return FOLDER_SRC.equals(resource.getName());
	}

}
