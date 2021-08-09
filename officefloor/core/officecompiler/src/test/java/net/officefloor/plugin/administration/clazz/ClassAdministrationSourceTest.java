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

package net.officefloor.plugin.administration.clazz;

import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Function;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.test.administration.AdministrationLoaderUtil;
import net.officefloor.compile.test.administration.AdministrationTypeBuilder;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.GovernanceManager;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Tests the {@link ClassAdministrationSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassAdministrationSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensures specification context.
	 */
	public void testSpecification() {
		AdministrationLoaderUtil.validateSpecification(ClassAdministrationSource.class,
				ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME, "Class");
	}

	/**
	 * Ensure {@link AdministrationType} is correct.
	 */
	public void testAdministrationWithoutFlowsType() {

		// Create the expected administration type
		AdministrationTypeBuilder<Indexed, Indexed> type = AdministrationLoaderUtil
				.createAdministrationTypeBuilder(MockExtensionInterface.class, null, null);

		// Validate the administration type
		AdministrationLoaderUtil.validateAdministratorType(type, ClassAdministrationSource.class,
				ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME, MockClassWithoutFlows.class.getName());
	}

	/**
	 * Ensures {@link AdministrationType} is correct.
	 */
	public void testAdministrationWithFlowsType() {

		// Create the expected administration type
		AdministrationTypeBuilder<Indexed, Indexed> type = AdministrationLoaderUtil
				.createAdministrationTypeBuilder(MockExtensionInterface.class, Indexed.class, null);
		type.addFlow("flowFour", Integer.class, 0, null);
		type.addFlow("flowOne", null, 1, null);
		type.addFlow("flowThree", null, 2, null);
		type.addFlow("flowTwo", String.class, 3, null);
		type.addEscalation(IOException.class.getName(), IOException.class);
		type.addEscalation(SQLException.class.getName(), SQLException.class);

		// Validate the administration type
		AdministrationLoaderUtil.validateAdministratorType(type, ClassAdministrationSource.class,
				ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME, MockClassWithFlows.class.getName());
	}

	/**
	 * Ensure {@link AdministrationType} is correct.
	 */
	public void testAdministrationWithGovernance() {

		// Create the expected administration type
		AdministrationTypeBuilder<Indexed, Indexed> type = AdministrationLoaderUtil
				.createAdministrationTypeBuilder(MockExtensionInterface.class, null, Indexed.class);
		type.addGovernance("0", 0, null);

		// Validate the administration type
		AdministrationLoaderUtil.validateAdministratorType(type, ClassAdministrationSource.class,
				ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME, MockClassWithGovernance.class.getName());
	}

	/**
	 * Ensure {@link AdministrationType} is correct.
	 */
	public void testAdministrationWithContext() {

		// Create the expected administration type
		AdministrationTypeBuilder<Indexed, Indexed> type = AdministrationLoaderUtil
				.createAdministrationTypeBuilder(MockExtensionInterface.class, null, null);

		// Validate the administration type
		AdministrationLoaderUtil.validateAdministratorType(type, ClassAdministrationSource.class,
				ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME, MockClassWithContext.class.getName());
	}

	/**
	 * Ensure invoke {@link Administration}.
	 */
	public void testInvokeAdministration() throws Throwable {
		this.doInvokeAdministration(MockClassWithoutFlows.class, (context, extensions) -> {
			this.recordReturn(context, context.getExtensions(), extensions);
			extensions[0].administer();
		});
	}

	/**
	 * Ensure invoke {@link Flow}.
	 */
	public void testInvokeFlow() throws Throwable {
		MockClassWithFlows.callback = this.callback;
		this.doInvokeAdministration(MockClassWithFlows.class, (context, extensions) -> {
			this.recordReturn(context, context.getExtensions(), extensions);
			extensions[0].administer();
			context.doFlow(1, null, null);
			context.doFlow(3, "TWO", null);
			context.doFlow(2, null, this.callback);
			context.doFlow(0, 4, this.callback);
		});
	}

	/**
	 * Ensure invoke {@link GovernanceManager}.
	 */
	public void testInvokeGovernance() throws Throwable {
		this.doInvokeAdministration(MockClassWithGovernance.class, (context, extensions) -> {
			this.recordReturn(context, context.getExtensions(), extensions);
			GovernanceManager governance = this.createMock(GovernanceManager.class);
			this.recordReturn(context, context.getGovernance(0), governance);
			governance.enforceGovernance();
		});
	}

	/**
	 * Ensure invoke {@link AdministrationContext}.
	 */
	public void testInvokeContext() throws Throwable {
		this.doInvokeAdministration(MockClassWithContext.class, (context, extensions) -> {
			this.recordReturn(context, context.getExtensions(), extensions);
			context.doFlow(0, "TEST", null);
		});
	}

	/**
	 * {@link Function} to record interaction.
	 */
	private static interface RecordFunctionality {

		/**
		 * Records interaction.
		 * 
		 * @param context
		 *            {@link AdministrationContext}.
		 * @param extensions
		 *            {@link MockExtensionInterface} array.
		 */
		void record(AdministrationContext<?, ?, ?> context, MockExtensionInterface[] extensions);
	}

	/**
	 * Undertakes invoking the {@link Administration}.
	 * 
	 * @param recorder
	 *            {@link RecordFunctionality}.
	 * @throws Throwable
	 *             If fails.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void doInvokeAdministration(Class<?> clazz, RecordFunctionality recorder) throws Throwable {

		// Load the type
		AdministrationType<?, ?, ?> type = AdministrationLoaderUtil.loadAdministrationType(
				ClassAdministrationSource.class, ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME, clazz.getName());

		// Create the administration
		Administration<?, ?, ?> administration = type.getAdministrationFactory().createAdministration();

		// Create the mocks
		AdministrationContext context = this.createMock(AdministrationContext.class);
		MockExtensionInterface extension = this.createMock(MockExtensionInterface.class);
		MockExtensionInterface[] extensions = new MockExtensionInterface[] { extension };

		// Record invoking administration
		recorder.record(context, extensions);

		this.replayMockObjects();

		// Invoke the administration
		administration.administer(context);

		// Verify functionality (extension interface invoked)
		this.verifyMockObjects();
	}

	/**
	 * Mock {@link FlowCallback}.
	 */
	private final FlowCallback callback = this.createMock(FlowCallback.class);

	/**
	 * Mock {@link Administration} class.
	 */
	public static class MockClassWithoutFlows {

		public void admin(MockExtensionInterface[] extensions) {
			for (MockExtensionInterface ei : extensions) {
				ei.administer();
			}
		}
	}

	/**
	 * Mock {@link Administration} class.
	 */
	public static class MockClassWithFlows {

		private static FlowCallback callback;

		@FlowInterface
		public static interface MockFlows {

			void flowOne();

			void flowTwo(String parameter);

			void flowThree(FlowCallback callback);

			void flowFour(Integer parameter, FlowCallback callback);
		}

		public void admin(MockExtensionInterface[] extensions, MockFlows flows) throws IOException, SQLException {
			for (MockExtensionInterface ei : extensions) {
				ei.administer();
			}
			flows.flowOne();
			flows.flowTwo("TWO");
			flows.flowThree(callback);
			flows.flowFour(4, callback);
		}
	}

	/**
	 * Mock {@link Administration} class.
	 */
	public static class MockClassWithGovernance {

		public void admin(MockExtensionInterface[] extensions, GovernanceManager governance) {
			governance.enforceGovernance();
		}
	}

	/**
	 * Mock {@link Administration} class.
	 */
	public static class MockClassWithContext {

		public void admin(AdministrationContext<MockExtensionInterface, ?, ?> context,
				MockExtensionInterface[] extensions) {
			context.doFlow(0, "TEST", null);
		}
	}

	/**
	 * Mock extension interface.
	 */
	public static interface MockExtensionInterface {

		void administer();
	}

}
