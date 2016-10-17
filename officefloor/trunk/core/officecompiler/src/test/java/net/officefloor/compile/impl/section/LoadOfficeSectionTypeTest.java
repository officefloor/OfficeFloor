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
package net.officefloor.compile.impl.section;

import java.sql.Connection;

import javax.sql.DataSource;
import javax.transaction.xa.XAResource;

import net.officefloor.compile.impl.structure.AbstractStructureTestCase;
import net.officefloor.compile.impl.structure.ManagedObjectDependencyNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectSourceNodeImpl;
import net.officefloor.compile.impl.structure.TaskNodeImpl;
import net.officefloor.compile.impl.structure.TaskObjectNodeImpl;
import net.officefloor.compile.object.DependentObjectType;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionManagedObjectSourceType;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.OfficeTaskType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.office.DependentManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionObject;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.DependencyLabeller;

/**
 * Tests loading the {@link OfficeSectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadOfficeSectionTypeTest extends AbstractStructureTestCase {

	/**
	 * Ensure can load an empty {@link OfficeSectionType}.
	 */
	public void testLoadEmptySection() {

		// Load the empty office section
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
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
				section.getOfficeSubSectionTypes().length);
		assertEquals("Should be no tasks", 0,
				section.getOfficeTaskTypes().length);
	}

	/**
	 * Ensure can load a {@link SubSectionType}.
	 */
	public void testLoadSubSection() {

		// Load the office section with a sub section
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.addSubSection("SUB_SECTION", null);
					}
				});

		// Validate results
		assertEquals("Should have a sub section", 1,
				section.getOfficeSubSectionTypes().length);
		OfficeSubSectionType subSection = section.getOfficeSubSectionTypes()[0];
		assertEquals("Incorrect sub section", "SUB_SECTION",
				subSection.getOfficeSectionName());
		assertSame("Incorrect parent section", section,
				subSection.getParentOfficeSubSectionType());
		assertEquals("Should be no sub section tasks", 0,
				subSection.getOfficeTaskTypes().length);
		assertEquals("Should be no tasks", 0,
				section.getOfficeTaskTypes().length);
	}

	/**
	 * Ensure can load a sub {@link SubSectionType}. To ensure recursive loading
	 * of the {@link SubSectionType} instances.
	 */
	public void testLoadSubSubSection() {

		// Load the office section with a sub sub section
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
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
				section.getOfficeSubSectionTypes().length);
		assertEquals("Should be no tasks on section", 0,
				section.getOfficeTaskTypes().length);
		OfficeSubSectionType subSection = section.getOfficeSubSectionTypes()[0];
		assertEquals("Should have a sub sub section", 1,
				subSection.getOfficeSubSectionTypes().length);
		assertSame("Incorrect parent of sub section", section,
				subSection.getParentOfficeSubSectionType());
		assertEquals("Should be no tasks on sub section", 0,
				subSection.getOfficeTaskTypes().length);
		OfficeSubSectionType subSubSection = subSection
				.getOfficeSubSectionTypes()[0];
		assertEquals("Incorrect sub sub section", "SUB_SUB_SECTION",
				subSubSection.getOfficeSectionName());
		assertSame("Incorrect parent of sub sub section", subSection,
				subSubSection.getParentOfficeSubSectionType());
		assertEquals("Should be no tasks on sub sub section", 0,
				subSubSection.getOfficeTaskTypes().length);
	}

	/**
	 * Ensure can load a {@link SectionTaskType}.
	 */
	public void testLoadSectionTask() {

		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		// Load the office section with a section task
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.addTask("WORK", workFactory, "TASK",
								taskFactory, null);
					}
				});

		// Validate results
		assertEquals("Should be no sub sections", 0,
				section.getOfficeSubSectionTypes().length);
		assertEquals("Should be no managed object sources", 0,
				section.getOfficeSectionManagedObjectSourceTypes().length);
		assertEquals("Should have a single task", 1,
				section.getOfficeTaskTypes().length);
		OfficeTaskType task = section.getOfficeTaskTypes()[0];
		assertEquals("Incorrect task name", "TASK", task.getOfficeTaskName());
		assertSame("Incorrect parent section for task", section,
				task.getOfficeSubSectionType());
	}

	/**
	 * Ensure no {@link DependentObjectType} if {@link TaskObjectType} not
	 * linked.
	 */
	public void testTaskObjectDependencyNotLinked() {

		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		// Record not linked on first attempt to retrieve dependent
		this.issues
				.recordIssue("QUALIFIED-OBJECT", TaskObjectNodeImpl.class,
						"Task Object QUALIFIED-OBJECT is not linked to a Dependent Object");
		this.issues
				.recordIssue("UNQUALIFIED-OBJECT", TaskObjectNodeImpl.class,
						"Task Object UNQUALIFIED-OBJECT is not linked to a Dependent Object");

		// Replay
		this.replayMockObjects();

		// Load the task object dependency not linked
		OfficeSectionType section = this.loadOfficeSectionType(false,
				"SECTION", new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						SectionTask task = context.addTask("WORK", workFactory,
								"TASK", taskFactory, new TaskMaker() {
									@Override
									public void make(TaskTypeMaker maker) {
										maker.addObject("UNQUALIFIED-OBJECT",
												Connection.class, null);
										maker.addObject("QUALIFIED-OBJECT",
												String.class, "QUALIFIED");
									}
								});
						task.getTaskObject("UNQUALIFIED-OBJECT");
						task.getTaskObject("QUALIFIED-OBJECT");
					}
				});

		// Ensure task object is correct
		OfficeTaskType task = section.getOfficeTaskTypes()[0];
		assertEquals("Incorrect number of dependencies", 0,
				task.getObjectDependencies().length);

		// Verify the mocks
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load a {@link OfficeSectionInputType}.
	 */
	public void testLoadOfficeSectionInput() {

		// Load the office section with an office section input
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.getBuilder().addSectionInput("INPUT",
								String.class.getName());
					}
				});

		// Validate results
		assertEquals("Should have office section input", 1,
				section.getOfficeSectionInputTypes().length);
		OfficeSectionInputType input = section.getOfficeSectionInputTypes()[0];
		assertEquals("Incorrect office section input", "INPUT",
				input.getOfficeSectionInputName());
		assertEquals("Incorrect office section input parameter type",
				String.class.getName(), input.getParameterType());
		assertEquals("Should be no office section outputs", 0,
				section.getOfficeSectionOutputTypes().length);
		assertEquals("Should have no office section objects", 0,
				section.getOfficeSectionObjectTypes().length);
	}

	/**
	 * Ensure can load a {@link OfficeSectionOutput}.
	 */
	public void testLoadOfficeSectionOutput() {

		// Load the office section with an office section output
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.getBuilder().addSectionOutput("OUTPUT",
								Exception.class.getName(), true);
					}
				});

		// Validate results
		assertEquals("Should be no office section inputs", 0,
				section.getOfficeSectionInputTypes().length);
		assertEquals("Should have office section output", 1,
				section.getOfficeSectionOutputTypes().length);
		OfficeSectionOutputType output = section.getOfficeSectionOutputTypes()[0];
		assertEquals("Incorrect office section output", "OUTPUT",
				output.getOfficeSectionOutputName());
		assertEquals("Incorrect office section output argument type",
				Exception.class.getName(), output.getArgumentType());
		assertTrue("Incorrect office section output escalation only flag",
				output.isEscalationOnly());
		assertEquals("Should have no office section objects", 0,
				section.getOfficeSectionObjectTypes().length);
	}

	/**
	 * Ensure can add an {@link OfficeSectionObject}.
	 */
	public void testLoadOfficeSectionObject() {

		// Load the office section with an office section object
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						context.getBuilder().addSectionObject("OBJECT",
								Connection.class.getName());
					}
				});

		// Validate results
		assertEquals("Should be no office section inputs", 0,
				section.getOfficeSectionInputTypes().length);
		assertEquals("Should be no office section outputs", 0,
				section.getOfficeSectionOutputTypes().length);
		assertEquals("Should have office section object", 1,
				section.getOfficeSectionObjectTypes().length);
		OfficeSectionObjectType object = section.getOfficeSectionObjectTypes()[0];
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
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
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
				section.getOfficeSectionInputTypes().length);
		assertEquals("Should be no office section outputs", 0,
				section.getOfficeSectionOutputTypes().length);
		assertEquals("Should have office section object", 1,
				section.getOfficeSectionObjectTypes().length);
		OfficeSectionObjectType object = section.getOfficeSectionObjectTypes()[0];
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
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
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
				section.getOfficeSubSectionTypes().length);
		assertEquals("Should have no tasks", 0,
				section.getOfficeTaskTypes().length);
		assertEquals("Should have a section managed object source", 1,
				section.getOfficeSectionManagedObjectSourceTypes().length);
		OfficeSectionManagedObjectSourceType moSource = section
				.getOfficeSectionManagedObjectSourceTypes()[0];
		assertEquals("Incorrect managed object source name", "MO_SOURCE",
				moSource.getOfficeSectionManagedObjectSourceName());
		assertEquals("Should have a section managed object", 1,
				moSource.getOfficeSectionManagedObjectTypes().length);
		OfficeSectionManagedObjectType mo = moSource
				.getOfficeSectionManagedObjectTypes()[0];
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
				.recordIssue(
						"QUALIFIED-DEPENDENCY",
						ManagedObjectDependencyNodeImpl.class,
						"Managed Object Dependency QUALIFIED-DEPENDENCY is not linked to a Dependent Object");
		this.issues.recordIssue("UNQUALIFIED-DEPENDENCY",
				ManagedObjectDependencyNodeImpl.class,
				"Breaks linking chain to a DependentObjectNode");

		// Load the section managed object with a dependency
		this.replayMockObjects();
		OfficeSectionType section = this.loadOfficeSectionType(false,
				"SECTION", new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						SectionManagedObjectSource source = context
								.addManagedObjectSource("MO_SOURCE",
										new ManagedObjectMaker() {
											@Override
											public void make(
													ManagedObjectMakerContext context) {

												// Qualified dependency
												DependencyLabeller qualified = context
														.getContext()
														.addDependency(
																Connection.class);
												qualified
														.setLabel("QUALIFIED-DEPENDENCY");
												qualified
														.setTypeQualifier("QUALIFIED");

												// Unqualified dependency
												context.getContext()
														.addDependency(
																String.class)
														.setLabel(
																"UNQUALIFIED-DEPENDENCY");
											}
										});
						SectionManagedObject mo = source
								.addSectionManagedObject("MO",
										ManagedObjectScope.PROCESS);
						mo.getManagedObjectDependency("QUALIFIED-DEPENDENCY");
						mo.getManagedObjectDependency("UNQUALIFIED-DEPENDENCY");
					}
				});

		// Validate the managed object
		assertEquals("Should have a section managed object source", 1,
				section.getOfficeSectionManagedObjectSourceTypes().length);
		OfficeSectionManagedObjectSourceType moSource = section
				.getOfficeSectionManagedObjectSourceTypes()[0];
		assertEquals("Incorrect managed object source name", "MO_SOURCE",
				moSource.getOfficeSectionManagedObjectSourceName());
		assertEquals("Should have a section managed object", 1,
				moSource.getOfficeSectionManagedObjectTypes().length);
		OfficeSectionManagedObjectType mo = moSource
				.getOfficeSectionManagedObjectTypes()[0];
		assertEquals("Incorrect managed object name", "MO",
				mo.getOfficeSectionManagedObjectName());
		assertEquals("Should have a dependencies", 2,
				mo.getObjectDependencies().length);

		// Validate qualified dependency
		ObjectDependencyType qualified = mo.getObjectDependencies()[0];
		assertEquals("Incorrect qualified dependency name",
				"QUALIFIED-DEPENDENCY", qualified.getObjectDependencyName());
		assertEquals("Incorrect qualified dependency type",
				Connection.class.getName(), qualified.getObjectDependencyType());
		assertEquals("Incorrect qualified dependency type qualifier",
				"QUALIFIED", qualified.getObjectDependencyTypeQualifier());
		assertNull("Qualified dependency should not be linked",
				qualified.getDependentObjectType());

		// Validate unqualified dependency
		ObjectDependencyType unqualified = mo.getObjectDependencies()[1];
		assertEquals("Incorrect unqualified dependency name",
				"UNQUALIFIED-DEPENDENCY", unqualified.getObjectDependencyName());
		assertEquals("Incorrect unqualified dependency type",
				String.class.getName(), unqualified.getObjectDependencyType());
		assertNull("Unqualified dependency should not have type qualifier",
				unqualified.getObjectDependencyTypeQualifier());
		assertNull("Unqualified dependency should not be linked",
				unqualified.getDependentObjectType());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load a {@link OfficeSectionManagedObject} that supports an
	 * extension interface.
	 */
	public void testLoadSectionManagedObjectSupportingAnExtensionInterface() {

		// Load the section managed object supporting an extension interface
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
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
				section.getOfficeSectionManagedObjectSourceTypes().length);
		OfficeSectionManagedObjectSourceType moSource = section
				.getOfficeSectionManagedObjectSourceTypes()[0];
		assertEquals("Incorrect managed object source name", "MO_SOURCE",
				moSource.getOfficeSectionManagedObjectSourceName());
		assertEquals("Should have a section managed object", 1,
				moSource.getOfficeSectionManagedObjectTypes().length);
		OfficeSectionManagedObjectType mo = moSource
				.getOfficeSectionManagedObjectTypes()[0];
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
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {

						// Add the task object and managed object
						TaskObject object = context.addTaskObject("WORK",
								workFactory, "TASK", taskFactory, "OBJECT",
								Connection.class, null);
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
		OfficeTaskType task = section.getOfficeTaskTypes()[0];
		assertEquals("Incorrect number of dependencies", 1,
				task.getObjectDependencies().length);
		ObjectDependencyType dependency = task.getObjectDependencies()[0];
		assertEquals("Incorrect object dependency", "OBJECT",
				dependency.getObjectDependencyName());
		DependentObjectType mo = dependency.getDependentObjectType();
		assertEquals("Incorrect dependent managed object", "MO",
				mo.getDependentObjectName());

		// Validate the default type qualification (from managed object type)
		assertEquals("Incorrect number of type qualifications", 1,
				mo.getTypeQualifications().length);
		TypeQualification qualification = mo.getTypeQualifications()[0];
		assertEquals("Incorrect dependent type", Object.class.getName(),
				qualification.getType());
		assertNull("Dependent should not be qualified for default auto-wire",
				qualification.getQualifier());
	}

	/**
	 * Ensure can get {@link DependentManagedObject} provides qualification.
	 */
	public void testTaskDependentOnQualifiedSectionManagedObject() {

		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		// Load the task object dependent on managed object of same section
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {

						// Add the task object and managed object
						TaskObject object = context.addTaskObject("WORK",
								workFactory, "TASK", taskFactory, "OBJECT",
								Connection.class, null);
						SectionManagedObjectSource moSource = context
								.addManagedObjectSource("MO_SOURCE", null);
						SectionManagedObject managedObject = moSource
								.addSectionManagedObject("MO",
										ManagedObjectScope.THREAD);

						// Add type qualification (allows distinguishing)
						managedObject.addTypeQualification("QUALIFIED",
								Integer.class.getName());
						managedObject.addTypeQualification(null,
								String.class.getName());

						// Link task object to managed object
						context.getBuilder().link(object, managedObject);
					}
				});

		// Validate link to dependent managed object
		OfficeTaskType task = section.getOfficeTaskTypes()[0];
		assertEquals("Incorrect number of dependencies", 1,
				task.getObjectDependencies().length);
		ObjectDependencyType dependency = task.getObjectDependencies()[0];
		assertEquals("Incorrect object dependency", "OBJECT",
				dependency.getObjectDependencyName());
		DependentObjectType mo = dependency.getDependentObjectType();
		assertEquals("Incorrect dependent managed object", "MO",
				mo.getDependentObjectName());
		assertTrue("Incorrect managed object type",
				mo instanceof OfficeSectionManagedObjectType);

		// Validate the specifying auto-wiring
		assertEquals("Incorrect number of type qualifiers", 2,
				mo.getTypeQualifications().length);
		TypeQualification one = mo.getTypeQualifications()[0];
		assertEquals("Incorrect first qualified type", Integer.class.getName(),
				one.getType());
		assertEquals("Incorrect first type qualifier", "QUALIFIED",
				one.getQualifier());
		TypeQualification two = mo.getTypeQualifications()[1];
		assertEquals("Incorrect second qualified type", String.class.getName(),
				two.getType());
		assertNull("Second type should not be qualified", two.getQualifier());
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
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
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
														Connection.class, null);

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
		OfficeTaskType task = section.getOfficeSubSectionTypes()[0]
				.getOfficeTaskTypes()[0];
		assertEquals("Incorrect number of dependencies", 1,
				task.getObjectDependencies().length);
		ObjectDependencyType dependency = task.getObjectDependencies()[0];
		assertEquals("Incorrect object dependency", "OBJECT",
				dependency.getObjectDependencyName());
		DependentObjectType mo = dependency.getDependentObjectType();
		assertEquals("Incorrect dependent managed object", "MO",
				mo.getDependentObjectName());
	}

	/**
	 * Ensure can get {@link DependentManagedObject} for a
	 * {@link ManagedObjectDependency}.
	 */
	public void testManagedObjectDependentOnAnotherManagedObject() {

		// Load the section managed object with a dependency
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
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
				section.getOfficeSectionManagedObjectSourceTypes().length);

		// Obtain appropriate sources
		OfficeSectionManagedObjectSourceType moSourceOne = section
				.getOfficeSectionManagedObjectSourceTypes()[0];
		OfficeSectionManagedObjectSourceType moSourceTwo = section
				.getOfficeSectionManagedObjectSourceTypes()[1];
		if (!("MO_SOURCE_ONE".equals(moSourceOne
				.getOfficeSectionManagedObjectSourceName()))) {
			// Wrong way round, so swap
			OfficeSectionManagedObjectSourceType tmp = moSourceOne;
			moSourceOne = moSourceTwo;
			moSourceTwo = tmp;
		}

		// Validate managed object one
		assertEquals("Incorrect managed object source name", "MO_SOURCE_ONE",
				moSourceOne.getOfficeSectionManagedObjectSourceName());
		assertEquals("MO_SOURCE_ONE should have a section managed object", 1,
				moSourceOne.getOfficeSectionManagedObjectTypes().length);
		OfficeSectionManagedObjectType moOne = moSourceOne
				.getOfficeSectionManagedObjectTypes()[0];
		assertEquals("Incorrect managed object name", "MO_ONE",
				moOne.getOfficeSectionManagedObjectName());
		assertEquals("MO_ONE should have a dependency", 1,
				moOne.getObjectDependencies().length);
		ObjectDependencyType dependency = moOne.getObjectDependencies()[0];
		assertEquals("Incorrect dependency name", "DEPENDENCY",
				dependency.getObjectDependencyName());
		assertEquals("Incorrect dependency type", Connection.class.getName(),
				dependency.getObjectDependencyType());
		assertNotNull("Dependency should be linked",
				dependency.getDependentObjectType());

		// Validate managed object two
		assertEquals("Incorrect managed object source name", "MO_SOURCE_TWO",
				moSourceTwo.getOfficeSectionManagedObjectSourceName());
		assertEquals("MO_SOURCE_TWO should have a section managed object", 1,
				moSourceTwo.getOfficeSectionManagedObjectTypes().length);
		OfficeSectionManagedObjectType moTwo = moSourceTwo
				.getOfficeSectionManagedObjectTypes()[0];
		assertEquals("Incorrect managed object name", "MO_TWO",
				moTwo.getOfficeSectionManagedObjectName());
		assertEquals("MO_TWO should not have a dependency", 0,
				moTwo.getObjectDependencies().length);

		// Ensure dependency is linked to correct managed object
		assertEquals("Incorrect dependent object",
				dependency.getDependentObjectType(), moTwo);
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
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {

						// Add the task object
						TaskObject taskObject = context.addTaskObject("WORK",
								workFactory, "TASK", taskFactory, "OBJECT",
								Connection.class, null);

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
		OfficeTaskType task = section.getOfficeTaskTypes()[0];
		assertEquals("Incorrect number of task object dependencies", 1,
				task.getObjectDependencies().length);
		ObjectDependencyType taskDependency = task.getObjectDependencies()[0];
		assertEquals("Incorrect task object dependency", "OBJECT",
				taskDependency.getObjectDependencyName());
		assertEquals("Incorrect task object dependency type",
				Connection.class.getName(),
				taskDependency.getObjectDependencyType());
		DependentObjectType mo = taskDependency.getDependentObjectType();
		assertEquals("Incorrect task dependent managed object", "MO_ONE",
				mo.getDependentObjectName());
		assertEquals("Incorrect number of managed object dependencies", 1,
				mo.getObjectDependencies().length);
		ObjectDependencyType moDependency = mo.getObjectDependencies()[0];
		assertEquals("Incorrect managed object dependency", "DEPENDENCY",
				moDependency.getObjectDependencyName());
		assertEquals("Incorrect managed object dependency type",
				DataSource.class.getName(),
				moDependency.getObjectDependencyType());
		DependentObjectType dependentMo = moDependency.getDependentObjectType();
		assertEquals("Incorrect managed object dependent managed object",
				"MO_TWO", dependentMo.getDependentObjectName());
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
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						// Add the task object as parameter
						TaskObject taskObject = context.addTaskObject("WORK",
								workFactory, "TASK", taskFactory, "OBJECT",
								Connection.class, null);
						taskObject.flagAsParameter();
					}
				});

		// Validate no dependent object
		OfficeTaskType task = section.getOfficeTaskTypes()[0];
		assertEquals("Incorrect number of task object dependencies", 1,
				task.getObjectDependencies().length);
		ObjectDependencyType taskDependency = task.getObjectDependencies()[0];
		assertEquals("Incorrect task object dependency", "OBJECT",
				taskDependency.getObjectDependencyName());
		assertEquals("Incorrect task object dependency type",
				Connection.class.getName(),
				taskDependency.getObjectDependencyType());
		assertTrue("Should be a paramaeter", taskDependency.isParameter());

		// Ensure no dependent managed object for parameter
		assertNull("Should be no dependent for task parameter",
				taskDependency.getDependentObjectType());
	}

	/**
	 * Ensure {@link TaskObject} not flagged as parameter but issue as not
	 * linked.
	 */
	public void testTaskObjectNotParameterAndNotLinked() {

		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		// Issue as not linked
		this.issues.recordIssue("OBJECT", TaskObjectNodeImpl.class,
				"Task Object OBJECT is not linked to a Dependent Object");

		// Load the task object dependent on section object
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						// Add the task object as parameter
						context.addTaskObject("WORK", workFactory, "TASK",
								taskFactory, "OBJECT", Connection.class, null);
					}
				});

		// Validate dependent object
		OfficeTaskType task = section.getOfficeTaskTypes()[0];
		assertEquals("Incorrect number of task object dependencies", 0,
				task.getObjectDependencies().length);
	}

	/**
	 * Ensure {@link TaskObject} not flagged as parameter.
	 */
	public void testTaskObjectNotParameterLinked() {

		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		// Load the task object dependent on section object
		OfficeSectionType section = this.loadOfficeSectionType("SECTION",
				new SectionMaker() {
					@Override
					public void make(SectionMakerContext context) {
						// Add the task object as parameter
						TaskObject taskObject = context.addTaskObject("WORK",
								workFactory, "TASK", taskFactory, "OBJECT",
								Connection.class, null);
						SectionObject sectionObject = context.getBuilder()
								.addSectionObject("SECTION_OBJECT",
										Connection.class.getName());
						sectionObject.setTypeQualifier("QUALIFIER");
						context.getBuilder().link(taskObject, sectionObject);
					}
				});

		// Validate dependent object
		OfficeTaskType task = section.getOfficeTaskTypes()[0];
		assertEquals("Incorrect number of task object dependencies", 1,
				task.getObjectDependencies().length);
		ObjectDependencyType taskDependency = task.getObjectDependencies()[0];
		assertEquals("Incorrect task object dependency", "OBJECT",
				taskDependency.getObjectDependencyName());
		assertEquals("Incorrect task object dependency type", Connection.class,
				taskDependency.getObjectDependencyType());
		assertFalse("Should not be a paramaeter", taskDependency.isParameter());

		// Ensure no dependent managed object for parameter
		DependentObjectType dependentObject = taskDependency
				.getDependentObjectType();
		assertNotNull("Should have dependent for object", dependentObject);
		assertEquals("Incorrect dependent object name", "SECTION_OBJECT",
				dependentObject.getDependentObjectName());
		assertEquals("Should not have dependent object dependencies", 0,
				dependentObject.getObjectDependencies().length);
		TypeQualification[] typeQualifications = dependentObject
				.getTypeQualifications();
		assertEquals("Incorrect number of dependent type qualifications", 1,
				typeQualifications.length);
		assertEquals("Incorrect dependent type", Connection.class.getName(),
				typeQualifications[0].getType());
		assertEquals("Incorrect dependent qualification", "QUALIFIER",
				typeQualifications[0].getQualifier());
	}

}