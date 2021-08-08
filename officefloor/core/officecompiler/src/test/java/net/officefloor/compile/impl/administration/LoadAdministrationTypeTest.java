/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.administration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.transaction.xa.XAResource;

import net.officefloor.compile.FailServiceFactory;
import net.officefloor.compile.MissingServiceFactory;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administration.AdministrationEscalationType;
import net.officefloor.compile.administration.AdministrationFlowType;
import net.officefloor.compile.administration.AdministrationGovernanceType;
import net.officefloor.compile.administration.AdministrationLoader;
import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.administration.source.AdministrationEscalationMetaData;
import net.officefloor.compile.spi.administration.source.AdministrationFlowMetaData;
import net.officefloor.compile.spi.administration.source.AdministrationGovernanceMetaData;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.administration.source.AdministrationSourceContext;
import net.officefloor.compile.spi.administration.source.AdministrationSourceMetaData;
import net.officefloor.compile.spi.administration.source.AdministrationSourceSpecification;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests loading the {@link AdministrationType}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadAdministrationTypeTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link AdministrationSourceMetaData}.
	 */
	private final AdministrationSourceMetaData<?, ?, ?> metaData = this.createMock(AdministrationSourceMetaData.class);

	/**
	 * {@link AdministrationFactory}.
	 */
	private final AdministrationFactory<?, ?, ?> factory = this.createMock(AdministrationFactory.class);

	@Override
	protected void setUp() throws Exception {
		MockAdministrationSource.reset(this.metaData);
	}

	/**
	 * Ensure issue if fail to instantiate the {@link AdministrationSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MockAdministrationSource.class.getName() + " by default constructor",
				failure);

		// Attempt to load
		MockAdministrationSource.instantiateFailure = failure;
		this.loadAdministrationType(false, null);
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues.recordIssue("Must specify property 'missing'");

		// Attempt to load
		this.loadAdministrationType(false, (context) -> {
			context.getProperty("missing");
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Record basic meta-data
		this.recordReturn(this.metaData, this.metaData.getAdministrationFactory(), this.factory);
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(), XAResource.class);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), new AdministrationFlowMetaData[0]);
		this.recordReturn(this.metaData, this.metaData.getEscalationMetaData(),
				new AdministrationEscalationMetaData[0]);
		this.recordReturn(this.metaData, this.metaData.getGovernanceMetaData(),
				new AdministrationGovernanceMetaData[0]);

		// Attempt to load
		this.loadAdministrationType(true, (context) -> {
			assertEquals("Ensure get defaulted property", "DEFAULT", context.getProperty("missing", "DEFAULT"));
			assertEquals("Ensure get property ONE", "1", context.getProperty("ONE"));
			assertEquals("Ensure get property TWO", "2", context.getProperty("TWO"));
			Properties properties = context.getProperties();
			assertEquals("Incorrect number of properties", 2, properties.size());
			assertEquals("Incorrect property ONE", "1", properties.get("ONE"));
			assertEquals("Incorrect property TWO", "2", properties.get("TWO"));
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure correct {@link Logger} name.
	 */
	public void testLogger() {

		// Record basic meta-data
		this.recordReturn(this.metaData, this.metaData.getAdministrationFactory(), this.factory);
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(), XAResource.class);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), new AdministrationFlowMetaData[0]);
		this.recordReturn(this.metaData, this.metaData.getEscalationMetaData(),
				new AdministrationEscalationMetaData[0]);
		this.recordReturn(this.metaData, this.metaData.getGovernanceMetaData(),
				new AdministrationGovernanceMetaData[0]);

		// Attempt to load
		MockAdministrationSource.loggerName = null;
		this.loadAdministrationType(true, null);
		assertEquals("Incorrect logger name", OfficeFloorCompiler.TYPE, MockAdministrationSource.loggerName);
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.issues.recordIssue("Can not load class 'missing'");

		// Attempt to load
		this.loadAdministrationType(false, (context) -> {
			context.loadClass("missing");
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing class
		this.issues.recordIssue("Can not obtain resource at location 'missing'");

		// Attempt to load
		this.loadAdministrationType(false, (context) -> {
			context.getResource("missing");
		});
	}

	/**
	 * Ensure issue if missing service.
	 */
	public void testMissingService() {

		// Record missing service
		this.issues.recordIssue(MissingServiceFactory.getIssueDescription());

		// Attempt to load
		this.loadAdministrationType(false, (context) -> context.loadService(MissingServiceFactory.class, null));
	}

	/**
	 * Ensure issue if fail to load service.
	 */
	public void testFailLoadService() {

		// Record load issue for service
		this.issues.recordIssue(FailServiceFactory.getIssueDescription(), FailServiceFactory.getCause());

		// Attempt to load
		this.loadAdministrationType(false, (context) -> context.loadService(FailServiceFactory.class, null));
	}

	/**
	 * Ensure issue if fails to init the {@link AdministrationSource}.
	 */
	public void testFailInitAdministrationSource() {

		final NullPointerException failure = new NullPointerException("Fail init AdministrationSource");

		// Record failure to init the Administration Source
		this.issues.recordIssue("Failed to init", failure);

		// Attempt to load
		this.loadAdministrationType(false, (context) -> {
			throw failure;
		});
	}

	/**
	 * Ensure issue if <code>null</code> {@link AdministrationSourceMetaData}.
	 */
	public void testNullAdministrationSourceMetaData() {

		// Record null the Administration Source meta-data
		this.issues.recordIssue("Returned null AdministrationSourceMetaData");

		// Attempt to load
		this.loadAdministrationType(false, (context) -> {
			MockAdministrationSource.metaData = null;
		});
	}

	/**
	 * Ensure issue if fails to obtain the {@link AdministrationSourceMetaData}.
	 */
	public void testFailGetAdministrationSourceMetaData() {

		final Error failure = new Error("Obtain meta-data failure");

		// Record failure to obtain the meta-data
		this.issues.recordIssue("Failed to init", failure);

		// Attempt to load
		MockAdministrationSource.metaDataFailure = failure;
		this.loadAdministrationType(false, null);
	}

	/**
	 * Ensure issue if no {@link AdministrationFactory} from meta-data.
	 */
	public void testNoAdministrationFactory() {

		// Record no administration factory
		this.recordReturn(this.metaData, this.metaData.getAdministrationFactory(), null);
		this.issues.recordIssue("No AdministrationFactory provided");

		// Attempt to load
		this.loadAdministrationType(false, null);
	}

	/**
	 * Ensure issue if no extension interface type from meta-data.
	 */
	public void testNoExtensionInterface() {

		// Record no extension class
		this.recordReturn(this.metaData, this.metaData.getAdministrationFactory(), this.factory);
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(), null);
		this.issues.recordIssue("No extension interface provided");

		// Attempt to load
		this.loadAdministrationType(false, null);
	}

	/**
	 * Ensure can load simple {@link Administration} (without
	 * {@link AdministrationFlowMetaData}, {@link AdministrationEscalationMetaData}
	 * nor {@link AdministrationGovernanceMetaData}).
	 */
	public void testSimpleAdministration() {

		// Record simple administration
		this.record_init();
		this.record_flowMetaData();
		this.record_escalationMetaData();
		this.record_governanceMetaData();

		// Validate simple details of type
		AdministrationType<?, ?, ?> type = this.loadAdministrationType(true, null);
		assertEquals("Incorrect administration factory", this.factory, type.getAdministrationFactory());
		assertEquals("Incorrect extension interface", XAResource.class, type.getExtensionType());
		assertNull("No flow key class", type.getFlowKeyClass());
		assertEquals("No flow meta-data", 0, type.getFlowTypes().length);
		assertEquals("No escalation meta-data", 0, type.getEscalationTypes().length);
		assertNull("No governance key class", type.getGovernanceKeyClass());
		assertEquals("No governance meta-data", 0, type.getGovernanceTypes().length);
	}

	/**
	 * Ensure issue if <code>null</code> {@link AdministrationFlowMetaData} array.
	 */
	public void testNullFlowMetaDataArray() {

		// Record null flow meta-data
		this.record_init();
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), null);
		this.issues.recordIssue("Must provide flow meta-data");

		// Attempt to load
		this.loadAdministrationType(false, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link AdministrationFlowMetaData} entry.
	 */
	public void testNullFlowMetaData() {

		// Record null flow meta-data
		this.record_init();
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), new AdministrationFlowMetaData[] { null });
		this.issues.recordIssue("Null meta-data for flow 0");

		// Attempt to load
		this.loadAdministrationType(false, null);
	}

	/**
	 * Ensure can load with empty {@link AdministrationFlowMetaData} details.
	 */
	public void testEmptyFlowMetaDataDetails() {

		AdministrationFlowMetaData<?> flowMetaData = this.createMock(AdministrationFlowMetaData.class);

		// Record empty flow meta-data
		this.record_init();
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new AdministrationFlowMetaData[] { flowMetaData });
		this.recordReturn(flowMetaData, flowMetaData.getKey(), null);
		this.recordReturn(flowMetaData, flowMetaData.getArgumentType(), null);
		this.recordReturn(flowMetaData, flowMetaData.getLabel(), null);
		this.record_escalationMetaData();
		this.record_governanceMetaData();

		// Load the administration type
		AdministrationType<?, ?, ?> type = this.loadAdministrationType(true, null);
		assertEquals("Should be no flow key class", Indexed.class, type.getFlowKeyClass());
		AdministrationFlowType<?>[] flows = type.getFlowTypes();
		assertEquals("Incorrect number of flows", 1, flows.length);
		AdministrationFlowType<?> flow = flows[0];
		assertEquals("Incorrect flow name", "0", flow.getFlowName());
		assertEquals("Incorrect index", 0, flow.getIndex());
		assertNull("Should be no flow key", flow.getKey());
		assertNull("Should be no argument", flow.getArgumentType());
	}

	/**
	 * Ensure can load {@link AdministrationFlowMetaData} with details.
	 */
	public void testDetailedFlowMetaData() {

		AdministrationFlowMetaData<?> flowMetaData = this.createMock(AdministrationFlowMetaData.class);

		// Record detailed flow meta-data
		this.record_init();
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new AdministrationFlowMetaData[] { flowMetaData });
		this.recordReturn(flowMetaData, flowMetaData.getKey(), MockFlowKey.FLOW);
		this.recordReturn(flowMetaData, flowMetaData.getArgumentType(), Connection.class);
		this.recordReturn(flowMetaData, flowMetaData.getLabel(), "LABEL");
		this.record_escalationMetaData();
		this.record_governanceMetaData();

		// Load the administration type
		AdministrationType<?, ?, ?> type = this.loadAdministrationType(true, null);
		assertEquals("Incorrect flow key class", MockFlowKey.class, type.getFlowKeyClass());
		AdministrationFlowType<?>[] flows = type.getFlowTypes();
		assertEquals("Incorrect number of flows", 1, flows.length);
		AdministrationFlowType<?> flow = flows[0];
		assertEquals("Incorrect flow name", "LABEL", flow.getFlowName());
		assertEquals("Incorrect index", 0, flow.getIndex());
		assertEquals("Incorrect flow key", MockFlowKey.FLOW, flow.getKey());
		assertEquals("Incorrect argument", Connection.class, flow.getArgumentType());
	}

	/**
	 * Ensure default {@link Flow} name to {@link Flow} name.
	 */
	public void testDefaultFlowNameToFlowKey() {

		AdministrationFlowMetaData<?> flowMetaData = this.createMock(AdministrationFlowMetaData.class);

		// Record detailed flow meta-data
		this.record_init();
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new AdministrationFlowMetaData[] { flowMetaData });
		this.recordReturn(flowMetaData, flowMetaData.getKey(), MockFlowKey.FLOW);
		this.recordReturn(flowMetaData, flowMetaData.getArgumentType(), null);
		this.recordReturn(flowMetaData, flowMetaData.getLabel(), null);
		this.record_escalationMetaData();
		this.record_governanceMetaData();

		// Load the administration type
		AdministrationType<?, ?, ?> type = this.loadAdministrationType(true, null);
		AdministrationFlowType<?> flow = type.getFlowTypes()[0];
		assertEquals("Incorrect flow name", "FLOW", flow.getFlowName());
	}

	/**
	 * Ensure issue if duplicate {@link Flow} key.
	 */
	public void testDuplicateFlowKey() {

		AdministrationFlowMetaData<?> flowOne = this.createMock(AdministrationFlowMetaData.class);
		AdministrationFlowMetaData<?> flowTwo = this.createMock(AdministrationFlowMetaData.class);

		// Record detailed flow meta-data
		this.record_init();
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new AdministrationFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getKey(), MockFlowKey.FLOW);
		this.recordReturn(flowOne, flowOne.getArgumentType(), null);
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), MockFlowKey.FLOW);
		this.issues.recordIssue("Duplicate flow key FLOW on flow meta-data");

		// Attempt to load administration type
		this.loadAdministrationType(false, null);
	}

	/**
	 * Ensure issue if invalid {@link Flow} key.
	 */
	public void testInvalidFlowKey() {

		AdministrationFlowMetaData<?> flowOne = this.createMock(AdministrationFlowMetaData.class);
		AdministrationFlowMetaData<?> flowTwo = this.createMock(AdministrationFlowMetaData.class);

		// Record detailed flow meta-data
		this.record_init();
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new AdministrationFlowMetaData[] { flowOne, flowTwo });
		this.recordReturn(flowOne, flowOne.getKey(), MockFlowKey.FLOW);
		this.recordReturn(flowOne, flowOne.getArgumentType(), null);
		this.recordReturn(flowOne, flowOne.getLabel(), null);
		this.recordReturn(flowTwo, flowTwo.getKey(), InvalidFlowKey.INVALID_KEY);
		this.issues.recordIssue("May only use one enum type to define flow keys (" + MockFlowKey.class.getName() + ", "
				+ InvalidFlowKey.class.getName() + ")");

		// Attempt to load administration type
		this.loadAdministrationType(false, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link AdministrationEscalationMetaData}
	 * array.
	 */
	public void testNullEscalationMetaDataArray() {

		// Record null escalation meta-data
		this.record_init();
		this.record_flowMetaData();
		this.recordReturn(this.metaData, this.metaData.getEscalationMetaData(), null);
		this.issues.recordIssue("Must provide escalation meta-data");

		// Attempt to load administration type
		this.loadAdministrationType(false, null);
	}

	/**
	 * Ensure no issue if <code>null</code> {@link AdministrationEscalationMetaData}
	 * entry.
	 */
	public void testNullEscalationMetaData() {

		// Record null escalation meta-data
		this.record_init();
		this.record_flowMetaData();
		this.recordReturn(this.metaData, this.metaData.getEscalationMetaData(),
				new AdministrationEscalationMetaData[] { null });
		this.issues.recordIssue("Null meta-data for escalation 0");

		// Attempt to load
		this.loadAdministrationType(false, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link Escalation} type from meta-data.
	 */
	public void testNullEscalationType() {

		AdministrationEscalationMetaData escalationMetaData = this.createMock(AdministrationEscalationMetaData.class);

		// Record null escalation type
		this.record_init();
		this.record_flowMetaData();
		this.recordReturn(this.metaData, this.metaData.getEscalationMetaData(),
				new AdministrationEscalationMetaData[] { escalationMetaData });
		this.recordReturn(escalationMetaData, escalationMetaData.getEscalationType(), null);
		this.issues.recordIssue("Null escalation type from escalation 0");

		// Attempt to load
		this.loadAdministrationType(false, null);
	}

	/**
	 * Ensure issue if same {@link Escalation} type twice.
	 */
	public void testDuplicateEscalationType() {

		AdministrationEscalationMetaData escalationOne = this.createMock(AdministrationEscalationMetaData.class);
		AdministrationEscalationMetaData escalationTwo = this.createMock(AdministrationEscalationMetaData.class);

		// Record duplicate escalation type
		this.record_init();
		this.record_flowMetaData();
		this.recordReturn(this.metaData, this.metaData.getEscalationMetaData(),
				new AdministrationEscalationMetaData[] { escalationOne, escalationTwo });
		this.recordReturn(escalationOne, escalationOne.getEscalationType(), SQLException.class);
		this.recordReturn(escalationOne, escalationOne.getLabel(), null);
		this.recordReturn(escalationTwo, escalationTwo.getEscalationType(), SQLException.class);
		this.issues.recordIssue("Escalation listed twice (" + SQLException.class.getName() + ")");

		// Attempt to load
		this.loadAdministrationType(false, null);
	}

	/**
	 * Ensure {@link Escalation}.
	 */
	public void testEscalation() {

		AdministrationEscalationMetaData escalationMetaData = this.createMock(AdministrationEscalationMetaData.class);

		// Record escalation
		this.record_init();
		this.record_flowMetaData();
		this.recordReturn(this.metaData, this.metaData.getEscalationMetaData(),
				new AdministrationEscalationMetaData[] { escalationMetaData });
		this.recordReturn(escalationMetaData, escalationMetaData.getEscalationType(), SQLException.class);
		this.recordReturn(escalationMetaData, escalationMetaData.getLabel(), "LABEL");
		this.record_governanceMetaData();

		// Validate escalation
		AdministrationType<?, ?, ?> type = this.loadAdministrationType(true, null);
		AdministrationEscalationType[] escalations = type.getEscalationTypes();
		assertEquals("Incorrect number of escalations", 1, escalations.length);
		AdministrationEscalationType escalation = escalations[0];
		assertEquals("Incorrect escalation type", SQLException.class, escalation.getEscalationType());
		assertEquals("Incorrect escalation name", "LABEL", escalation.getEscalationName());
	}

	/**
	 * Ensure default {@link Escalation} name.
	 */
	public void testDefaultEscalationName() {

		AdministrationEscalationMetaData escalationMetaData = this.createMock(AdministrationEscalationMetaData.class);

		// Record null escalation label
		this.record_init();
		this.record_flowMetaData();
		this.recordReturn(this.metaData, this.metaData.getEscalationMetaData(),
				new AdministrationEscalationMetaData[] { escalationMetaData });
		this.recordReturn(escalationMetaData, escalationMetaData.getEscalationType(), SQLException.class);
		this.recordReturn(escalationMetaData, escalationMetaData.getLabel(), null);
		this.record_governanceMetaData();

		// Validate default escalation
		AdministrationType<?, ?, ?> type = this.loadAdministrationType(true, null);
		AdministrationEscalationType escalation = type.getEscalationTypes()[0];
		assertEquals("Incorrect escalation name", SQLException.class.getName(), escalation.getEscalationName());
	}

	/**
	 * Ensure issue if <code>null</code> {@link AdministrationGovernanceMetaData}
	 * array.
	 */
	public void testNullGovernanceMetaDataArray() {

		// Record null governance meta-data
		this.record_init();
		this.record_flowMetaData();
		this.record_escalationMetaData();
		this.recordReturn(this.metaData, this.metaData.getGovernanceMetaData(), null);
		this.issues.recordIssue("Must provide governance meta-data");

		// Attempt to load
		this.loadAdministrationType(false, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link AdministrationGovernanceMetaData}
	 * entry.
	 */
	public void testNullGovernanceMetaData() {

		// Record null governance meta-data
		this.record_init();
		this.record_flowMetaData();
		this.record_escalationMetaData();
		this.recordReturn(this.metaData, this.metaData.getGovernanceMetaData(),
				new AdministrationGovernanceMetaData[] { null });
		this.issues.recordIssue("Null meta-data for governance 0");

		// Attempt to load
		this.loadAdministrationType(false, null);
	}

	/**
	 * Ensure can load with empty {@link AdministrationGovernanceMetaData} details.
	 */
	public void testEmptyGovernanceMetaDataDetails() {

		AdministrationGovernanceMetaData<?> governanceMetaData = this
				.createMock(AdministrationGovernanceMetaData.class);

		// Record empty governance meta-data
		this.record_init();
		this.record_flowMetaData();
		this.record_escalationMetaData();
		this.recordReturn(this.metaData, this.metaData.getGovernanceMetaData(),
				new AdministrationGovernanceMetaData[] { governanceMetaData });
		this.recordReturn(governanceMetaData, governanceMetaData.getKey(), null);
		this.recordReturn(governanceMetaData, governanceMetaData.getLabel(), null);

		// Load the administration type
		AdministrationType<?, ?, ?> type = this.loadAdministrationType(true, null);
		assertEquals("Incorrect governance key class", Indexed.class, type.getGovernanceKeyClass());
		AdministrationGovernanceType<?>[] governances = type.getGovernanceTypes();
		assertEquals("Incorrect number of governances", 1, governances.length);
		AdministrationGovernanceType<?> governance = governances[0];
		assertEquals("Incorrect governance name", "0", governance.getGovernanceName());
		assertEquals("Incorrect index", 0, governance.getIndex());
		assertNull("Should be no governance key", governance.getKey());
	}

	/**
	 * Ensure can load {@link AdministrationGovernanceMetaData} with details.
	 */
	public void testDetailedGovernanceMetaData() {

		AdministrationGovernanceMetaData<?> governanceMetaData = this
				.createMock(AdministrationGovernanceMetaData.class);

		// Record detailed governance meta-data
		this.record_init();
		this.record_flowMetaData();
		this.record_escalationMetaData();
		this.recordReturn(this.metaData, this.metaData.getGovernanceMetaData(),
				new AdministrationGovernanceMetaData[] { governanceMetaData });
		this.recordReturn(governanceMetaData, governanceMetaData.getKey(), MockGovernanceKey.GOVERNANCE);
		this.recordReturn(governanceMetaData, governanceMetaData.getLabel(), "LABEL");

		// Load the administration type
		AdministrationType<?, ?, ?> type = this.loadAdministrationType(true, null);
		assertEquals("Incorrect governance key class", MockGovernanceKey.class, type.getGovernanceKeyClass());
		AdministrationGovernanceType<?>[] governances = type.getGovernanceTypes();
		assertEquals("Incorrect number of flows", 1, governances.length);
		AdministrationGovernanceType<?> governance = governances[0];
		assertEquals("Incorrect governance name", "LABEL", governance.getGovernanceName());
		assertEquals("Incorrect index", 0, governance.getIndex());
		assertEquals("Incorrect governance key", MockGovernanceKey.GOVERNANCE, governance.getKey());
	}

	/**
	 * Ensure default {@link Governance} name to {@link Governance} name.
	 */
	public void testDefaultGovernanceNameToGovernanceKey() {

		AdministrationGovernanceMetaData<?> governanceMetaData = this
				.createMock(AdministrationGovernanceMetaData.class);

		// Record detailed governance meta-data
		this.record_init();
		this.record_flowMetaData();
		this.record_escalationMetaData();
		this.recordReturn(this.metaData, this.metaData.getGovernanceMetaData(),
				new AdministrationGovernanceMetaData[] { governanceMetaData });
		this.recordReturn(governanceMetaData, governanceMetaData.getKey(), MockGovernanceKey.GOVERNANCE);
		this.recordReturn(governanceMetaData, governanceMetaData.getLabel(), null);

		// Load the administration type
		AdministrationType<?, ?, ?> type = this.loadAdministrationType(true, null);
		AdministrationGovernanceType<?> governance = type.getGovernanceTypes()[0];
		assertEquals("Incorrect governance name", "GOVERNANCE", governance.getGovernanceName());
	}

	/**
	 * Ensure issue if duplicate {@link Governance} key.
	 */
	public void testDuplicateGovernanceKey() {

		AdministrationGovernanceMetaData<?> governanceOne = this.createMock(AdministrationGovernanceMetaData.class);
		AdministrationGovernanceMetaData<?> governanceTwo = this.createMock(AdministrationGovernanceMetaData.class);

		// Record detailed governance meta-data
		this.record_init();
		this.record_flowMetaData();
		this.record_escalationMetaData();
		this.recordReturn(this.metaData, this.metaData.getGovernanceMetaData(),
				new AdministrationGovernanceMetaData[] { governanceOne, governanceTwo });
		this.recordReturn(governanceOne, governanceOne.getKey(), MockGovernanceKey.GOVERNANCE);
		this.recordReturn(governanceOne, governanceOne.getLabel(), null);
		this.recordReturn(governanceTwo, governanceTwo.getKey(), MockGovernanceKey.GOVERNANCE);
		this.issues.recordIssue("Duplicate governance key GOVERNANCE on governance meta-data");

		// Attempt to load administration type
		this.loadAdministrationType(false, null);
	}

	/**
	 * Ensure issue if invalid {@link Governance} key.
	 */
	public void testInvalidGovernanceKey() {

		AdministrationGovernanceMetaData<?> governanceOne = this.createMock(AdministrationGovernanceMetaData.class);
		AdministrationGovernanceMetaData<?> governanceTwo = this.createMock(AdministrationGovernanceMetaData.class);

		// Record detailed governance meta-data
		this.record_init();
		this.record_flowMetaData();
		this.record_escalationMetaData();
		this.recordReturn(this.metaData, this.metaData.getGovernanceMetaData(),
				new AdministrationGovernanceMetaData[] { governanceOne, governanceTwo });
		this.recordReturn(governanceOne, governanceOne.getKey(), MockGovernanceKey.GOVERNANCE);
		this.recordReturn(governanceOne, governanceOne.getLabel(), null);
		this.recordReturn(governanceTwo, governanceTwo.getKey(), InvalidGovernanceKey.INVALID_KEY);
		this.issues.recordIssue("May only use one enum type to define governance keys ("
				+ MockGovernanceKey.class.getName() + ", " + InvalidGovernanceKey.class.getName() + ")");

		// Attempt to load administration type
		this.loadAdministrationType(false, null);
	}

	/**
	 * Mock {@link Flow} key.
	 */
	private static enum MockFlowKey {
		FLOW
	}

	/**
	 * Invalid {@link Flow} key.
	 */
	private static enum InvalidFlowKey {
		INVALID_KEY
	}

	/**
	 * Mock {@link Governance} key.
	 */
	private static enum MockGovernanceKey {
		GOVERNANCE
	}

	/**
	 * Invalid {@link Governance} key.
	 */
	public static enum InvalidGovernanceKey {
		INVALID_KEY
	}

	/**
	 * Record initial details of extension and {@link AdministrationFactory}.
	 */
	private void record_init() {
		this.recordReturn(this.metaData, this.metaData.getAdministrationFactory(), this.factory);
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(), XAResource.class);
	}

	/**
	 * Record {@link AdministrationFlowMetaData}.
	 */
	private void record_flowMetaData() {
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(), new AdministrationFlowMetaData[0]);
	}

	/**
	 * Record {@link AdministrationEscalationMetaData}.
	 */
	private void record_escalationMetaData() {
		this.recordReturn(this.metaData, this.metaData.getEscalationMetaData(),
				new AdministrationEscalationMetaData[0]);
	}

	/**
	 * Record {@link AdministrationGovernanceMetaData}.
	 */
	private void record_governanceMetaData() {
		this.recordReturn(this.metaData, this.metaData.getGovernanceMetaData(),
				new AdministrationGovernanceMetaData[0]);
	}

	/**
	 * Loads the {@link AdministrationType}.
	 * 
	 * @param isExpectedToLoad       Flag indicating if expecting to load the
	 *                               {@link FunctionNamespaceType}.
	 * @param init                   {@link Init}.
	 * @param propertyNameValuePairs {@link Property} name value pairs.
	 * @return Loaded {@link AdministrationType}.
	 */
	@SuppressWarnings("rawtypes")
	public AdministrationType<?, ?, ?> loadAdministrationType(boolean isExpectedToLoad, Init init,
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

		// Create the administrator loader and load the administrator type
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		AdministrationLoader adminLoader = compiler.getAdministrationLoader();
		MockAdministrationSource.init = init;
		AdministrationType adminType = adminLoader.loadAdministrationType(MockAdministrationSource.class, propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the administrator type", adminType);
		} else {
			assertNull("Should not load the administrator type", adminType);
		}

		// Return the administrator type
		return adminType;
	}

	/**
	 * Implement to initialise the {@link MockAdministrationSource}.
	 */
	private static interface Init {

		/**
		 * Implemented to init the {@link AdministrationSource}.
		 * 
		 * @param context {@link AdministrationSourceContext}.
		 */
		void init(AdministrationSourceContext context);
	}

	/**
	 * Mock {@link AdministrationSource}.
	 */
	@TestSource
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class MockAdministrationSource implements AdministrationSource<Object, Indexed, Indexed> {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * {@link Init} to init the {@link AdministrationSource}.
		 */
		public static Init init = null;

		/**
		 * Failure to obtain the {@link AdministrationMetaData}.
		 */
		public static Error metaDataFailure = null;

		/**
		 * {@link AdministrationSourceSpecification}.
		 */
		public static AdministrationSourceMetaData metaData;

		/**
		 * Name of {@link Logger}.
		 */
		public static String loggerName;

		/**
		 * Resets the state for next test.
		 * 
		 * @param metaData {@link AdministrationSourceMetaData}.
		 */
		public static void reset(AdministrationSourceMetaData<?, ?, ?> metaData) {
			instantiateFailure = null;
			init = null;
			metaDataFailure = null;
			MockAdministrationSource.metaData = metaData;
			loggerName = null;
		}

		/**
		 * Initiate with possible failure.
		 */
		public MockAdministrationSource() {
			// Throw instantiate failure
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================== AdministrationSource ===========================
		 */

		@Override
		public AdministrationSourceSpecification getSpecification() {
			fail("Should not obtain specification");
			return null;
		}

		@Override
		public AdministrationSourceMetaData init(AdministrationSourceContext context) throws Exception {

			// Capture the logger name
			loggerName = context.getLogger().getName();

			// Run the init if available
			if (init != null) {
				init.init(context);
			}

			// Throw meta-data failure
			if (metaDataFailure != null) {
				throw metaDataFailure;
			}

			// Return the meta-data
			return metaData;
		}
	}

}
