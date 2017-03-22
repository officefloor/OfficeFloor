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
package net.officefloor.plugin.administrator.clazz;

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.test.administration.AdministrationLoaderUtil;
import net.officefloor.compile.test.administration.AdministrationTypeBuilder;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;

/**
 * Tests the {@link ClassAdministrationSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassAdministratorSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensures specification context.
	 */
	public void testSpecification() {
		AdministrationLoaderUtil.validateSpecification(ClassAdministrationSource.class,
				ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME, "Class");
	}

	/**
	 * Ensures {@link AdministrationType} is correct.
	 */
	public void testAdministratorType() {

		// Create the expected administration type
		AdministrationTypeBuilder<Indexed, Indexed> type = AdministrationLoaderUtil
				.createAdministrationTypeBuilder(MockExtensionInterface.class, Indexed.class, Indexed.class);
		type.addFlow("flowOne", null, 0, null);
		type.addFlow("flowTwo", String.class, 1, null);
		type.addFlow("flowThree", null, 2, null);
		type.addFlow("flowFour", Integer.class, 3, null);
		type.addEscalation(IOException.class.getSimpleName(), IOException.class);
		type.addEscalation(SQLException.class.getSimpleName(), SQLException.class);

		// Validate the administration type
		AdministrationLoaderUtil.validateAdministratorType(type, ClassAdministrationSource.class,
				ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME, MockClass.class.getName());
	}

	/**
	 * Ensure invoke {@link Administration}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testInvokeAdministration() throws Throwable {

		// Load the type
		AdministrationType<?, ?, ?> type = AdministrationLoaderUtil.loadAdministrationType(
				ClassAdministrationSource.class, ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());

		// Create the administration
		Administration<?, ?, ?> administration = type.getAdministrationFactory().createAdministration();

		// Create the mocks
		AdministrationContext context = this.createMock(AdministrationContext.class);
		MockExtensionInterface extension = this.createMock(MockExtensionInterface.class);
		MockExtensionInterface[] extensions = new MockExtensionInterface[] { extension };

		// Record invoking administration
		this.recordReturn(context, context.getExtensions(), extensions);
		extension.administer();

		this.replayMockObjects();

		// Invoke the administration
		administration.administer(context);

		// Verify functionality (extension interface invoked)
		this.verifyMockObjects();
	}

	/**
	 * Ensure unit tests can invoke {@link Flow} from {@link Administration} in
	 * testing.
	 */
	public void testInvokeFlowFromAdministration() {
		fail("TODO implement invoking a flow from administration when writing unit tests");
	}

	/**
	 * Mock {@link Administration} class.
	 */
	public static class MockClass {

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
		}
	}

	/**
	 * Mock extension interface.
	 */
	public static interface MockExtensionInterface {

		void administer();
	}

}