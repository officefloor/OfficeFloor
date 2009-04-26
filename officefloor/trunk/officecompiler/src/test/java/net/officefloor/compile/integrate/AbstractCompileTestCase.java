/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.integrate;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.test.issues.StderrCompilerIssuesWrapper;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.TypeMatcher;
import net.officefloor.model.impl.repository.xml.XmlConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * Provides abstract functionality for testing integration of the
 * {@link OfficeFloorCompiler}.
 * 
 * @author Daniel
 */
public abstract class AbstractCompileTestCase extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	protected final CompilerIssues issues = this
			.createMock(CompilerIssues.class);

	/**
	 * Enhances {@link CompilerIssues}.
	 */
	private final CompilerIssues enhancedIssues = this
			.enhanceIssues(this.issues);

	/**
	 * {@link OfficeFloorBuilder}.
	 */
	protected final OfficeFloorBuilder officeFloorBuilder = this
			.createMock(OfficeFloorBuilder.class);

	/**
	 * <p>
	 * Allow enhancing the {@link CompilerIssues}. For example allows wrapping
	 * with a {@link StderrCompilerIssuesWrapper}.
	 * <p>
	 * This is available for {@link TestCase} instances to override.
	 * 
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return By default returns input {@link CompilerIssues}.
	 */
	protected CompilerIssues enhanceIssues(CompilerIssues issues) {
		return this.issues;
	}

	/**
	 * Records adding a {@link Team} to the {@link OfficeFloorBuilder}.
	 * 
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @param teamSourceClass
	 *            {@link TeamSource} class.
	 * @param propertyNameValues
	 *            {@link Property} name/value listing.
	 * @return {@link TeamBuilder} for the added {@link Team}.
	 */
	@SuppressWarnings("unchecked")
	protected <S extends TeamSource> TeamBuilder<S> record_officefloor_addTeam(
			String teamName, Class<S> teamSourceClass,
			String... propertyNameValues) {
		TeamBuilder<S> builder = this.createMock(TeamBuilder.class);
		this.recordReturn(this.officeFloorBuilder, this.officeFloorBuilder
				.addTeam(teamName, teamSourceClass), builder);
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			builder.addProperty(name, value);
		}
		return builder;
	}

	/**
	 * Compiles the {@link OfficeFloor} verifying correctly built into the
	 * {@link OfficeFloorBuilder}.
	 * 
	 * @param isExpectBuild
	 *            If the {@link OfficeFloor} is expected to be built.
	 * @param propertyNameValues
	 *            {@link Property} name/value pair listing for the
	 *            {@link OfficeFloorCompiler}.
	 */
	protected void compile(boolean isExpectBuild, String... propertyNameValues) {

		// Office floor potentially built
		OfficeFloor officeFloor = null;

		// Record building if expected to build office floor
		if (isExpectBuild) {
			// Create the mock office floor built
			officeFloor = this.createMock(OfficeFloor.class);

			// Record successfully building the office floor
			this.recordReturn(this.officeFloorBuilder, this.officeFloorBuilder
					.buildOfficeFloor(null), officeFloor, new TypeMatcher(
					OfficeFloorIssues.class));
		}

		// Replay the mocks
		this.replayMockObjects();

		// Move the 'Test' to start of test case name
		String testCaseName = this.getClass().getSimpleName();
		testCaseName = "Test"
				+ testCaseName.substring(0, (testCaseName.length() - "Test"
						.length()));

		// Remove the 'test' from the start of the test name
		String testName = this.getName();
		testName = testName.substring("test".length());

		// Create the configuration context
		String configFileName = testCaseName + "/" + testName + ".xml";
		ConfigurationContext configurationContext;
		try {
			configurationContext = new XmlConfigurationContext(this,
					configFileName);
		} catch (Exception ex) {
			// Wrap failure to not require tests to have to handle
			StringWriter stackTrace = new StringWriter();
			ex.printStackTrace(new PrintWriter(stackTrace));
			fail("Failed to obtain configuration: " + configFileName + "\n"
					+ stackTrace.toString());
			return; // fail should propagate exception
		}

		// Create the office frame to return the mock office floor builder
		OfficeFrame officeFrame = new OfficeFrame() {
			@Override
			public OfficeFloorBuilder createOfficeFloorBuilder(
					String officeFloorName) {
				return AbstractCompileTestCase.this.officeFloorBuilder;
			}
		};

		// Create the compiler (overriding values to allow testing)
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler();
		compiler.setCompilerIssues(this.enhancedIssues);
		compiler.setOfficeFrame(officeFrame);
		compiler.setConfigurationContext(configurationContext);

		// Add the properties
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			compiler.addProperty(name, value);
		}

		// Compile the office floor
		OfficeFloor loadedOfficeFloor = compiler.compile("office-floor");

		// Verify the mocks
		this.verifyMockObjects();

		// Ensure the correct loaded office floor
		if (isExpectBuild) {
			assertEquals("Incorrect built office floor", officeFloor,
					loadedOfficeFloor);
		} else {
			assertNull("Should not build the office floor", officeFloor);
		}
	}

}