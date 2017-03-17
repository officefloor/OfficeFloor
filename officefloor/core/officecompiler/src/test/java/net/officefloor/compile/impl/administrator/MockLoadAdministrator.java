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
package net.officefloor.compile.impl.administrator;

import junit.framework.TestCase;
import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.administration.DutyType;
import net.officefloor.plugin.administrator.clazz.ClassAdministrationSource;

/**
 * Class for {@link ClassAdministrationSource} that enables validating loading a
 * {@link AdministrationType}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockLoadAdministrator {

	/**
	 * Mock extension interface.
	 */
	public static class MockExtensionInterface {
	}

	/**
	 * Administration method.
	 * 
	 * @param interfaces
	 *            Extension interfaces.
	 */
	public void admin(MockExtensionInterface[] interfaces) {
	}

	/**
	 * Validates the {@link AdministrationType} is correct for this class object.
	 * 
	 * @param administratorType
	 *            {@link AdministrationType}
	 */
	public static void assertAdministratorType(
			AdministrationType<?, ?> administratorType) {

		TestCase.assertEquals("Incorrect extension interface",
				MockExtensionInterface.class, administratorType
						.getExtensionInterface());

		TestCase.assertEquals("Incorrect number of duties", 1,
				administratorType.getDutyTypes().length);
		DutyType<?, ?> dutyType = administratorType.getDutyTypes()[0];
		TestCase.assertEquals("Incorrect duty name", "admin", dutyType
				.getDutyName());
	}

}