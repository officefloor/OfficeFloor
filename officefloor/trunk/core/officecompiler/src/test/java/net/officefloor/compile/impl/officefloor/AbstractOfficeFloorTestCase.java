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
package net.officefloor.compile.impl.officefloor;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.AbstractStructureTestCase;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.test.issues.StderrCompilerIssuesWrapper;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.match.TypeMatcher;

/**
 * Tests loading the {@link OfficeFloor} via the {@link OfficeFloorLoader} from
 * the {@link OfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeFloorTestCase extends
		AbstractStructureTestCase {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	protected final String OFFICE_FLOOR_LOCATION = "OFFICE_FLOOR";

	/**
	 * Enhances {@link CompilerIssues}.
	 */
	private final CompilerIssues enhancedIssues = this
			.enhanceIssues(this.issues);

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
	 * {@link OfficeFloorBuilder}.
	 */
	protected final OfficeFloorBuilder officeFloorBuilder = this
			.createMock(OfficeFloorBuilder.class);

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
	 * Creates a mock {@link WorkBuilder}.
	 * 
	 * @return Mock {@link WorkBuilder}.
	 */
	@SuppressWarnings("unchecked")
	protected WorkBuilder<Work> createMockWorkBuilder() {
		return this.createMock(WorkBuilder.class);
	}

	/**
	 * Creates the mock {@link TaskBuilder}.
	 * 
	 * @return Mock {@link TaskBuilder}.
	 */
	@SuppressWarnings("unchecked")
	protected TaskBuilder<Work, ?, ?> createMockTaskBuilder() {
		return this.createMock(TaskBuilder.class);
	}

	/**
	 * Records initiating the {@link OfficeFloorBuilder}.
	 */
	protected void record_initiateOfficeFloorBuilder() {
		// Record initiate OfficeFloor builder
		this.officeFloorBuilder.setClassLoader(Thread.currentThread()
				.getContextClassLoader());
		this.officeFloorBuilder.addResources(this.resourceSource);
	}

	/**
	 * {@link MakerTeamSource} identifier.
	 */
	private int makerTeamSourceIdentifier = 0;

	/**
	 * Records adding a {@link Team}.
	 * 
	 * @param Name
	 *            of the {@link Team}.
	 * @return {@link TeamBuilder}.
	 */
	protected TeamBuilder<?> record_officefloor_addTeam(String teamName) {
		TeamBuilder<?> teamBuilder = this.createMockTeamBuilder();
		this.recordReturn(this.officeFloorBuilder, this.officeFloorBuilder
				.addTeam(teamName, MakerTeamSource.class), teamBuilder);
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
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @return {@link OfficeBuilder}.
	 */
	protected OfficeBuilder record_officefloor_addOffice(String officeName) {
		this.officeBuilder = this.createMockOfficeBuilder();
		this.recordReturn(this.officeFloorBuilder,
				this.officeFloorBuilder.addOffice(officeName),
				this.officeBuilder);
		return this.officeBuilder;
	}

	/**
	 * Current {@link WorkBuilder}.
	 */
	@SuppressWarnings("rawtypes")
	private WorkBuilder workBuilder = null;

	/**
	 * Records adding a {@link Work}.
	 * 
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param workFactory
	 *            {@link WorkFactory}.
	 * @return {@link WorkBuilder}.
	 */
	@SuppressWarnings("unchecked")
	protected <W extends Work> WorkBuilder<W> record_office_addWork(
			String workName, WorkFactory<W> workFactory) {
		this.workBuilder = this.createMockWorkBuilder();
		this.recordReturn(this.officeBuilder,
				this.officeBuilder.addWork(workName, workFactory),
				this.workBuilder);
		return this.workBuilder;
	}

	/**
	 * Current {@link TaskBuilder}.
	 */
	@SuppressWarnings("rawtypes")
	private TaskBuilder taskBuilder = null;

	/**
	 * Records adding a {@link Task}.
	 * 
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param factory
	 *            {@link TaskFactory}.
	 * @return {@link TaskBuilder}.
	 */
	@SuppressWarnings("unchecked")
	protected <W extends Work> TaskBuilder<W, ?, ?> record_work_addTask(
			String taskName, TaskFactory<W, ?, ?> factory) {

		// Ensure manufacturer is a mock
		assertNotNull("Manufacturer must be a mock", this.control(factory));

		// Record adding the task
		this.taskBuilder = this.createMockTaskBuilder();
		this.recordReturn(this.workBuilder,
				this.workBuilder.addTask("TASK", factory), this.taskBuilder);
		return this.taskBuilder;
	}

	/**
	 * Loads the {@link OfficeFloor}.
	 * 
	 * @param isExpectBuild
	 *            If expected to build the {@link OfficeFloor}.
	 * @param maker
	 *            {@link OfficeFloorMaker}.
	 * @param propertyNameValuePairs
	 *            Name/value pairs for the necessary {@link Property} instances
	 *            to load the {@link OfficeFloor}.
	 */
	protected void loadOfficeFloor(boolean isExpectBuild,
			OfficeFloorMaker maker, String... propertyNameValuePairs) {

		// Office floor potentially built
		OfficeFloor officeFloor = null;

		// Record building if expected to build OfficeFloor
		if (isExpectBuild) {

			// Create the mock office floor built
			officeFloor = this.createMock(OfficeFloor.class);

			// Record successfully building the office floor
			this.recordReturn(this.officeFloorBuilder,
					this.officeFloorBuilder.buildOfficeFloor(null),
					officeFloor, new TypeMatcher(OfficeFloorIssues.class));
		}

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Create the office frame to return mock office floor builder
		OfficeFrame officeFrame = new OfficeFrame() {
			@Override
			public OfficeFloorBuilder createOfficeFloorBuilder(
					String officeFloorName) {
				return AbstractOfficeFloorTestCase.this.officeFloorBuilder;
			}
		};

		// Register the office floor maker and add to property list
		PropertyList registerProperties = MakerOfficeFloorSource
				.register(maker);
		for (Property property : registerProperties) {
			propertyList.addProperty(property.getName()).setValue(
					property.getValue());
		}

		// Create the office loader and load the office floor
		this.replayMockObjects();
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.enhancedIssues);
		compiler.addResources(this.resourceSource);
		compiler.setOfficeFrame(officeFrame);
		OfficeFloorLoader officeFloorLoader = compiler.getOfficeFloorLoader();
		OfficeFloor loadedOfficeFloor = officeFloorLoader.loadOfficeFloor(
				MakerOfficeFloorSource.class, OFFICE_FLOOR_LOCATION,
				propertyList);
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