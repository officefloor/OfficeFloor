/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.governance.clazz;

import java.sql.SQLException;

import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.test.governance.GovernanceLoaderUtil;
import net.officefloor.compile.test.governance.GovernanceTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceContext;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ClassGovernanceSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassGovernanceSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensures specification context.
	 */
	public void testSpecification() {
		GovernanceLoaderUtil.validateSpecification(ClassGovernanceSource.class,
				ClassGovernanceSource.CLASS_NAME_PROPERTY_NAME, "Class");
	}

	/**
	 * Ensures {@link GovernanceType} is correct.
	 */
	public void testGovernanceType() {

		// Create the expected type
		GovernanceTypeBuilder<?> type = GovernanceLoaderUtil
				.createGovernanceTypeBuilder();
		type.setExtensionInterface(MockExtensionInterface.class);
		type.addEscalation(RuntimeException.class);
		type.addEscalation(SQLException.class);

		// Validate correct type
		GovernanceLoaderUtil.validateGovernanceType(type,
				ClassGovernanceSource.class,
				ClassGovernanceSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
	}

	/**
	 * Validate the {@link GovernanceActivity}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testGovernanceActivities() throws Throwable {

		final MockExtensionInterface extension = this
				.createMock(MockExtensionInterface.class);
		final GovernanceContext<Indexed> context = this
				.createMock(GovernanceContext.class);

		// Reset for testing
		MockClass.reset();

		// Test
		this.replayMockObjects();

		// Load the governance type
		GovernanceType<Object, Indexed> type = GovernanceLoaderUtil
				.loadGovernanceType(ClassGovernanceSource.class,
						ClassGovernanceSource.CLASS_NAME_PROPERTY_NAME,
						MockClass.class.getName());

		// Create the governance
		Governance governance = type.getGovernanceFactory().createGovernance();

		// Ensure can govern managed object
		governance.governManagedObject(extension, context);
		assertEquals("Incorrect extension", extension, MockClass.extension);

		// Ensure can enforce governance
		governance.enforceGovernance(context);
		assertTrue("Should enforce governance", MockClass.isEnforced);

		// Ensure can disregard governance
		governance.disregardGovernance(context);
		assertTrue("Should disregard governance", MockClass.isDisregard);
	}

	/**
	 * Mock {@link Governance} class.
	 */
	public static class MockClass {

		/**
		 * {@link MockExtensionInterface}.
		 */
		public static MockExtensionInterface extension;

		/**
		 * Indicates if triggered to enforce {@link Governance}.
		 */
		public static boolean isEnforced = false;

		/**
		 * Indicates if triggered to disregard {@link Governance}.
		 */
		public static boolean isDisregard = false;

		/**
		 * Rests for the next test.
		 */
		public static void reset() {
			extension = null;
			isEnforced = false;
			isDisregard = false;
		}

		@Govern
		public void govern(MockExtensionInterface extension)
				throws RuntimeException {
			MockClass.extension = extension;
		}

		@Enforce
		public void enforce() throws SQLException {
			isEnforced = true;
		}

		@Disregard
		public void disregard() throws RuntimeException {
			isDisregard = true;
		}
	}

	/**
	 * Mock extension interface.
	 */
	public static interface MockExtensionInterface {

		void govern(String methodName);
	}

}
