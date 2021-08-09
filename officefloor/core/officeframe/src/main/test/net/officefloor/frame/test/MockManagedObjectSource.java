/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.test;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.api.source.TestSource;

/**
 * Mock implementation of the {@link ManagedObjectSource} for testing.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
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
	 * @param <F>
	 *            Flow type.
	 * @param name
	 *            Name to bind under.
	 * @param managedObject
	 *            {@link ManagedObject} to bind.
	 * @param sourceMetaData
	 *            {@link ManagedObjectSourceMetaData} to bind.
	 * @param officeFloorBuilder
	 *            {@link OfficeFloorBuilder} to bind {@link ManagedObject}.
	 * @return {@link ManagedObjectBuilder} for additional configuration.
	 */
	@SuppressWarnings("unchecked")
	public static <F extends Enum<F>> ManagedObjectBuilder<F> bindManagedObject(String name,
			ManagedObject managedObject, ManagedObjectSourceMetaData<?, F> sourceMetaData,
			OfficeFloorBuilder officeFloorBuilder) {

		// Create the Managed Object Builder
		ManagedObjectBuilder<None> builder = officeFloorBuilder.addManagedObject(name, MockManagedObjectSource.class);

		// Provide managed object link to meta-data
		builder.addProperty(MANAGED_OBJECT_PROPERTY, name);

		// Create the Managed Object Source State
		ManagedObjectSourceState state = new ManagedObjectSourceState();
		state.managedObject = managedObject;
		state.managedObjectSourceMetaData = sourceMetaData;

		// Bind the managed object in registry
		REGISTRY.put(name, state);

		// Return the builder
		return (ManagedObjectBuilder<F>) builder;
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
	 * =================== ManagedObjectSource ===============================
	 */

	@Override
	public ManagedObjectSourceSpecification getSpecification() {
		throw new UnsupportedOperationException("Not supported by mock implementation");
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ManagedObjectSourceMetaData<None, None> init(ManagedObjectSourceContext context) throws Exception {

		// Obtain the name of the Managed Object
		String name = context.getProperties().getProperty(MANAGED_OBJECT_PROPERTY);

		// Ensure have Managed Object
		if (name == null) {
			throw new Exception(
					"Property '" + MANAGED_OBJECT_PROPERTY + "' must be specified - likely that not bound.");
		}

		// Obtain the Managed Object Source State
		this.managedObjectSourceState = REGISTRY.get(name);

		// Obtain the managed object source meta-data
		return this.managedObjectSourceState.managedObjectSourceMetaData;
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context) throws Exception {
		// Mock not require starting
	}

	@Override
	public void sourceManagedObject(ManagedObjectUser user) {
		user.setManagedObject(this.managedObjectSourceState.managedObject);
	}

	@Override
	public void stop() {
		// Nothing to stop
	}

	/**
	 * State of the {@link ManagedObjectSource}.
	 */
	private static class ManagedObjectSourceState {

		/**
		 * {@link ManagedObject}.
		 */
		public ManagedObject managedObject;

		/**
		 * {@link ManagedObject}.
		 */
		@SuppressWarnings("rawtypes")
		public ManagedObjectSourceMetaData managedObjectSourceMetaData;

	}

}
