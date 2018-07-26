/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.executive;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.internal.configuration.ExecutiveConfiguration;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawExecutiveMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawExecutiveMetaDataTest extends OfficeFrameTestCase {

	/**
	 * Name of the {@link OfficeFloor}.
	 */
	private static final String OFFICE_FLOOR_NAME = "OFFICE_FLOOR_NAME";

	/**
	 * {@link Executive} {@link AssetType} name.
	 */
	private static final String EXECUTIVE_NAME = "Executive";

	/**
	 * {@link ExecutiveConfiguration}.
	 */
	private ExecutiveBuilderImpl<?> configuration = new ExecutiveBuilderImpl<>(ExecutiveSource.class);

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContextImpl sourceContext = new SourceContextImpl(false,
			Thread.currentThread().getContextClassLoader());

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * Ensures issue if no {@link ExecutiveSource}.
	 */
	public void testNoExecutiveSource() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>((ExecutiveSource) null);
		this.issues.addIssue(AssetType.EXECUTIVE, "Executive", "No ExecutiveSource class provided");

		// Attempt to construct team
		this.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fail to instantiate the {@link ExecutiveSource}.
	 */
	public void testExecutiveSourceInstantiateFailure() {

		final Exception failure = new Exception("Instantiate failure");
		this.configuration = new ExecutiveBuilderImpl<>(FailInstantiateExecutiveSource.class);
		FailInstantiateExecutiveSource.instantiateFailure = failure;
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
				"Failed to instantiate " + FailInstantiateExecutiveSource.class.getName(), failure);

		// Attempt to construct executive
		this.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * {@link ExecutiveSource} that will be failed to instantiate.
	 */
	@TestSource
	public static class FailInstantiateExecutiveSource extends ExecutiveSourceAdapter {

		/**
		 * {@link Exception} to be thrown on instantiating.
		 */
		public static Exception instantiateFailure;

		/**
		 * Constructor that will fail instantiation.
		 * 
		 * @throws Exception Failure to instantiate.
		 */
		public FailInstantiateExecutiveSource() throws Exception {
			throw instantiateFailure;
		}
	}

	/**
	 * Ensures issue if required property is not specified.
	 */
	public void testMissingProperty() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(NoPropertyExecutiveSource.class);
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
				"Must specify property '" + NoPropertyExecutiveSource.PROPERTY_NAME + "'");

		// Construct
		this.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * {@link ExecutiveSource} that obtains a property and creates a
	 * {@link Executive}.
	 */
	@TestSource
	public static class NoPropertyExecutiveSource extends ExecutiveSourceAdapter {

		/**
		 * Name of the required property.
		 */
		public static final String PROPERTY_NAME = "required.property";

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			context.getProperty(PROPERTY_NAME);
			fail("Should not sucessfully obtain property");
			return super.createExecutive(context);
		}
	}

	/**
	 * Ensures issue if required class is not available.
	 */
	public void testMissingClass() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(NoClassExecutiveSource.class);
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
				"Can not load class '" + NoClassExecutiveSource.CLASS_NAME + "'");

		// Attempt to construct executive
		this.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * {@link ExecutiveSource} that obtains a {@link Class} and creates a
	 * {@link Executive}.
	 */
	@TestSource
	public static class NoClassExecutiveSource extends ExecutiveSourceAdapter {

		/**
		 * Name of the required {@link Class}.
		 */
		public static final String CLASS_NAME = "REQUIRED CLASS";

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			context.loadClass(CLASS_NAME);
			fail("Should not successfully load class");
			return super.createExecutive(context);
		}
	}

	/**
	 * Ensures issue if required resource is not available.
	 */
	public void testMissingResource() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(NoResourceExecutiveSource.class);
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
				"Can not obtain resource at location '" + NoResourceExecutiveSource.RESOURCE_LOCATION + "'");

		// Construct
		this.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * {@link ExecutiveSource} that obtains a resource and creates a
	 * {@link Executive}.
	 */
	@TestSource
	public static class NoResourceExecutiveSource extends ExecutiveSourceAdapter {

		/**
		 * Location of the required resource.
		 */
		public static final String RESOURCE_LOCATION = "REQUIRED RESOURCE";

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			context.getResource(RESOURCE_LOCATION);
			fail("Should not successfully obtain a resource");
			return super.createExecutive(context);
		}
	}

	/**
	 * Ensures handles failure to create the {@link Executive}.
	 */
	public void testExecutiveCreateFailure() {

		final RuntimeException failure = new RuntimeException("create failure");

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(FailCreateExecutiveSource.class);
		FailCreateExecutiveSource.createFailure = failure;
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME, "Failed to create Executive", failure);

		// Attempt to construct
		this.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * {@link ExecutiveSource} that fails to create {@link Executive}.
	 */
	@TestSource
	public static class FailCreateExecutiveSource extends ExecutiveSourceAdapter {

		/**
		 * {@link Exception} to be thrown on create.
		 */
		public static RuntimeException createFailure;

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			throw createFailure;
		}
	}

	/**
	 * Ensures indicates issue if no {@link Executive} created.
	 */
	public void testNullExecutiveSourced() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(SourceExecutiveSource.class);
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME, "ExecutiveSource failed to provide Executive");

		// Construct
		this.replayMockObjects();
		SourceExecutiveSource.executive = null;
		this.constructRawExecutiveMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to successfully source the {@link Executive} and details of
	 * {@link RawExecutiveMetaData} are correct.
	 */
	public void testExecutiveSourcedViaClass() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(SourceExecutiveSource.class);
		SourceExecutiveSource.executive = this.createMock(Executive.class);

		// Construct
		this.replayMockObjects();
		RawExecutiveMetaData metaData = this.constructRawExecutiveMetaData(true);
		this.verifyMockObjects();

		// Ensure meta-data is correct
		assertEquals("Incorrect executive", SourceExecutiveSource.executive, metaData.getExecutive());
	}

	/**
	 * Ensures able to successfully source the {@link Executive} and details of
	 * {@link RawExecutiveMetaData} are correct.
	 */
	public void testExecutiveSourcedViaInstance() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(new SourceExecutiveSource());
		SourceExecutiveSource.executive = this.createMock(Executive.class);

		// Construct
		this.replayMockObjects();
		RawExecutiveMetaData metaData = this.constructRawExecutiveMetaData(true);
		this.verifyMockObjects();

		// Ensure meta-data is correct
		assertEquals("Incorrect executive", SourceExecutiveSource.executive, metaData.getExecutive());
	}

	/**
	 * {@link ExecutiveSource} that sources a {@link Executive}.
	 */
	@TestSource
	public static class SourceExecutiveSource extends ExecutiveSourceAdapter {

		/**
		 * {@link Executive} to be returned.
		 */
		public static Executive executive;

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			return executive;
		}
	}

	/**
	 * Indicate missing {@link ExecutionStrategy} instances.
	 */
	public void testMissingExecutionStrategies() {
		fail("TODO implement");
	}

	/**
	 * Indicate missing {@link ExecutionStrategy} name.
	 */
	public void testMissingExecutionStrategyName() {
		fail("TODO implement");
	}

	/**
	 * Indicating missing {@link ThreadFactory} instances of the
	 * {@link ExecutionStrategy}.
	 */
	public void testMissingThreadFactories() {
		fail("TODO implement");
	}

	/**
	 * Constructs the {@link RawExecutiveMetaData} with the mock objects.
	 * 
	 * @return {@link RawExecutiveMetaData}.
	 */
	private RawExecutiveMetaData constructRawExecutiveMetaData(boolean isExpectConstruction) {

		// Attempt to construct
		RawExecutiveMetaData metaData = new RawExecutiveMetaDataFactory(this.sourceContext)
				.constructRawExecutiveMetaData(this.configuration, OFFICE_FLOOR_NAME, this.issues);

		// Provide assertion on whether should be constructed
		if (isExpectConstruction) {
			assertNotNull("Should have constructed meta-data", metaData);
		} else {
			assertNull("Should not construct meta-data", metaData);
		}

		// Return the meta-data
		return metaData;
	}

}