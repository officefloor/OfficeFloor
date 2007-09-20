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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.managedobject.source.impl.ManagedObjectSourcePropertyImpl;

/**
 * Mock
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}.
 * 
 * @author Daniel
 */
public abstract class AbstractMockManagedObjectSource<D extends Enum<D>, H extends Enum<H>>
		implements ManagedObjectSource {

	/**
	 * {@link ManagedObjectSourceContext}.
	 */
	private ManagedObjectSourceContext sourceContext;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<H> executeContext;

	/**
	 * Obtains the {@link ManagedObjectSourceContext}.
	 * 
	 * @return {@link ManagedObjectSourceContext}.
	 */
	protected final ManagedObjectSourceContext getSourceContext() {
		return this.sourceContext;
	}

	/**
	 * Obtains the {@link ManagedObjectExecuteContext}.
	 * 
	 * @return {@link ManagedObjectExecuteContext}.
	 */
	protected final ManagedObjectExecuteContext<H> getExecuteContext() {
		return this.executeContext;
	}

	/**
	 * Override to specify the property names.
	 * 
	 * @return Property names.
	 */
	protected String[] getPropertyNames() {
		return null;
	}

	/**
	 * Override to initialise.
	 * 
	 * @throws Exception
	 *             If fails.
	 */
	protected void init() throws Exception {
	}

	/**
	 * Override to specify dependency meta-data.
	 * 
	 * @param dependencyKeys
	 *            Allows specifying keys.
	 * @param dependencies
	 *            Dependency types.
	 */
	protected void initDependencies(PassByReference<Class<D>> dependencyKeys,
			Map<D, Class<?>> dependencies) {
	}

	/**
	 * Override to specify handler meta-data.
	 * 
	 * @param handlerKeys
	 *            Allow specifying keys.
	 * @param handlers
	 *            Handler types.
	 */
	protected void initHandlers(PassByReference<Class<H>> handlerKeys,
			Map<H, Class<?>> handlers) {
	}

	/**
	 * Override to specify the {@link ManagedObject} class.
	 * 
	 * @return {@link ManagedObject} class.
	 */
	protected Class<?> getManagedObjectClass() {
		return ManagedObject.class;
	}

	/**
	 * Override to specify the object class.
	 * 
	 * @return Object class.
	 */
	protected Class<?> getObjectClass() {
		return Object.class;
	}

	/**
	 * Override to start.
	 * 
	 * @throws Exception
	 *             If fails.
	 */
	protected void start() throws Exception {
	}

	/**
	 * Override to source the {@link ManagedObject} instances.
	 * 
	 * @return {@link ManagedObject}.
	 * @throws Exception
	 *             Failure sourcing the {@link ManagedObject}.
	 */
	protected ManagedObject sourceManagedObject() throws Exception {
		return new ManagedObject() {
			public Object getObject() throws Exception {
				return new Object();
			}
		};
	}

	/*
	 * ====================================================================
	 * ManagedObjectSource
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#getSpecification()
	 */
	public final ManagedObjectSourceSpecification getSpecification() {
		return new MockManagedObjectSourceSpecification(this.getPropertyNames());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#init(net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext)
	 */
	public final void init(ManagedObjectSourceContext context) throws Exception {
		// Specify context
		this.sourceContext = context;

		// Initialise
		this.init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#getMetaData()
	 */
	@SuppressWarnings("unchecked")
	public final ManagedObjectSourceMetaData<?, ?> getMetaData() {

		// Create the listing of dependencies
		final Class[] dependencyKeys = new Class[1];
		Map<D, Class<?>> dependencies = new HashMap();
		this.initDependencies(new PassByReference<Class<D>>() {
			public void setValue(Class<D> value) {
				dependencyKeys[0] = value;
			}
		}, dependencies);

		// Create the listing of handlers
		final Class[] handlerKeys = new Class[1];
		Map<H, Class<?>> handlers = new HashMap();
		this.initHandlers(new PassByReference<Class<H>>() {
			public void setValue(Class<H> value) {
				handlerKeys[0] = value;
			}
		}, handlers);

		// Return the meta-data
		return new MockManagedObjectSourceMetaData(
				this.getManagedObjectClass(), this.getObjectClass(),
				dependencyKeys[0], dependencies, handlerKeys[0], handlers);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#start(net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext)
	 */
	@SuppressWarnings("unchecked")
	public final void start(ManagedObjectExecuteContext<?> context)
			throws Exception {
		// Specify context
		this.executeContext = (ManagedObjectExecuteContext<H>) context;

		// Start
		this.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#sourceManagedObject(net.officefloor.frame.spi.managedobject.source.ManagedObjectUser)
	 */
	public final void sourceManagedObject(ManagedObjectUser user) {
		try {
			// Source the managed object
			ManagedObject managedObject = this.sourceManagedObject();

			// Specify if have
			if (managedObject != null) {
				user.setManagedObject(managedObject);
			}

		} catch (Throwable ex) {
			// Specify failure
			user.setFailure(ex);
		}
	}

}

/**
 * Mock
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification}.
 */
class MockManagedObjectSourceSpecification implements
		ManagedObjectSourceSpecification {

	/**
	 * Properties.
	 */
	protected final ManagedObjectSourceProperty[] properties;

	/**
	 * Initiate.
	 * 
	 * @param propertyNames
	 *            Names of the properties.
	 */
	public MockManagedObjectSourceSpecification(String[] propertyNames) {
		// Load the properties from the input names
		if (propertyNames == null) {
			this.properties = new ManagedObjectSourceProperty[0];
		} else {
			this.properties = new ManagedObjectSourceProperty[propertyNames.length];
			for (int i = 0; i < this.properties.length; i++) {
				this.properties[i] = new ManagedObjectSourcePropertyImpl(
						propertyNames[i], propertyNames[i]);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification#getProperties()
	 */
	public ManagedObjectSourceProperty[] getProperties() {
		// Return the properties
		return this.properties;
	}
}
