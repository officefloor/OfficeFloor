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
package net.officefloor.compile.impl.managedobject;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedobject.ManagedObjectLoader;
import net.officefloor.compile.spi.managedobject.ManagedObjectType;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;

/**
 * {@link ManagedObjectLoader} implementation.
 * 
 * @author Daniel
 */
public class ManagedObjectLoaderImpl implements ManagedObjectLoader {

	/**
	 * {@link LocationType}.
	 */
	private LocationType locationType;

	/**
	 * Location.
	 */
	private final String location;

	/**
	 * Name of the {@link ManagedObject}.
	 */
	private final String managedObjectName;

	/**
	 * Initiate.
	 * 
	 * @param locationType
	 *            {@link LocationType}.
	 * @param location
	 *            Location.
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 */
	public ManagedObjectLoaderImpl(LocationType locationType, String location,
			String managedObjectName) {
		this.locationType = locationType;
		this.location = location;
		this.managedObjectName = managedObjectName;
	}

	/*
	 * ===================== ManagedObjectLoader ==============================
	 */

	@Override
	public <D extends Enum<D>, H extends Enum<H>, MS extends ManagedObjectSource<D, H>> PropertyList loadSpecification(
			Class<MS> managedObjectSourceClass, CompilerIssues issues) {

		// Instantiate the managed object source
		ManagedObjectSource<D, H> managedObjectSource = CompileUtil
				.newInstance(managedObjectSourceClass,
						ManagedObjectSource.class, this.locationType,
						this.location, AssetType.MANAGED_OBJECT,
						this.managedObjectName, issues);
		if (managedObjectSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		ManagedObjectSourceSpecification specification;
		try {
			specification = managedObjectSource.getSpecification();
		} catch (Throwable ex) {
			this
					.addIssue("Failed to obtain "
							+ ManagedObjectSourceSpecification.class
									.getSimpleName() + " from "
							+ managedObjectSourceClass.getName(), ex, issues);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No "
					+ ManagedObjectSourceSpecification.class.getSimpleName()
					+ " returned from " + managedObjectSourceClass.getName(),
					issues);
			return null; // no specification obtained
		}

		// Obtain the properties
		ManagedObjectSourceProperty[] managedObjectSourceProperties;
		try {
			managedObjectSourceProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ ManagedObjectSourceProperty.class.getSimpleName()
					+ " instances from "
					+ ManagedObjectSourceSpecification.class.getSimpleName()
					+ " for " + managedObjectSourceClass.getName(), ex, issues);
			return null; // failed to obtain properties
		}

		// Load the managed object source properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (managedObjectSourceProperties != null) {
			for (int i = 0; i < managedObjectSourceProperties.length; i++) {
				ManagedObjectSourceProperty mosProperty = managedObjectSourceProperties[i];

				// Ensure have the managed object source property
				if (mosProperty == null) {
					this.addIssue(ManagedObjectSourceProperty.class
							.getSimpleName()
							+ " "
							+ i
							+ " is null from "
							+ ManagedObjectSourceSpecification.class
									.getSimpleName()
							+ " for "
							+ managedObjectSourceClass.getName(), issues);
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = mosProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for "
							+ ManagedObjectSourceProperty.class.getSimpleName()
							+ " "
							+ i
							+ " from "
							+ ManagedObjectSourceSpecification.class
									.getSimpleName() + " for "
							+ managedObjectSourceClass.getName(), ex, issues);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(ManagedObjectSourceProperty.class
							.getSimpleName()
							+ " "
							+ i
							+ " provided blank name from "
							+ ManagedObjectSourceSpecification.class
									.getSimpleName()
							+ " for "
							+ managedObjectSourceClass.getName(), issues);
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = mosProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for "
							+ ManagedObjectSourceProperty.class.getSimpleName()
							+ " "
							+ i
							+ " ("
							+ name
							+ ") from "
							+ ManagedObjectSourceSpecification.class
									.getSimpleName() + " for "
							+ managedObjectSourceClass.getName(), ex, issues);
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
	public <D extends Enum<D>, H extends Enum<H>, MS extends ManagedObjectSource<D, H>> ManagedObjectType<D, H> loadManagedObject(
			Class<MS> managedObjectSourceClass, PropertyList propertyList,
			ClassLoader classLoader, CompilerIssues issues) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement ManagedObjectLoader.loadManagedObject");
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	private void addIssue(String issueDescription, CompilerIssues issues) {
		issues.addIssue(this.locationType, this.location,
				AssetType.MANAGED_OBJECT, this.managedObjectName,
				issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	private void addIssue(String issueDescription, Throwable cause,
			CompilerIssues issues) {
		issues.addIssue(this.locationType, this.location,
				AssetType.MANAGED_OBJECT, this.managedObjectName,
				issueDescription, cause);
	}

}