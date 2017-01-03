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
package net.officefloor.frame.impl.execute.managedobject.coordinate;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure can have multiple {@link CoordinatingManagedObject} dependencies that
 * ensure coordination of dependency before the
 * {@link CoordinatingManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class ChainCoordinateManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link AsynchronousListener} instances to trigger.
	 */
	private static final List<AsynchronousListener> listeners = new ArrayList<AsynchronousListener>();

	/**
	 * Ensure that {@link CoordinatingManagedObject} can depend on another
	 * {@link CoordinatingManagedObject}.
	 */
	public void testChainingTogetherCoordinatingManagedObjects() throws Exception {

		// Ensure no listeners
		listeners.clear();

		// Obtain the office name
		final String officeName = this.getOfficeName();

		// Calculate expected coordination
		String identifier = "0";
		String expectedCoordination = identifier;

		// Construct the seed value
		this.constructManagedObject(identifier, identifier, officeName);
		this.getOfficeBuilder().addProcessManagedObject(identifier, identifier);

		// Create chaining of coordination (start identifier after seed)
		for (int i = 1; i < 100; i++) {

			// Obtain the identifier for this coordination
			identifier = String.valueOf(i);

			// Obtain the identifier for previous in chain
			final String PREVIOUS = String.valueOf(i - 1);

			// Calculate the expected coordination after this chaining
			expectedCoordination = identifier + "-" + expectedCoordination;

			// Provide the chained coordinating managed object
			ManagedObjectBuilder<None> moBuilder = this.constructManagedObject(identifier,
					ChainCoordinatingManagedObjectSource.class);
			moBuilder.addProperty(ChainCoordinatingManagedObjectSource.PROPERTY_PREFIX, identifier);
			moBuilder.setManagingOffice(officeName);
			moBuilder.setTimeout(100000); // 100 seconds (large, not fail test)
			DependencyMappingBuilder dependencies = this.getOfficeBuilder().addProcessManagedObject(identifier,
					identifier);
			dependencies.mapDependency(0, PREVIOUS);
		}

		// Construct the work
		ChainCoordinatingWork coordinate = new ChainCoordinatingWork();
		ReflectiveFunctionBuilder task = this.constructFunction(coordinate, "service");
		task.buildObject(identifier);

		// Invoke the work
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();
		FunctionManager functionManager = officeFloor.getOffice(officeName).getFunctionManager("service");
		boolean[] isComplete = new boolean[] { false };
		functionManager.invokeProcess(null, (escalation) -> isComplete[0] = true);

		// Allow processing of work
		while (!isComplete[0]) {

			// Ensure have completion listener
			// (otherwise should be complete)
			assertTrue("Should have completion listener", listeners.size() > 0);

			// Trigger completion of listeners
			while (listeners.size() > 0) {
				AsynchronousListener listener = listeners.remove(0);
				listener.notifyComplete();
			}
		}

		// Verify the resulting coordination
		assertEquals("Incorrect coordination", expectedCoordination, coordinate.coordination);
	}

	/**
	 * Functionality for obtaining the resulting chained coordination.
	 */
	public static class ChainCoordinatingWork {

		/**
		 * Result of chained coordination.
		 */
		public volatile String coordination = null;

		/**
		 * {@link ManagedFunction} to receive results of chained coordination.
		 * 
		 * @param coordination
		 *            Result of chained coordination.
		 */
		public void service(String coordination) {
			this.coordination = coordination;
		}
	}

	/**
	 * Chaining together {@link CoordinatingManagedObject}.
	 */
	public static class ChainCoordinatingManagedObjectSource extends AbstractManagedObjectSource<Indexed, None> {

		/**
		 * Name of property to specify the prefix.
		 */
		public static final String PROPERTY_PREFIX = "prefix";

		/**
		 * Prefix to apply to dependency.
		 */
		private String prefix;

		/*
		 * ====================== ManagedObjectSource =======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty(PROPERTY_PREFIX);
		}

		@Override
		protected void loadMetaData(MetaDataContext<Indexed, None> context) throws Exception {
			ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

			// Obtain the prefix
			this.prefix = mosContext.getProperty(PROPERTY_PREFIX);

			// Specify the meta-data
			context.setObjectClass(String.class);
			context.setManagedObjectClass(ChainCoordinatingManagedObject.class);
			context.addDependency(String.class).setLabel("Dependency");
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return new ChainCoordinatingManagedObject(this.prefix);
		}
	}

	/**
	 * Chaining together {@link CoordinatingManagedObject}.
	 */
	public static class ChainCoordinatingManagedObject
			implements CoordinatingManagedObject<Indexed>, AsynchronousManagedObject {

		/**
		 * Prefix to apply to the dependency.
		 */
		private final String prefix;

		/**
		 * {@link AsynchronousListener}.
		 */
		private AsynchronousListener listener;

		/**
		 * Dependency to return as object.
		 */
		private String dependency;

		/**
		 * Initiate.
		 * 
		 * @param prefix
		 *            Prefix to apply to dependency.
		 */
		public ChainCoordinatingManagedObject(String prefix) {
			this.prefix = prefix;
		}

		/*
		 * ======================== ManagedObject ============================
		 */

		@Override
		public void registerAsynchronousCompletionListener(AsynchronousListener listener) {
			this.listener = listener;
		}

		@Override
		public void loadObjects(ObjectRegistry<Indexed> registry) throws Throwable {
			// Obtain the dependency (applying prefix)
			this.dependency = this.prefix + "-" + ((String) registry.getObject(0));

			// Trigger asynchronous operation (so continues coordinating)
			this.listener.notifyStarted();

			// Register the lister for triggering completion
			listeners.add(this.listener);
		}

		@Override
		public Object getObject() throws Throwable {
			// Return the dependency
			return this.dependency;
		}
	}

}