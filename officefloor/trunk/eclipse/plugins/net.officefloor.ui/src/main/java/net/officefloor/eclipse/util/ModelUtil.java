/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.util;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.WorkLoader;
import net.officefloor.compile.work.WorkType;
import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.model.Model;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;

import org.eclipse.core.resources.IProject;

/**
 * Utility class for working with the {@link Model} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ModelUtil {

	/**
	 * Obtains the {@link WorkType} for the {@link WorkModel}.
	 * 
	 * @param workModel
	 *            {@link WorkModel}.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor} requiring the
	 *            {@link WorkType}.
	 * @return {@link WorkType} or <code>null</code> if issue obtaining it.
	 */
	@SuppressWarnings("unchecked")
	public static WorkType<?> getWorkType(WorkModel workModel,
			AbstractOfficeFloorEditor<?, ?> editor) {

		// Obtain the office floor compiler
		OfficeFloorCompiler compiler = OfficeFloorPlugin.getDefault()
				.createCompiler(editor);

		// Obtain the work loader
		WorkLoader workLoader = compiler.getWorkLoader();

		// Obtain the project class loader
		ClassLoader classLoader = compiler.getClassLoader();

		// Obtain the work class
		Class<? extends WorkSource> workSourceClass = obtainClass(workModel
				.getWorkSourceClassName(), WorkSource.class, classLoader,
				editor);
		if (workSourceClass == null) {
			return null; // must have work source class
		}

		// Obtain the properties
		PropertyList properties = compiler.createPropertyList();
		for (PropertyModel property : workModel.getProperties()) {
			properties.addProperty(property.getName()).setValue(
					property.getValue());
		}

		// Load and return the work type
		WorkType<?> workType = workLoader.loadWorkType(workSourceClass,
				properties);
		return workType;
	}

	/**
	 * Obtains the {@link ManagedObjectType} for the
	 * {@link OfficeFloorManagedObjectSourceModel}.
	 * 
	 * @param managedObjectSource
	 *            {@link OfficeFloorManagedObjectSourceModel}.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor} requiring the
	 *            {@link ManagedObjectType}.
	 * @return {@link ManagedObjectType} or <code>null</code> if issue obtaining
	 *         it.
	 */
	@SuppressWarnings("unchecked")
	public static ManagedObjectType<?> getManagedObjectType(
			OfficeFloorManagedObjectSourceModel managedObjectSource,
			AbstractOfficeFloorEditor<?, ?> editor) {

		// Obtain the office floor compiler
		OfficeFloorCompiler compiler = OfficeFloorPlugin.getDefault()
				.createCompiler(editor);

		// Obtain the managed object loader
		ManagedObjectLoader managedObjectLoader = compiler
				.getManagedObjectLoader();

		// Obtain the project class loader
		ClassLoader classLoader = compiler.getClassLoader();

		// Obtain the managed object source class
		Class<? extends ManagedObjectSource> managedObjectSourceClass = obtainClass(
				managedObjectSource.getManagedObjectSourceClassName(),
				ManagedObjectSource.class, classLoader, editor);
		if (managedObjectSourceClass == null) {
			return null; // must have managed object source class
		}

		// Obtain the properties
		PropertyList properties = compiler.createPropertyList();
		for (net.officefloor.model.officefloor.PropertyModel property : managedObjectSource
				.getProperties()) {
			properties.addProperty(property.getName()).setValue(
					property.getValue());
		}

		// Load and return the managed object type
		ManagedObjectType<?> managedObjectType = managedObjectLoader
				.loadManagedObjectType(managedObjectSourceClass, properties);
		return managedObjectType;
	}

	/**
	 * Obtains the {@link OfficeSection} for the {@link OfficeSectionModel}.
	 * 
	 * @param officeSection
	 *            {@link OfficeSectionModel}.
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor}.
	 * @return {@link OfficeSection} or <code>null</code> if failed to load.
	 */
	public static OfficeSection getOfficeSection(
			OfficeSectionModel officeSection, OfficeFloorCompiler compiler,
			CompilerIssues issues, AbstractOfficeFloorEditor<?, ?> editor) {
		try {
			// Obtain the class loader
			ClassLoader classLoader = compiler.getClassLoader();

			// Obtain the section source class
			Class<? extends SectionSource> sectionSourceClass = obtainClass(
					officeSection.getSectionSourceClassName(),
					SectionSource.class, classLoader, editor);

			// Create the property list
			PropertyList propertyList = compiler.createPropertyList();
			for (net.officefloor.model.office.PropertyModel property : officeSection
					.getProperties()) {
				propertyList.addProperty(property.getName()).setValue(
						property.getValue());
			}

			// Obtain the section loader
			SectionLoader sectionLoader = compiler.getSectionLoader();

			// Load and return the section
			return sectionLoader.loadOfficeSection(officeSection
					.getOfficeSectionName(), sectionSourceClass, officeSection
					.getSectionLocation(), propertyList);

		} catch (Throwable ex) {
			// Report issue in loading section
			issues.addIssue(LocationType.SECTION, officeSection
					.getSectionLocation(), null, null,
					"Failed to load office section: " + ex.getMessage() + " ["
							+ ex.getClass().getSimpleName() + "]");
			return null;
		}
	}

	/**
	 * Obtains the {@link Class} by its name.
	 * 
	 * @param className
	 *            Fully qualified name of the class.
	 * @param superType
	 *            Type that the class must be a sub type.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor} to report issues and obtain
	 *            the {@link IProject}.
	 * @return {@link Class} or <code>null</code> if could not obtain.
	 */
	@SuppressWarnings("unchecked")
	public static <S> Class<S> obtainClass(String className,
			Class<S> superType, ClassLoader classLoader,
			AbstractOfficeFloorEditor<?, ?> editor) {
		try {
			// Create the class
			Class clazz = classLoader.loadClass(className);

			// Ensure correct super type
			if (!(superType.isAssignableFrom(clazz))) {
				editor.messageError("Class '" + clazz.getName()
						+ "' must be a sub type of " + superType.getName());
			}

			// Return the class
			return (Class<S>) clazz;

		} catch (Throwable ex) {
			editor.messageError("Failed to obtain class " + className, ex);
			return null; // can not obtain class
		}
	}

	/**
	 * All access via static methods.
	 */
	private ModelUtil() {
	}

}