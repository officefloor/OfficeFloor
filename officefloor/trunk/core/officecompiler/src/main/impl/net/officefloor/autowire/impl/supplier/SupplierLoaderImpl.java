/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.autowire.impl.supplier;

import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceProperty;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceSpecification;
import net.officefloor.autowire.supplier.SupplierLoader;
import net.officefloor.autowire.supplier.SupplierType;
import net.officefloor.autowire.supplier.SupplyOrder;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link SupplierLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierLoaderImpl implements SupplierLoader {

	/**
	 * {@link OfficeFloor} Location.
	 */
	private final String officeFloorLocation;

	/**
	 * {@link OfficeFloorSupplier} name.
	 */
	private final String supplierName;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext nodeContext;

	/**
	 * Initiate for building.
	 * 
	 * @param officeFloorLocation
	 *            {@link OfficeFloor} location.
	 * @param supplierName
	 *            {@link OfficeFloorSupplier} name.
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public SupplierLoaderImpl(String officeFloorLocation, String supplierName,
			NodeContext nodeContext) {
		this.officeFloorLocation = officeFloorLocation;
		this.supplierName = supplierName;
		this.nodeContext = nodeContext;
	}

	/**
	 * Initiate from {@link OfficeFloorCompiler}.
	 * 
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public SupplierLoaderImpl(NodeContext nodeContext) {
		this(null, null, nodeContext);
	}

	/*
	 * ======================== SupplierLoader ==========================
	 */

	@Override
	public <S extends SupplierSource> PropertyList loadSpecification(
			Class<S> supplierSourceClass) {

		// Instantiate the supplier source
		SupplierSource supplierSource = CompileUtil.newInstance(
				supplierSourceClass, SupplierSource.class,
				LocationType.OFFICE_FLOOR, this.officeFloorLocation, null,
				this.supplierName, this.nodeContext.getCompilerIssues());
		if (supplierSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		SupplierSourceSpecification specification;
		try {
			specification = supplierSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ SupplierSourceSpecification.class.getSimpleName()
					+ " from " + supplierSourceClass.getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No "
					+ SupplierSourceSpecification.class.getSimpleName()
					+ " returned from " + supplierSourceClass.getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		SupplierSourceProperty[] supplierProperties;
		try {
			supplierProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue(
					"Failed to obtain "
							+ SupplierSourceProperty.class.getSimpleName()
							+ " instances from "
							+ SupplierSourceSpecification.class.getSimpleName()
							+ " for " + supplierSourceClass.getName(), ex);
			return null; // failed to obtain properties
		}

		// Load the supplier properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (supplierProperties != null) {
			for (int i = 0; i < supplierProperties.length; i++) {
				SupplierSourceProperty supplierProperty = supplierProperties[i];

				// Ensure have the supplier property
				if (supplierProperty == null) {
					this.addIssue(SupplierSourceProperty.class.getSimpleName()
							+ " " + i + " is null from "
							+ SupplierSourceSpecification.class.getSimpleName()
							+ " for " + supplierSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = supplierProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for "
							+ SupplierSourceProperty.class.getSimpleName()
							+ " " + i + " from "
							+ SupplierSourceSpecification.class.getSimpleName()
							+ " for " + supplierSourceClass.getName(), ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(SupplierSourceProperty.class.getSimpleName()
							+ " " + i + " provided blank name from "
							+ SupplierSourceSpecification.class.getSimpleName()
							+ " for " + supplierSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = supplierProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for "
							+ SupplierSourceProperty.class.getSimpleName()
							+ " " + i + " (" + name + ") from "
							+ SupplierSourceSpecification.class.getSimpleName()
							+ " for " + supplierSourceClass.getName(), ex);
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
	public <S extends SupplierSource> SupplierType loadSupplierType(
			Class<S> supplierSourceClass, PropertyList propertyList) {

		// Create the supplier source context
		SupplierSourceContextImpl supplierSourceContext = new SupplierSourceContextImpl(
				true, this.supplierName, this.officeFloorLocation,
				propertyList, this.nodeContext);

		// Load and return the supplier type
		return supplierSourceContext.loadSupplier(supplierSourceClass,
				propertyList);
	}

	@Override
	public <S extends SupplierSource> void fillSupplyOrders(
			Class<S> supplierSourceClass, PropertyList propertyList,
			SupplyOrder... supplyOrders) {

		// Create the supplier source context
		SupplierSourceContextImpl supplierSourceContext = new SupplierSourceContextImpl(
				true, this.supplierName, this.officeFloorLocation,
				propertyList, this.nodeContext);

		// Fill the supply orders
		supplierSourceContext.loadSupplier(supplierSourceClass, propertyList,
				supplyOrders);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Issue description.
	 */
	private void addIssue(String issueDescription) {
		this.nodeContext.getCompilerIssues().addIssue(
				LocationType.OFFICE_FLOOR, this.officeFloorLocation, null,
				null, issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of issue.
	 */
	private void addIssue(String issueDescription, Throwable cause) {
		this.nodeContext.getCompilerIssues().addIssue(
				LocationType.OFFICE_FLOOR, this.officeFloorLocation, null,
				null, issueDescription, cause);
	}

}