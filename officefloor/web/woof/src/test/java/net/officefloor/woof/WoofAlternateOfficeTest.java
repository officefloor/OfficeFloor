/*-
 * #%L
 * Web on OfficeFloor
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

package net.officefloor.woof;

import java.io.IOException;
import java.util.Properties;

import net.officefloor.frame.api.manage.Office;

/**
 * Ensure can load alternate {@link Office} handling for {@link WoOF}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofAlternateOfficeTest extends AbstractTestCase {

	@Override
	protected void setUp() throws Exception {
		SecondOfficeSetup.isConfigureSecond = true;
	}

	@Override
	protected void tearDown() throws Exception {
		SecondOfficeSetup.isConfigureSecond = false;
	}

	/**
	 * Ensure can register second {@link Office}.
	 */
	public void testSecondOffice() throws IOException {
		this.doRequestTest("/second", "SECOND TEMPLATE");
	}

	/**
	 * Ensure can register objects for second {@link Office}.
	 */
	public void testSecondOfficeObjects() throws IOException {
		this.doRequestTest("/second-objects", "\"second-objects\"");
	}

	/**
	 * Ensure can register teams for second {@link Office}.
	 */
	public void testSecondOfficeTeams() throws IOException {
		this.doRequestTest("/second-teams", "\"DIFFERENT SECOND THREAD\"");
	}

	/**
	 * Ensure can register teams for second {@link Office}.
	 */
	public void testSecondOfficeProcedure() throws IOException {
		this.doRequestTest("/second-procedure", "\"PROCEDURE\"");
	}

	/**
	 * Ensure can register {@link Properties} for second {@link Office}.
	 */
	public void testSecondOfficeProperties() throws IOException {
		this.doRequestTest("/second-property", "SECOND OVERRIDE");
	}

	/**
	 * Ensure can register {@link Properties} for second {@link Office}.
	 */
	public void testSecondSystemProperties() throws IOException {
		this.doSystemPropertiesTest("/second-property", "SYSTEM SECOND OVERRIDE", "second.Property.function.override",
				"SYSTEM SECOND OVERRIDE");
	}

}
