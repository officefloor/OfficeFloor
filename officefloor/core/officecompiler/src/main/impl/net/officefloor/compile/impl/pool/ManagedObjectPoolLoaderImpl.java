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
package net.officefloor.compile.impl.pool;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.pool.ManagedObjectPoolLoader;
import net.officefloor.compile.pool.ManagedObjectPoolType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceProperty;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceSpecification;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;

/**
 * {@link ManagedObjectPoolLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolLoaderImpl implements ManagedObjectPoolLoader {

	/**
	 * {@link Node} requiring the {@link ManagedObjectPool}.
	 */
	private Node node;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Instantiate.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link ManagedObjectPool}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagedObjectPoolLoaderImpl(Node node, NodeContext context) {
		this.node = node;
		this.context = context;
	}

	/*
	 * ================= ManagedObjectPoolLoader ===============================
	 */

	@Override
	public <PS extends ManagedObjectPoolSource> PropertyList loadSpecification(
			Class<PS> managedObjectPoolSourceClass) {

		// Instantiate the managed object pool source
		ManagedObjectPoolSource managedObjectPoolSource = CompileUtil
				.newInstance(managedObjectPoolSourceClass,
						ManagedObjectPoolSource.class, this.node,
						this.context.getCompilerIssues());
		if (managedObjectPoolSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		ManagedObjectPoolSourceSpecification specification;
		try {
			specification = managedObjectPoolSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue(
					"Failed to obtain "
							+ ManagedObjectPoolSourceSpecification.class
									.getSimpleName() + " from "
							+ managedObjectPoolSourceClass.getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No "
					+ ManagedObjectPoolSourceSpecification.class
							.getSimpleName() + " returned from "
					+ managedObjectPoolSourceClass.getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		ManagedObjectPoolSourceProperty[] managedObjectPoolSourceProperties;
		try {
			managedObjectPoolSourceProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue(
					"Failed to obtain "
							+ ManagedObjectPoolSourceProperty.class
									.getSimpleName()
							+ " instances from "
							+ ManagedObjectPoolSourceSpecification.class
									.getSimpleName() + " for "
							+ managedObjectPoolSourceClass.getName(), ex);
			return null; // failed to obtain properties
		}

		// Load the managed object pool source properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (managedObjectPoolSourceProperties != null) {
			for (int i = 0; i < managedObjectPoolSourceProperties.length; i++) {
				ManagedObjectPoolSourceProperty mopProperty = managedObjectPoolSourceProperties[i];

				// Ensure have the managed object pool source property
				if (mopProperty == null) {
					this.addIssue(ManagedObjectPoolSourceProperty.class
							.getSimpleName()
							+ " "
							+ i
							+ " is null from "
							+ ManagedObjectPoolSourceSpecification.class
									.getSimpleName()
							+ " for "
							+ managedObjectPoolSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = mopProperty.getName();
				} catch (Throwable ex) {
					this.addIssue(
							"Failed to get name for "
									+ ManagedObjectPoolSourceProperty.class
											.getSimpleName()
									+ " "
									+ i
									+ " from "
									+ ManagedObjectPoolSourceSpecification.class
											.getSimpleName() + " for "
									+ managedObjectPoolSourceClass.getName(),
							ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(ManagedObjectPoolSourceProperty.class
							.getSimpleName()
							+ " "
							+ i
							+ " provided blank name from "
							+ ManagedObjectPoolSourceSpecification.class
									.getSimpleName()
							+ " for "
							+ managedObjectPoolSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = mopProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue(
							"Failed to get label for "
									+ ManagedObjectPoolSourceProperty.class
											.getSimpleName()
									+ " "
									+ i
									+ " ("
									+ name
									+ ") from "
									+ ManagedObjectPoolSourceSpecification.class
											.getSimpleName() + " for "
									+ managedObjectPoolSourceClass.getName(),
							ex);
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
	public <PS extends ManagedObjectPoolSource> ManagedObjectPoolType loadManagedObjectPoolType(
			Class<PS> managedObjectPoolSourceClass, PropertyList propertyList) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement ManagedObjectPoolLoader.loadManagedObjectPool");
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void addIssue(String issueDescription) {
		this.context.getCompilerIssues().addIssue(this.node, issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 */
	private void addIssue(String issueDescription, Throwable cause) {
		this.context.getCompilerIssues().addIssue(this.node, issueDescription,
				cause);
	}

}