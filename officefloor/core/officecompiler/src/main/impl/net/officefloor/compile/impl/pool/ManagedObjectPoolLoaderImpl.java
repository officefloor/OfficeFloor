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

package net.officefloor.compile.impl.pool;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.pool.ManagedObjectPoolLoader;
import net.officefloor.compile.pool.ManagedObjectPoolType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceContext;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceMetaData;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceProperty;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceSpecification;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.api.source.UnknownPropertyError;
import net.officefloor.frame.api.source.UnknownResourceError;

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
	private final NodeContext nodeContext;

	/**
	 * Instantiate.
	 * 
	 * @param node        {@link Node} requiring the {@link ManagedObjectPool}.
	 * @param nodeContext {@link NodeContext}.
	 */
	public ManagedObjectPoolLoaderImpl(Node node, NodeContext nodeContext) {
		this.node = node;
		this.nodeContext = nodeContext;
	}

	/*
	 * ================= ManagedObjectPoolLoader ===============================
	 */

	@Override
	public <PS extends ManagedObjectPoolSource> PropertyList loadSpecification(Class<PS> managedObjectPoolSourceClass) {

		// Instantiate the managed object pool source
		ManagedObjectPoolSource managedObjectPoolSource = CompileUtil.newInstance(managedObjectPoolSourceClass,
				ManagedObjectPoolSource.class, this.node, this.nodeContext.getCompilerIssues());
		if (managedObjectPoolSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		ManagedObjectPoolSourceSpecification specification;
		try {
			specification = managedObjectPoolSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + ManagedObjectPoolSourceSpecification.class.getSimpleName() + " from "
					+ managedObjectPoolSourceClass.getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No " + ManagedObjectPoolSourceSpecification.class.getSimpleName() + " returned from "
					+ managedObjectPoolSourceClass.getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		ManagedObjectPoolSourceProperty[] managedObjectPoolSourceProperties;
		try {
			managedObjectPoolSourceProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + ManagedObjectPoolSourceProperty.class.getSimpleName()
					+ " instances from " + ManagedObjectPoolSourceSpecification.class.getSimpleName() + " for "
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
					this.addIssue(ManagedObjectPoolSourceProperty.class.getSimpleName() + " " + i + " is null from "
							+ ManagedObjectPoolSourceSpecification.class.getSimpleName() + " for "
							+ managedObjectPoolSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = mopProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for " + ManagedObjectPoolSourceProperty.class.getSimpleName()
							+ " " + i + " from " + ManagedObjectPoolSourceSpecification.class.getSimpleName() + " for "
							+ managedObjectPoolSourceClass.getName(), ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(ManagedObjectPoolSourceProperty.class.getSimpleName() + " " + i
							+ " provided blank name from " + ManagedObjectPoolSourceSpecification.class.getSimpleName()
							+ " for " + managedObjectPoolSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = mopProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for " + ManagedObjectPoolSourceProperty.class.getSimpleName()
							+ " " + i + " (" + name + ") from "
							+ ManagedObjectPoolSourceSpecification.class.getSimpleName() + " for "
							+ managedObjectPoolSourceClass.getName(), ex);
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

		// Create an instance of the managed object pool source
		ManagedObjectPoolSource managedObjectPoolSource = CompileUtil.newInstance(managedObjectPoolSourceClass,
				ManagedObjectPoolSource.class, this.node, this.nodeContext.getCompilerIssues());
		if (managedObjectPoolSource == null) {
			return null; // failed to instantiate
		}

		// Load and return the managed object pool type
		return this.loadManagedObjectPoolType(managedObjectPoolSource, propertyList);
	}

	@Override
	public ManagedObjectPoolType loadManagedObjectPoolType(ManagedObjectPoolSource managedObjectPoolSource,
			PropertyList propertyList) {

		// Obtain qualified name
		String qualifiedName = this.node.getQualifiedName();

		// Obtain the overridden properties
		PropertyList overriddenProperties = this.nodeContext.overrideProperties(this.node, qualifiedName, propertyList);

		// Create the managed object pool source context to initialise
		ManagedObjectPoolSourceContext sourceContext = new ManagedObjectPoolSourceContextImpl(qualifiedName, true,
				new PropertyListSourceProperties(overriddenProperties), this.nodeContext.getRootSourceContext());

		// Initialise the managed object pool source
		ManagedObjectPoolSourceMetaData metaData;
		try {
			metaData = managedObjectPoolSource.init(sourceContext);

		} catch (UnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknownPropertyName() + "'");
			return null; // must have property

		} catch (UnknownClassError ex) {
			this.addIssue("Can not load class '" + ex.getUnknownClassName() + "'");
			return null; // must have class

		} catch (UnknownResourceError ex) {
			this.addIssue("Can not obtain resource at location '" + ex.getUnknownResourceLocation() + "'");
			return null; // must have resource

		} catch (Throwable ex) {
			this.addIssue("Failed to init", ex);
			return null; // must initialise
		}

		// Ensure have meta-data
		if (metaData == null) {
			this.addIssue("Returned null " + ManagedObjectPoolSourceMetaData.class.getSimpleName());
			return null; // must have meta-data
		}

		// Ensure handle any issue in interacting with meta-data
		Class<?> pooledObjectType;
		ManagedObjectPoolFactory managedObjectPoolFactory;
		ThreadCompletionListenerFactory[] threadCompletionListenerFactories;
		try {

			// Obtain the pooled object type
			pooledObjectType = metaData.getPooledObjectType();
			if (pooledObjectType == null) {
				this.addIssue("No pooled object type provided");
				return null; // must have object type
			}

			// Obtain the managed object pool factory
			managedObjectPoolFactory = metaData.getManagedObjectPoolFactory();
			if (managedObjectPoolFactory == null) {
				this.addIssue("No " + ManagedObjectPoolFactory.class.getSimpleName() + " provided");
				return null; // must have factory
			}

			// Obtain the thread completion listener factories
			threadCompletionListenerFactories = metaData.getThreadCompleteListenerFactories();
			if (threadCompletionListenerFactories == null) {
				// Default to none
				threadCompletionListenerFactories = new ThreadCompletionListenerFactory[0];
			}

		} catch (Throwable ex) {
			this.addIssue("Exception from " + managedObjectPoolSource.getClass().getName(), ex);
			return null; // must be successful with meta-data
		}

		// Create and return the managed object pool type
		return new ManagedObjectPoolTypeImpl(pooledObjectType, managedObjectPoolFactory,
				threadCompletionListenerFactories);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription Description of the issue.
	 */
	private void addIssue(String issueDescription) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription Description of the issue.
	 * @param cause            Cause of the issue.
	 */
	private void addIssue(String issueDescription, Throwable cause) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription, cause);
	}

}
