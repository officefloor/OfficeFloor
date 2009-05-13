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
package net.officefloor.eclipse.util;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.WorkLoader;
import net.officefloor.compile.work.WorkType;
import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.model.Model;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;

/**
 * Utility class for working with the {@link Model} instances.
 * 
 * @author Daniel
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

		// Obtain the work class
		Class<? extends WorkSource> workSourceClass = EclipseUtil.obtainClass(
				workModel.getWorkSourceClassName(), WorkSource.class, editor);
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

		// Obtain the managed object source class
		Class<? extends ManagedObjectSource> managedObjectSourceClass = EclipseUtil
				.obtainClass(managedObjectSource
						.getManagedObjectSourceClassName(),
						ManagedObjectSource.class, editor);
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
	 * All access via static methods.
	 */
	private ModelUtil() {
	}

}