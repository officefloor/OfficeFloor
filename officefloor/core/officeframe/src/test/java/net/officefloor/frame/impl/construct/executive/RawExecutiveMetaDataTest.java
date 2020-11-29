/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.construct.executive;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;
import java.util.concurrent.ThreadFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.execute.execution.ManagedExecutionFactoryImpl;
import net.officefloor.frame.impl.execute.execution.ThreadFactoryManufacturer;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.internal.configuration.ExecutiveConfiguration;
import net.officefloor.frame.internal.structure.ManagedExecutionFactory;
import net.officefloor.frame.test.MockClockFactory;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Tests the {@link RawExecutiveMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class RawExecutiveMetaDataTest {

	/**
	 * Name of the {@link OfficeFloor}.
	 */
	private static final String OFFICE_FLOOR_NAME = "OFFICE_FLOOR_NAME";

	/**
	 * {@link Executive} {@link AssetType} name.
	 */
	private static final String EXECUTIVE_NAME = "Executive";

	/**
	 * {@link MockTestSupport}.
	 */
	private final MockTestSupport mocks = new MockTestSupport();

	/**
	 * {@link ExecutiveConfiguration}.
	 */
	private ExecutiveBuilderImpl<?> configuration = new ExecutiveBuilderImpl<>(ExecutiveSource.class);

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContextImpl sourceContext = new SourceContextImpl(this.getClass().getName(), false,
			new String[0], Thread.currentThread().getContextClassLoader(), new MockClockFactory());

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private OfficeFloorIssues issues;

	@BeforeEach
	public void setup() {
		this.issues = this.mocks.createMock(OfficeFloorIssues.class);
	}

	/**
	 * Ensures issue if no {@link ExecutiveSource}.
	 */
	@Test
	public void noExecutiveSource() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>((ExecutiveSource) null);
		this.issues.addIssue(AssetType.EXECUTIVE, "Executive", "No ExecutiveSource class provided");

		// Attempt to construct team
		this.mocks.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensures issue if fail to instantiate the {@link ExecutiveSource}.
	 */
	@Test
	public void executiveSourceInstantiateFailure() {

		final Exception failure = new Exception("Instantiate failure");
		this.configuration = new ExecutiveBuilderImpl<>(FailInstantiateExecutiveSource.class);
		FailInstantiateExecutiveSource.instantiateFailure = failure;
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
				"Failed to instantiate " + FailInstantiateExecutiveSource.class.getName(), failure);

		// Attempt to construct executive
		this.mocks.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.mocks.verifyMockObjects();
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
	@Test
	public void missingProperty() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(NoPropertyExecutiveSource.class);
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
				"Must specify property '" + NoPropertyExecutiveSource.PROPERTY_NAME + "'");

		// Construct
		this.mocks.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.mocks.verifyMockObjects();
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
	@Test
	public void missingClass() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(NoClassExecutiveSource.class);
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
				"Can not load class '" + NoClassExecutiveSource.CLASS_NAME + "'");

		// Attempt to construct executive
		this.mocks.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.mocks.verifyMockObjects();
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
	@Test
	public void missingResource() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(NoResourceExecutiveSource.class);
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
				"Can not obtain resource at location '" + NoResourceExecutiveSource.RESOURCE_LOCATION + "'");

		// Construct
		this.mocks.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.mocks.verifyMockObjects();
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
	@Test
	public void executiveCreateFailure() {

		final RuntimeException failure = new RuntimeException("create failure");

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(FailCreateExecutiveSource.class);
		FailCreateExecutiveSource.createFailure = failure;
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME, "Failed to create Executive", failure);

		// Attempt to construct
		this.mocks.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.mocks.verifyMockObjects();
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
	@Test
	public void nullExecutiveSourced() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(SourceExecutiveSource.class);
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME, "ExecutiveSource failed to provide Executive");

		// Construct
		this.mocks.replayMockObjects();
		SourceExecutiveSource.executive = null;
		this.constructRawExecutiveMetaData(false);
		this.mocks.verifyMockObjects();
	}

	/**
	 * Indicate missing {@link ExecutionStrategy} instances.
	 */
	@Test
	public void missingExecutionStrategies() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(ExecutionStrategyExecutiveSource.class);
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME, "Must have at least one ExecutionStrategy");

		// Construct
		ExecutionStrategyExecutiveSource.strategies = null;
		this.mocks.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.mocks.verifyMockObjects();
	}

	/**
	 * Indicate missing {@link ExecutionStrategy} instance within returned listing.
	 */
	@Test
	public void missingAnExecutionStrategy() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(ExecutionStrategyExecutiveSource.class);
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME, "Null ExecutionStrategy provided for index 0");

		// Construct
		ExecutionStrategyExecutiveSource.strategies = new ExecutionStrategy[] { null };
		this.mocks.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.mocks.verifyMockObjects();
	}

	/**
	 * Indicate missing {@link ExecutionStrategy} name.
	 */
	@Test
	public void missingExecutionStrategyName() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(ExecutionStrategyExecutiveSource.class);
		ExecutionStrategy strategy = this.mocks.createMock(ExecutionStrategy.class);
		this.mocks.recordReturn(strategy, strategy.getExecutionStrategyName(), null);
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
				"ExecutionStrategy for index 0 did not provide a name");

		// Construct
		ExecutionStrategyExecutiveSource.strategies = new ExecutionStrategy[] { strategy };
		this.mocks.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.mocks.verifyMockObjects();
	}

	/**
	 * Indicating missing {@link ThreadFactory} instances of the
	 * {@link ExecutionStrategy}.
	 */
	@Test
	public void missingThreadFactories() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(ExecutionStrategyExecutiveSource.class);
		ExecutionStrategy strategy = this.mocks.createMock(ExecutionStrategy.class);
		this.mocks.recordReturn(strategy, strategy.getExecutionStrategyName(), "test");
		this.mocks.recordReturn(strategy, strategy.getThreadFactories(), null);
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
				"ExecutionStrategy 'test' must provide at least one ThreadFactory");

		// Construct
		ExecutionStrategyExecutiveSource.strategies = new ExecutionStrategy[] { strategy };
		this.mocks.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.mocks.verifyMockObjects();
	}

	/**
	 * Indicating no {@link ThreadFactory} instances of the
	 * {@link ExecutionStrategy}.
	 */
	@Test
	public void noThreadFactories() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(ExecutionStrategyExecutiveSource.class);
		ExecutionStrategy strategy = this.mocks.createMock(ExecutionStrategy.class);
		this.mocks.recordReturn(strategy, strategy.getExecutionStrategyName(), "test");
		this.mocks.recordReturn(strategy, strategy.getThreadFactories(), new ThreadFactory[0]);
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
				"ExecutionStrategy 'test' must provide at least one ThreadFactory");

		// Construct
		ExecutionStrategyExecutiveSource.strategies = new ExecutionStrategy[] { strategy };
		this.mocks.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.mocks.verifyMockObjects();
	}

	/**
	 * Indicating issue if duplicate {@link ExecutionStrategy} names.
	 */
	@Test
	public void duplicateExecutionStrategyNames() {

		// Record
		final String STRATEGY_NAME = "strategy";
		this.configuration = new ExecutiveBuilderImpl<>(ExecutionStrategyExecutiveSource.class);
		ExecutionStrategy strategyOne = this.mocks.createMock(ExecutionStrategy.class);
		this.mocks.recordReturn(strategyOne, strategyOne.getExecutionStrategyName(), STRATEGY_NAME);
		ThreadFactory threadFactory = this.mocks.createMock(ThreadFactory.class);
		this.mocks.recordReturn(strategyOne, strategyOne.getThreadFactories(), new ThreadFactory[] { threadFactory });
		ExecutionStrategy strategyTwo = this.mocks.createMock(ExecutionStrategy.class);
		this.mocks.recordReturn(strategyTwo, strategyTwo.getExecutionStrategyName(), STRATEGY_NAME);
		this.issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME,
				"One or more ExecutionStrategy instances provided by the same name 'strategy'");

		// Construct
		ExecutionStrategyExecutiveSource.strategies = new ExecutionStrategy[] { strategyOne, strategyTwo };
		this.mocks.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure can obtain the {@link ExecutionStrategy}.
	 */
	@Test
	public void executionStrategyAvailable() {

		// Record
		final String STRATEGY_NAME = "test";
		this.configuration = new ExecutiveBuilderImpl<>(ExecutionStrategyExecutiveSource.class);
		ExecutionStrategy strategy = this.mocks.createMock(ExecutionStrategy.class);
		this.mocks.recordReturn(strategy, strategy.getExecutionStrategyName(), STRATEGY_NAME);
		ThreadFactory[] threadFactories = new ThreadFactory[] { this.mocks.createMock(ThreadFactory.class) };
		this.mocks.recordReturn(strategy, strategy.getThreadFactories(), threadFactories);

		// Construct
		ExecutionStrategyExecutiveSource.strategies = new ExecutionStrategy[] { strategy };
		this.mocks.replayMockObjects();
		RawExecutiveMetaData metaData = this.constructRawExecutiveMetaData(true);
		this.mocks.verifyMockObjects();

		// Ensure exeuction strategy available
		Map<String, ThreadFactory[]> executionStrategies = metaData.getExecutionStrategies();
		assertEquals(1, executionStrategies.size(), "Should have one execution strategy");
		assertSame(threadFactories, executionStrategies.get(STRATEGY_NAME),
				"Incorrect thread factories for strategy name");
	}

	@TestSource
	public static class ExecutionStrategyExecutiveSource extends ExecutiveSourceAdapter {

		private static ExecutionStrategy[] strategies;

		/*
		 * ======================== Executive ==============================
		 */

		@Override
		public ExecutionStrategy[] getExcutionStrategies() {
			return strategies;
		}
	}

	/**
	 * Indicate no {@link TeamOversight}.
	 */
	@Test
	public void noTeamOversight() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(TeamOversightExecutiveSource.class);

		// Construct
		TeamOversightExecutiveSource.oversight = null;
		this.mocks.replayMockObjects();
		this.constructRawExecutiveMetaData(false);
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure can obtain the {@link TeamOversight}.
	 */
	@Test
	public void teamOversightAvailable() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(TeamOversightExecutiveSource.class);
		TeamOversight oversight = this.mocks.createMock(TeamOversight.class);

		// Construct
		TeamOversightExecutiveSource.oversight = oversight;
		this.mocks.replayMockObjects();
		RawExecutiveMetaData metaData = this.constructRawExecutiveMetaData(true);
		this.mocks.verifyMockObjects();

		// Ensure team oversight available
		assertEquals(oversight, metaData.getTeamOversight(), "Incorrect team oversight");
	}

	@TestSource
	public static class TeamOversightExecutiveSource extends DefaultExecutive {

		private static TeamOversight oversight;

		@Override
		public TeamOversight getTeamOversight() {
			return oversight;
		}
	}

	/**
	 * Ensures able to successfully source the {@link Executive} and details of
	 * {@link RawExecutiveMetaData} are correct.
	 */
	@Test
	public void executiveSourcedViaClass() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(SourceExecutiveSource.class);
		Executive executive = this.mocks.createMock(Executive.class);
		ExecutionStrategy strategy = this.mocks.createMock(ExecutionStrategy.class);
		this.mocks.recordReturn(executive, executive.getExcutionStrategies(), new ExecutionStrategy[] { strategy });
		this.mocks.recordReturn(strategy, strategy.getExecutionStrategyName(), "strategy");
		ThreadFactory threadFactory = this.mocks.createMock(ThreadFactory.class);
		this.mocks.recordReturn(strategy, strategy.getThreadFactories(), new ThreadFactory[] { threadFactory });
		TeamOversight oversight = this.mocks.createMock(TeamOversight.class);
		this.mocks.recordReturn(executive, executive.getTeamOversight(), oversight);
		SourceExecutiveSource.executive = executive;

		// Construct
		this.mocks.replayMockObjects();
		RawExecutiveMetaData metaData = this.constructRawExecutiveMetaData(true);
		this.mocks.verifyMockObjects();

		// Ensure meta-data is correct
		assertEquals(SourceExecutiveSource.executive, metaData.getExecutive(), "Incorrect executive");
	}

	/**
	 * Ensures able to successfully source the {@link Executive} and details of
	 * {@link RawExecutiveMetaData} are correct.
	 */
	@Test
	public void executiveSourcedViaInstance() {

		// Record
		this.configuration = new ExecutiveBuilderImpl<>(new SourceExecutiveSource());
		Executive executive = this.mocks.createMock(Executive.class);
		ExecutionStrategy strategy = this.mocks.createMock(ExecutionStrategy.class);
		this.mocks.recordReturn(executive, executive.getExcutionStrategies(), new ExecutionStrategy[] { strategy });
		this.mocks.recordReturn(strategy, strategy.getExecutionStrategyName(), "strategy");
		ThreadFactory threadFactory = this.mocks.createMock(ThreadFactory.class);
		this.mocks.recordReturn(strategy, strategy.getThreadFactories(), new ThreadFactory[] { threadFactory });
		TeamOversight oversight = this.mocks.createMock(TeamOversight.class);
		this.mocks.recordReturn(executive, executive.getTeamOversight(), oversight);
		SourceExecutiveSource.executive = executive;

		// Construct
		this.mocks.replayMockObjects();
		RawExecutiveMetaData metaData = this.constructRawExecutiveMetaData(true);
		this.mocks.verifyMockObjects();

		// Ensure meta-data is correct
		assertEquals(SourceExecutiveSource.executive, metaData.getExecutive(), "Incorrect executive");
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
	 * Constructs the {@link RawExecutiveMetaData} with the mock objects.
	 * 
	 * @return {@link RawExecutiveMetaData}.
	 */
	private RawExecutiveMetaData constructRawExecutiveMetaData(boolean isExpectConstruction) {

		// Attempt to construct
		ManagedExecutionFactory managedExecutionFactory = new ManagedExecutionFactoryImpl(
				new ThreadCompletionListener[0]);
		ThreadFactoryManufacturer threadFactoryManufacturer = new ThreadFactoryManufacturer(managedExecutionFactory,
				null);
		RawExecutiveMetaData metaData = new RawExecutiveMetaDataFactory(this.sourceContext, threadFactoryManufacturer)
				.constructRawExecutiveMetaData(this.configuration, OFFICE_FLOOR_NAME, this.issues);

		// Provide assertion on whether should be constructed
		if (isExpectConstruction) {
			assertNotNull(metaData, "Should have constructed meta-data");
		} else {
			assertNull(metaData, "Should not construct meta-data");
		}

		// Return the meta-data
		return metaData;
	}

}
