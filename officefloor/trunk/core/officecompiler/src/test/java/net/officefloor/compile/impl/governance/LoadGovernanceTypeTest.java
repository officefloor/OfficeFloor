/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.compile.impl.governance;

import java.sql.Connection;
import java.util.Properties;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.governance.GovernanceEscalationType;
import net.officefloor.compile.governance.GovernanceFlowType;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceFlowMetaData;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.GovernanceSourceContext;
import net.officefloor.compile.spi.governance.source.GovernanceSourceMetaData;
import net.officefloor.compile.spi.governance.source.GovernanceSourceSpecification;
import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests loading the {@link GovernanceType} via the {@link GovernanceLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadGovernanceTypeTest extends OfficeFrameTestCase {

	/**
	 * {@link GovernanceSourceMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private final GovernanceSourceMetaData<Object, None> metaData = this
			.createMock(GovernanceSourceMetaData.class);

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues = this.createMock(CompilerIssues.class);

	@Override
	protected void setUp() throws Exception {
		// Reset the mock governance source state
		MockGovernanceSource.reset(this.metaData);
	}

	/**
	 * Ensure issue if fail to instantiate the {@link GovernanceSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.record_issue(
				"Failed to instantiate " + MockGovernanceSource.class.getName()
						+ " by default constructor", failure);

		// Attempt to obtain specification
		MockGovernanceSource.instantiateFailure = failure;
		this.loadGovernanceType(false, null);
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.record_issue("Property 'missing' must be specified");

		// Attempt to load
		this.loadGovernanceType(false, new Init<None>() {
			@Override
			public void init(GovernanceSourceContext context) {
				context.getProperty("missing");
			}
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Record simple meta-data
		this.record_simpleMetaData();

		// Attempt to load
		this.loadGovernanceType(true, new Init<None>() {
			@Override
			public void init(GovernanceSourceContext context) {
				assertEquals("Ensure get defaulted property", "DEFAULT",
						context.getProperty("missing", "DEFAULT"));
				assertEquals("Ensure get property ONE", "1",
						context.getProperty("ONE"));
				assertEquals("Ensure get property TWO", "2",
						context.getProperty("TWO"));
				Properties properties = context.getProperties();
				assertEquals("Incorrect number of properties", 2,
						properties.size());
				assertEquals("Incorrect property ONE", "1",
						properties.get("ONE"));
				assertEquals("Incorrect property TWO", "2",
						properties.get("TWO"));
			}
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.record_issue("Can not load class 'missing'");

		// Attempt to load
		this.loadGovernanceType(false, new Init<None>() {
			@Override
			public void init(GovernanceSourceContext context) {
				context.loadClass("missing");
			}
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing resource
		this.record_issue("Can not obtain resource at location 'missing'");

		// Attempt to load
		this.loadGovernanceType(false, new Init<None>() {
			@Override
			public void init(GovernanceSourceContext context) {
				context.getResource("missing");
			}
		});
	}

	/**
	 * Ensure able to get resource.
	 */
	public void testGetResource() {

		// Record basic meta-data
		this.record_simpleMetaData();

		// Obtain path
		final String objectPath = Object.class.getName().replace('.', '/')
				+ ".class";

		// Attempt to load
		this.loadGovernanceType(true, new Init<None>() {
			@Override
			public void init(GovernanceSourceContext context) {
				assertEquals("Incorrect resource locator",
						LoadGovernanceTypeTest.class.getClassLoader()
								.getResource(objectPath), context
								.getClassLoader().getResource(objectPath));
			}
		});
	}

	/**
	 * Ensure issue if fails to init the {@link GovernanceSource}.
	 */
	public void testFailInitGovernanceSource() {

		final NullPointerException failure = new NullPointerException(
				"Fail init GovernanceSource");

		// Record failure to init the Governance Source
		this.record_issue(
				"Failed to initialise " + MockGovernanceSource.class.getName(),
				failure);

		// Attempt to load
		this.loadGovernanceType(false, new Init<None>() {
			@Override
			public void init(GovernanceSourceContext context) {
				throw failure;
			}
		});
	}

	/**
	 * Ensure issue if <code>null</code> {@link GovernanceSourceMetaData}.
	 */
	public void testNullGovernanceSourceMetaData() {

		// Record null the Governance Source meta-data
		this.record_issue("Must provide meta-data");

		// Attempt to load
		this.loadGovernanceType(false, new Init<None>() {
			@Override
			public void init(GovernanceSourceContext context) {
				MockGovernanceSource.metaData = null;
			}
		});
	}

	/**
	 * Ensure issue if fails to obtain the {@link GovernanceSourceMetaData}.
	 */
	public void testFailGetGovernanceSourceMetaData() {

		final Error failure = new Error("Obtain meta-data failure");

		// Record failure to obtain the meta-data
		this.record_issue("Failed to get GovernanceSourceMetaData", failure);

		// Attempt to load
		MockGovernanceSource.metaDataFailure = failure;
		this.loadGovernanceType(false, null);
	}

	/**
	 * Ensure issue if no {@link GovernanceFactory} from meta-data.
	 */
	public void testNoGovernanceFactory() {

		// Record no governance factory
		this.recordReturn(this.metaData, this.metaData.getGovernanceFactory(),
				null);
		this.record_issue("No GovernanceFactory provided");

		// Attempt to load
		this.loadGovernanceType(false, null);
	}

	/**
	 * Ensure issue if no extension interface type from meta-data.
	 */
	public void testNoExtensionInterfaceType() {

		// Record no extension interface
		this.recordReturn(this.metaData, this.metaData.getGovernanceFactory(),
				this.createMock(GovernanceFactory.class));
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(),
				null);
		this.record_issue("No extension interface type provided");

		// Attempt to load
		this.loadGovernanceType(false, null);
	}

	/**
	 * Ensure {@link GovernanceType} for {@link GovernanceSourceMetaData}.
	 */
	public void testSimpleGovernance() {

		final GovernanceFactory<?, ?> factory = this
				.createMock(GovernanceFactory.class);
		final Class<?> extensionInterface = Connection.class;

		// Record governance meta-data
		this.recordReturn(this.metaData, this.metaData.getGovernanceFactory(),
				factory);
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(),
				extensionInterface);
		this.record_flowMetaData();
		this.record_escalationTypes();

		// Load
		GovernanceType<Object, None> type = this.loadGovernanceType(true, null);

		// Validate details of type
		assertEquals("Incorrect governance factory", factory,
				type.getGovernanceFactory());
		assertEquals("Incorrect extension interface", extensionInterface,
				type.getExtensionInterface());
		assertEquals("Should be no flows", 0, type.getFlowTypes().length);
		assertEquals("Should be no escalations", 0,
				type.getEscalationTypes().length);
	}

	/**
	 * Ensure issue if <code>null</code> {@link GovernanceFlowMetaData} in
	 * array.
	 */
	public void testNullFlowMetaData() {

		// Record no flow type
		this.record_factoryAndExtensionInterfaceType();
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new GovernanceFlowMetaData[] { null });
		this.record_issue("Null GovernanceFlowMetaData for flow 0");

		// Attempt to load
		this.loadGovernanceType(false, null);
	}

	/**
	 * Ensure able to default the flow argument type.
	 */
	public void testDefaultFlowArgumentType() {

		final GovernanceFlowMetaData<?> flowDefaulted = this
				.createMock(GovernanceFlowMetaData.class);
		final GovernanceFlowMetaData<?> flowProvided = this
				.createMock(GovernanceFlowMetaData.class);

		// Record no flow argument type
		this.record_factoryAndExtensionInterfaceType();
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new GovernanceFlowMetaData<?>[] { flowDefaulted, flowProvided });
		this.recordReturn(flowDefaulted, flowDefaulted.getLabel(), "DEFAULTED");
		this.recordReturn(flowDefaulted, flowDefaulted.getKey(), null);
		this.recordReturn(flowDefaulted, flowDefaulted.getArgumentType(), null);
		this.recordReturn(flowProvided, flowProvided.getLabel(), null);
		this.recordReturn(flowProvided, flowProvided.getKey(), null);
		this.recordReturn(flowProvided, flowProvided.getArgumentType(),
				Connection.class);
		this.record_escalationTypes();

		// Attempt to load
		GovernanceType<?, ?> governanceType = this.loadGovernanceType(true,
				null);

		// Validate argument types of flows
		GovernanceFlowType<?>[] flowTypes = governanceType.getFlowTypes();
		assertEquals("Incorrect number of flows", 2, flowTypes.length);
		GovernanceFlowType<?> defaulted = flowTypes[0];
		assertEquals("Incorrect name for defaulted argument flow", "DEFAULTED",
				defaulted.getFlowName());
		assertEquals("Incorrect defaulted argument type", Void.class,
				defaulted.getArgumentType());
		GovernanceFlowType<?> provided = flowTypes[1];
		assertEquals("Incorrect name for provided argument flow", "1",
				provided.getFlowName());
		assertEquals("Incorrect provided argument type", Connection.class,
				provided.getArgumentType());
	}

	/**
	 * Ensure issue if flow keys of different types.
	 */
	public void testInvalidFlowKey() {

		final GovernanceFlowMetaData<?> flowOne = this
				.createMock(GovernanceFlowMetaData.class);
		final GovernanceFlowMetaData<?> flowTwo = this
				.createMock(GovernanceFlowMetaData.class);

		// Record missing flow key
		this.record_factoryAndExtensionInterfaceType();
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new GovernanceFlowMetaData<?>[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.ONE);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Connection.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), InvalidKey.INVALID);
		this.record_issue("Meta-data flows identified by different key types ("
				+ TwoKey.class.getName() + ", " + InvalidKey.class.getName()
				+ ")");

		// Attempt to load
		this.loadGovernanceType(false, null);
	}

	/**
	 * Ensure issue if flow mixing using keys and indexes.
	 */
	public void testFlowMixingKeyAndIndexes() {

		final GovernanceFlowMetaData<?> flowOne = this
				.createMock(GovernanceFlowMetaData.class);
		final GovernanceFlowMetaData<?> flowTwo = this
				.createMock(GovernanceFlowMetaData.class);

		// Record missing flow key
		this.record_factoryAndExtensionInterfaceType();
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new GovernanceFlowMetaData<?>[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.ONE);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Connection.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), null);
		this.record_issue("Meta-data flows mixing keys and indexes");

		// Attempt to load
		this.loadGovernanceType(false, null);
	}

	/**
	 * Ensure issue if key used more than once for a flow.
	 */
	public void testDuplicateFlowKey() {

		final GovernanceFlowMetaData<?> flowOne = this
				.createMock(GovernanceFlowMetaData.class);
		final GovernanceFlowMetaData<?> flowTwo = this
				.createMock(GovernanceFlowMetaData.class);

		// Record missing flow key
		this.record_factoryAndExtensionInterfaceType();
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new GovernanceFlowMetaData<?>[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowOne, flowOne.getKey(), TwoKey.ONE);
		this.recordReturn(flowOne, flowOne.getArgumentType(), Connection.class);
		this.recordReturn(flowTwo, flowTwo.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), TwoKey.ONE);
		this.recordReturn(flowTwo, flowTwo.getArgumentType(), String.class);
		this.record_issue("Must have exactly one flow per key (key="
				+ TwoKey.ONE + ")");

		// Attempt to load
		this.loadGovernanceType(false, null);
	}

	/**
	 * Ensure issue if not have meta-data for each flow key.
	 */
	public void testNotAllFlowKeys() {

		final GovernanceFlowMetaData<?> flow = this
				.createMock(GovernanceFlowMetaData.class);

		// Record not all flow keys
		this.record_factoryAndExtensionInterfaceType();
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new GovernanceFlowMetaData<?>[] { flow });
		this.recordReturn(flow, flow.getLabel(), null);
		this.recordReturn(flow, flow.getKey(), TwoKey.ONE);
		this.recordReturn(flow, flow.getArgumentType(), Connection.class);
		this.record_issue("Missing flow meta-data (keys=" + TwoKey.TWO + ")");

		// Attempt to load
		this.loadGovernanceType(false, null);
	}

	/**
	 * Ensure issue if no escalation type.
	 */
	public void testNoEscalationType() {

		// Record no escalation type
		this.record_issue("Null escalation type for 0");

		// Record no escalation type
		this.record_factoryAndExtensionInterfaceType();
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new GovernanceFlowMetaData<?>[0]);
		this.recordReturn(this.metaData, this.metaData.getEscalationTypes(),
				new Class<?>[1]);

		// Attempt to load
		this.loadGovernanceType(false, null);
	}

	/**
	 * Ensure can include escalation.
	 */
	public void testEscalation() {

		// Record no escalation type
		this.record_factoryAndExtensionInterfaceType();
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new GovernanceFlowMetaData<?>[0]);
		this.recordReturn(this.metaData, this.metaData.getEscalationTypes(),
				new Class<?>[] { Error.class, RuntimeException.class });

		// Load governance type
		GovernanceType<?, ?> type = this.loadGovernanceType(true, null);

		// Validate the escalations
		GovernanceEscalationType[] escalations = type.getEscalationTypes();
		assertEquals("Incorrect number of escalation", 2, escalations.length);
		assertEquals("Incorrect first escalation type", Error.class,
				escalations[0].getEscalationType());
		assertEquals("Incorrect first escalation name",
				Error.class.getSimpleName(), escalations[0].getEscalationName());
		assertEquals("Incorrect second escalation type",
				RuntimeException.class, escalations[1].getEscalationType());
		assertEquals("Incorrect second escalation name",
				RuntimeException.class.getSimpleName(),
				escalations[1].getEscalationName());
	}

	/**
	 * Loads the {@link GovernanceType}.
	 * 
	 * @param isExpectedToLoad
	 *            Flag indicating if expecting to load the
	 *            {@link GovernanceType}.
	 * @param init
	 *            {@link Init}. May be <code>null</code>.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 * @return Loaded {@link GovernanceType}.
	 */
	private GovernanceType<Object, None> loadGovernanceType(
			boolean isExpectedToLoad, Init<?> init,
			String... propertyNameValuePairs) {

		// Replay mock objects
		this.replayMockObjects();

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Specify init
		MockGovernanceSource.init = init;

		// Create the governance loader and load the governance type
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		GovernanceLoader governanceLoader = compiler.getGovernanceLoader();
		GovernanceType<Object, None> governanceType = governanceLoader
				.loadGovernanceType(MockGovernanceSource.class, propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the governance type",
					governanceType);
		} else {
			assertNull("Should not load the governance type", governanceType);
		}

		// Return the governance type
		return governanceType;
	}

	/**
	 * Two key {@link Enum}.
	 */
	private enum TwoKey {
		ONE, TWO
	}

	/**
	 * Invalid key {@link Enum}.
	 */
	private enum InvalidKey {
		INVALID
	}

	/**
	 * Records obtaining the {@link GovernanceFactory} and extension interface
	 * type from the {@link GovernanceSourceMetaData}.
	 */
	private void record_factoryAndExtensionInterfaceType() {
		this.recordReturn(this.metaData, this.metaData.getGovernanceFactory(),
				this.createMock(GovernanceFactory.class));
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(),
				Connection.class);
	}

	/**
	 * Records simple {@link FlowMetaData}.
	 */
	private void record_flowMetaData() {
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
	}

	/**
	 * Records simple escalation types.
	 */
	private void record_escalationTypes(Class<?>... escalationTypes) {
		this.recordReturn(this.metaData, this.metaData.getEscalationTypes(),
				(escalationTypes.length == 0 ? null : escalationTypes));
	}

	/**
	 * Records obtaining simple meta-data from the
	 * {@link GovernanceSourceMetaData}.
	 */
	private void record_simpleMetaData() {
		this.record_factoryAndExtensionInterfaceType();
		this.record_flowMetaData();
		this.record_escalationTypes();
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(LocationType.OFFICE, null, AssetType.GOVERNANCE,
				null, issueDescription);
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 */
	private void record_issue(String issueDescription, Throwable cause) {
		this.issues.addIssue(LocationType.OFFICE, null, AssetType.GOVERNANCE,
				null, issueDescription, cause);
	}

	/**
	 * Implement to initialise the {@link MockGovernanceSource}.
	 */
	private static interface Init<F extends Enum<F>> {

		/**
		 * Implemented to init the {@link GovernanceSource}.
		 * 
		 * @param context
		 *            {@link GovernanceSourceContext}.
		 */
		void init(GovernanceSourceContext context);
	}

	/**
	 * Mock {@link GovernanceSource}.
	 */
	@TestSource
	public static class MockGovernanceSource implements
			GovernanceSource<Object, None> {

		/**
		 * Instantiate exception.
		 */
		public static Exception instantiateFailure = null;

		/**
		 * {@link Init} to init the {@link GovernanceSource}.
		 */
		public static Init<?> init = null;

		/**
		 * Failure to obtain the {@link GovernanceSourceMetaData}.
		 */
		public static Error metaDataFailure = null;

		/**
		 * {@link GovernanceMetaData}.
		 */
		public static GovernanceSourceMetaData<Object, None> metaData = null;

		/**
		 * Resets state of {@link MockGovernanceSource} for testing.
		 * 
		 * @param metaData
		 *            {@link GovernanceSourceMetaData}.
		 */
		public static void reset(GovernanceSourceMetaData<Object, None> metaData) {
			instantiateFailure = null;
			init = null;
			metaDataFailure = null;
			MockGovernanceSource.metaData = metaData;
		}

		/**
		 * Instantiate.
		 * 
		 * @throws Exception
		 *             Possible instantiate failure.
		 */
		public MockGovernanceSource() throws Exception {
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ==================== GovernanceSource ====================
		 */

		@Override
		public GovernanceSourceSpecification getSpecification() {
			fail("Should not call getSpecification");
			return null;
		}

		@Override
		public void init(GovernanceSourceContext context) throws Exception {

			// Run the init if available
			if (init != null) {
				init.init(context);
			}
		}

		@Override
		public GovernanceSourceMetaData<Object, None> getMetaData() {

			// Throw meta-data failure
			if (metaDataFailure != null) {
				throw metaDataFailure;
			}

			// Return the meta-data
			return metaData;
		}
	}

}