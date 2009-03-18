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
package net.officefloor.compile.impl.work;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.WorkLoader;
import net.officefloor.compile.spi.work.WorkType;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkSpecification;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.desk.DeskModel;

/**
 * Tests loading the {@link WorkType} from the {@link WorkSource}.
 * 
 * @author Daniel
 */
public class LoadWorkTypeTest extends OfficeFrameTestCase {

	/**
	 * Location of the {@link DeskModel}.
	 */
	private final String DESK_LOCATION = "DESK";

	/**
	 * Name of the {@link Work}.
	 */
	private final String WORK_NAME = "WORK";

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues = this.createMock(CompilerIssues.class);

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property

		// Attempt to load work type
		this.loadWorkType(false, new Loader() {
			@Override
			public void sourceWork(WorkTypeBuilder<Work> work,
					WorkSourceContext context) throws Exception {
				context.getProperty("missing");
			}
		});
	}

	/**
	 * Loads the {@link WorkType} within the input {@link Loader}.
	 * 
	 * @param isExpectedToLoad
	 *            Flag indicating if expecting to load the {@link WorkType}.
	 * @param loader
	 *            {@link Loader}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 * @return Loaded {@link WorkType}.
	 */
	private WorkType<Work> loadWorkType(boolean isExpectedToLoad,
			Loader loader, String... propertyNameValuePairs) {

		// Replay mock objects
		this.replayMockObjects();

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Create the work loader and load the work
		WorkLoader workLoader = new WorkLoaderImpl(DESK_LOCATION, WORK_NAME);
		MockWorkSource.loader = loader;
		WorkType<Work> workType = workLoader.loadWork(MockWorkSource.class,
				propertyList, loader.getClass().getClassLoader(), this.issues);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the work type", workType);
		} else {
			assertNull("Should not load the work type", workType);
		}

		// Return the work type
		return workType;
	}

	/**
	 * Implemented to load the {@link WorkType}.
	 */
	private interface Loader {

		/**
		 * Implemented to load the {@link WorkType}.
		 * 
		 * @param work
		 *            {@link WorkTypeBuilder}.
		 * @param context
		 *            {@link WorkSourceContext}.
		 * @throws Exception
		 *             If fails to source {@link WorkType}.
		 */
		void sourceWork(WorkTypeBuilder<Work> work, WorkSourceContext context)
				throws Exception;
	}

	/**
	 * Mock {@link WorkSource} for testing.
	 */
	public static class MockWorkSource implements WorkSource<Work> {

		/**
		 * {@link Loader} to load the {@link WorkType}.
		 */
		public static Loader loader;

		/*
		 * ================ WorkSource ======================================
		 */

		@Override
		public WorkSpecification getSpecification() {
			fail("Should not be invoked in obtaining work type");
			return null;
		}

		@Override
		public void sourceWork(WorkTypeBuilder<Work> workTypeBuilder,
				WorkSourceContext context) throws Exception {
			loader.sourceWork(workTypeBuilder, context);
		}
	}

}