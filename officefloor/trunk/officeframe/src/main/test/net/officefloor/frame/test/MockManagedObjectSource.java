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
package net.officefloor.frame.test;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;

/**
 * Mock implementation of the
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}
 * for testing.
 * 
 * @author Daniel
 */
public class MockManagedObjectSource implements ManagedObjectSource<None, None> {

	/**
	 * Property name to source the {@link ManagedObject}.
	 */
	private static final String MANAGED_OBJECT_PROPERTY = "net.officefloor.frame.construct.managedobject";

	/**
	 * Registry of the {@link ManagedObject} instances.
	 */
	private static final Map<String, ManagedObjectSourceState> REGISTRY = new HashMap<String, ManagedObjectSourceState>();

	/**
	 * Convenience method to bind the {@link ManagedObject} instance to the
	 * {@link ManagedObjectBuilder}.
	 * 
	 * @param metaData
	 *            {@link ManagedObjectBuilder} to bind the {@link ManagedObject}.
	 * @param name
	 *            Name to bind under.
	 * @param managedObject
	 *            {@link ManagedObject} to bind.
	 * @param sourceMetaData
	 *            {@link ManagedObjectSourceMetaData} to bind.
	 * @throws BuildException
	 *             If bind fails.
	 */
	@SuppressWarnings("unchecked")
	public static void bindManagedObject(ManagedObjectBuilder metaData,
			String name, ManagedObject managedObject,
			ManagedObjectSourceMetaData<?, ?> sourceMetaData)
			throws BuildException {

		// Specify the managed object source class
		metaData.setManagedObjectSourceClass(MockManagedObjectSource.class);

		// Provide managed object link to meta-data
		metaData.addProperty(MANAGED_OBJECT_PROPERTY, name);

		// Create the Managed Object Source State
		ManagedObjectSourceState state = new ManagedObjectSourceState();
		state.managedObject = managedObject;
		state.managedObjectSourceMetaData = sourceMetaData;

		// Bind the managed object in registry
		REGISTRY.put(name, state);
	}

	/**
	 * {@link ManagedObjectSourceState}.
	 */
	protected ManagedObjectSourceState managedObjectSourceState;

	/**
	 * Default constructor.
	 */
	public MockManagedObjectSource() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#getSpecification()
	 */
	public ManagedObjectSourceSpecification getSpecification() {
		throw new UnsupportedOperationException(
				"Not supported by mock implementation");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#init(java.util.Properties,
	 *      net.officefloor.frame.spi.managedobject.source.ResourceLocator,
	 *      net.officefloor.frame.spi.managedobject.source.ManagedObjectPoolFactory)
	 */
	public void init(ManagedObjectSourceContext context) throws Exception {

		// Obtain the name of the Managed Object
		String name = context.getProperties().getProperty(
				MANAGED_OBJECT_PROPERTY);

		// Ensure have Managed Object
		if (name == null) {
			throw new Exception("Property '" + MANAGED_OBJECT_PROPERTY
					+ "' must be specified - likely that not bound.");
		}

		// Obtain the Managed Object Source State
		this.managedObjectSourceState = REGISTRY.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#getMetaData()
	 */
	@SuppressWarnings("unchecked")
	public ManagedObjectSourceMetaData<None, None> getMetaData() {
		return this.managedObjectSourceState.managedObjectSourceMetaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#start(net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext)
	 */
	public void start(ManagedObjectExecuteContext<None> context)
			throws Exception {
		// Mock not require starting
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#getManagedObject()
	 */
	public void sourceManagedObject(ManagedObjectUser user) {
		user.setManagedObject(this.managedObjectSourceState.managedObject);
	}

	/**
	 * State of the
	 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}.
	 */
	private static class ManagedObjectSourceState {

		/**
		 * {@link ManagedObject}.
		 */
		public ManagedObject managedObject;

		/**
		 * {@link ManagedObject}.
		 */
		@SuppressWarnings("unchecked")
		public ManagedObjectSourceMetaData managedObjectSourceMetaData;

	}

}
