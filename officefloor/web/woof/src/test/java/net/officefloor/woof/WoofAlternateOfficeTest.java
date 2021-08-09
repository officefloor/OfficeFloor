/*-
 * #%L
 * Web on OfficeFloor
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
