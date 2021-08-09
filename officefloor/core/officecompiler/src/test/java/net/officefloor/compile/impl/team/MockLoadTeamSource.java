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
