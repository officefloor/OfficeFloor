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
package net.officefloor.compile.impl.administration;

import org.junit.Assert;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.plugin.administrator.clazz.ClassAdministrationSource;

/**
 * Class for {@link ClassAdministrationSource} that enables validating loading a
 * {@link AdministrationType}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockLoadAdministration {

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
	 * Validates the {@link AdministrationType} is correct for this class
	 * object.
	 * 
	 * @param administrationType
	 *            {@link AdministrationType}
	 */
	public static void assertAdministrationType(AdministrationType<?, ?, ?> administrationType) {
		Assert.assertEquals("Incorrect extension interface", MockExtensionInterface.class,
				administrationType.getExtensionInterface());
	}

}