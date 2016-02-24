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
package net.officefloor.compile.impl.managedobject;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link ManagedObjectLoader} in loading the
 * {@link OfficeFloorManagedObjectSourceType}.
 *
 * @author Daniel Sagenschneider
 */
public class LoadOfficeFloorManagedObjectSourceTypeTest extends
		OfficeFrameTestCase {

	/**
	 * Ensure can load via {@link ClassManagedObjectSource} {@link Class}.
	 */
	public void testLoadByClass() {

		// Name of the managed object source
		final String MANAGED_OBJECT_SOURCE_NAME = "MOS";

		// Configure test
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());

		// Configure to load simple class
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME).setValue(
				MockLoadManagedObject.class.getName());

		// Obtain node context
		NodeContext nodeContext = (NodeContext) compiler;

		// Load the officeflooor managed object source type
		ManagedObjectLoader moLoader = nodeContext.getManagedObjectLoader(
				LocationType.OFFICE_FLOOR, "OFFICE_FLOOR",
				MANAGED_OBJECT_SOURCE_NAME);
		OfficeFloorManagedObjectSourceType mosType = moLoader
				.loadOfficeFloorManagedObjectSourceType(
						ClassManagedObjectSource.class, properties);
		MockLoadManagedObject.assertOfficeFloorManagedObjectSourceType(mosType,
				MANAGED_OBJECT_SOURCE_NAME);
	}

	/**
	 * Ensure can load via {@link ClassManagedObjectSource} instance.
	 */
	public void testLoadByInstance() {

		// Name of the managed object source
		final String MANAGED_OBJECT_SOURCE_NAME = "MOS";

		// Configure test
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());

		// Configure to load simple class
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME).setValue(
				MockLoadManagedObject.class.getName());

		// Obtain node context
		NodeContext nodeContext = (NodeContext) compiler;

		// Load the officeflooor managed object source type
		ManagedObjectLoader moLoader = nodeContext.getManagedObjectLoader(
				LocationType.OFFICE_FLOOR, "OFFICE_FLOOR",
				MANAGED_OBJECT_SOURCE_NAME);
		OfficeFloorManagedObjectSourceType mosType = moLoader
				.loadOfficeFloorManagedObjectSourceType(
						new ClassManagedObjectSource(), properties);
		MockLoadManagedObject.assertOfficeFloorManagedObjectSourceType(mosType,
				MANAGED_OBJECT_SOURCE_NAME);
	}

	/**
	 * Ensures issue if failure in obtaining the
	 * {@link ManagedObjectSourceSpecification}.
	 */
	public void testFailGetManagedObjectSourceSpecification() {

		final Error failure = new Error("specification failure");
		final CompilerIssues issues = this.createMock(CompilerIssues.class);

		// Record failure to instantiate
		issues.addIssue(LocationType.OFFICE_FLOOR, "OFFICE_FLOOR",
				AssetType.MANAGED_OBJECT, "mos",
				"Failed to obtain ManagedObjectSourceSpecification from "
						+ MockManagedObjectSource.class.getName(), failure);

		// Attempt to obtain specification
		MockManagedObjectSource.reset();
		MockManagedObjectSource.specificationFailure = failure;
		this.replayMockObjects();

		// Configure test
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Obtain node context
		NodeContext nodeContext = (NodeContext) compiler;

		// Load the officelooor managed object source type
		ManagedObjectLoader moLoader = nodeContext.getManagedObjectLoader(
				LocationType.OFFICE_FLOOR, "OFFICE_FLOOR", "mos");
		OfficeFloorManagedObjectSourceType mosType = moLoader
				.loadOfficeFloorManagedObjectSourceType(
						MockManagedObjectSource.class,
						compiler.createPropertyList());

		// Ensure not loaded
		TestCase.assertNull("Should not load type", mosType);

		this.verifyMockObjects();
	}

	/**
	 * Mock {@link ManagedObjectSource} for testing.
	 */
	@TestSource
	public static class MockManagedObjectSource implements
			ManagedObjectSource<None, None> {

		/**
		 * Failure to obtain the {@link ManagedObjectSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * Resets the state for next test.
		 */
		public static void reset() {
			specificationFailure = null;
		}

		/*
		 * ================ ManagedObjectSource ================================
		 */

		@Override
		public ManagedObjectSourceSpecification getSpecification() {
			// Throw failure
			throw specificationFailure;
		}

		@Override
		public void init(ManagedObjectSourceContext<None> context)
				throws Exception {
			fail("Should not be invoked for obtaining specification");
		}

		@Override
		public ManagedObjectSourceMetaData<None, None> getMetaData() {
			fail("Should not be invoked for obtaining specification");
			return null;
		}

		@Override
		public void start(ManagedObjectExecuteContext<None> context)
				throws Exception {
			fail("Should not be invoked for obtaining specification");
		}

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			fail("Should not be invoked for obtaining specification");
		}

		@Override
		public void stop() {
			fail("Should not be invoked for obtaining specification");
		}
	}

}