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
package net.officefloor.frame.impl;

import java.util.Properties;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ResourceLocator;

/**
 * Implementation of the
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext}.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceContextImpl implements
		ManagedObjectSourceContext {

	/**
	 * Name of the {@link ManagedObject}.
	 */
	protected final String managedObjectName;

	/**
	 * Properties.
	 */
	protected final Properties properties;

	/**
	 * Resource locator.
	 */
	protected final ResourceLocator resourceLocator;

	/**
	 * {@link ManagedObjectBuilder}.
	 */
	protected ManagedObjectBuilder managedObjectBuilder;

	/**
	 * {@link OfficeBuilder} for the office using the
	 * {@link ManagedObjectSource}.
	 */
	protected OfficeBuilder officeBuilder;

	/**
	 * {@link OfficeFrame}.
	 */
	protected OfficeFrame officeFrame;

	/**
	 * Name of the {@link Work} to clean up this {@link ManagedObject}.
	 */
	protected String recycleWorkName = null;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param properties
	 *            Properties.
	 * @param resourceLocator
	 *            {@link ResourceLocator}.
	 * @param managedObjectBuilder
	 *            {@link ManagedObjectBuilder}.
	 * @param officeBuilder
	 *            {@link OfficeBuilder} for the office using the
	 *            {@link ManagedObjectSource}.
	 * @param officeFrame
	 *            {@link OfficeFrame}.
	 */
	public ManagedObjectSourceContextImpl(String managedObjectName,
			Properties properties, ResourceLocator resourceLocator,
			ManagedObjectBuilder managedObjectBuilder,
			OfficeBuilder officeBuilder, OfficeFrame officeFrame) {
		this.managedObjectName = managedObjectName;
		this.properties = properties;
		this.resourceLocator = resourceLocator;
		this.managedObjectBuilder = managedObjectBuilder;
		this.officeBuilder = officeBuilder;
		this.officeFrame = officeFrame;
	}

	/**
	 * Indicates that the
	 * {@link ManagedObjectSource#init(ManagedObjectSourceContext)} method has
	 * completed.
	 */
	public void flagInitOver() {
		// Disallow further configuration
		this.managedObjectBuilder = null;
		this.officeBuilder = null;
		this.officeFrame = null;
	}

	/**
	 * Obtains the name of the {@link Work} to recycle this
	 * {@link ManagedObject}.
	 * 
	 * @return name of the {@link Work} to recycle this {@link ManagedObject} or
	 *         <code>null</code> if no recycling of this {@link ManagedObject}.
	 */
	public String getRecycleWorkName() {
		return this.recycleWorkName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getProperties()
	 */
	public Properties getProperties() {
		return this.properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getResourceLocator()
	 */
	public ResourceLocator getResourceLocator() {
		return this.resourceLocator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getManagedObjectBuilder()
	 */
	public ManagedObjectBuilder getManagedObjectBuilder() {
		return this.managedObjectBuilder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getOfficeBuilder()
	 */
	public OfficeBuilder getOfficeBuilder() {
		return this.officeBuilder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getOfficeFloor()
	 */
	public OfficeFrame getOfficeFrame() {
		return this.officeFrame;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getCleanupWorkBuilder(java.lang.Class)
	 */
	public <W extends Work> WorkBuilder<W> getRecycleWorkBuilder(
			Class<W> typeOfWork) {

		// Ensure not already created
		if (this.recycleWorkName != null) {
			throw new IllegalStateException(
					"Only one clean up per Managed Object");
		}

		// Create the clean up work
		WorkBuilder<W> workBuilder = this.getOfficeFrame().getMetaDataFactory()
				.createWorkBuilder(typeOfWork);

		// Name the clean up work
		this.recycleWorkName = RawManagedObjectMetaData.MANAGED_OBJECT_CLEAN_UP_WORK_PREFIX
				+ this.managedObjectName;

		// Register the clean up work builder
		try {
			this.officeBuilder.addWork(this.recycleWorkName, workBuilder);
		} catch (BuildException ex) {
			// TODO: consider how to propagate
			throw new Error(ex);
		}

		// Return the clean up work
		return workBuilder;
	}

}
