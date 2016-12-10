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

import java.util.Arrays;
import java.util.List;

import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.test.administrator.AdministratorLoaderUtil;
import net.officefloor.compile.test.administrator.AdministratorTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.DutyContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.AdministratorSourceStandAlone;

/**
 * Tests the {@link ClassAdministratorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassAdministratorSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensures specification context.
	 */
	public void testSpecification() {
		AdministratorLoaderUtil.validateSpecification(
				ClassAdministratorSource.class,
				ClassAdministratorSource.CLASS_NAME_PROPERTY_NAME, "Class");
	}

	/**
	 * Ensures {@link AdministratorType} is correct.
	 */
	public void testAdministratorType() {

		// Create the expected administration type
		AdministratorTypeBuilder type = AdministratorLoaderUtil
				.createAdministratorTypeBuilder();
		type.setExtensionInterface(MockExtensionInterface.class);
		type.addDuty("admin_A", null, None.class);
		type.addDuty("admin_B", null, None.class);
		type.addDuty("admin_C", null, None.class);

		// Validate the administration type
		AdministratorLoaderUtil.validateAdministratorType(type,
				ClassAdministratorSource.class,
				ClassAdministratorSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
	}

	/**
	 * Ensures able to invoke administration where array type is exact type of
	 * extension interface.
	 */
	public void testInvokeExactTypeAdministration() throws Throwable {
		this.doInvokeAdministrationTest(0, "admin_A");
	}

	/**
	 * Ensures able to invoke administration where input array type is super
	 * type of extension interface.
	 */
	public void testInvokeSuperTypeAdministration() throws Throwable {
		this.doInvokeAdministrationTest(2, "admin_C");
	}

	/**
	 * Does the invoking of administration testing.
	 * 
	 * @param dutyIndex
	 *            Index of the {@link Duty} to invoke.
	 * @param methodName
	 *            Name of administration method.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void doInvokeAdministrationTest(int dutyIndex, String methodName)
			throws Throwable {

		final DutyContext<Object, ?, ?> dutyContext = this
				.createMock(DutyContext.class);
		final MockExtensionInterface extensionInterface = this
				.createMock(MockExtensionInterface.class);
		final List<MockExtensionInterface> interfaces = Arrays
				.asList(extensionInterface);

		// Record invoking duty
		this.recordReturn(dutyContext, dutyContext.getExtensionInterfaces(),
				interfaces);
		extensionInterface.administer(methodName);

		this.replayMockObjects();

		// Load the class administrator source
		AdministratorSourceStandAlone standAlone = new AdministratorSourceStandAlone();
		standAlone.addProperty(
				ClassAdministratorSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
		ClassAdministratorSource adminSource = standAlone
				.loadAdministratorSource(ClassAdministratorSource.class);

		// Obtain the duty to invoke
		Administrator<Object, Indexed> admin = adminSource
				.createAdministrator();
		Duty duty = standAlone.getDuty(admin, dutyIndex);

		// Invoke the duty
		duty.doDuty(dutyContext);

		// Verify functionality (extension interface invoked for method)
		this.verifyMockObjects();
	}

	/**
	 * Mock {@link Administrator} class.
	 */
	public static class MockClass {

		public void admin_A(MockExtensionInterface[] extensions) {
			this.administer(extensions, "admin_A");
		}

		public void admin_B(MockExtensionInterface[] extensions) {
			this.administer(extensions, "admin_B");
		}

		public void admin_C(Object[] extensions) {
			this.administer((MockExtensionInterface[]) extensions, "admin_C");
		}

		public void notAdmin(MockExtensionInterface singleExtension) {
			fail("Should not be invoked");
		}

		private void administer(MockExtensionInterface[] extensions,
				String methodName) {
			for (MockExtensionInterface ei : extensions) {
				ei.administer(methodName);
			}
		}
	}

	/**
	 * Mock extension interface.
	 */
	public static interface MockExtensionInterface {

		void administer(String methodName);
	}

}