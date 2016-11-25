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
package net.officefloor.compile.integrate;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.issues.MockCompilerIssues;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.TypeMatcher;
import net.officefloor.model.impl.repository.xml.XmlConfigurationContext;

import org.easymock.AbstractMatcher;

/**
 * Provides abstract functionality for testing integration of the
 * {@link OfficeFloorCompiler}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractCompileTestCase extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	protected final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * Enhances {@link CompilerIssues}.
	 */
	private final CompilerIssues enhancedIssues = this
			.enhanceIssues(this.issues);

	/**
	 * {@link XmlConfigurationContext} for testing.
	 */
	private XmlConfigurationContext configurationContext = null;

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
		return issues;
	}

	/**
	 * Records initialising the {@link OfficeFloorBuilder}.
	 * 
	 * @param resourceSources
	 *            {@link ResourceSource} instances.
	 */
	protected void record_init(ResourceSource... resourceSources) {
		this.officeFloorBuilder.setClassLoader(Thread.currentThread()
				.getContextClassLoader());
		for (ResourceSource resourceSource : resourceSources) {
			this.officeFloorBuilder.addResources(resourceSource);
		}
		this.officeFloorBuilder.addResources(this.getResourceSource());
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
	protected <S extends TeamSource> TeamBuilder<S> record_officeFloorBuilder_addTeam(
			String teamName, Class<S> teamSourceClass,
			String... propertyNameValues) {
		TeamBuilder<S> builder = this.createMock(TeamBuilder.class);
		this.recordReturn(this.officeFloorBuilder,
				this.officeFloorBuilder.addTeam(teamName, teamSourceClass),
				builder);
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			builder.addProperty(name, value);
		}
		return builder;
	}

	/**
	 * Current {@link ManagedObjectBuilder}.
	 */
	@SuppressWarnings("rawtypes")
	private ManagedObjectBuilder managedObjectBuilder = null;

	/**
	 * Current {@link ManagingOfficeBuilder}.
	 */
	private ManagingOfficeBuilder<?> managingOfficeBuilder = null;

	/**
	 * Records adding a {@link ManagedObjectSource} to the
	 * {@link OfficeFloorBuilder}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} class.
	 * @param timeout
	 *            Timeout of the {@link ManagedObject}.
	 * @param propertyNameValues
	 *            {@link Property} name/value listing.
	 * @param {@link ManagedObjectBuilder} for the added
	 *        {@link ManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	protected <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> ManagedObjectBuilder<F> record_officeFloorBuilder_addManagedObject(
			String managedObjectSourceName, Class<S> managedObjectSourceClass,
			long timeout, String... propertyNameValues) {
		this.managedObjectBuilder = this.createMock(ManagedObjectBuilder.class);
		this.recordReturn(this.officeFloorBuilder, this.officeFloorBuilder
				.addManagedObject(managedObjectSourceName,
						managedObjectSourceClass), this.managedObjectBuilder);
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			this.managedObjectBuilder.addProperty(name, value);
		}
		if (timeout > 0) {
			this.managedObjectBuilder.setTimeout(timeout);
		}
		return this.managedObjectBuilder;
	}

	/**
	 * Records adding a {@link ManagedObjectSource} to the
	 * {@link OfficeFloorBuilder}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 * @param timeout
	 *            Timeout of the {@link ManagedObject}.
	 * @param propertyNameValues
	 *            {@link Property} name/value listing.
	 * @param {@link ManagedObjectBuilder} for the added
	 *        {@link ManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	protected <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> ManagedObjectBuilder<F> record_officeFloorBuilder_addManagedObject(
			String managedObjectSourceName, S managedObjectSource,
			long timeout, String... propertyNameValues) {
		this.managedObjectBuilder = this.createMock(ManagedObjectBuilder.class);
		this.recordReturn(this.officeFloorBuilder,
				this.officeFloorBuilder.addManagedObject(
						managedObjectSourceName, managedObjectSource),
				this.managedObjectBuilder);
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			this.managedObjectBuilder.addProperty(name, value);
		}
		this.managedObjectBuilder.setTimeout(timeout);
		return this.managedObjectBuilder;
	}

	/**
	 * Records specifying the {@link ManagingOffice}.
	 * 
	 * @param officeName
	 *            Name of the {@link ManagingOffice}.
	 * @return {@link ManagingOfficeBuilder}.
	 */
	protected ManagingOfficeBuilder<?> record_managedObjectBuilder_setManagingOffice(
			String officeName) {
		this.managingOfficeBuilder = this
				.createMock(ManagingOfficeBuilder.class);
		this.recordReturn(this.managedObjectBuilder,
				this.managedObjectBuilder.setManagingOffice(officeName),
				this.managingOfficeBuilder);
		return this.managingOfficeBuilder;
	}

	/**
	 * Records specifying the Input {@link ManagedObject} name.
	 * 
	 * @param inputManagedObjectName
	 *            Input {@link ManagedObject} name.
	 * @return {@link DependencyMappingBuilder} for the Input
	 *         {@link ManagedObject}.
	 */
	protected DependencyMappingBuilder record_managingOfficeBuilder_setInputManagedObjectName(
			String inputManagedObjectName) {
		DependencyMappingBuilder dependencyMapper = this
				.createMock(DependencyMappingBuilder.class);
		this.recordReturn(this.managingOfficeBuilder,
				this.managingOfficeBuilder
						.setInputManagedObjectName(inputManagedObjectName),
				dependencyMapper);
		return dependencyMapper;
	}

	/**
	 * Current {@link OfficeBuilder}.
	 */
	private OfficeBuilder officeBuilder = null;

	/**
	 * Records adding a {@link OfficeBuilder}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @return Added {@link OfficeBuilder}.
	 */
	protected OfficeBuilder record_officeFloorBuilder_addOffice(
			String officeName) {

		// Record adding the office
		this.officeBuilder = this.createMock(OfficeBuilder.class);
		this.recordReturn(this.officeFloorBuilder,
				this.officeFloorBuilder.addOffice(officeName),
				this.officeBuilder);

		// Reset add work matcher as new mock office builder
		this.isMatcherSet_officeBuilder_addWork = false;

		// Return the office builder
		return this.officeBuilder;
	}

	/**
	 * Records adding a {@link ProcessState} {@link ManagedObject} to the
	 * {@link Office}.
	 * 
	 * @param processManagedObjectName
	 *            {@link ThreadState} bound name.
	 * @param officeManagedObjectName
	 *            {@link Office} registered {@link ManagedObject} name.
	 */
	protected DependencyMappingBuilder record_officeBuilder_addProcessManagedObject(
			String processManagedObjectName, String officeManagedObjectName) {
		DependencyMappingBuilder builder = this
				.createMock(DependencyMappingBuilder.class);
		this.recordReturn(this.officeBuilder, this.officeBuilder
				.addProcessManagedObject(processManagedObjectName,
						officeManagedObjectName), builder);
		return builder;
	}

	/**
	 * Records adding a {@link ThreadState} {@link ManagedObject} to the
	 * {@link Office}.
	 * 
	 * @param threadManagedObjectName
	 *            {@link ThreadState} bound name.
	 * @param officeManagedObjectName
	 *            {@link Office} registered {@link ManagedObject} name.
	 */
	protected DependencyMappingBuilder record_officeBuilder_addThreadManagedObject(
			String threadManagedObjectName, String officeManagedObjectName) {
		DependencyMappingBuilder builder = this
				.createMock(DependencyMappingBuilder.class);
		this.recordReturn(this.officeBuilder, this.officeBuilder
				.addThreadManagedObject(threadManagedObjectName,
						officeManagedObjectName), builder);
		return builder;
	}

	/**
	 * Flags if the matcher has been specified to add a {@link Governance}.
	 */
	private boolean isMatcherSet_officeBuilder_addGovernance = false;

	/**
	 * Records adding a {@link GovernanceSource} to the {@link OfficeBuilder}.
	 * 
	 * @param governanceSourceName
	 *            Name of the {@link GovernanceSource}.
	 * @param governanceSourceClass
	 *            {@link GovernanceSource} class.
	 * @param extensionInterface
	 *            Extension interface.
	 * @return {@link GovernanceBuilder} for the added {@link GovernanceSource}.
	 */
	@SuppressWarnings("unchecked")
	protected <I, F extends Enum<F>, S extends GovernanceSource<I, F>> GovernanceBuilder<F> record_officeBuilder_addGovernance(
			String governanceName, Class<S> governanceSourceClass,
			Class<?> extensionInterface) {
		GovernanceBuilder<F> governanceBuilder = this
				.createMock(GovernanceBuilder.class);
		this.recordReturn(this.officeBuilder, this.officeBuilder.addGovernance(
				governanceName, (GovernanceFactory<I, F>) null,
				(Class<I>) extensionInterface), governanceBuilder);
		if (!this.isMatcherSet_officeBuilder_addGovernance) {
			this.control(this.officeBuilder).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					// Ensure have governance factory
					assertNotNull("Must have governance factory", actual[1]);

					// Match if name and extension interface same
					return this.argumentMatches(expected[0], actual[0])
							&& this.argumentMatches(expected[2], actual[2]);
				}
			});
			this.isMatcherSet_officeBuilder_addGovernance = true;
		}
		return governanceBuilder;
	}

	/**
	 * Records adding a {@link GovernanceSource} to the {@link OfficeBuilder}.
	 * 
	 * @param governanceSourceName
	 *            Name of the {@link GovernanceSource}.
	 * @param teamName
	 *            Name of {@link Team} responsible for {@link Governance}.
	 * @param governanceSourceClass
	 *            {@link GovernanceSource} class.
	 * @param extensionInterface
	 *            Extension interface.
	 * @return {@link GovernanceBuilder} for the added {@link GovernanceSource}.
	 */
	protected <I, F extends Enum<F>, S extends GovernanceSource<I, F>> GovernanceBuilder<F> record_officeBuilder_addGovernance(
			String governanceName, String teamName,
			Class<S> governanceSourceClass, Class<?> extensionInterface) {
		GovernanceBuilder<F> governanceBuilder = this
				.record_officeBuilder_addGovernance(governanceName,
						governanceSourceClass, extensionInterface);
		governanceBuilder.setTeam(teamName);
		return governanceBuilder;
	}

	/**
	 * Records adding a {@link ThreadState} bound {@link Administrator}.
	 * 
	 * @param administratorName
	 *            Name of the {@link Administrator}.
	 * @param administratorSourceClass
	 *            {@link AdministratorSource} class.
	 * @param propertyNameValues
	 *            {@link Property} name/value listing.
	 * @return {@link AdministratorBuilder} for the added {@link Administrator}.
	 */
	@SuppressWarnings("unchecked")
	protected <I, A extends Enum<A>, S extends AdministratorSource<I, A>> AdministratorBuilder<A> record_officeBuilder_addThreadAdministrator(
			String administratorName, Class<S> administratorSourceClass,
			String... propertyNameValues) {
		final AdministratorBuilder<A> admin = this
				.createMock(AdministratorBuilder.class);
		this.recordReturn(this.officeBuilder, this.officeBuilder
				.addThreadAdministrator(administratorName,
						administratorSourceClass), admin);
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			admin.addProperty(name, value);
		}
		return admin;
	}

	/**
	 * Convenience method to record adding a {@link ThreadState} bound
	 * {@link Administrator} and specifying {@link Team} responsible for the
	 * administration.
	 * 
	 * @param administratorName
	 *            Name of the {@link Administrator}.
	 * @param officeTeamName
	 *            {@link Office} {@link Team} name responsible for
	 *            administration.
	 * @param administratorSourceClass
	 *            {@link AdministratorSource} class.
	 * @param propertyNameValues
	 *            {@link Property} name/value listing.
	 * @return {@link AdministratorBuilder} for the added {@link Administrator}.
	 */
	protected <I, A extends Enum<A>, S extends AdministratorSource<I, A>> AdministratorBuilder<A> record_officeBuilder_addThreadAdministrator(
			String administratorName, String officeTeamName,
			Class<S> administratorSourceClass, String... propertyNameValues) {
		AdministratorBuilder<A> builder = this
				.record_officeBuilder_addThreadAdministrator(administratorName,
						administratorSourceClass, propertyNameValues);
		builder.setTeam(officeTeamName);
		return builder;
	}

	/**
	 * Records registering the {@link EscalationProcedure}.
	 * 
	 * @param typeOfCause
	 *            Type of cause handled by {@link EscalationProcedure}.
	 * @param workName
	 *            Name of {@link Work} to handle {@link Escalation}.
	 * @param taskName
	 *            Name of {@link Task} within {@link Work} to handle
	 *            {@link Escalation}.
	 */
	protected <E extends Throwable> void record_officeBuilder_addEscalation(
			Class<E> typeOfCause, String workName, String taskName) {
		this.officeBuilder.addEscalation(typeOfCause, workName, taskName);
	}

	/**
	 * Records registering the {@link Team}.
	 * 
	 * @param officeTeamName
	 *            {@link Office} {@link Team} name.
	 * @param officeFloorTeamName
	 *            {@link OfficeFloor} {@link Team} name.
	 */
	protected void record_officeBuilder_registerTeam(String officeTeamName,
			String officeFloorTeamName) {
		this.officeBuilder.registerTeam(officeTeamName, officeFloorTeamName);
	}

	/**
	 * Convenience method to both add the {@link Office} and register a
	 * {@link Team} to it.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param officeTeamName
	 *            {@link Office} {@link Team} name.
	 * @param officeFloorTeamName
	 *            {@link OfficeFloor} {@link Team} name.
	 * @return Added {@link OfficeBuilder}.
	 */
	protected OfficeBuilder record_officeFloorBuilder_addOffice(
			String officeName, String officeTeamName, String officeFloorTeamName) {
		this.record_officeFloorBuilder_addOffice(officeName);
		this.record_officeBuilder_registerTeam(officeTeamName,
				officeFloorTeamName);
		return this.officeBuilder;
	}

	/**
	 * Records adding a start-up {@link Task} to the {@link OfficeBuilder}.
	 * 
	 * @param workName
	 *            Name of {@link Work} for the start-up {@link Task}.
	 * @param taskName
	 *            Name of start-up {@link Task}.
	 */
	protected void record_officeBuilder_addStartupTask(String workName,
			String taskName) {
		this.officeBuilder.addStartupTask(workName, taskName);
	}

	/**
	 * Current {@link WorkBuilder}.
	 */
	private WorkBuilder<Work> workBuilder = null;

	/**
	 * Flags if the matcher has been specified to add a {@link Work}.
	 */
	private boolean isMatcherSet_officeBuilder_addWork = false;

	/**
	 * Records adding a {@link WorkBuilder}.
	 * 
	 * @param workName
	 *            Name of the {@link Work}.
	 * @return Added {@link WorkBuilder}.
	 */
	@SuppressWarnings("unchecked")
	protected WorkBuilder<Work> record_officeBuilder_addWork(String workName) {

		// Record adding the work
		this.workBuilder = this.createMock(WorkBuilder.class);
		WorkFactory<Work> workFactory = null;
		this.recordReturn(this.officeBuilder,
				this.officeBuilder.addWork(workName, workFactory),
				this.workBuilder);
		if (!this.isMatcherSet_officeBuilder_addWork) {
			this.control(this.officeBuilder).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					assertNotNull("Must have work factory", actual[1]);
					// Match based on work name
					return expected[0].equals(actual[0]);
				}
			});
			this.isMatcherSet_officeBuilder_addWork = true;
		}

		// Reset add task matcher as new mock work builder
		this.isMatcherSet_workBuilder_addTask = false;

		// Return the work builder
		return this.workBuilder;
	}

	/**
	 * Current {@link TaskBuilder}.
	 */
	private TaskBuilder<Work, ?, ?> taskBuilder;

	/**
	 * Records adding a {@link TaskBuilder}.
	 * 
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @return Added {@link TaskBuilder}.
	 */
	protected TaskBuilder<Work, ?, ?> record_workBuilder_addTask(String taskName) {
		return this.record_workBuilder_addTask(taskName, null);
	}

	/**
	 * Flags if the matcher has been specified to add a {@link Task}.
	 */
	private boolean isMatcherSet_workBuilder_addTask = false;

	/**
	 * Convenience method for recording adding a {@link TaskBuilder} and
	 * specifying the {@link Team} for the {@link Task}.
	 * 
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param officeTeamName
	 *            {@link Office} {@link Team} name.
	 * @return Added {@link TaskBuilder}.
	 */
	@SuppressWarnings("unchecked")
	protected TaskBuilder<Work, ?, ?> record_workBuilder_addTask(
			String taskName, String officeTeamName) {

		// Record adding the task
		this.taskBuilder = this.createMock(TaskBuilder.class);
		TaskFactory<Work, Indexed, Indexed> taskFactory = null;
		this.recordReturn(this.workBuilder,
				this.workBuilder.addTask(taskName, taskFactory),
				this.taskBuilder);
		if (!this.isMatcherSet_workBuilder_addTask) {
			this.control(this.workBuilder).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					assertNotNull("Must have task factory", actual[1]);
					// Match based on task name
					return expected[0].equals(actual[0]);
				}
			});
			this.isMatcherSet_workBuilder_addTask = true;
		}

		// Determine if record specifying the team responsible for task
		if (officeTeamName != null) {
			this.taskBuilder.setTeam(officeTeamName);
		}

		// Return the task builder
		return this.taskBuilder;
	}

	/**
	 * Specifies the {@link Team} for the {@link Task}.
	 * 
	 * @param officeTeamName
	 *            {@link Office} {@link Team} name.
	 */
	protected void record_taskBuilder_setTeam(String officeTeamName) {
		this.taskBuilder.setTeam(officeTeamName);
	}

	/**
	 * Specifies the Differentiator for the {@link Task}.
	 * 
	 * @param differentiator
	 *            Differentiator.
	 */
	protected void record_taskBuilder_setDifferentiator(Object differentiator) {
		this.taskBuilder.setDifferentiator(differentiator);
	}

	/**
	 * Obtains the {@link ResourceSource} for test being run.
	 * 
	 * @return {@link ResourceSource} for test being run.
	 */
	protected ResourceSource getResourceSource() {

		// Determine if already available
		if (this.configurationContext != null) {
			return this.configurationContext;
		}

		// Move the 'Test' to start of test case name
		String testCaseName = this.getClass().getSimpleName();
		testCaseName = "Test"
				+ testCaseName.substring(0,
						(testCaseName.length() - "Test".length()));

		// Remove the 'test' from the start of the test name
		String testName = this.getName();
		testName = testName.substring("test".length());

		// Create the configuration context
		String configFileName = testCaseName + "/" + testName + ".xml";
		try {
			this.configurationContext = new XmlConfigurationContext(this,
					configFileName);

			// Add the tag replacements
			this.configurationContext.addTag("testcase", this.getClass()
					.getName());

		} catch (Exception ex) {
			// Wrap failure to not require tests to have to handle
			StringWriter stackTrace = new StringWriter();
			ex.printStackTrace(new PrintWriter(stackTrace));
			fail("Failed to obtain configuration: " + configFileName + "\n"
					+ stackTrace.toString());
			return null; // fail should propagate exception
		}

		// Return the configuration context
		return this.configurationContext;
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

		// OfficeFloor potentially built
		OfficeFloor officeFloor = null;

		// Record building if expected to build OfficeFloor
		if (isExpectBuild) {
			// Create the mock OfficeFloor built
			officeFloor = this.createMock(OfficeFloor.class);

			// Record successfully building the OfficeFloor
			this.recordReturn(this.officeFloorBuilder,
					this.officeFloorBuilder.buildOfficeFloor(null),
					officeFloor, new TypeMatcher(OfficeFloorIssues.class));
		}

		// Replay the mocks
		this.replayMockObjects();

		// Create the office frame to return the mock OfficeFloor builder
		OfficeFrame officeFrame = new OfficeFrame() {
			@Override
			public OfficeFloorBuilder createOfficeFloorBuilder(
					String officeFloorName) {
				return AbstractCompileTestCase.this.officeFloorBuilder;
			}
		};

		// Obtain the resource source
		ResourceSource resourceSource = this.getResourceSource();

		// Create the compiler (overriding values to allow testing)
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.enhancedIssues);
		compiler.setOfficeFrame(officeFrame);
		compiler.addResources(resourceSource);

		// Add the properties
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			compiler.addProperty(name, value);
		}

		// Compile the OfficeFloor
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