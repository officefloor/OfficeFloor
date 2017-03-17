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
package net.officefloor.compile.impl.office;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

import javax.transaction.xa.XAResource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.impl.administrator.MockLoadAdministrator;
import net.officefloor.compile.impl.governance.MockLoadGovernance;
import net.officefloor.compile.impl.managedobject.MockLoadManagedObject;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.AbstractStructureTestCase;
import net.officefloor.compile.impl.structure.OfficeInputNodeImpl;
import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.impl.structure.OfficeObjectNodeImpl;
import net.officefloor.compile.impl.structure.OfficeOutputNodeImpl;
import net.officefloor.compile.impl.structure.OfficeTeamNodeImpl;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeInput;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeOutput;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.OfficeSourceSpecification;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.administrator.clazz.ClassAdministrationSource;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Tests loading the {@link OfficeType}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadOfficeTypeTest extends AbstractStructureTestCase {

	/**
	 * Location of the {@link Office}.
	 */
	private final String OFFICE_LOCATION = "OFFICE_LOCATION";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		MockOfficeSource.reset();
	}

	/**
	 * Ensure able to load by {@link OfficeSource} {@link Class}.
	 */
	public void testLoadByClass() {

		// Configure test
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());

		// Configure to load simple class
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(MockLoadOfficeSource.PROPERTY_REQUIRED)
				.setValue("available");

		// Load the office type
		OfficeLoader officeLoader = compiler.getOfficeLoader();
		OfficeType officeType = officeLoader.loadOfficeType(
				MockLoadOfficeSource.class, "LOCATION", properties);
		MockLoadOfficeSource.assertOfficeType(officeType);
	}

	/**
	 * Ensure able to load by {@link OfficeSource} instances.
	 */
	public void testLoadByInstance() {

		// Configure test
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());

		// Configure to load simple class
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(MockLoadOfficeSource.PROPERTY_REQUIRED)
				.setValue("available");

		// Load the office type
		OfficeLoader officeLoader = compiler.getOfficeLoader();
		OfficeType officeType = officeLoader.loadOfficeType(
				new MockLoadOfficeSource(), "LOCATION", properties);
		MockLoadOfficeSource.assertOfficeType(officeType);
	}

	/**
	 * Ensure issue if fail to instantiate the {@link OfficeSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue("Failed to instantiate "
				+ MockOfficeSource.class.getName() + " by default constructor",
				failure);

		// Attempt to obtain specification
		MockOfficeSource.instantiateFailure = failure;
		this.loadOfficeType(false, null);
	}

	/**
	 * Ensure obtain the correct {@link Office} location.
	 */
	public void testOfficeLocation() {
		this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				assertEquals("Incorrect office location", OFFICE_LOCATION,
						context.getOfficeLocation());
			}
		});
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues.recordIssue("Type", OfficeNodeImpl.class,
				"Missing property 'missing' for OfficeSource "
						+ MockOfficeSource.class.getName());

		// Attempt to load office type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				context.getProperty("missing");
			}
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Attempt to load office type
		this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				assertEquals("Ensure get defaulted property", "DEFAULT",
						context.getProperty("missing", "DEFAULT"));
				assertEquals("Ensure get property ONE", "1",
						context.getProperty("ONE"));
				assertEquals("Ensure get property TWO", "2",
						context.getProperty("TWO"));
				String[] names = context.getPropertyNames();
				assertEquals("Incorrect number of property names", 2,
						names.length);
				assertEquals("Incorrect property name 0", "ONE", names[0]);
				assertEquals("Incorrect property name 1", "TWO", names[1]);
				Properties properties = context.getProperties();
				assertEquals("Incorrect number of properties", 2,
						properties.size());
				assertEquals("Incorrect property ONE", "1",
						properties.get("ONE"));
				assertEquals("Incorrect property TWO", "2",
						properties.get("TWO"));
			}
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.issues.recordIssue("Type", OfficeNodeImpl.class,
				"Can not load class 'missing' for OfficeSource "
						+ MockOfficeSource.class.getName());

		// Attempt to load office type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				context.loadClass("missing");
			}
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing resource
		this.recordReturn(this.resourceSource,
				this.resourceSource.sourceResource("missing"), null);
		this.issues.recordIssue("Type", OfficeNodeImpl.class,
				"Can not obtain resource at location 'missing' for OfficeSource "
						+ MockOfficeSource.class.getName());

		// Attempt to load office type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				context.getResource("missing");
			}
		});
	}

	/**
	 * Ensure able to obtain a resource.
	 */
	public void testGetResource() throws Exception {

		final String location = "LOCATION";
		final InputStream resource = new ByteArrayInputStream(new byte[0]);

		// Record obtaining the resource
		this.recordReturn(this.resourceSource,
				this.resourceSource.sourceResource(location), resource);

		// Obtain the configuration item
		this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				assertSame("Incorrect resource", resource,
						context.getResource(location));
			}
		});
	}

	/**
	 * Ensure able to get the {@link ClassLoader}.
	 */
	public void testGetClassLoader() {

		// Attempt to load office type
		this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				assertEquals("Incorrect class loader",
						LoadOfficeTypeTest.class.getClassLoader(),
						context.getClassLoader());
			}
		});
	}

	/**
	 * Ensure issue if fails to source the {@link OfficeType}.
	 */
	public void testFailSourceOfficeType() {

		final NullPointerException failure = new NullPointerException(
				"Fail source office type");

		// Record failure to source the office type
		this.issues.recordIssue("Type", OfficeNodeImpl.class,
				"Failed to source OfficeType definition from OfficeSource "
						+ MockOfficeSource.class.getName(), failure);

		// Attempt to load office type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				throw failure;
			}
		});
	}

	/**
	 * Ensure obtain the {@link OfficeInputType}.
	 */
	public void testInputType_Asynchronous() {

		// Load the type
		OfficeType type = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				office.addOfficeInput("INPUT", Integer.class.getName());
			}
		});

		// Validate the type
		OfficeInputType[] inputs = type.getOfficeInputTypes();
		assertEquals("Incorrect number of inputs", 1, inputs.length);
		OfficeInputType input = inputs[0];
		assertEquals("Incorrect input name", "INPUT",
				input.getOfficeInputName());
		assertEquals("Incorrect input parameter type", Integer.class.getName(),
				input.getParameterType());
		assertNull("Should be no corresponding output",
				input.getResponseOfficeOutputType());
	}

	/**
	 * Ensure issue if duplicate name for {@link OfficeInput} instances.
	 */
	public void testInputType_DuplicateName() {
		this.issues.recordIssue("TEST", OfficeInputNodeImpl.class,
				"Office Input TEST already added");

		// Load the type
		OfficeType type = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				OfficeInput inputOne = office.addOfficeInput("TEST",
						String.class.getName());
				OfficeInput inputTwo = office.addOfficeInput("TEST",
						Integer.class.getName());
				assertSame("Should be the same object", inputOne, inputTwo);
			}
		});

		// Validate the type
		OfficeInputType[] inputs = type.getOfficeInputTypes();
		assertEquals("Incorrect number of inputs", 1, inputs.length);
		OfficeInputType input = inputs[0];
		assertEquals("Incorrect input name", "TEST", input.getOfficeInputName());
	}

	/**
	 * Ensure obtain the {@link OfficeOutputType}.
	 */
	public void testOutputType_Asynchronous() {

		// Load the type
		OfficeType type = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				office.addOfficeOutput("OUTPUT", Long.class.getName());
			}
		});

		// Validate the type
		OfficeOutputType[] outputs = type.getOfficeOutputTypes();
		assertEquals("Incorrect number of outputs", 1, outputs.length);
		OfficeOutputType output = outputs[0];
		assertEquals("Incorrect output name", "OUTPUT",
				output.getOfficeOutputName());
		assertEquals("Incorrect output argument type", Long.class.getName(),
				output.getArgumentType());
		assertNull("Should be no corresponding input",
				output.getHandlingOfficeInputType());
	}

	/**
	 * Ensure issue if duplicate name for {@link OfficeOutput} instances.
	 */
	public void testOutputType_DuplicateName() {
		this.issues.recordIssue("TEST", OfficeOutputNodeImpl.class,
				"Office Output TEST already added");

		// Load the type
		OfficeType type = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				OfficeOutput outputOne = office.addOfficeOutput("TEST",
						String.class.getName());
				OfficeOutput outputTwo = office.addOfficeOutput("TEST",
						Integer.class.getName());
				assertSame("Should be the same object", outputOne, outputTwo);
			}
		});

		// Validate the type
		OfficeOutputType[] outputs = type.getOfficeOutputTypes();
		assertEquals("Incorrect number of outputs", 1, outputs.length);
		OfficeOutputType output = outputs[0];
		assertEquals("Incorrect output name", "TEST",
				output.getOfficeOutputName());
	}

	/**
	 * Ensure obtain synchronous {@link OfficeOutputType}.
	 */
	public void testInputOutput_Synchronous() {

		// Load the type
		OfficeType type = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				OfficeInput input = office.addOfficeInput("INPUT",
						Integer.class.getName());
				OfficeOutput output = office.addOfficeOutput("OUTPUT",
						Long.class.getName());
				office.link(input, output);
			}
		});

		// Validate synchronous input
		OfficeInputType[] inputs = type.getOfficeInputTypes();
		assertEquals("Incorrect number of inputs", 1, inputs.length);
		OfficeInputType input = inputs[0];
		assertEquals("Incorrect input name", "INPUT",
				input.getOfficeInputName());
		OfficeOutputType output = input.getResponseOfficeOutputType();
		assertNotNull("Should have response output", output);
		assertEquals("Incorrect output name", "OUTPUT",
				output.getOfficeOutputName());

		// Ensure synchronous output not included in outputs
		assertEquals("Should be no outputs", 0,
				type.getOfficeOutputTypes().length);
	}

	/**
	 * Ensure obtain synchronous {@link OfficeInputType}.
	 */
	public void testOutputInput_Synchronous() {

		// Load the type
		OfficeType type = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				OfficeOutput output = office.addOfficeOutput("OUTPUT",
						Long.class.getName());
				OfficeInput input = office.addOfficeInput("INPUT",
						Integer.class.getName());
				office.link(output, input);
			}
		});

		// Validate synchronous output
		OfficeOutputType[] outputs = type.getOfficeOutputTypes();
		assertEquals("Incorrect number of outputs", 1, outputs.length);
		OfficeOutputType output = outputs[0];
		assertEquals("Incorrect output name", "OUTPUT",
				output.getOfficeOutputName());
		OfficeInputType input = output.getHandlingOfficeInputType();
		assertNotNull("Should have handling input", input);
		assertEquals("Incorrect input name", "INPUT",
				input.getOfficeInputName());

		// Ensure synchronous input not included in inputs
		assertEquals("Should be no inputs", 0,
				type.getOfficeInputTypes().length);
	}

	/**
	 * Ensure issue if <code>null</code> {@link OfficeManagedObjectType} name.
	 */
	public void testNullManagedObjectName() {

		// Record null managed object name
		this.issues.recordIssue(null, OfficeObjectNodeImpl.class,
				"Null name for Office Object");

		// Attempt to load office type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				office.addOfficeObject(null, Connection.class.getName());
			}
		});
	}

	/**
	 * Ensure issue if <code>null</code> {@link OfficeManagedObjectType}
	 * required type.
	 */
	public void testNullManagedObjectType() {

		// Record null required type
		this.issues.recordIssue("MO", OfficeObjectNodeImpl.class,
				"Null type for managed object (name=MO)");

		// Attempt to load office type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				office.addOfficeObject("MO", null);
			}
		});
	}

	/**
	 * Ensure get {@link OfficeManagedObjectType} not being administered (no
	 * extension interfaces).
	 */
	public void testManagedObjectType() {

		// Load office type with office floor managed object
		OfficeType officeType = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {

				final String MANAGED_OBJECT_NAME = "MO";

				// Add the office object
				OfficeObject officeObject = office.addOfficeObject(
						MANAGED_OBJECT_NAME, Connection.class.getName());

				// Ensure the office object is correct
				assertEquals("Incorrect office object name",
						MANAGED_OBJECT_NAME, officeObject.getOfficeObjectName());
				assertEquals("Incorrect dependent name", MANAGED_OBJECT_NAME,
						officeObject.getDependentManagedObjectName());
				assertEquals("Incorrect administerable name",
						MANAGED_OBJECT_NAME,
						officeObject.getAdministerableManagedObjectName());
				assertEquals("Incorrect governerable name",
						MANAGED_OBJECT_NAME,
						officeObject.getGovernerableManagedObjectName());
			}
		});

		// Validate type
		assertEquals("Incorrect number of managed object types", 1,
				officeType.getOfficeManagedObjectTypes().length);
		OfficeManagedObjectType moType = officeType
				.getOfficeManagedObjectTypes()[0];
		assertEquals("Incorrect name", "MO",
				moType.getOfficeManagedObjectName());
		assertEquals("Incorrect type", Connection.class.getName(),
				moType.getObjectType());
		assertNull("Should not be qualified", moType.getTypeQualifier());
		assertEquals("Should be no required extension interfaces", 0,
				moType.getExtensionInterfaces().length);
	}

	/**
	 * Ensure get qualified {@link OfficeManagedObjectType} not being
	 * administered (no extension interfaces).
	 */
	public void testQualifiedManagedObjectType() {

		// Load office type with office floor managed object
		OfficeType officeType = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {

				// Add the office object
				OfficeObject officeObject = office.addOfficeObject("MO",
						Connection.class.getName());
				officeObject.setTypeQualifier("QUALIFIED");
			}
		});

		// Validate type
		assertEquals("Incorrect number of managed object types", 1,
				officeType.getOfficeManagedObjectTypes().length);
		OfficeManagedObjectType moType = officeType
				.getOfficeManagedObjectTypes()[0];
		assertEquals("Incorrect name", "MO",
				moType.getOfficeManagedObjectName());
		assertEquals("Incorrect type", Connection.class.getName(),
				moType.getObjectType());
		assertEquals("Incorrect type qualifier", "QUALIFIED",
				moType.getTypeQualifier());
	}

	/**
	 * Ensure get {@link OfficeManagedObjectType} being administered (has
	 * extension interfaces).
	 */
	public void testAdministeredManagedObjectType() {
		// Load office type with administered office floor managed object
		OfficeType officeType = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				OfficeObject mo = office.addOfficeObject("MO",
						Connection.class.getName());
				OfficeAdministration admin = LoadOfficeTypeTest.this
						.addAdministrator(office, "ADMIN", XAResource.class,
								SimpleDutyKey.DUTY);
				admin.administerManagedObject(mo);
			}
		});

		// Validate type
		assertEquals("Incorrect number of managed object types", 1,
				officeType.getOfficeManagedObjectTypes().length);
		OfficeManagedObjectType moType = officeType
				.getOfficeManagedObjectTypes()[0];
		assertEquals("Incorrect name", "MO",
				moType.getOfficeManagedObjectName());
		assertEquals("Incorrect type", Connection.class.getName(),
				moType.getObjectType());
		assertEquals("Incorrect number of extension interfaces", 1,
				moType.getExtensionInterfaces().length);
		String extensionInterface = moType.getExtensionInterfaces()[0];
		assertEquals("Incorrect extension interface",
				XAResource.class.getName(), extensionInterface);
	}

	/**
	 * Ensure get {@link OfficeManagedObjectType} being governed (has extension
	 * interfaces).
	 */
	public void testGovernedManagedObjectType() {
		// Load office type with governed office floor managed object
		OfficeType officeType = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				OfficeObject mo = office.addOfficeObject("MO",
						Connection.class.getName());
				OfficeGovernance governance = LoadOfficeTypeTest.this
						.addGovernance(office, "GOVERNANCE",
								new GovernanceMaker() {
									@Override
									public void make(
											GovernanceMakerContext context) {
										context.setExtensionInterface(XAResource.class);
									}
								});
				governance.governManagedObject(mo);
			}
		});

		// Validate type
		assertEquals("Incorrect number of managed object types", 1,
				officeType.getOfficeManagedObjectTypes().length);
		OfficeManagedObjectType moType = officeType
				.getOfficeManagedObjectTypes()[0];
		assertEquals("Incorrect name", "MO",
				moType.getOfficeManagedObjectName());
		assertEquals("Incorrect type", Connection.class.getName(),
				moType.getObjectType());
		assertEquals("Incorrect number of extension interfaces", 1,
				moType.getExtensionInterfaces().length);
		String extensionInterface = moType.getExtensionInterfaces()[0];
		assertEquals("Incorrect extension interface",
				XAResource.class.getName(), extensionInterface);
	}

	/**
	 * Ensure issue if <code>null</code> {@link OfficeTeamType} name.
	 */
	public void testNullTeamName() {

		// Record null required type
		this.issues.recordIssue(null, OfficeTeamNodeImpl.class,
				"Null name for Office Team");

		// Attempt to load office type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				office.addOfficeTeam(null);
			}
		});
	}

	/**
	 * Ensure obtain the {@link OfficeTeamType}.
	 */
	public void testTeamType() {

		// Load office type
		OfficeType officeType = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				office.addOfficeTeam("TEAM");
			}
		});

		// Validate type
		assertEquals("Incorrect number of teams", 1,
				officeType.getOfficeTeamTypes().length);
		OfficeTeamType team = officeType.getOfficeTeamTypes()[0];
		assertEquals("Incorrect team name", "TEAM", team.getOfficeTeamName());
	}

	/**
	 * Ensure obtain the {@link OfficeAvailableSectionInputType}.
	 */
	public void testSectionInputType() {

		// Load office type
		OfficeType officeType = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {
				// Add section with an input
				LoadOfficeTypeTest.this.addSection(office, "SECTION",
						new SectionMaker() {
							@Override
							public void make(SectionMakerContext context) {
								context.getBuilder().addSectionInput("INPUT",
										String.class.getName());
							}
						});
			}
		});

		// Validate type
		assertEquals("Incorrect number of section inputs", 0,
				officeType.getOfficeInputTypes().length);
		assertEquals("Incorrect number of available section inputs", 1,
				officeType.getOfficeSectionInputTypes().length);
		OfficeAvailableSectionInputType input = officeType
				.getOfficeSectionInputTypes()[0];
		assertEquals("Incorrect section name", "SECTION",
				input.getOfficeSectionName());
		assertEquals("Incorrect input name", "INPUT",
				input.getOfficeSectionInputName());
		assertEquals("Incorrect parameter type", String.class.getName(),
				input.getParameterType());
	}

	/**
	 * Ensure can obtain the {@link ManagedObjectType}.
	 */
	public void testLoadManagedObjectType() {

		// Record capture issues for loading managed object type
		this.issues.recordCaptureIssues(false);

		// Load the type
		this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect architect,
					OfficeSourceContext context) throws Exception {

				// Load the managed object type
				PropertyList properties = context.createPropertyList();
				properties.addProperty(
						ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
						.setValue(MockLoadManagedObject.class.getName());
				ManagedObjectType<?> managedObjectType = context
						.loadManagedObjectType(
								ClassManagedObjectSource.class.getName(),
								properties);

				// Ensure correct managed object type
				MockLoadManagedObject
						.assertManagedObjectType(managedObjectType);
			}
		});
	}

	/**
	 * Ensure issue if fails to load the {@link ManagedObjectType}.
	 */
	public void testFailLoadingManagedObjectType() {

		// Ensure issue in not loading managed object type
		CompilerIssue[] causes = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue("Type", OfficeNodeImpl.class,
				"Missing property 'class.name'");
		this.issues.recordIssue("Type", OfficeNodeImpl.class,
				"Failure loading ManagedObjectType from source "
						+ ClassManagedObjectSource.class.getName(), causes);

		// Fail to load the managed object type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect architect,
					OfficeSourceContext context) throws Exception {

				// Do not specify class causing failure to load type
				PropertyList properties = context.createPropertyList();
				context.loadManagedObjectType(
						ClassManagedObjectSource.class.getName(), properties);

				// Should not reach this point
				fail("Should not successfully load managed object type");
			}
		});
	}

	/**
	 * Ensure can obtain the {@link GovernanceType}.
	 */
	public void testLoadGovernanceType() {

		// Record capture of issues for governance type
		this.issues.recordCaptureIssues(false);

		// Load the office type
		this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect architect,
					OfficeSourceContext context) throws Exception {

				// Load the governance type
				PropertyList properties = context.createPropertyList();
				properties.addProperty(
						ClassGovernanceSource.CLASS_NAME_PROPERTY_NAME)
						.setValue(MockLoadGovernance.class.getName());
				GovernanceType<?, ?> governanceType = context
						.loadGovernanceType(
								ClassGovernanceSource.class.getName(),
								properties);

				// Ensure correct governance type
				MockLoadGovernance.assertGovernanceType(governanceType);
			}
		});
	}

	/**
	 * Ensure issue if fails to load the {@link GovernanceType}.
	 */
	public void testFailLoadingGovernanceType() {

		// Ensure issue in not loading governance type
		CompilerIssue[] causes = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue("Type", OfficeNodeImpl.class,
				"Property 'class.name' must be specified");
		this.issues.recordIssue("Type", OfficeNodeImpl.class,
				"Failure loading GovernanceType from source "
						+ ClassGovernanceSource.class.getName(), causes);

		// Fail to load the governance type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect architect,
					OfficeSourceContext context) throws Exception {

				// Do not specify class causing failure to load type
				PropertyList properties = context.createPropertyList();
				context.loadGovernanceType(
						ClassGovernanceSource.class.getName(), properties);

				// Should not reach this point
				fail("Should not successfully load governance type");
			}
		});
	}

	/**
	 * Ensure can obtain the {@link AdministrationType}.
	 */
	public void testLoadAdministratorType() {

		// Record capture issues for loading administrator type
		this.issues.recordCaptureIssues(false);

		// Load Office type
		this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect architect,
					OfficeSourceContext context) throws Exception {

				// Load the administrator type
				PropertyList properties = context.createPropertyList();
				properties.addProperty(
						ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME)
						.setValue(MockLoadAdministrator.class.getName());
				AdministrationType<?, ?> administratorType = context
						.loadAdministratorType(
								ClassAdministrationSource.class.getName(),
								properties);

				// Ensure correct administrator type
				MockLoadAdministrator
						.assertAdministratorType(administratorType);
			}
		});
	}

	/**
	 * Ensure issue if fails to load the {@link AdministrationType}.
	 */
	public void testFailLoadingAdministratorType() {

		// Ensure issue in not loading administrator type
		CompilerIssue[] causes = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue("Type", OfficeNodeImpl.class,
				"Missing property 'class.name'");
		this.issues.recordIssue("Type", OfficeNodeImpl.class,
				"Failure loading AdministratorType from source "
						+ ClassAdministrationSource.class.getName(), causes);

		// Fail to load the administrator type
		this.loadOfficeType(false, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect architect,
					OfficeSourceContext context) throws Exception {

				// Do not specify class causing failure to load type
				PropertyList properties = context.createPropertyList();
				context.loadAdministratorType(
						ClassAdministrationSource.class.getName(), properties);

				// Should not reach this point
				fail("Should not successfully load administrator type");
			}
		});
	}

	/**
	 * {@link OfficeManagedObject} should not show up in {@link OfficeType}.
	 */
	public void testLinkedOfficeSectionManagedObject() {

		// Record capture issues for class loader to load type
		this.issues.recordCaptureIssues(false);

		// Load office type with office managed object
		OfficeType officeType = this.loadOfficeType(true, new Loader() {
			@Override
			public void sourceOffice(OfficeArchitect office,
					OfficeSourceContext context) throws Exception {

				// Add the office managed object
				OfficeManagedObjectSource source = office
						.addOfficeManagedObjectSource("MOS",
								ClassManagedObjectSource.class.getName());
				source.addProperty(
						ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
						MockOfficeManagedObject.class.getName());
				OfficeManagedObject mo = source.addOfficeManagedObject("MO",
						ManagedObjectScope.PROCESS);

				// Add section
				OfficeSection section = office.addOfficeSection("SECTION",
						ClassSectionSource.class.getName(),
						MockOfficeSection.class.getName());

				// Link section object to office managed object
				OfficeSectionObject object = section
						.getOfficeSectionObject(MockOfficeManagedObject.class
								.getName());
				office.link(object, mo);
			}
		});

		// Validate type (not shows office managed object)
		assertEquals("Incorrect number of managed object types", 0,
				officeType.getOfficeManagedObjectTypes().length);
	}

	/**
	 * Mock {@link OfficeManagedObject} for {@link ClassManagedObjectSource}.
	 */
	public static class MockOfficeManagedObject {
	}

	public static class MockOfficeSection {

		public void task(MockOfficeManagedObject mo) {
		}
	}

	/**
	 * Loads the {@link OfficeType} within the input {@link Loader}.
	 * 
	 * @param isExpectedToLoad
	 *            Flag indicating if expecting to load the {@link OfficeType}.
	 * @param loader
	 *            {@link Loader}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 * @return Loaded {@link OfficeType}.
	 */
	private OfficeType loadOfficeType(boolean isExpectedToLoad, Loader loader,
			String... propertyNameValuePairs) {

		// Replay mock objects
		this.replayMockObjects();

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Create the office loader and load the office
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		compiler.addResources(this.resourceSource);
		OfficeLoader officeLoader = compiler.getOfficeLoader();
		MockOfficeSource.loader = loader;
		OfficeType officeType = officeLoader.loadOfficeType(
				MockOfficeSource.class, OFFICE_LOCATION, propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the office type", officeType);
		} else {
			assertNull("Should not load the office type", officeType);
		}

		// Return the office type
		return officeType;
	}

	/**
	 * Implemented to load the {@link OfficeType}.
	 */
	private interface Loader {

		/**
		 * Implemented to load the {@link OfficeType}.
		 * 
		 * @param office
		 *            {@link OfficeArchitect}.
		 * @param context
		 *            {@link OfficeSourceContext}.
		 * @throws Exception
		 *             If fails to source {@link OfficeType}.
		 */
		void sourceOffice(OfficeArchitect office, OfficeSourceContext context)
				throws Exception;
	}

	/**
	 * Mock {@link OfficeSource} for testing.
	 */
	@TestSource
	public static class MockOfficeSource implements OfficeSource {

		/**
		 * {@link Loader} to load the {@link OfficeType}.
		 */
		public static Loader loader;

		/**
		 * Failure in instantiating an instance.
		 */
		public static RuntimeException instantiateFailure;

		/**
		 * Resets the state for the next test.
		 */
		public static void reset() {
			loader = null;
			instantiateFailure = null;
		}

		/**
		 * Default constructor.
		 */
		public MockOfficeSource() {
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================ OfficeSource ======================================
		 */

		@Override
		public OfficeSourceSpecification getSpecification() {
			fail("Should not be invoked in obtaining office type");
			return null;
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect,
				OfficeSourceContext context) throws Exception {
			loader.sourceOffice(officeArchitect, context);
		}
	}

}