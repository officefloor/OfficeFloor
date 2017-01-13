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
package net.officefloor.compile.impl.team;

import org.junit.Assert;

import net.officefloor.compile.officefloor.OfficeFloorTeamSourcePropertyType;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;

/**
 * Mock {@link TeamSource} for testing loading.
 *
 * @author Daniel Sagenschneider
 */
@TestSource
public class MockLoadTeamSource extends AbstractTeamSource {

	/**
	 * Mock {@link Property}.
	 */
	public static final String MOCK_PROPERTY = "mock.property";

	/**
	 * Validates the {@link OfficeFloorTeamSourceType}.
	 * 
	 * @param teamType
	 *            {@link OfficeFloorTeamSourceType} to validate.
	 * @param teamSourceName
	 *            Name of the {@link TeamSource}.
	 * @param mockPropertyValue
	 *            {@link Property} value for the {@link #MOCK_PROPERTY}.
	 */
	public static void assertOfficeFloorTeamSourceType(
			OfficeFloorTeamSourceType teamType, String teamSourceName,
			String mockPropertyValue) {
		Assert.assertEquals("Incorrect TeamSource name", teamSourceName,
				teamType.getOfficeFloorTeamSourceName());
		OfficeFloorTeamSourcePropertyType[] properties = teamType
				.getOfficeFloorTeamSourcePropertyTypes();
		Assert.assertNotNull("Should have properties", properties);
		Assert.assertEquals("Incorrect number of properties", 1,
				properties.length);
		OfficeFloorTeamSourcePropertyType property = properties[0];
		Assert.assertEquals("Incorrect property name", MOCK_PROPERTY,
				property.getName());
		Assert.assertEquals("Incorrect property label", "Mock Property",
				property.getLabel());
		Assert.assertEquals("Incorrect property default value",
				mockPropertyValue, property.getDefaultValue());
	}

	/*
	 * ====================== TeamSource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(MOCK_PROPERTY, "Mock Property");
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {
		Assert.fail("Should not require to create team for loading types");
		return null;
	}

}