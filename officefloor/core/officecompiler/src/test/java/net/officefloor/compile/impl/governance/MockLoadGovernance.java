/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.governance;

import java.sql.SQLException;

import junit.framework.TestCase;
import net.officefloor.compile.governance.GovernanceEscalationType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;

/**
 * Class for {@link ClassGovernanceSource} that enables validating loading a
 * {@link GovernanceType}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockLoadGovernance {

	/**
	 * Mock extension interface.
	 */
	public static class MockExtensionInterface {
	}

	/**
	 * Govern method.
	 * 
	 * @param interfaces
	 *            Extension interfaces.
	 */
	@Govern
	public void govern(MockExtensionInterface extensionInterface) {
	}

	/**
	 * Enforce method.
	 */
	@Enforce
	public void enforce() throws SQLException {
	}

	/**
	 * Validates the {@link GovernanceType} is correct for this class object.
	 * 
	 * @param governanceType
	 *            {@link GovernanceType}
	 */
	public static void assertGovernanceType(GovernanceType<?, ?> governanceType) {

		TestCase.assertEquals("Incorrect extension interface",
				MockExtensionInterface.class,
				governanceType.getExtensionType());

		TestCase.assertEquals("Incorrect number of escalations", 1,
				governanceType.getEscalationTypes().length);
		GovernanceEscalationType escalationType = governanceType
				.getEscalationTypes()[0];
		TestCase.assertEquals("Incorrect escalation type", SQLException.class,
				escalationType.getEscalationType());
	}

}