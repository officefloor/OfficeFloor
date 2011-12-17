/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.compile.impl.section;

import java.sql.Connection;

import javax.sql.DataSource;
import javax.transaction.xa.XAResource;

import net.officefloor.compile.impl.structure.AbstractStructureTestCase;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.DependentManagedObject;
import net.officefloor.compile.spi.office.ObjectDependency;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionObject;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Tests loading the {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadOfficeSectionTest extends AbstractStructureTestCase {

	/**
	 * Ensure can load an empty {@link OfficeSection}.
	 */
	public void testLoadEmptySection() {

		// Load the empty office section
		OfficeSection section = this.loadOfficeSection("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						// Leave empty
					}
				});

		// Ensure empty
		assertEquals("Incorrect section name", "SECTION",
				section.getOfficeSectionName());
		assertEquals("Should be no sub section", 0,
				section.getOfficeSubSections().length);
		assertEquals("Should be no tasks", 0, section.getOfficeTasks().length);
	}

	/**
	 * Ensure can load a {@link SubSection}.
	 */
	public void testLoadSubSection() {

		// Load the office section with a sub section
		OfficeSection section = this.loadOfficeSection("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.addSubSection("SUB_SECTION", null);
					}
				});

		// Validate results
		assertEquals("Should have a sub section", 1,
				section.getOfficeSubSections().length);
		OfficeSubSection subSection = section.getOfficeSubSections()[0];
		assertEquals("Incorrect sub section", "SUB_SECTION",
				subSection.getOfficeSectionName());
		assertEquals("Should be no sub section tasks", 0,
				subSection.getOfficeTasks().length);
		assertEquals("Should be no tasks", 0, section.getOfficeTasks().length);
	}

	/**
	 * Ensure can load a sub {@link SubSection}. To ensure recursive loading of
	 * the {@link SubSection} instances.
	 */
	public void testLoadSubSubSection() {

		// Load the office section with a sub sub section
		OfficeSection section = this.loadOfficeSection("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.addSubSection("SUB_SECTION",
								new SectionMaker() {
									@Override
									public void make(SectionMakerContext context) {
										context.addSubSection(
												"SUB_SUB_SECTION", null);
									}
								});
					}
				});

		// Validate the results
		assertEquals("Should have a sub section", 1,
				section.getOfficeSubSections().length);
		assertEquals("Should be no tasks on section", 0,
				section.getOfficeTasks().length);
		OfficeSubSection subSection = section.getOfficeSubSections()[0];
		assertEquals("Should have a sub sub section", 1,
				subSection.getOfficeSubSections().length);
		assertEquals("Should be no tasks on sub section", 0,
				subSection.getOfficeTasks().length);
		OfficeSubSection subSubSection = subSection.getOfficeSubSections()[0];
		assertEquals("Incorrect sub sub section", "SUB_SUB_SECTION",
				subSubSection.getOfficeSectionName());
		assertEquals("Should be no tasks on sub sub section", 0,
				subSubSection.getOfficeTasks().length);
	}

	/**
	 * Ensure can load a {@link SectionTask}.
	 */
	public void testLoadSectionTask() {

		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		// Load the office section with a section task
		OfficeSection section = this.loadOfficeSection("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.addTask("WORK", workFactory, "TASK",
								taskFactory, null);
					}
				});

		// Validate results
		assertEquals("Should be no sub sections", 0,
				section.getOfficeSubSections().length);
		assertEquals("Should be no managed object sources", 0,
				section.getOfficeSectionManagedObjectSources().length);
		assertEquals("Should have a single task", 1,
				section.getOfficeTasks().length);
		OfficeTask task = section.getOfficeTasks()[0];
		assertEquals("Incorrect task name", "TASK", task.getOfficeTaskName());
	}

	/**
	 * Ensure no {@link DependentManagedObject} if {@link TaskObject} not
	 * linked.
	 */
	public void testTaskObjectDependencyNotLinked() {

		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		// Record not linked on first attempt to retrieve dependent
		this.issues.addIssue(LocationType.OFFICE, SECTION_LOCATION, null, null,
				"TaskObject OBJECT is not linked to a DependentManagedObject");

		// Replay mocks
		this.replayMockObjects();

		// Load the task object dependency not linked
		OfficeSection section = this.loadOfficeSection(false, "SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.addTaskObject("WORK", workFactory, "TASK",
								taskFactory, "OBJECT", Connection.class);
					}
				});

		// Ensure task object is correct
		OfficeTask task = section.getOfficeTasks()[0];
		assertEquals("Incorrect number of dependencies", 1,
				task.getObjectDependencies().length);
		ObjectDependency dependency = task.getObjectDependencies()[0];
		assertEquals("Incorrect object dependency name", "OBJECT",
				dependency.getObjectDependencyName());
		assertEquals("Incorrect object dependency type", Connection.class,
				dependency.getObjectDependencyType());

		// Validate not linked to dependent managed object
		assertNull("Should not be linked to dependent managed object",
				dependency.getDependentManagedObject());

		// Verify the mocks
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load a {@link OfficeSectionInput}.
	 */
	public void testLoadOfficeSectionInput() {

		// Load the office section with an office section input
		OfficeSection section = this.loadOfficeSection("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.getBuilder().addSectionInput("INPUT",
								String.class.getName());
					}
				});

		// Validate results
		assertEquals("Should have office section input", 1,
				section.getOfficeSectionInputs().length);
		OfficeSectionInput input = section.getOfficeSectionInputs()[0];
		assertEquals("Incorrect office section input", "INPUT",
				input.getOfficeSectionInputName());
		assertEquals("Incorrect office section input parameter type",
				String.class.getName(), input.getParameterType());
		assertEquals("Should be no office section outputs", 0,
				section.getOfficeSectionOutputs().length);
		assertEquals("Should have no office section objects", 0,
				section.getOfficeSectionObjects().length);
	}

	/**
	 * Ensure can load a {@link OfficeSectionOutput}.
	 */
	public void testLoadOfficeSectionOutput() {

		// Load the office section with an office section output
		OfficeSection section = this.loadOfficeSection("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.getBuilder().addSectionOutput("OUTPUT",
								Exception.class.getName(), true);
					}
				});

		// Validate results
		assertEquals("Should be no office section inputs", 0,
				section.getOfficeSectionInputs().length);
		assertEquals("Should have office section output", 1,
				section.getOfficeSectionOutputs().length);
		OfficeSectionOutput output = section.getOfficeSectionOutputs()[0];
		assertEquals("Incorrect office section output", "OUTPUT",
				output.getOfficeSectionOutputName());
		assertEquals("Incorrect office section output argument type",
				Exception.class.getName(), output.getArgumentType());
		assertTrue("Incorrect office section output escalation only flag",
				output.isEscalationOnly());
		assertEquals("Should have no office section objects", 0,
				section.getOfficeSectionObjects().length);
	}

	/**
	 * Ensure can add an {@link OfficeSectionObject}.
	 */
	public void testLoadOfficeSectionObject() {

		// Load the office section with an office section object
		OfficeSection section = this.loadOfficeSection("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.getBuilder().addSectionObject("OBJECT",
								Connection.class.getName());
					}
				});

		// Validate results
		assertEquals("Should be no office section inputs", 0,
				section.getOfficeSectionInputs().length);
		assertEquals("Should be no office section outputs", 0,
				section.getOfficeSectionOutputs().length);
		assertEquals("Should have office section object", 1,
				section.getOfficeSectionObjects().length);
		OfficeSectionObject object = section.getOfficeSectionObjects()[0];
		assertEquals("Incorrect office section object", "OBJECT",
				object.getOfficeSectionObjectName());
		assertEquals("Incorrect office section object, object type",
				Connection.class.getName(), object.getObjectType());
		assertNull("Office section object should not be qualified",
				object.getTypeQualifier());
	}

	/**
	 * Ensure can add a qualified {@link OfficeSectionObject}.
	 */
	public void testLoadQualifiedOfficeSectionObject() {

		// Load the office section with an office section object
		OfficeSection section = this.loadOfficeSection("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						SectionObject object = context.getBuilder()
								.addSectionObject("OBJECT",
										Connection.class.getName());
						object.setTypeQualifier("QUALIFIED");
					}
				});

		// Validate results
		assertEquals("Should be no office section inputs", 0,
				section.getOfficeSectionInputs().length);
		assertEquals("Should be no office section outputs", 0,
				section.getOfficeSectionOutputs().length);
		assertEquals("Should have office section object", 1,
				section.getOfficeSectionObjects().length);
		OfficeSectionObject object = section.getOfficeSectionObjects()[0];
		assertEquals("Incorrect office section object", "OBJECT",
				object.getOfficeSectionObjectName());
		assertEquals("Incorrect office section object, object type",
				Connection.class.getName(), object.getObjectType());
		assertEquals("Incorrect office section object, type qualifier",
				"QUALIFIED", object.getTypeQualifier());
	}

	/**
	 * Ensure can load a {@link OfficeSectionManagedObject}.
	 */
	public void testLoadSectionManagedObject() {

		// Load the section managed object
		OfficeSection section = this.loadOfficeSection("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						SectionManagedObjectSource source = context
								.addManagedObjectSource("MO_SOURCE", null);
						source.addSectionManagedObject("MO",
								ManagedObjectScope.PROCESS);
					}
				});

		// Validate the results
		assertEquals("Should have no sub section", 0,
				section.getOfficeSubSections().length);
		assertEquals("Should have no tasks", 0, section.getOfficeTasks().length);
		assertEquals("Should have a section managed object source", 1,
				section.getOfficeSectionManagedObjectSources().length);
		OfficeSectionManagedObjectSource moSource = section
				.getOfficeSectionManagedObjectSources()[0];
		assertEquals("Incorrect managed object source name", "MO_SOURCE",
				moSource.getOfficeSectionManagedObjectSourceName());
		assertEquals("Should have a section managed object", 1,
				moSource.getOfficeSectionManagedObjects().length);
		OfficeSectionManagedObject mo = moSource
				.getOfficeSectionManagedObjects()[0];
		assertEquals("Incorrect managed object name", "MO",
				mo.getOfficeSectionManagedObjectName());
		assertEquals("Should not have dependencies", 0,
				mo.getObjectDependencies().length);
	}

	/**
	 * Ensure no {@link DependentManagedObject} if
	 * {@link ManagedObjectDependency} not linked.
	 */
	public void testSectionManagedObjectDependencyNotLinked() {

		// Record not linked on first attempt to retrieve dependent
		this.issues
				.addIssue(LocationType.SECTION, SECTION_LOCATION, null, null,
						"ManagedObjectDependency DEPENDENCY is not linked to a DependentManagedObject");

		// Load the section managed object with a dependency
		this.replayMockObjects();
		OfficeSection section = this.loadOfficeSection(false, "SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						SectionManagedObjectSource source = context
								.addManagedObjectSource("MO_SOURCE",
										new ManagedObjectMaker() {
											@Override
											public void make(
													ManagedObjectMakerContext context) {
												context.getContext()
														.addDependency(
																Connection.class)
														.setLabel("DEPENDENCY");
											}
										});
						SectionManagedObject mo = source
								.addSectionManagedObject("MO",
										ManagedObjectScope.PROCESS);
						mo.getManagedObjectDependency("DEPENDENCY");
					}
				});

		// Validate the managed object
		assertEquals("Should have a section managed object source", 1,
				section.getOfficeSectionManagedObjectSources().length);
		OfficeSectionManagedObjectSource moSource = section
				.getOfficeSectionManagedObjectSources()[0];
		assertEquals("Incorrect managed object source name", "MO_SOURCE",
				moSource.getOfficeSectionManagedObjectSourceName());
		assertEquals("Should have a section managed object", 1,
				moSource.getOfficeSectionManagedObjects().length);
		OfficeSectionManagedObject mo = moSource
				.getOfficeSectionManagedObjects()[0];
		assertEquals("Incorrect managed object name", "MO",
				mo.getOfficeSectionManagedObjectName());
		assertEquals("Should have a dependency", 1,
				mo.getObjectDependencies().length);
		ObjectDependency dependency = mo.getObjectDependencies()[0];
		assertEquals("Incorrect dependency name", "DEPENDENCY",
				dependency.getObjectDependencyName());
		assertEquals("Incorrect dependency type", Connection.class,
				dependency.getObjectDependencyType());

		// Dependency should not be linked and report the issue
		assertNull("Dependency should not be linked",
				dependency.getDependentManagedObject());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load a {@link OfficeSectionManagedObject} that supports an
	 * extension interface.
	 */
	public void testLoadSectionManagedObjectSupportingAnExtensionInterface() {

		// Load the section managed object supporting an extension interface
		OfficeSection section = this.loadOfficeSection("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						SectionManagedObjectSource source = context
								.addManagedObjectSource("MO_SOURCE",
										new ManagedObjectMaker() {
											@Override
											public void make(
													ManagedObjectMakerContext context) {
												context.addExtensionInterface(XAResource.class);
											}
										});
						source.addSectionManagedObject("MO",
								ManagedObjectScope.WORK);
					}
				});

		// Validate the results
		assertEquals("Should have a section managed object source", 1,
				section.getOfficeSectionManagedObjectSources().length);
		OfficeSectionManagedObjectSource moSource = section
				.getOfficeSectionManagedObjectSources()[0];
		assertEquals("Incorrect managed object source name", "MO_SOURCE",
				moSource.getOfficeSectionManagedObjectSourceName());
		assertEquals("Should have a section managed object", 1,
				moSource.getOfficeSectionManagedObjects().length);
		OfficeSectionManagedObject mo = moSource
				.getOfficeSectionManagedObjects()[0];
		assertEquals("Should have a supported extension interface", 1,
				mo.getSupportedExtensionInterfaces().length);
		Class<?> supportedEi = mo.getSupportedExtensionInterfaces()[0];
		assertEquals("Incorrect supported extension interface",
				XAResource.class, supportedEi);
	}

	/**
	 * Ensure can get {@link DependentManagedObject} linked to
	 * {@link SectionManagedObject} of same {@link OfficeSubSection}.
	 */
	public void testTaskDependentOnManagedObjectOfSameSection() {

		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		// Load the task object dependent on managed object of same section
		OfficeSection section = this.loadOfficeSection("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {

						// Add the task object and managed object
						TaskObject object = context.addTaskObject("WORK",
								workFactory, "TASK", taskFactory, "OBJECT",
								Connection.class);
						SectionManagedObjectSource moSource = context
								.addManagedObjectSource("MO_SOURCE", null);
						SectionManagedObject managedObject = moSource
								.addSectionManagedObject("MO",
										ManagedObjectScope.THREAD);

						// Link task object to managed object
						context.getBuilder().link(object, managedObject);
					}
				});

		// Validate link to dependent managed object
		OfficeTask task = section.getOfficeTasks()[0];
		assertEquals("Incorrect number of dependencies", 1,
				task.getObjectDependencies().length);
		ObjectDependency dependency = task.getObjectDependencies()[0];
		assertEquals("Incorrect object dependency", "OBJECT",
				dependency.getObjectDependencyName());
		DependentManagedObject mo = dependency.getDependentManagedObject();
		assertEquals("Incorrect dependent managed object", "MO",
				mo.getDependentManagedObjectName());
		assertTrue("Incorrect managed object type",
				mo instanceof OfficeSectionManagedObject);
	}

	/**
	 * Ensure can get {@link DependentManagedObject} linked to
	 * {@link SectionManagedObject} of another {@link OfficeSubSection}.
	 */
	public void testTaskDependentOnManagedObjectOfAnotherSection() {

		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		// Load the task object dependent on managed object of another section
		OfficeSection section = this.loadOfficeSection("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {

						// Add the section with task object
						SubSection objectSection = context.addSubSection(
								"OBJECT_SECTION", new SectionMaker() {
									@Override
									public void make(SectionMakerContext context) {
										// Add the task object
										TaskObject object = context
												.addTaskObject("WORK",
														workFactory, "TASK",
														taskFactory, "OBJECT",
														Connection.class);

										// Link task object to section output
										SectionObject sectionObject = context
												.getBuilder().addSectionObject(
														"SECTION_OBJECT",
														Connection.class
																.getName());
										context.getBuilder().link(object,
												sectionObject);
									}
								});
						SubSectionObject subSectionObject = objectSection
								.getSubSectionObject("SECTION_OBJECT");

						// Add the managed object
						SectionManagedObjectSource moSource = context
								.addManagedObjectSource("MO_SOURCE", null);
						SectionManagedObject managedObject = moSource
								.addSectionManagedObject("MO",
										ManagedObjectScope.PROCESS);

						// Link task object to managed object
						context.getBuilder().link(subSectionObject,
								managedObject);
					}
				});

		// Validate link to dependent managed object
		OfficeTask task = section.getOfficeSubSections()[0].getOfficeTasks()[0];
		assertEquals("Incorrect number of dependencies", 1,
				task.getObjectDependencies().length);
		ObjectDependency dependency = task.getObjectDependencies()[0];
		assertEquals("Incorrect object dependency", "OBJECT",
				dependency.getObjectDependencyName());
		DependentManagedObject mo = dependency.getDependentManagedObject();
		assertEquals("Incorrect dependent managed object", "MO",
				mo.getDependentManagedObjectName());
		assertTrue("Incorrect managed object type",
				mo instanceof OfficeSectionManagedObject);
	}

	/**
	 * Ensure can get {@link DependentManagedObject} for a
	 * {@link ManagedObjectDependency}.
	 */
	public void testManagedObjectDependentOnAnotherManagedObject() {

		// Load the section managed object with a dependency
		OfficeSection section = this.loadOfficeSection("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {

						// Add the first managed object (with dependency)
						SectionManagedObjectSource sourceOne = context
								.addManagedObjectSource("MO_SOURCE_ONE",
										new ManagedObjectMaker() {
											@Override
											public void make(
													ManagedObjectMakerContext context) {
												context.getContext()
														.addDependency(
																Connection.class)
														.setLabel("DEPENDENCY");
											}
										});
						SectionManagedObject moOne = sourceOne
								.addSectionManagedObject("MO_ONE",
										ManagedObjectScope.PROCESS);
						ManagedObjectDependency dependency = moOne
								.getManagedObjectDependency("DEPENDENCY");

						// Add the second managed object (no dependency)
						SectionManagedObjectSource sourceTwo = context
								.addManagedObjectSource("MO_SOURCE_TWO", null);
						SectionManagedObject moTwo = sourceTwo
								.addSectionManagedObject("MO_TWO",
										ManagedObjectScope.PROCESS);

						// Link dependency to second managed object
						context.getBuilder().link(dependency, moTwo);
					}
				});

		// Ensure correct number of managed object sources
		assertEquals("Should have a two managed object sources", 2,
				section.getOfficeSectionManagedObjectSources().length);

		// Obtain appropriate sources
		OfficeSectionManagedObjectSource moSourceOne = section
				.getOfficeSectionManagedObjectSources()[0];
		OfficeSectionManagedObjectSource moSourceTwo = section
				.getOfficeSectionManagedObjectSources()[1];
		if (!("MO_SOURCE_ONE".equals(moSourceOne
				.getOfficeSectionManagedObjectSourceName()))) {
			// Wrong way round, so swap
			OfficeSectionManagedObjectSource tmp = moSourceOne;
			moSourceOne = moSourceTwo;
			moSourceTwo = tmp;
		}

		// Validate managed object one
		assertEquals("Incorrect managed object source name", "MO_SOURCE_ONE",
				moSourceOne.getOfficeSectionManagedObjectSourceName());
		assertEquals("MO_SOURCE_ONE should have a section managed object", 1,
				moSourceOne.getOfficeSectionManagedObjects().length);
		OfficeSectionManagedObject moOne = moSourceOne
				.getOfficeSectionManagedObjects()[0];
		assertEquals("Incorrect managed object name", "MO_ONE",
				moOne.getOfficeSectionManagedObjectName());
		assertEquals("MO_ONE should have a dependency", 1,
				moOne.getObjectDependencies().length);
		ObjectDependency dependency = moOne.getObjectDependencies()[0];
		assertEquals("Incorrect dependency name", "DEPENDENCY",
				dependency.getObjectDependencyName());
		assertEquals("Incorrect dependency type", Connection.class,
				dependency.getObjectDependencyType());
		assertNotNull("Dependency should be linked",
				dependency.getDependentManagedObject());

		// Validate managed object two
		assertEquals("Incorrect managed object source name", "MO_SOURCE_TWO",
				moSourceTwo.getOfficeSectionManagedObjectSourceName());
		assertEquals("MO_SOURCE_TWO should have a section managed object", 1,
				moSourceTwo.getOfficeSectionManagedObjects().length);
		OfficeSectionManagedObject moTwo = moSourceTwo
				.getOfficeSectionManagedObjects()[0];
		assertEquals("Incorrect managed object name", "MO_TWO",
				moTwo.getOfficeSectionManagedObjectName());
		assertEquals("MO_TWO should not have a dependency", 0,
				moTwo.getObjectDependencies().length);

		// Ensure dependency is linked to correct managed object
		assertEquals("Incorrect dependent managed object",
				dependency.getDependentManagedObject(), moTwo);
	}

	/**
	 * Ensure can get {@link TaskObject} linked to {@link ManagedObject} which
	 * has a {@link ManagedObjectDependency} linked to another
	 * {@link ManagedObject}.
	 */
	public void testTaskDependentOnManagedObjectDependentOnAnotherManagedObject() {

		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		// Load the task object dependent on managed object of same section
		OfficeSection section = this.loadOfficeSection("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {

						// Add the task object
						TaskObject taskObject = context.addTaskObject("WORK",
								workFactory, "TASK", taskFactory, "OBJECT",
								Connection.class);

						// Add the first managed object with a dependency
						SectionManagedObjectSource moSourceOne = context
								.addManagedObjectSource("MO_SOURCE_ONE",
										new ManagedObjectMaker() {
											@Override
											public void make(
													ManagedObjectMakerContext context) {
												context.getContext()
														.addDependency(
																DataSource.class)
														.setLabel("DEPENDENCY");
											}
										});
						SectionManagedObject moOne = moSourceOne
								.addSectionManagedObject("MO_ONE",
										ManagedObjectScope.THREAD);
						ManagedObjectDependency dependency = moOne
								.getManagedObjectDependency("DEPENDENCY");

						// Add the second managed object (no dependency)
						SectionManagedObjectSource sourceTwo = context
								.addManagedObjectSource("MO_SOURCE_TWO", null);
						SectionManagedObject moTwo = sourceTwo
								.addSectionManagedObject("MO_TWO",
										ManagedObjectScope.PROCESS);

						// Link task object to first managed object
						context.getBuilder().link(taskObject, moOne);

						// Link managed object dependency to managed object
						context.getBuilder().link(dependency, moTwo);
					}
				});

		// Validate link to dependent managed object
		OfficeTask task = section.getOfficeTasks()[0];
		assertEquals("Incorrect number of task object dependencies", 1,
				task.getObjectDependencies().length);
		ObjectDependency taskDependency = task.getObjectDependencies()[0];
		assertEquals("Incorrect task object dependency", "OBJECT",
				taskDependency.getObjectDependencyName());
		assertEquals("Incorrect task object dependency type", Connection.class,
				taskDependency.getObjectDependencyType());
		DependentManagedObject mo = taskDependency.getDependentManagedObject();
		assertEquals("Incorrect task dependent managed object", "MO_ONE",
				mo.getDependentManagedObjectName());
		assertEquals("Incorrect number of managed object dependencies", 1,
				mo.getObjectDependencies().length);
		ObjectDependency moDependency = mo.getObjectDependencies()[0];
		assertEquals("Incorrect managed object dependency", "DEPENDENCY",
				moDependency.getObjectDependencyName());
		assertEquals("Incorrect managed object dependency type",
				DataSource.class, moDependency.getObjectDependencyType());
		DependentManagedObject dependentMo = moDependency
				.getDependentManagedObject();
		assertEquals("Incorrect managed object dependent managed object",
				"MO_TWO", dependentMo.getDependentManagedObjectName());
		assertEquals("Incorrect number of dependencies for managed object", 0,
				dependentMo.getObjectDependencies().length);
	}

	/**
	 * Ensure {@link TaskObject} flagged as parameter does not provide a
	 * {@link DependentManagedObject}.
	 */
	public void testTaskObjectAsParameter() {

		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		// Load the task object dependent on managed object of same section
		OfficeSection section = this.loadOfficeSection("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						// Add the task object as parameter
						TaskObject taskObject = context.addTaskObject("WORK",
								workFactory, "TASK", taskFactory, "OBJECT",
								Connection.class);
						taskObject.flagAsParameter();
					}
				});

		// Validate no dependent managed object
		OfficeTask task = section.getOfficeTasks()[0];
		assertEquals("Incorrect number of task object dependencies", 1,
				task.getObjectDependencies().length);
		ObjectDependency taskDependency = task.getObjectDependencies()[0];
		assertEquals("Incorrect task object dependency", "OBJECT",
				taskDependency.getObjectDependencyName());
		assertEquals("Incorrect task object dependency type", Connection.class,
				taskDependency.getObjectDependencyType());

		// Ensure no dependent managed object for parameter
		assertNull("Should be no dependent for task parameter",
				taskDependency.getDependentManagedObject());
	}

}