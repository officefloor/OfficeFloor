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
package net.officefloor.compile.impl.work;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.WorkLoader;
import net.officefloor.compile.spi.work.source.WorkProperty;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSpecification;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.work.WorkModel;

/**
 * {@link WorkLoader} implementation.
 * 
 * @author Daniel
 */
public class WorkLoaderImpl implements WorkLoader {

	/**
	 * Location of the {@link DeskModel}.
	 */
	private final String deskLocation;

	/**
	 * Name of the {@link Work}.
	 */
	private final String workName;

	/**
	 * Initiate.
	 * 
	 * @param deskLocation
	 *            Location of the {@link DeskModel}.
	 * @param workName
	 *            Name of the {@link Work}.
	 */
	public WorkLoaderImpl(String deskName, String workName) {
		this.deskLocation = deskName;
		this.workName = workName;
	}

	/*
	 * ====================== WorkLoader ====================================
	 */

	@Override
	public <W extends Work, WS extends WorkSource<W>> PropertyList loadSpecification(
			Class<WS> workSourceClass, CompilerIssues issues) {

		// Instantiate the work source
		WorkSource<W> workSource = CompileUtil.newInstance(workSourceClass,
				WorkSource.class, LocationType.DESK, this.deskLocation,
				AssetType.WORK, this.workName, issues);
		if (workSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		WorkSpecification specification;
		try {
			specification = workSource.getSpecification();
		} catch (Throwable ex) {
			issues.addIssue(LocationType.DESK, this.deskLocation,
					AssetType.WORK, this.workName, "Failed to obtain "
							+ WorkSpecification.class.getSimpleName()
							+ " from " + workSourceClass.getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			issues.addIssue(LocationType.DESK, this.deskLocation,
					AssetType.WORK, this.workName, "No "
							+ WorkSpecification.class.getSimpleName()
							+ " returned from " + workSourceClass.getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		WorkProperty[] workProperties;
		try {
			workProperties = specification.getProperties();
		} catch (Throwable ex) {
			issues.addIssue(LocationType.DESK, this.deskLocation,
					AssetType.WORK, this.workName, "Failed to obtain "
							+ WorkProperty.class.getSimpleName()
							+ " instances from "
							+ WorkSpecification.class.getSimpleName() + " for "
							+ workSourceClass.getName(), ex);
			return null; // failed to obtain properties
		}

		// Load the work properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (workProperties != null) {
			for (int i = 0; i < workProperties.length; i++) {
				WorkProperty workProperty = workProperties[i];

				// Ensure have the work property
				if (workProperty == null) {
					issues.addIssue(LocationType.DESK, this.deskLocation,
							AssetType.WORK, this.workName, WorkProperty.class
									.getSimpleName()
									+ " "
									+ i
									+ " is null from "
									+ WorkSpecification.class.getSimpleName()
									+ " for " + workSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = workProperty.getName();
				} catch (Throwable ex) {
					issues.addIssue(LocationType.DESK, this.deskLocation,
							AssetType.WORK, this.workName,
							"Failed to get name for "
									+ WorkProperty.class.getSimpleName() + " "
									+ i + " from "
									+ WorkSpecification.class.getSimpleName()
									+ " for " + workSourceClass.getName(), ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					issues.addIssue(LocationType.DESK, this.deskLocation,
							AssetType.WORK, this.workName, WorkProperty.class
									.getSimpleName()
									+ " "
									+ i
									+ " provided blank name from "
									+ WorkSpecification.class.getSimpleName()
									+ " for " + workSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = workProperty.getLabel();
				} catch (Throwable ex) {
					issues.addIssue(LocationType.DESK, this.deskLocation,
							AssetType.WORK, this.workName,
							"Failed to get label for "
									+ WorkProperty.class.getSimpleName() + " "
									+ i + " (" + name + ") from "
									+ WorkSpecification.class.getSimpleName()
									+ " for " + workSourceClass.getName(), ex);
					return null; // must have complete property details
				}

				// Add to the properties
				propertyList.addProperty(name, label);
			}
		}

		// Return the property list
		return propertyList;
	}

	@Override
	public <W extends Work, WS extends WorkSource<W>> WorkType<W> loadWork(
			Class<WS> workSourceClass, PropertyList propertyList,
			ClassLoader classLoader, CompilerIssues issues) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement WorkLoader.loadWork");
	}

}