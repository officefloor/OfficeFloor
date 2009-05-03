/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.desk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.work.WorkLoader;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.workloader.WorkLoaderExtension;
import net.officefloor.eclipse.extension.workloader.WorkLoaderProperty;
import net.officefloor.eclipse.java.JavaUtil;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.PropertyModel;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Utility methods for the {@link DeskModel}.
 * 
 * @author Daniel
 */
public class DeskUtil {

	/**
	 * Obtains the listing of {@link WorkLoaderInstance} instances.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @return {@link WorkLoaderInstance} instances.
	 */
	public static WorkLoaderInstance[] createWorkLoaderInstances(
			IProject project) {

		// Obtain the work loader instances
		Map<String, WorkLoaderInstance> workLoaderInstances = createWorkLoaderInstanceMap(project);

		// Obtain the listing of work loader instances (in order)
		WorkLoaderInstance[] instances = workLoaderInstances.values().toArray(
				new WorkLoaderInstance[0]);
		Arrays.sort(instances, new Comparator<WorkLoaderInstance>() {
			@Override
			public int compare(WorkLoaderInstance a, WorkLoaderInstance b) {
				return a.getClassName().compareTo(b.getClassName());
			}
		});

		// Return the listing of instances
		return instances;
	}

	/**
	 * Obtains the {@link WorkLoaderInstance} for the {@link WorkLoader} class
	 * name.
	 * 
	 * @param workLoaderClassName
	 *            {@link WorkLoader} class name.
	 * @param project
	 *            {@link IProject}.
	 * @return {@link WorkLoaderInstance} for the {@link WorkLoader} class name.
	 *         <code>null</code> if can not obtain the
	 *         {@link WorkLoaderInstance}.
	 */
	public static WorkLoaderInstance createWorkLoaderInstance(
			String workLoaderClassName, IProject project) {

		// Obtain the work loader instances
		Map<String, WorkLoaderInstance> workLoaderInstances = createWorkLoaderInstanceMap(project);

		// Return the work loader instance specified
		return workLoaderInstances.get(workLoaderClassName);
	}

	/**
	 * Creates the mapping of {@link WorkLoader} class name to its
	 * {@link WorkLoaderInstance}.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @return Mapping of {@link WorkLoader} class name to its
	 *         {@link WorkLoaderInstance}.
	 */
	private static Map<String, WorkLoaderInstance> createWorkLoaderInstanceMap(
			IProject project) {

		// Obtain the work loader instances (by class name to get unique set)
		Map<String, WorkLoaderInstance> workLoaderInstances = new HashMap<String, WorkLoaderInstance>();

		// Obtain the class loader
		ProjectClassLoader classLoader = ProjectClassLoader.create(project);

		// Obtain from project class path
		try {
			// Obtain the types on the class path
			IType[] types = JavaUtil.getSubTypes(project, WorkLoader.class
					.getName());
			for (IType type : types) {
				String className = type.getFullyQualifiedName();
				workLoaderInstances.put(className, new WorkLoaderInstance(
						className, null, classLoader));
			}
		} catch (JavaModelException ex) {
			// Do not add the types
		}

		// Obtain via extension point second to override
		try {
			List<WorkLoaderExtension> workLoaderExtensions = ExtensionUtil
					.createExecutableExtensions(
							WorkLoaderExtension.EXTENSION_ID,
							WorkLoaderExtension.class);
			for (WorkLoaderExtension workLoaderExtension : workLoaderExtensions) {
				Class<?> workLoaderClass = workLoaderExtension
						.getWorkSourceClass();
				String className = workLoaderClass.getName();
				workLoaderInstances.put(className, new WorkLoaderInstance(
						className, workLoaderExtension, classLoader));
			}
		} catch (Exception ex) {
			// Do not add the types
		}

		// Return work loader instances by the work loader class name
		return workLoaderInstances;
	}

	/**
	 * Translates the listing of {@link WorkLoaderProperty} instances into
	 * {@link PropertyModel} instances.
	 * 
	 * @param properties
	 *            {@link WorkLoaderProperty} instances.
	 * @return Translated {@link PropertyModel} instances.
	 */
	public static List<PropertyModel> translateForWorkLoader(
			List<WorkLoaderProperty> properties) {
		List<PropertyModel> translated;
		if (properties == null) {
			translated = new ArrayList<PropertyModel>(0);
		} else {
			translated = new ArrayList<PropertyModel>(properties.size());
			for (WorkLoaderProperty property : properties) {
				translated.add(new PropertyModel(property.getName(), property
						.getValue()));
			}
		}
		return translated;
	}

	/**
	 * Translates the listing of {@link PropertyModel} instances into
	 * {@link WorkLoaderProperty} instances.
	 * 
	 * @param properties
	 *            {@link PropertyModel} instances.
	 * @return Translated {@link WorkLoaderProperty} instances.
	 */
	public static List<WorkLoaderProperty> translateForExtension(
			List<PropertyModel> properties) {
		List<WorkLoaderProperty> translated;
		if (properties == null) {
			translated = new ArrayList<WorkLoaderProperty>(0);
		} else {
			translated = new ArrayList<WorkLoaderProperty>(properties.size());
			for (PropertyModel property : properties) {
				translated.add(new WorkLoaderProperty(property.getName(),
						property.getValue()));
			}
		}
		return translated;
	}

	/**
	 * All access via static methods.
	 */
	private DeskUtil() {
	}

}
