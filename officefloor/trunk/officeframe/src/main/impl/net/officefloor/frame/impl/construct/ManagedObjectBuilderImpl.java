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
package net.officefloor.frame.impl.construct;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeScope;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.HandlerConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.pool.ManagedObjectPool;

/**
 * Implements the {@link net.officefloor.frame.api.build.ManagedObjectBuilder}.
 * 
 * @author Daniel
 */
public class ManagedObjectBuilderImpl<H extends Enum<H>> implements
		ManagedObjectBuilder<H>, ManagedObjectSourceConfiguration {

	/**
	 * Registry of {@link HandlerBuilder} instances.
	 */
	private final Map<Enum, HandlerBuilderImpl<H, ?>> handlers = new HashMap<Enum, HandlerBuilderImpl<H, ?>>();

	/**
	 * Name of {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	private String managedObjectName;

	/**
	 * Name of {@link net.officefloor.frame.api.manage.Office} managing this
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	private String managingOfficeName;

	/**
	 * {@link Class} of the {@link ManagedObjectSource}.
	 */
	private Class managedObjectSourceClass;

	/**
	 * {@link Properties} for the {@link ManagedObjectSource}.
	 */
	private Properties properties = new Properties();

	/**
	 * {@link OfficeScope} for the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	private OfficeScope managedObjectScope = OfficeScope.WORK;

	/**
	 * {@link ManagedObjectPool}.
	 */
	private ManagedObjectPool pool;

	/**
	 * Default timeout for asynchronous operations on the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	private long defaultTimeout = 0;

	/**
	 * Specifies the name for this
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * 
	 * @param name
	 *            Name for this
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	protected void setManagedObjectName(String name) {
		this.managedObjectName = name;
	}

	/*
	 * ====================================================================
	 * ManagedObjectBuilder
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.construct.managedobject.ManagedObjectMetaData#setManagedObjectSourceClass(java.lang.Class)
	 */
	public <S extends ManagedObjectSource> void setManagedObjectSourceClass(
			Class<S> managedObjectSourceClass) {
		this.managedObjectSourceClass = managedObjectSourceClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.construct.managedobject.ManagedObjectMetaData#addProperty(java.lang.String,
	 *      java.lang.String)
	 */
	public void addProperty(String name, String value) {
		this.properties.put(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.ManagedObjectBuilder#setManagedObjectPool(net.officefloor.frame.internal.structure.ManagedObjectPool)
	 */
	public void setManagedObjectPool(ManagedObjectPool pool) {
		this.pool = pool;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.ManagedObjectBuilder#setDefaultTimeout(long)
	 */
	public void setDefaultTimeout(long timeout) throws BuildException {
		this.defaultTimeout = timeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.ManagedObjectBuilder#setManagingOffice(java.lang.String)
	 */
	public void setManagingOffice(String officeName) throws BuildException {
		this.managingOfficeName = officeName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.ManagedObjectBuilder#registerHandler(java.lang.String,
	 *      java.lang.Class)
	 */
	public <F extends Enum<F>> HandlerBuilder<F> registerHandler(H key,
			Class<F> processListingEnum) throws BuildException {

		// Create the handler builder
		HandlerBuilderImpl<H, F> handlerBuilder = new HandlerBuilderImpl<H, F>(
				key);

		// Register the handler
		this.handlers.put(key, handlerBuilder);

		// Return the builder
		return handlerBuilder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.ManagedObjectBuilder#registerHandler(java.lang.String)
	 */
	public HandlerBuilder<Indexed> registerHandler(H key) throws BuildException {

		// Create the handler builder
		HandlerBuilderImpl<H, Indexed> handlerBuilder = new HandlerBuilderImpl<H, Indexed>(
				key);

		// Register the handler
		this.handlers.put(key, handlerBuilder);

		// Return the builder
		return handlerBuilder;
	}

	/*
	 * ====================================================================
	 * ManagedObjectSourceConfiguration
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration#getManagedObjectName()
	 */
	public String getManagedObjectName() {
		return this.managedObjectName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration#getManagingOfficeName()
	 */
	public String getManagingOfficeName() {
		return this.managingOfficeName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration#getManagedObjectSourceClass()
	 */
	@SuppressWarnings("unchecked")
	public <MS extends ManagedObjectSource> Class<MS> getManagedObjectSourceClass()
			throws ConfigurationException {
		return this.managedObjectSourceClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration#getProperties()
	 */
	public Properties getProperties() {
		return this.properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration#getManagedObjectScope()
	 */
	public OfficeScope getManagedObjectScope() {
		return this.managedObjectScope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration#getManagedObjectPool()
	 */
	public ManagedObjectPool getManagedObjectPool() {
		return this.pool;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration#getDefaultTimeout()
	 */
	public long getDefaultTimeout() {
		return this.defaultTimeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration#getHandlerConfiguration()
	 */
	public HandlerConfiguration<?, ?>[] getHandlerConfiguration() {
		// Return the handler configuration
		return this.handlers.values().toArray(new HandlerConfiguration[0]);
	}

}
