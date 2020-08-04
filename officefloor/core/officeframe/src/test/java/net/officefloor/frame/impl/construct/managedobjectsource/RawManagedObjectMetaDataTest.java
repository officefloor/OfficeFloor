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

package net.officefloor.frame.impl.construct.managedobjectsource;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.api.source.UnknownResourceError;
import net.officefloor.frame.impl.construct.MockConstruct;
import net.officefloor.frame.impl.construct.MockConstruct.ConstructManagedObjectSource;
import net.officefloor.frame.impl.construct.MockConstruct.ManagedObjectSourceMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawBoundManagedObjectInstanceMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawBoundManagedObjectMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.asset.AssetManagerFactory;
import net.officefloor.frame.impl.construct.officefloor.OfficeFloorBuilderImpl;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the creation of a {@link RawManagedObjectMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawManagedObjectMetaDataTest extends OfficeFrameTestCase {

	/**
	 * Name of the {@link ManagedObjectSource}.
	 */
	private final String MANAGED_OBJECT_NAME = "MANAGED OBJECT NAME";

	/**
	 * {@link ManagedObjectSourceConfiguration}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ManagedObjectBuilderImpl<?, ?, ?> configuration = new ManagedObjectBuilderImpl(MANAGED_OBJECT_NAME,
			MockManagedObjectSource.class);

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext = this.createMock(SourceContext.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * {@link OfficeFloorConfiguration}.
	 */
	private final OfficeFloorBuilderImpl officeFloorConfiguration = new OfficeFloorBuilderImpl("OfficeFloor");

	/**
	 * {@link AssetManagerFactory}.
	 */
	private final AssetManagerFactory assetManagerFactory = MockConstruct.mockAssetManagerFactory();

	/**
	 * {@link ManagedObjectSourceMetaData}.
	 */
	private final ManagedObjectSourceMetaDataMockBuilder<Indexed, FlowKey> metaData = MockConstruct
			.mockManagedObjectSourceMetaData();

	/**
	 * {@link ManagedFunctionFactory}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedFunctionFactory<None, Indexed> functionFactory = this.createMock(ManagedFunctionFactory.class);

	@Override
	protected void setUp() throws Exception {
		// Reset the mock managed object source state
		MockManagedObjectSource.reset(this.functionFactory, this.metaData);
	}

	/**
	 * Records setting up the {@link ManagedObjectSourceContext}.
	 */
	private void record_setupContext(String... profiles) {
		this.recordReturn(this.sourceContext, this.sourceContext.getProfiles(), Arrays.asList(profiles));
	}

	/**
	 * Ensures issue if no {@link ManagedObjectSource} name.
	 */
	public void testNoManagedObjectSourceName() {

		// Record no name
		this.configuration = MockConstruct.mockManagedObjectBuilder(null);
		this.issues.addIssue(AssetType.OFFICE_FLOOR, "OFFICE_FLOOR", "ManagedObject added without a name");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link ManagedObjectSource} class.
	 */
	public void testNoManagedObjectSourceClass() {

		// Record no class
		this.configuration = new ManagedObjectBuilderImpl<>(MANAGED_OBJECT_NAME,
				(ConstructManagedObjectSource<?, ?>) null);
		this.record_issue("No ManagedObjectSource class provided");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fail to instantiate {@link ManagedObjectSource}.
	 */
	public void testFailInstantiateManagedObjectSource() {

		final Exception failure = new Exception("instantiate failure");

		// Record fail instantiate
		MockManagedObjectSource.instantiateFailure = failure;
		this.record_issue("Failed to instantiate " + MockManagedObjectSource.class.getName(), failure);

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link ManagingOfficeConfiguration}.
	 */
	public void testNoManagingOfficeConfiguration() {

		// Record no managing office configuration
		this.record_issue("No managing office configuration");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no managing {@link Office} name.
	 */
	public void testNoManagingOfficeName() {

		// Record no managing office name provided
		this.configuration.setManagingOffice(null);
		this.record_issue("No managing office specified");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no managing {@link Office} found.
	 */
	public void testNoManagingOfficeFound() {

		// Record no managing office found
		this.configuration.setManagingOffice("OFFICE");
		this.record_issue("Can not find managing office 'OFFICE'");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure inherit profiles from {@link OfficeFloorBuilder}.
	 */
	public void testOfficeFloorProfiles() {

		// Record setup for profiles
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		MockManagedObjectSource.profiles = null;
		this.record_setupContext("profile");

		// Construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();

		// Ensure correct profiles
		assertEquals("Incorrect number of profiles", 1, MockManagedObjectSource.profiles.size());
		assertEquals("Incorrect profile", "profile", MockManagedObjectSource.profiles.get(0));
	}

	/**
	 * Ensure additional profiles.
	 */
	public void testAdditionalProfiles() {

		// Record additional profiles
		this.configuration.setManagingOffice("OFFICE");
		this.configuration.addAdditionalProfile("additional");
		this.configuration.addAdditionalProfile("profile");
		this.officeFloorConfiguration.addOffice("OFFICE");
		MockManagedObjectSource.profiles = null;
		this.record_setupContext("inherit", "profile");

		// Construct managed object with additional profiles
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();

		// Ensure correct profiles
		assertEquals("Incorrect number of profiles", 3, MockManagedObjectSource.profiles.size());
		assertEquals("Incorrect additional profile", "additional", MockManagedObjectSource.profiles.get(0));
		assertEquals("Incorrect reordered profile", "profile", MockManagedObjectSource.profiles.get(1));
		assertEquals("Incorrect inherit profile", "inherit", MockManagedObjectSource.profiles.get(2));
	}

	/**
	 * Ensure correct logger name.
	 */
	public void testLoggerName() {

		// Record setup for logger name
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		MockManagedObjectSource.loggerName = null;
		this.record_setupContext();

		// Construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();

		// Ensure correct logger name
		assertEquals("Incorrect logger name", MANAGED_OBJECT_NAME, MockManagedObjectSource.loggerName);
	}

	/**
	 * Ensures issue if missing required property.
	 */
	public void testMissingProperty() {

		// Record fail instantiate due to missing property
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		MockManagedObjectSource.requiredPropertyName = "required.property";
		this.record_setupContext();
		this.record_issue("Must specify property 'required.property'");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testClassLoaderAndMissingClass() {

		final ClassLoader classLoader = new URLClassLoader(new URL[0]);
		final String CLASS_NAME = "UNKNOWN CLASS";

		// Record fail instantiate due to missing class
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		this.record_setupContext();
		this.recordReturn(this.sourceContext, this.sourceContext.getClassLoader(), classLoader);
		this.recordThrows(this.sourceContext, this.sourceContext.loadClass(CLASS_NAME),
				new UnknownClassError(CLASS_NAME));
		MockManagedObjectSource.classLoader = classLoader;
		MockManagedObjectSource.requiredClassName = CLASS_NAME;
		this.record_issue("Can not load class '" + CLASS_NAME + "'");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if missing a resource.
	 */
	public void testMissingResource() {

		final String RESOURCE_LOCATION = "RESOURCE LOCATION";

		// Record fail instantiate due to missing resource
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		this.record_setupContext();
		this.recordThrows(this.sourceContext, this.sourceContext.getResource(RESOURCE_LOCATION),
				new UnknownResourceError(RESOURCE_LOCATION));
		MockManagedObjectSource.requiredResourceLocation = RESOURCE_LOCATION;
		this.record_issue("Can not obtain resource at location '" + RESOURCE_LOCATION + "'");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in initialising {@link ManagedObjectSource}.
	 */
	public void testFailInitManagedObjectSource() {

		final Exception failure = new Exception("init failure");

		// Record fail instantiate
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		MockManagedObjectSource.initFailure = failure;
		this.record_setupContext();
		this.record_issue("Failed to initialise " + MockManagedObjectSource.class.getName(), failure);

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if null {@link ManagedObjectSourceMetaData}.
	 */
	public void testNullMetaData() {

		// Record null meta-data
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		MockManagedObjectSource.metaData = null;
		this.record_setupContext();
		this.record_issue("Must provide meta-data");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link Object} type.
	 */
	public void testNoObjectType() {

		// Record no object type
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		this.metaData.setObjectClass(null);
		this.record_setupContext();
		this.record_issue("No object type provided");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link ManagedObject} class.
	 */
	public void testNoManagedObjectClass() {

		// Record no managed object class
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		this.metaData.setManagedObjectClass(null);
		this.record_setupContext();
		this.record_issue("No managed object class provided");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if negative timeout.
	 */
	public void testNegativeTimeout() {

		// Record negative default timeout
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		this.configuration.setTimeout(-1);
		this.record_setupContext();
		this.record_issue("Must not have negative timeout");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if {@link AsynchronousManagedObject} but 0 timeout.
	 */
	public void testAsynchronousManagedObjectWithZeroTimeout() {

		// Record asynchronous managed object with no timeout
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		this.metaData.setManagedObjectClass(AsynchronousManagedObject.class);
		this.configuration.setTimeout(0);
		this.record_setupContext();
		this.record_issue("Non-zero timeout must be provided for AsynchronousManagedObject");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link InputManagedObjectConfiguration}.
	 */
	public void testNoInputConfiguration() {

		// Record no process bound name
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		this.metaData.addFlow(Object.class, null);
		this.record_setupContext();
		this.record_issue("Must provide Input configuration as Managed Object Source requires flows");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to add a {@link ManagedFunction}.
	 */
	public void testAddFunction() {

		final String FUNCTION_NAME = MANAGED_OBJECT_NAME + ".FUNCTION";

		// Record adding a function
		this.configuration.setManagingOffice("OFFICE");
		OfficeConfiguration office = (OfficeConfiguration) this.officeFloorConfiguration.addOffice("OFFICE");
		MockManagedObjectSource.addFunctionName = "FUNCTION";
		this.record_setupContext();

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();

		// Ensure function added to office
		ManagedFunctionConfiguration<?, ?>[] functions = office.getManagedFunctionConfiguration();
		assertEquals("Should have added function", 1, functions.length);
		assertEquals("Incorrect function", FUNCTION_NAME, functions[0].getFunctionName());
	}

	/**
	 * Ensure able to link a parameter to the added {@link ManagedFunction}.
	 */
	public void testLinkParameterToAddedFunction() {

		final String FUNCTION_NAME = MANAGED_OBJECT_NAME + ".FUNCTION";
		final Class<?> parameterType = String.class;

		// Record linking a parameter to the added function
		this.configuration.setManagingOffice("OFFICE");
		OfficeConfiguration office = (OfficeConfiguration) this.officeFloorConfiguration.addOffice("OFFICE");
		MockManagedObjectSource.addFunctionName = "FUNCTION";
		MockManagedObjectSource.addFunctionLinkedParameter = parameterType;
		this.record_setupContext();

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();

		// Ensure added parameter to function
		ManagedFunctionConfiguration<?, ?> function = office.getManagedFunctionConfiguration()[0];
		assertEquals("Incorrect function", FUNCTION_NAME, function.getFunctionName());
		ManagedFunctionObjectConfiguration<?>[] parameters = function.getObjectConfiguration();
		assertEquals("Incorrect number of parameters", 1, parameters.length);
		assertTrue("Ensure is a parameter", parameters[0].isParameter());
	}

	/**
	 * Ensure able to link the {@link ManagedObject} to the added
	 * {@link ManagedFunction}.
	 */
	public void testLinkManagedObjectToAddedFunction() {

		final String FUNCTION_NAME = MANAGED_OBJECT_NAME + ".FUNCTION";

		// Record linking the managed object to the added function
		this.configuration.setManagingOffice("OFFICE");
		OfficeConfiguration office = (OfficeConfiguration) this.officeFloorConfiguration.addOffice("OFFICE");
		MockManagedObjectSource.addFunctionName = "FUNCTION";
		MockManagedObjectSource.isFunctionLinkManagedObject = true;
		this.record_setupContext();

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();

		// Ensure added parameter to function
		ManagedFunctionConfiguration<?, ?> function = office.getManagedFunctionConfiguration()[0];
		assertEquals("Incorrect function", FUNCTION_NAME, function.getFunctionName());
		ManagedFunctionObjectConfiguration<?>[] dependencies = function.getObjectConfiguration();
		assertEquals("Incorrect number of dependencies", 1, dependencies.length);
		assertEquals("Ensure is managed object", MANAGED_OBJECT_NAME, dependencies[0].getScopeManagedObjectName());
	}

	/**
	 * Ensure able to link the input {@link ManagedObject} to the added
	 * {@link ManagedFunction}.
	 */
	public void testLinkInputManagedObjectToAddedFunction() {

		final String FUNCTION_NAME = MANAGED_OBJECT_NAME + ".FUNCTION";

		// Record linking the managed object to the added function
		this.configuration.setManagingOffice("OFFICE").setInputManagedObjectName("INPUT");
		OfficeConfiguration office = (OfficeConfiguration) this.officeFloorConfiguration.addOffice("OFFICE");
		MockManagedObjectSource.addFunctionName = "FUNCTION";
		MockManagedObjectSource.isFunctionLinkManagedObject = true;
		this.metaData.addFlow(String.class, null);
		this.record_setupContext();

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();

		// Ensure added parameter to function
		ManagedFunctionConfiguration<?, ?> function = office.getManagedFunctionConfiguration()[0];
		assertEquals("Incorrect function", FUNCTION_NAME, function.getFunctionName());
		ManagedFunctionObjectConfiguration<?>[] dependencies = function.getObjectConfiguration();
		assertEquals("Incorrect number of dependencies", 1, dependencies.length);
		assertEquals("Ensure is input managed object", "INPUT", dependencies[0].getScopeManagedObjectName());
	}

	/**
	 * Ensure issue if no {@link ManagedObject} is configured for the
	 * {@link ManagedObjectFunctionDependency}.
	 */
	public void testNoDependencyForAddedFunction() {

		// Record no dependency available to the added function
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		MockManagedObjectSource.addFunctionName = "FUNCTION";
		MockManagedObjectSource.addFunctionLinkedDependency = "DEPENDENCY";
		this.record_setupContext();
		this.record_issue("No dependency configured for ManagedObjectFunctionDependency 'DEPENDENCY'");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to link the dependency to the added {@link ManagedFunction}.
	 */
	public void testLinkDependencyToAddedFunction() {
		final String FUNCTION_NAME = MANAGED_OBJECT_NAME + ".FUNCTION";

		// Record linking the managed object to the added function
		ManagingOfficeBuilder<?> managingOffice = this.configuration.setManagingOffice("OFFICE");
		managingOffice.mapFunctionDependency("DEPENDENCY", "AVAILABLE");
		managingOffice.setInputManagedObjectName("INPUT");
		OfficeConfiguration office = (OfficeConfiguration) this.officeFloorConfiguration.addOffice("OFFICE");
		MockManagedObjectSource.addFunctionName = "FUNCTION";
		MockManagedObjectSource.addFunctionLinkedDependency = "DEPENDENCY";
		this.metaData.addFlow(String.class, null);
		this.record_setupContext();

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();

		// Ensure added parameter to function
		ManagedFunctionConfiguration<?, ?> function = office.getManagedFunctionConfiguration()[0];
		assertEquals("Incorrect function", FUNCTION_NAME, function.getFunctionName());
		ManagedFunctionObjectConfiguration<?>[] dependencies = function.getObjectConfiguration();
		assertEquals("Incorrect number of dependencies", 1, dependencies.length);
		assertEquals("Ensure is dependency", "AVAILABLE", dependencies[0].getScopeManagedObjectName());
	}

	/**
	 * Ensure able to link a parameter to the added {@link ManagedFunction}.
	 */
	public void testLinkFlowToAddedFunction() {

		final String FUNCTION_NAME = MANAGED_OBJECT_NAME + ".FUNCTION";
		final String LINK_FUNCTION_NAME = MANAGED_OBJECT_NAME + ".LINK_FUNCTION";

		// Record linking a parameter to the added function
		this.configuration.setManagingOffice("OFFICE");
		OfficeConfiguration office = (OfficeConfiguration) this.officeFloorConfiguration.addOffice("OFFICE");
		MockManagedObjectSource.addFunctionName = "FUNCTION";
		MockManagedObjectSource.addFunctionLinkFunctionName = "LINK_FUNCTION";
		MockManagedObjectSource.addFunctionLinkedParameter = null;
		this.record_setupContext();

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();

		// Ensure added parameter to function
		ManagedFunctionConfiguration<?, ?> function = office.getManagedFunctionConfiguration()[0];
		assertEquals("Incorrect function", FUNCTION_NAME, function.getFunctionName());
		FlowConfiguration<?>[] flows = function.getFlowConfiguration();
		assertEquals("Incorrect number of flows", 1, flows.length);
		assertEquals("Flow linked to incorrect function", LINK_FUNCTION_NAME,
				flows[0].getInitialFunction().getFunctionName());
	}

	/**
	 * Ensure able to add a startup {@link ManagedFunction}.
	 */
	public void testAddStartupFunction() {

		// Record registering a start up function
		this.configuration.setManagingOffice("OFFICE");
		OfficeConfiguration office = (OfficeConfiguration) this.officeFloorConfiguration.addOffice("OFFICE");
		MockManagedObjectSource.startupFunctionName = "STARTUP_FUNCTION";
		this.record_setupContext();

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();

		// Ensure start up function added
		ManagedFunctionReference[] startUps = office.getStartupFunctions();
		assertEquals("Should add start up function", 1, startUps.length);
		assertEquals("Incorrect start up function", MANAGED_OBJECT_NAME + ".STARTUP_FUNCTION",
				startUps[0].getFunctionName());
	}

	/**
	 * Ensures able to configure {@link ManagedObjectPool}.
	 */
	public void testManagedObjectPool() throws Exception {

		// Record plain managed object
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");

		// Configure the pool with thread completion listener
		ManagedObjectPool managedObjectPool = this.createMock(ManagedObjectPool.class);
		ThreadCompletionListener threadCompletionListener = this.createMock(ThreadCompletionListener.class);
		this.configuration.setManagedObjectPool((context) -> managedObjectPool).addThreadCompletionListener((pool) -> {
			assertSame("Incorrect pool for thread completion listener", managedObjectPool, pool);
			return threadCompletionListener;
		});
		this.record_setupContext();

		// Attempt to construct managed object
		this.replayMockObjects();
		RawManagedObjectMetaData<?, ?> rawMetaData = this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();

		// Verify the managed object pool
		assertEquals("Incorrect managed object pool", managedObjectPool, rawMetaData.getManagedObjectPool());
		ThreadCompletionListener[] threadCompletionListeners = rawMetaData.getThreadCompletionListeners();
		assertEquals("Incorrect number of thread completion listeners", 1, threadCompletionListeners.length);
		assertEquals("Incorrect thread completion listener", threadCompletionListener, threadCompletionListeners[0]);
	}

	/**
	 * Ensures gracefully handles failure to create {@link ManagedObjectPool}.
	 */
	public void testManagedObjectPoolCreationFailure() throws Exception {

		final Throwable FAILURE = new Throwable("TEST");

		// Record plain managed object
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		this.record_setupContext();

		// Record issue
		this.issues.addIssue(AssetType.MANAGED_OBJECT, MANAGED_OBJECT_NAME, "Failed to create ManagedObjectPool",
				FAILURE);

		// Configure the pool with thread completion listener
		this.configuration.setManagedObjectPool((context) -> {
			throw FAILURE;
		});

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to handle plain {@link ManagedObject} (ie not
	 * {@link AsynchronousManagedObject} or {@link CoordinatingManagedObject}).
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testPlainManagedObject() {

		// Record plain managed object
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		this.record_setupContext();

		// Attempt to construct managed object
		this.replayMockObjects();
		RawManagedObjectMetaData rawMetaData = this.constructRawManagedObjectMetaData(true);

		// Provide the bound configuration
		final int INSTANCE_INDEX = 0;
		final String BOUND_NAME = "BOUND_NAME";
		RawBoundManagedObjectMetaDataMockBuilder<?, ?> boundMetaData = MockConstruct
				.mockRawBoundManagedObjectMetaData(BOUND_NAME, rawMetaData);
		boundMetaData.setManagedObjectIndex(ManagedObjectScope.FUNCTION, INSTANCE_INDEX);
		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> boundInstanceMetaData = boundMetaData
				.addRawBoundManagedObjectInstanceMetaData();

		// Create the managed object meta-data
		ManagedObjectMetaData<?> moMetaData = rawMetaData.createManagedObjectMetaData(AssetType.FUNCTION,
				"testFunction", boundMetaData.build(), INSTANCE_INDEX, boundInstanceMetaData.build(),
				new ManagedObjectIndex[0], new ManagedObjectGovernanceMetaData[0], this.assetManagerFactory,
				this.issues);
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		assertEquals("Incorrect managed object name", MANAGED_OBJECT_NAME, rawMetaData.getManagedObjectName());
		assertEquals("Incorrect managed object source configuration", this.configuration,
				rawMetaData.getManagedObjectSourceConfiguration());
		assertTrue("Incorrect managed object source",
				(rawMetaData.getManagedObjectSource() instanceof MockManagedObjectSource));
		assertEquals("Incorrect source meta-data", this.metaData, rawMetaData.getManagedObjectSourceMetaData());
		assertNull("Should be no managed object pool", rawMetaData.getManagedObjectPool());
		assertNull("Should be no thread completion listeners", rawMetaData.getThreadCompletionListeners());
		assertEquals("Ensure round trip managing office details", rawMetaData,
				rawMetaData.getRawManagingOfficeMetaData().getRawManagedObjectMetaData());

		// Verify managed object meta-data
		assertEquals("Incorrect bound name", BOUND_NAME, moMetaData.getBoundManagedObjectName());
		assertTrue("Incorrect managed object source",
				(moMetaData.getManagedObjectSource() instanceof MockManagedObjectSource));
		assertEquals("Incorrect object type", Object.class, moMetaData.getObjectType());
		assertEquals("Incorrect instance index", INSTANCE_INDEX, moMetaData.getInstanceIndex());
		assertEquals("Incorrect timeout", 0, moMetaData.getTimeout());
		assertFalse("Should not be context aware", moMetaData.isContextAwareManagedObject());
		assertFalse("Should not be asynchronous", moMetaData.isManagedObjectAsynchronous());
		assertFalse("Should not be coordinating", moMetaData.isCoordinatingManagedObject());

		// Verify the assets
		AssetManager[] assetManagers = this.assetManagerFactory.getAssetManagers();
		assertEquals("Should have asset manager", 1, assetManagers.length);
		assertEquals("Incorrect source asset manager", assetManagers[0], moMetaData.getSourcingManager());
		assertNull("Not asynchronous so no operations asset manager", moMetaData.getOperationsManager());
	}

	/**
	 * Ensure can load with {@link ManagedObjectSource} instantiated.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testPlainManagedObjectInstance() throws Exception {

		// Use ManagedObjectSource instance
		this.configuration = new ManagedObjectBuilderImpl(MANAGED_OBJECT_NAME, new MockManagedObjectSource());

		// Undertake test
		this.testPlainManagedObject();
	}

	/**
	 * Ensures flag name aware for {@link ContextAwareManagedObject}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testContextAwareManagedObject() {

		// Record plain managed object
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		this.metaData.setManagedObjectClass(ContextAwareManagedObject.class);
		this.record_setupContext();

		// Attempt to construct managed object
		this.replayMockObjects();
		RawManagedObjectMetaData rawMetaData = this.constructRawManagedObjectMetaData(true);

		// Provide the bound configuration
		final int INSTANCE_INDEX = 0;
		final String BOUND_NAME = "BOUND_NAME";
		RawBoundManagedObjectMetaDataMockBuilder<?, ?> boundMetaData = MockConstruct
				.mockRawBoundManagedObjectMetaData(BOUND_NAME, rawMetaData);
		boundMetaData.setManagedObjectIndex(ManagedObjectScope.FUNCTION, INSTANCE_INDEX);
		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> boundInstanceMetaData = boundMetaData
				.addRawBoundManagedObjectInstanceMetaData();

		// Create the managed object meta-data
		ManagedObjectMetaData<?> moMetaData = rawMetaData.createManagedObjectMetaData(AssetType.FUNCTION,
				"testFunction", boundMetaData.build(), INSTANCE_INDEX, boundInstanceMetaData.build(),
				new ManagedObjectIndex[0], new ManagedObjectGovernanceMetaData[0], this.assetManagerFactory,
				this.issues);
		this.verifyMockObjects();

		// Verify is context aware
		assertTrue("Should be context aware", moMetaData.isContextAwareManagedObject());
	}

	/**
	 * Ensures flag asynchronous for {@link AsynchronousManagedObject}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testAsynchronousManagedObject() {

		// Record plain managed object
		this.configuration.setManagingOffice("OFFICE");
		this.configuration.setTimeout(1000);
		this.officeFloorConfiguration.addOffice("OFFICE");
		this.metaData.setManagedObjectClass(AsynchronousManagedObject.class);
		this.record_setupContext();

		// Attempt to construct managed object
		this.replayMockObjects();
		RawManagedObjectMetaData rawMetaData = this.constructRawManagedObjectMetaData(true);

		// Provide the bound configuration
		final int INSTANCE_INDEX = 0;
		final String BOUND_NAME = "BOUND_NAME";
		RawBoundManagedObjectMetaDataMockBuilder<?, ?> boundMetaData = MockConstruct
				.mockRawBoundManagedObjectMetaData(BOUND_NAME, rawMetaData);
		boundMetaData.setManagedObjectIndex(ManagedObjectScope.FUNCTION, INSTANCE_INDEX);
		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> boundInstanceMetaData = boundMetaData
				.addRawBoundManagedObjectInstanceMetaData();

		// Create the managed object meta-data
		ManagedObjectMetaData<?> moMetaData = rawMetaData.createManagedObjectMetaData(AssetType.FUNCTION,
				"testFunction", boundMetaData.build(), INSTANCE_INDEX, boundInstanceMetaData.build(),
				new ManagedObjectIndex[0], new ManagedObjectGovernanceMetaData[0], this.assetManagerFactory,
				this.issues);
		this.verifyMockObjects();

		// Verify different index
		assertEquals("Incorrect instance index", INSTANCE_INDEX, moMetaData.getInstanceIndex());

		// Verify is asynchronous with operations manager
		Set<AssetManager> assetManagers = new HashSet<>(Arrays.asList(this.assetManagerFactory.getAssetManagers()));
		assertTrue("Should be asynchronous", moMetaData.isManagedObjectAsynchronous());
		AssetManager operationsAssetManager = moMetaData.getOperationsManager();
		assertNotNull("SHould have operations asset manager", operationsAssetManager);
		assertTrue("Incorrect operations asset manager", assetManagers.contains(operationsAssetManager));
		assertEquals("Incorrect timeout", 1000, moMetaData.getTimeout());
	}

	/**
	 * Ensures flag coordinating for {@link CoordinatingManagedObject}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testCoordinatingManagedObject() {

		// Record plain managed object
		this.configuration.setManagingOffice("OFFICE");
		this.officeFloorConfiguration.addOffice("OFFICE");
		this.metaData.setManagedObjectClass(CoordinatingManagedObject.class);
		this.record_setupContext();

		// Attempt to construct managed object
		this.replayMockObjects();
		RawManagedObjectMetaData rawMetaData = this.constructRawManagedObjectMetaData(true);

		// Provide the bound configuration
		final int INSTANCE_INDEX = 0;
		final String BOUND_NAME = "BOUND_NAME";
		RawBoundManagedObjectMetaDataMockBuilder<?, ?> boundMetaData = MockConstruct
				.mockRawBoundManagedObjectMetaData(BOUND_NAME, rawMetaData);
		boundMetaData.setManagedObjectIndex(ManagedObjectScope.FUNCTION, INSTANCE_INDEX);
		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> boundInstanceMetaData = boundMetaData
				.addRawBoundManagedObjectInstanceMetaData();

		// Create the managed object meta-data
		ManagedObjectMetaData<?> moMetaData = rawMetaData.createManagedObjectMetaData(AssetType.FUNCTION,
				"testFunction", boundMetaData.build(), INSTANCE_INDEX, boundInstanceMetaData.build(),
				new ManagedObjectIndex[0], new ManagedObjectGovernanceMetaData[0], this.assetManagerFactory,
				this.issues);
		this.verifyMockObjects();

		// Verify flagged as coordinating
		assertTrue("Should be coordinating", moMetaData.isCoordinatingManagedObject());
	}

	/**
	 * Records an issue for the {@link ManagedObject}.
	 * 
	 * @param issueDescription Issue description.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(AssetType.MANAGED_OBJECT, MANAGED_OBJECT_NAME, issueDescription);
	}

	/**
	 * Records an issue for the {@link ManagedObject}.
	 * 
	 * @param issueDescription Issue description.
	 * @param cause            Cause.
	 */
	private void record_issue(String issueDescription, Throwable cause) {
		this.issues.addIssue(AssetType.MANAGED_OBJECT, MANAGED_OBJECT_NAME, issueDescription, cause);
	}

	/**
	 * {@link Flow} keys.
	 */
	private static enum FlowKey {
		KEY
	}

	/**
	 * Mock {@link ManagedObjectSource}.
	 */
	@TestSource
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class MockManagedObjectSource implements ManagedObjectSource {

		/**
		 * Instantiate exception.
		 */
		public static Exception instantiateFailure = null;

		/**
		 * Name of required property.
		 */
		public static String requiredPropertyName = null;

		/**
		 * Name of required {@link Class}.
		 */
		public static String requiredClassName = null;

		/**
		 * Location of required resource.
		 */
		public static String requiredResourceLocation = null;

		/**
		 * {@link ClassLoader}.
		 */
		public static ClassLoader classLoader = null;

		/**
		 * Recycle {@link ManagedFunctionFactory}.
		 */
		public static ManagedFunctionFactory<Indexed, None> recycleFactory = null;

		/**
		 * Name to add {@link ManagedFunction}.
		 */
		public static String addFunctionName = null;

		/**
		 * {@link ManagedFunctionFactory}.
		 */
		public static ManagedFunctionFactory<None, Indexed> functionFactory = null;

		/**
		 * Parameter type for a linked parameter to the {@link ManagedFunction}.
		 */
		public static Class<?> addFunctionLinkedParameter = null;

		/**
		 * Indicates for the {@link ManagedFunction} to link the {@link ManagedObject}.
		 */
		public static boolean isFunctionLinkManagedObject = false;

		/**
		 * Indicates for {@link ManagedObjectFunctionDependency} to link to
		 * {@link ManagedObject}.
		 */
		public static String addFunctionLinkedDependency = null;

		/**
		 * Name of {@link Flow} to link to the added {@link ManagedFunction}.
		 */
		public static String addFunctionLinkFunctionName = null;

		/**
		 * Init exception.
		 */
		public static Exception initFailure = null;

		/**
		 * Name of startup {@link ManagedFunction}.
		 */
		public static String startupFunctionName = null;

		/**
		 * {@link ManagedObjectMetaData}.
		 */
		public static ManagedObjectSourceMetaData<Indexed, FlowKey> metaData;

		/**
		 * {@link Logger} name.
		 */
		public static String loggerName = null;

		/**
		 * Profiles.
		 */
		public static List<String> profiles = null;

		/**
		 * Resets state of {@link MockManagedObjectSource} for testing.
		 * 
		 * @param taskFactory {@link ManagedFunctionFactory}.
		 * @param metaData    {@link ManagedObjectSourceMetaData}.
		 */
		public static void reset(ManagedFunctionFactory<None, Indexed> functionFactory,
				ManagedObjectSourceMetaData<Indexed, FlowKey> metaData) {
			instantiateFailure = null;
			requiredPropertyName = null;
			requiredClassName = null;
			requiredResourceLocation = null;
			classLoader = null;
			recycleFactory = null;
			addFunctionName = null;
			addFunctionLinkFunctionName = null;
			addFunctionLinkedParameter = null;
			isFunctionLinkManagedObject = false;
			addFunctionLinkedDependency = null;
			MockManagedObjectSource.functionFactory = functionFactory;
			initFailure = null;
			startupFunctionName = null;
			MockManagedObjectSource.metaData = metaData;
			loggerName = null;
			profiles = null;
		}

		/**
		 * Instantiate.
		 * 
		 * @throws Exception Possible instantiate failure.
		 */
		public MockManagedObjectSource() throws Exception {
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ==================== ManagedObjectSource ====================
		 */

		@Override
		public ManagedObjectSourceSpecification getSpecification() {
			fail("Should not call getSpecification");
			return null;
		}

		@Override
		public ManagedObjectSourceMetaData init(ManagedObjectSourceContext context) throws Exception {

			// Capture the profiles
			profiles = context.getProfiles();

			// Capture the logger name
			loggerName = context.getLogger().getName();

			// Obtain the required property
			if (requiredPropertyName != null) {
				context.getProperty(requiredPropertyName);
			}

			// Obtain class loader
			if (classLoader != null) {
				assertSame("Incorrect class loader", classLoader, context.getClassLoader());
			}

			// Load the required class
			if (requiredClassName != null) {
				context.loadClass(requiredClassName);
			}

			// Obtain the required resource
			if (requiredResourceLocation != null) {
				context.getResource(requiredResourceLocation);
			}

			// Ensure can obtain defaulted property
			assertEquals("Must default property", "DEFAULT", context.getProperty("property to default", "DEFAULT"));

			// Register the recycle function
			if (recycleFactory != null) {
				// Add work and task that should have name spaced names
				context.getRecycleFunction(recycleFactory);
			}

			// Add a function
			if (addFunctionName != null) {
				// Add work and task that should have name spaced names
				ManagedObjectFunctionBuilder<?, ?> taskBuilder = context.addManagedFunction(addFunctionName,
						functionFactory);

				// Link in the parameter
				if (addFunctionLinkedParameter != null) {
					taskBuilder.linkParameter(0, addFunctionLinkedParameter);
				}

				// Link in the managed object
				if (isFunctionLinkManagedObject) {
					taskBuilder.linkManagedObject(0);
				}

				// Link in the function dependency
				if (addFunctionLinkedDependency != null) {
					ManagedObjectFunctionDependency dependency = context
							.addFunctionDependency(addFunctionLinkedDependency, String.class);
					taskBuilder.linkObject(0, dependency);
				}

				// Link in the flow
				if (addFunctionLinkFunctionName != null) {
					taskBuilder.linkFlow(0, addFunctionLinkFunctionName, Object.class, false);
				}
			}

			// Register the startup function
			if (startupFunctionName != null) {
				context.addStartupFunction(startupFunctionName);
			}

			// Determine if failure in initialising
			if (initFailure != null) {
				throw initFailure;
			}

			// Return the meta-data
			return metaData;
		}

		@Override
		public void start(ManagedObjectExecuteContext context) throws Exception {
			fail("Should not call start");
		}

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			fail("Should not call sourceManagedObject");
		}

		@Override
		public void stop() {
			fail("Should not call stop");
		}
	}

	/**
	 * Constructs the {@link RawManagedObjectMetaData} with the mock objects.
	 * 
	 * @return {@link RawManagedObjectMetaData}.
	 */
	@SuppressWarnings("rawtypes")
	private RawManagedObjectMetaData constructRawManagedObjectMetaData(boolean isExpectConstruction) {

		// Attempt to construct
		RawManagedObjectMetaData metaData = new RawManagedObjectMetaDataFactory(this.sourceContext,
				this.officeFloorConfiguration).constructRawManagedObjectMetaData(this.configuration, "OFFICE_FLOOR",
						this.issues);

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
