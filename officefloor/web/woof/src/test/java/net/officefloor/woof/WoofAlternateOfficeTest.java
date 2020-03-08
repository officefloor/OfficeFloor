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