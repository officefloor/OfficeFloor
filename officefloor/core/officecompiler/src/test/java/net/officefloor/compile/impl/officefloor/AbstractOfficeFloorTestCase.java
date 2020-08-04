/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.officefloor;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.structure.AbstractStructureTestCase;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;

/**
 * Tests loading the {@link OfficeFloor} via the {@link OfficeFloorLoader} from
 * the {@link OfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeFloorTestCase extends AbstractStructureTestCase {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	protected final String OFFICE_FLOOR_LOCATION = "OFFICE_FLOOR";

	/**
	 * Enhances {@link CompilerIssues}.
	 */
	private final CompilerIssues enhancedIssues = this.enhanceIssues(this.issues);

	/**
	 * <p>
	 * Allow enhancing the {@link CompilerIssues}. For example allows wrapping with
	 * a {@link StderrCompilerIssuesWrapper}.
	 * <p>
	 * This is available for {@link TestCase} instances to override.
	 * 
	 * @param issues {@link CompilerIssues}.
	 * @return By default returns input {@link CompilerIssues}.
	 */
	protected CompilerIssues enhanceIssues(CompilerIssues issues) {
		return this.issues;
	}

	/**
	 * {@link OfficeFloorBuilder}.
	 */
	protected final OfficeFloorBuilder officeFloorBuilder = this.createMock(OfficeFloorBuilder.class);

	/**
	 * Creates a mock {@link TeamBuilder}.
	 * 
	 * @return Mock {@link TeamBuilder}.
	 */
	@SuppressWarnings("unchecked")
	protected TeamBuilder<MakerTeamSource> createMockTeamBuilder() {
		return this.createMock(TeamBuilder.class);
	}

	/**
	 * Creates a mock {@link OfficeBuilder}.
	 * 
	 * @return Mock {@link OfficeBuilder}.
	 */
	protected OfficeBuilder createMockOfficeBuilder() {
		return this.createMock(OfficeBuilder.class);
	}

	/**
	 * Creates the mock {@link ManagedFunctionBuilder}.
	 * 
	 * @return Mock {@link ManagedFunctionBuilder}.
	 */
	protected ManagedFunctionBuilder<?, ?> createMockManagedFunctionBuilder() {
		return this.createMock(ManagedFunctionBuilder.class);
	}

	/**
	 * Records initiating the {@link OfficeFloorBuilder}.
	 */
	protected void record_initiateOfficeFloorBuilder() {

		// Record adding listener for clock factory and external service handling
		this.officeFloorBuilder.addOfficeFloorListener(this.paramType(OfficeFloorListener.class));
		this.officeFloorBuilder.addOfficeFloorListener(this.paramType(OfficeFloorListener.class));

		// Record initiate OfficeFloor builder
		this.officeFloorBuilder.setClassLoader(Thread.currentThread().getContextClassLoader());
		this.officeFloorBuilder.setClockFactory(this.paramType(ClockFactory.class));
		this.officeFloorBuilder.addResources(this.resourceSource);
	}

	/**
	 * {@link MakerTeamSource} identifier.
	 */
	private int makerTeamSourceIdentifier = 0;

	/**
	 * Records adding a {@link Team}.
	 * 
	 * @param Name of the {@link Team}.
	 * @return {@link TeamBuilder}.
	 */
	protected TeamBuilder<?> record_officefloor_addTeam(String teamName) {
		TeamBuilder<?> teamBuilder = this.createMockTeamBuilder();
		this.recordReturn(this.officeFloorBuilder,
				this.officeFloorBuilder.addTeam(this.param(teamName), this.paramType(MakerTeamSource.class)),
				teamBuilder);
		teamBuilder.addProperty(MakerTeamSource.MAKER_IDENTIFIER_PROPERTY_NAME,
				String.valueOf(this.makerTeamSourceIdentifier++));
		return teamBuilder;
	}

	/**
	 * Current {@link OfficeBuilder}.
	 */
	private OfficeBuilder officeBuilder = null;

	/**
	 * Records adding an {@link Office}.
	 * 
	 * @param officeName Name of the {@link Office}.
	 * @return {@link OfficeBuilder}.
	 */
	protected OfficeBuilder record_officefloor_addOffice(String officeName) {
		this.officeBuilder = this.createMockOfficeBuilder();
		this.recordReturn(this.officeFloorBuilder, this.officeFloorBuilder.addOffice(officeName), this.officeBuilder);
		return this.officeBuilder;
	}

	/**
	 * Current {@link ManagedFunctionBuilder}.
	 */
	@SuppressWarnings("rawtypes")
	private ManagedFunctionBuilder functionBuilder = null;

	/**
	 * Records adding a {@link ManagedFunction}.
	 * 
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @param factory      {@link ManagedFunctionFactory}.
	 * @return {@link ManagedFunctionBuilder}.
	 */
	protected ManagedFunctionBuilder<?, ?> record_office_addFunction(String functionName,
			ManagedFunctionFactory<?, ?> factory) {

		// Record adding the function
		this.functionBuilder = this.createMockManagedFunctionBuilder();
		this.recordReturn(this.officeBuilder, this.officeBuilder.addManagedFunction(functionName, factory),
				this.functionBuilder);
		return this.functionBuilder;
	}

	/**
	 * Loads the {@link OfficeFloor}.
	 * 
	 * @param isExpectBuild          If expected to build the {@link OfficeFloor}.
	 * @param maker                  {@link OfficeFloorMaker}.
	 * @param propertyNameValuePairs Name/value pairs for the necessary
	 *                               {@link Property} instances to load the
	 *                               {@link OfficeFloor}.
	 */
	protected void loadOfficeFloor(boolean isExpectBuild, OfficeFloorMaker maker, String... propertyNameValuePairs) {

		// Office floor potentially built
		OfficeFloor officeFloor = null;

		// Record building if expected to build OfficeFloor
		if (isExpectBuild) {

			// Create the mock office floor built
			officeFloor = this.createMock(OfficeFloor.class);

			// Record successfully building the office floor
			this.recordReturn(this.officeFloorBuilder,
					this.officeFloorBuilder.buildOfficeFloor(this.paramType(OfficeFloorIssues.class)), officeFloor);
		}

		// Create the office frame to return mock OfficeFloor builder
		OfficeFrame officeFrame = new OfficeFrame() {
			@Override
			public OfficeFloorBuilder createOfficeFloorBuilder(String officeFloorName) {
				return AbstractOfficeFloorTestCase.this.officeFloorBuilder;
			}
		};

		// Create the compiler and load the OfficeFloor
		this.replayMockObjects();
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

		// Configure the compiler
		compiler.setCompilerIssues(this.enhancedIssues);
		compiler.addResources(this.resourceSource);
		compiler.setOfficeFrame(officeFrame);

		// Configure the OfficeFloor details
		compiler.setOfficeFloorSourceClass(MakerOfficeFloorSource.class);
		compiler.setOfficeFloorLocation(OFFICE_FLOOR_LOCATION);
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			compiler.addProperty(name, value);
		}

		// Register the OfficeFloor maker and add properties
		PropertyList registerProperties = MakerOfficeFloorSource.register(maker);
		for (Property property : registerProperties) {
			compiler.addProperty(property.getName(), property.getValue());
		}

		// Compile the OfficeFloor
		OfficeFloor loadedOfficeFloor = compiler.compile("OfficeFloor");
		this.verifyMockObjects();

		// Ensure the correct loaded office floor
		if (isExpectBuild) {
			assertEquals("Incorrect built office floor", officeFloor, loadedOfficeFloor);
		} else {
			assertNull("Should not build the office floor", officeFloor);
		}
	}

}
