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
package net.officefloor.eclipse.officefloor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.managedobjectsource.InitiateProperty;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.workloader.WorkLoaderProperty;
import net.officefloor.eclipse.java.JavaUtil;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.model.officefloor.OfficeFloorModel;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Utility methods for the {@link OfficeFloorModel}.
 * 
 * @author Daniel
 */
public class OfficeFloorUtil {

	/**
	 * Obtains the listing of {@link ManagedObjectSourceInstance} instances.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @return {@link ManagedObjectSourceInstance} instances.
	 */
	public static ManagedObjectSourceInstance[] createManagedObjectSourceInstances(
			IProject project) {

		// Obtain the managed object source instances
		Map<String, ManagedObjectSourceInstance> managedObjectSourceInstances = createManagedObjectSourceInstanceMap(project);

		// Obtain the listing of managed object source instances (in order)
		ManagedObjectSourceInstance[] instances = managedObjectSourceInstances
				.values().toArray(new ManagedObjectSourceInstance[0]);
		Arrays.sort(instances, new Comparator<ManagedObjectSourceInstance>() {
			@Override
			public int compare(ManagedObjectSourceInstance a,
					ManagedObjectSourceInstance b) {
				return a.getClassName().compareTo(b.getClassName());
			}
		});

		// Return the listing of instances
		return instances;
	}

	/**
	 * Creates the mapping of {@link ManagedObjectSource} class name to its
	 * {@link ManagedObjectSourceInstance}.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @return Mapping of {@link ManagedObjectSource} class name to its
	 *         {@link ManagedObjectSourceInstance}.
	 */
	private static Map<String, ManagedObjectSourceInstance> createManagedObjectSourceInstanceMap(
			IProject project) {

		// Obtain the by class name to get unique set
		Map<String, ManagedObjectSourceInstance> managedObjectSourceInstances = new HashMap<String, ManagedObjectSourceInstance>();

		// Obtain the class loader
		ProjectClassLoader classLoader = ProjectClassLoader.create(project);

		// Obtain from project class path
		try {
			// Obtain the types on the class path
			IType[] types = JavaUtil.getSubTypes(project,
					ManagedObjectSource.class.getName());
			for (IType type : types) {
				String className = type.getFullyQualifiedName();
				managedObjectSourceInstances.put(className,
						new ManagedObjectSourceInstance(className, null,
								classLoader));
			}
		} catch (JavaModelException ex) {
			// Do not add the types
		}

		// Obtain via extension point second to override
		try {
			List<ManagedObjectSourceExtension> managedObjectSourceExtensions = ExtensionUtil
					.createExecutableExtensions(
							ManagedObjectSourceExtension.EXTENSION_ID,
							ManagedObjectSourceExtension.class);
			for (ManagedObjectSourceExtension managedObjectSourceExtension : managedObjectSourceExtensions) {
				Class<?> managedObjectSourceClass = managedObjectSourceExtension
						.getManagedObjectSourceClass();
				String className = managedObjectSourceClass.getName();
				managedObjectSourceInstances.put(className,
						new ManagedObjectSourceInstance(className,
								managedObjectSourceExtension, classLoader));
			}
		} catch (Exception ex) {
			// Do not add the types
		}

		// Return instances by managed object source class name
		return managedObjectSourceInstances;
	}

	/**
	 * Translates the listing of {@link InitiateProperty} instances into
	 * {@link PropertyModel} instances.
	 * 
	 * @param properties
	 *            {@link InitiateProperty} instances.
	 * @return Translated {@link PropertyModel} instances.
	 */
	public static List<PropertyModel> translateForManagedObjectSource(
			List<InitiateProperty> properties) {
		List<PropertyModel> translated;
		if (properties == null) {
			translated = new ArrayList<PropertyModel>(0);
		} else {
			translated = new ArrayList<PropertyModel>(properties.size());
			for (InitiateProperty property : properties) {
				translated.add(new PropertyModel(property.getName(), property
						.getValue()));
			}
		}
		return translated;
	}

	/**
	 * Translates the listing of {@link PropertyModel} instances into
	 * {@link InitiateProperty} instances.
	 * 
	 * @param properties
	 *            {@link PropertyModel} instances.
	 * @return Translated {@link WorkLoaderProperty} instances.
	 */
	public static List<InitiateProperty> translateForExtension(
			List<PropertyModel> properties) {
		List<InitiateProperty> translated;
		if (properties == null) {
			translated = new ArrayList<InitiateProperty>(0);
		} else {
			translated = new ArrayList<InitiateProperty>(properties.size());
			for (PropertyModel property : properties) {
				translated.add(new InitiateProperty(property.getName(),
						property.getValue()));
			}
		}
		return translated;
	}

	/**
	 * All access via static methods.
	 */
	private OfficeFloorUtil() {
	}
}
