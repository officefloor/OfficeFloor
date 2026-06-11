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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Ensure can load alternate {@link Office} handling for {@link WoOF}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofAlternateOfficeTest extends AbstractModelTestCase {

	@BeforeEach
	public void setUp() {
		SecondOfficeSetup.isConfigureSecond = true;
	}

	@AfterEach
	public void clear() {
		SecondOfficeSetup.isConfigureSecond = false;
	}

	/**
	 * Ensure can register second {@link Office}.
	 */
	@Test
	public void secondOffice() throws IOException {
		this.doRequestTest("/second", "SECOND TEMPLATE");
	}

	/**
	 * Ensure can register objects for second {@link Office}.
	 */
	@Test
	public void secondOfficeObjects() throws IOException {
		this.doRequestTest("/second-objects", "\"second-objects\"");
	}

	/**
	 * Ensure can register teams for second {@link Office}.
	 */
	@Test
	public void secondOfficeTeams() throws IOException {
		this.doRequestTest("/second-teams", "\"DIFFERENT SECOND THREAD\"");
	}

	/**
	 * Ensure can register teams for second {@link Office}.
	 */
	@Test
	public void secondOfficeProcedure() throws IOException {
		this.doRequestTest("/second-procedure", "\"PROCEDURE\"");
	}

	/**
	 * Ensure can register {@link Properties} for second {@link Office}.
	 */
	@Test
	public void secondOfficeProperties() throws IOException {
		this.doRequestTest("/second-property", "SECOND OVERRIDE");
	}

	/**
	 * Ensure can register {@link Properties} for second {@link Office}.
	 */
	@Test
	public void secondSystemProperties() throws IOException {
		this.doSystemPropertiesTest("/second-property", "SYSTEM SECOND OVERRIDE",
				"second.Property.function.override", "SYSTEM SECOND OVERRIDE");
	}

}
