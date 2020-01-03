/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.impl.supplier;

import java.util.function.Supplier;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceProperty;
import net.officefloor.compile.spi.supplier.source.SupplierSourceSpecification;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierLoader;
import net.officefloor.compile.supplier.SupplierThreadLocalType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.api.source.UnknownPropertyError;
import net.officefloor.frame.api.source.UnknownResourceError;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;

/**
 * {@link SupplierLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierLoaderImpl implements SupplierLoader {

	/**
	 * {@link Node} requiring the {@link Supplier}.
	 */
	private final Node node;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext nodeContext;

	/**
	 * Instantiate.
	 * 
	 * @param node        {@link Node} requiring the {@link Supplier}.
	 * @param nodeContext {@link NodeContext}.
	 */
	public SupplierLoaderImpl(Node node, NodeContext nodeContext) {
		this.node = node;
		this.nodeContext = nodeContext;
	}

	/*
	 * ======================== SupplierLoader ==========================
	 */

	@Override
	public <S extends SupplierSource> PropertyList loadSpecification(Class<S> supplierSourceClass) {

		// Instantiate the supplier source
		SupplierSource supplierSource = CompileUtil.newInstance(supplierSourceClass, SupplierSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (supplierSource == null) {
			return null; // failed to instantiate
		}

		// Load and return specification
		return this.loadSpecification(supplierSource);
	}

	@Override
	public PropertyList loadSpecification(SupplierSource supplierSource) {

		// Obtain the specification
		SupplierSourceSpecification specification;
		try {
			specification = supplierSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + SupplierSourceSpecification.class.getSimpleName() + " from "
					+ supplierSource.getClass().getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No " + SupplierSourceSpecification.class.getSimpleName() + " returned from "
					+ supplierSource.getClass().getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		SupplierSourceProperty[] supplierProperties;
		try {
			supplierProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + SupplierSourceProperty.class.getSimpleName() + " instances from "
					+ SupplierSourceSpecification.class.getSimpleName() + " for " + supplierSource.getClass().getName(),
					ex);
			return null; // failed to obtain properties
		}

		// Load the supplier properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (supplierProperties != null) {
			for (int i = 0; i < supplierProperties.length; i++) {
				SupplierSourceProperty supplierProperty = supplierProperties[i];

				// Ensure have the supplier property
				if (supplierProperty == null) {
					this.addIssue(SupplierSourceProperty.class.getSimpleName() + " " + i + " is null from "
							+ SupplierSourceSpecification.class.getSimpleName() + " for "
							+ supplierSource.getClass().getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = supplierProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for " + SupplierSourceProperty.class.getSimpleName() + " " + i
							+ " from " + SupplierSourceSpecification.class.getSimpleName() + " for "
							+ supplierSource.getClass().getName(), ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(SupplierSourceProperty.class.getSimpleName() + " " + i + " provided blank name from "
							+ SupplierSourceSpecification.class.getSimpleName() + " for "
							+ supplierSource.getClass().getName());
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = supplierProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for " + SupplierSourceProperty.class.getSimpleName() + " " + i
							+ " (" + name + ") from " + SupplierSourceSpecification.class.getSimpleName() + " for "
							+ supplierSource.getClass().getName(), ex);
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
	public <S extends SupplierSource> SupplierType loadSupplierType(Class<S> supplierSourceClass,
			PropertyList propertyList) {

		// Instantiate the supplier source
		S supplierSource = CompileUtil.newInstance(supplierSourceClass, SupplierSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (supplierSource == null) {
			return null; // failed to instantiate
		}

		// Load and return the type
		return this.loadSupplierType(supplierSource, propertyList);
	}

	@Override
	public SupplierType loadSupplierType(SupplierSource supplierSource, PropertyList propertyList) {

		// Obtain qualified name
		String qualifiedName = this.node.getQualifiedName();

		// Obtain the overridden properties
		PropertyList overriddenProperties = this.nodeContext.overrideProperties(this.node, qualifiedName, propertyList);

		// Create the supplier source context
		SupplierSourceContextImpl sourceContext = new SupplierSourceContextImpl(qualifiedName, true,
				overriddenProperties, this.nodeContext);

		try {
			// Source the supplier
			supplierSource.supply(sourceContext);

		} catch (UnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknownPropertyName() + "' for "
					+ SupplierSource.class.getSimpleName() + " " + supplierSource.getClass().getName());
			return null; // must have property

		} catch (UnknownClassError ex) {
			this.addIssue("Can not load class '" + ex.getUnknownClassName() + "' for "
					+ SupplierSource.class.getSimpleName() + " " + supplierSource.getClass().getName());
			return null; // must have class

		} catch (UnknownResourceError ex) {
			this.addIssue("Can not obtain resource at location '" + ex.getUnknownResourceLocation() + "' for "
					+ SupplierSource.class.getSimpleName() + " " + supplierSource.getClass().getName());
			return null; // must have resource

		} catch (Throwable ex) {
			this.addIssue("Failed to source " + SupplierType.class.getSimpleName() + " definition from "
					+ SupplierSource.class.getSimpleName() + " " + supplierSource.getClass().getName(), ex);
			return null; // must be successful
		}

		// Validate the supplier thread local types
		SupplierThreadLocalType[] threadLocalTypes = sourceContext.getSupplierThreadLocalTypes();
		for (int i = 0; i < threadLocalTypes.length; i++) {
			SupplierThreadLocalType threadLocalType = threadLocalTypes[i];

			// Ensure have type
			Class<?> objectType = threadLocalType.getObjectType();
			if (objectType == null) {
				this.addIssue("Must provide type for " + SupplierThreadLocal.class.getSimpleName() + " " + i);
				return null; // can not load supplier thread local
			}
		}

		// Validate the thread synchronisers
		ThreadSynchroniserFactory[] threadSynchronisers = sourceContext.getThreadSynchronisers();
		for (int i = 0; i < threadSynchronisers.length; i++) {
			ThreadSynchroniserFactory threadSynchroniser = threadSynchronisers[i];

			// Ensure have thread synchroniser
			if (threadSynchroniser == null) {
				this.addIssue(
						"Must provide " + ThreadSynchroniserFactory.class.getSimpleName() + " for added instance " + i);
				return null; // can not load
			}
		}

		// Validate the supplied managed object source types
		SuppliedManagedObjectSourceType[] managedObjectSourceTypes = sourceContext
				.getSuppliedManagedObjectSourceTypes();
		for (int i = 0; i < managedObjectSourceTypes.length; i++) {
			SuppliedManagedObjectSourceType managedObjectSourceType = managedObjectSourceTypes[i];

			// Ensure have type
			Class<?> objectType = managedObjectSourceType.getObjectType();
			if (objectType == null) {
				this.addIssue("Must provide type for " + ManagedObject.class.getSimpleName() + " " + i);
				return null; // can not load supplied managed object source
			}

			// Obtain the name of the managed object source
			String qualifier = managedObjectSourceType.getQualifier();
			String managedObjectSourceName = (qualifier != null ? qualifier + "-" : "") + objectType.getName();
			String issueSuffix = " for " + ManagedObject.class.getSimpleName() + " " + managedObjectSourceName;

			// Ensure have managed object source
			if (managedObjectSourceType.getManagedObjectSource() == null) {
				this.addIssue("Must provide a " + ManagedObjectSource.class.getSimpleName() + issueSuffix);
				return null; // can not load supplied managed object source
			}
		}

		// Return the supplier type
		return new SupplierTypeImpl(threadLocalTypes, threadSynchronisers, managedObjectSourceTypes);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription Issue description.
	 */
	private void addIssue(String issueDescription) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription Description of the issue.
	 * @param cause            Cause of issue.
	 */
	private void addIssue(String issueDescription, Throwable cause) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription, cause);
	}

}
