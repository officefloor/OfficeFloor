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
package net.officefloor.autowire.impl.supplier;

import java.sql.Connection;
import java.util.Properties;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireTeam;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceContext;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceSpecification;
import net.officefloor.autowire.supplier.SuppliedManagedObjectDependencyType;
import net.officefloor.autowire.supplier.SuppliedManagedObjectType;
import net.officefloor.autowire.supplier.SupplierLoader;
import net.officefloor.autowire.supplier.SupplierType;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.ManagedObjectSourceNodeImpl;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests loading the {@link SupplierType}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadSupplierTypeTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	@Override
	protected void setUp() throws Exception {
		MockSupplierSource.reset();
	}

	/**
	 * Ensure issue if fail to instantiate the {@link SupplierSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue("Failed to instantiate "
				+ MockSupplierSource.class.getName()
				+ " by default constructor", failure);

		// Attempt to load
		MockSupplierSource.instantiateFailure = failure;
		this.loadSupplierType(false, null);
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues
				.recordIssue("Missing property 'missing' for SupplierSource "
						+ MockSupplierSource.class.getName());

		// Attempt to load
		this.loadSupplierType(false, new Init() {
			@Override
			public void supply(SupplierSourceContext context) {
				context.getProperty("missing");
			}
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Attempt to load
		this.loadSupplierType(true, new Init() {
			@Override
			public void supply(SupplierSourceContext context) {
				assertEquals("Ensure get defaulted property", "DEFAULT",
						context.getProperty("missing", "DEFAULT"));
				assertEquals("Ensure get property ONE", "1",
						context.getProperty("ONE"));
				assertEquals("Ensure get property TWO", "2",
						context.getProperty("TWO"));
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
		this.issues
				.recordIssue("Can not load class 'missing' for SupplierSource "
						+ MockSupplierSource.class.getName());

		// Attempt to load
		this.loadSupplierType(false, new Init() {
			@Override
			public void supply(SupplierSourceContext context) {
				context.loadClass("missing");
			}
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing resource
		this.issues
				.recordIssue("Can not obtain resource at location 'missing' for SupplierSource "
						+ MockSupplierSource.class.getName());

		// Attempt to load
		this.loadSupplierType(false, new Init() {
			@Override
			public void supply(SupplierSourceContext context) {
				context.getResource("missing");
			}
		});
	}

	/**
	 * Ensure able to get resource.
	 */
	public void testGetResource() {

		// Obtain path
		final String objectPath = Object.class.getName().replace('.', '/')
				+ ".class";

		// Attempt to load
		this.loadSupplierType(true, new Init() {
			@Override
			public void supply(SupplierSourceContext context) {
				assertEquals("Incorrect resource locator",
						LoadSupplierTypeTest.class.getClassLoader()
								.getResource(objectPath), context
								.getClassLoader().getResource(objectPath));
			}
		});
	}

	/**
	 * Ensure issue if fails to init the {@link SupplierSource}.
	 */
	public void testFailInitSupplierSource() {

		final NullPointerException failure = new NullPointerException(
				"Fail init SupplierSource");

		// Record failure to init the Supplier Source
		this.issues.recordIssue(
				"Failed to source SupplierType definition from SupplierSource "
						+ MockSupplierSource.class.getName(), failure);

		// Attempt to load
		this.loadSupplierType(false, new Init() {
			@Override
			public void supply(SupplierSourceContext context) {
				throw failure;
			}
		});
	}

	/**
	 * <p>
	 * Ensure able to load with no {@link ManagedObject} instances.
	 * <p>
	 * No {@link ManagedObject} instances is allowed as may have
	 * {@link SupplierSource} turn off supplying {@link ManagedObject}
	 * instances. Allowing none, will prevent this case having to load an
	 * arbitrary {@link ManagedObject}.
	 */
	public void testNoSuppliedManagedObjects() {

		// Attempt to load
		this.loadEmptySupplierType(new Init() {
			@Override
			public void supply(SupplierSourceContext context) {
				// No supplied managed objects
			}
		});
	}

	/**
	 * Ensure issue if no {@link AutoWire} instance for {@link ManagedObject}.
	 */
	public void testNoAutoWiring() {

		// Record no auto-wiring
		this.issues.recordIssue("Must provide auto-wiring for ManagedObject 1");

		// Attempt to load
		this.loadEmptySupplierType(new Init() {
			@Override
			public void supply(SupplierSourceContext context) {
				context.addManagedObject(new ClassManagedObjectSource(), null);
			}
		});
	}

	/**
	 * Ensure issue if null {@link ManagedObjectSource}.
	 */
	public void testNullManagedObjectSource() {

		final AutoWire autoWire = new AutoWire(Connection.class);

		// Record no null managed object source
		this.issues
				.recordIssue("Must provide a ManagedObjectSource for ManagedObject "
						+ autoWire.getQualifiedType());

		// Attempt to load
		this.loadEmptySupplierType(new Init() {
			@Override
			public void supply(SupplierSourceContext context) {
				context.addManagedObject(
						(ManagedObjectSource<None, None>) null, null, autoWire);
			}
		});
	}

	/**
	 * Ensure issue if supplied {@link ManagedObject} can not have its type
	 * loaded.
	 */
	public void testInvalidManagedObjectType() {

		final AutoWire autoWire = new AutoWire(Connection.class);

		// Record invalid managed object type
		CompilerIssue[] capturedIssues = this.issues.recordCaptureIssues(true);
		this.issues.recordIssue(autoWire.getQualifiedType(),
				ManagedObjectSourceNodeImpl.class,
				"Missing property 'class.name'");
		this.issues.recordIssue(
				"Failure loading ManagedObjectType from source "
						+ ClassManagedObjectSource.class.getName(),
				capturedIssues);

		// Attempt to load
		this.loadEmptySupplierType(new Init() {
			@Override
			public void supply(SupplierSourceContext context) {
				// Load invalid managed object (missing property)
				context.addManagedObject(new ClassManagedObjectSource(), null,
						autoWire);
			}
		});
	}

	/**
	 * Ensure can load simple {@link ManagedObject}.
	 */
	public void testSimpleManagedObject() {

		// Record obtaining managed object type
		this.issues.recordCaptureIssues(false);

		// Load
		SuppliedManagedObjectType type = this
				.loadSuppliedManagedObjectType(new Init() {
					@Override
					public void supply(SupplierSourceContext context) {
						addClassManagedObject(context,
								SimpleManagedObject.class, null, new AutoWire(
										SimpleManagedObject.class));
					}
				});

		// Validate the managed object type
		AutoWire[] autoWiring = type.getAutoWiring();
		assertEquals("Should have auto-wiring", 1, autoWiring.length);
		assertEquals("Incorrect auto-wire", new AutoWire(
				SimpleManagedObject.class), autoWiring[0]);
		assertFalse("Should not be input managed object",
				type.isInputManagedObject());
		assertEquals("Should be no dependencies", 0,
				type.getDependencyTypes().length);
		assertEquals("Should be no flows", 0, type.getFlowTypes().length);
		assertEquals("Should be no teams", 0, type.getTeamTypes().length);
	}

	/**
	 * Mock class for test by similar name.
	 */
	public static class SimpleManagedObject {
	}

	/**
	 * Ensure configuration items provide correct details.
	 */
	public void testAutoWireConfigurationItems() {

		// Record obtaining managed object type
		this.issues.recordCaptureIssues(false);

		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {

				// Add the supplied team
				AutoWireTeam team = context.mapTeam("team", "PASSIVE");
				team.addProperty("NAME", "VALUE");

				// Validate auto-wire team
				assertEquals("Incorrect team name", "team", team.getTeamName());
				assertEquals("Incorrect team source class name", "PASSIVE",
						team.getTeamSourceClassName());
				Properties properties = team.getProperties().getProperties();
				assertEquals("Incorrect number of properties", 1,
						properties.size());
				assertEquals("Incorrect team property", "VALUE",
						properties.getProperty("NAME"));
				assertEquals(
						"Should have no auto-wiring as just for the managed object",
						0, team.getAutoWiring().length);
			}
		};

		this.loadSuppliedManagedObjectType(new Init() {
			@Override
			public void supply(SupplierSourceContext context) throws Exception {

				// Add the managed object
				MockTypeManagedObjectSource source = new MockTypeManagedObjectSource(
						Object.class);
				source.addTeam("team");
				AutoWireObject object = context.addManagedObject(source, wirer,
						new AutoWire(Object.class));
				object.addProperty("NAME", "VALUE");
				object.setTimeout(1000);

				// Validate auto-wire object
				assertEquals("Incorrect managed object source class name",
						MockTypeManagedObjectSource.class.getName(),
						object.getManagedObjectSourceClassName());
				assertEquals("Incorrect wirer", wirer,
						object.getManagedObjectSourceWirer());
				assertEquals("Incorrect timeout", 1000, object.getTimeout());
				AutoWire[] autoWiring = object.getAutoWiring();
				assertEquals("Incorrect number of auto-wiring", 1,
						autoWiring.length);
				assertEquals("Incorrect auto-wiring",
						new AutoWire(Object.class), autoWiring[0]);
				Properties properties = object.getProperties().getProperties();
				assertEquals("Incorrect number of properties", 1,
						properties.size());
				assertEquals("Incorrect property", "VALUE",
						properties.getProperty("NAME"));
			}
		});
	}

	/**
	 * Ensure can load complex {@link ManagedObject}.
	 */
	public void testComplexManagedObject() {

		// Record obtaining the two managed object types
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);

		// Load
		SupplierType type = this.loadSupplierType(true, new Init() {
			@Override
			public void supply(SupplierSourceContext context) throws Exception {
				new MockLoadSupplierSource().supply(context);
			}
		}, MockLoadSupplierSource.PROPERTY_TEST,
				MockLoadSupplierSource.PROPERTY_TEST);

		// Validate the supplier type
		MockLoadSupplierSource.assertSupplierType(type);
	}

	/**
	 * Ensure issue if unknown dependency {@link AutoWire}.
	 */
	public void testUnknownManagedObjectDependencyAutoWire() {

		final MockTypeManagedObjectSource source = new MockTypeManagedObjectSource(
				Object.class);

		// Record issue as unknown dependency
		this.issues.recordCaptureIssues(false);
		this.issues
				.recordIssue("Wired dependency 'dependency' not specified on ManagedObjectType for ManagedObject "
						+ new AutoWire(Object.class).getQualifiedType());

		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapDependency("dependency", new AutoWire("QUALIFIED",
						Connection.class.getName()));
			}
		};

		// Load
		this.loadSuppliedManagedObjectType(new Init() {
			@Override
			public void supply(SupplierSourceContext context) {
				source.addAsManagedObject(context, wirer);
			}
		});
	}

	/**
	 * Ensure able to override the dependency {@link AutoWire}.
	 */
	public void testOverrideManagedObjectDependencyAutoWire() {

		final AutoWire overrideAutoWire = new AutoWire("QUALIFIED",
				Connection.class.getName());

		// Record obtaining managed object type
		this.issues.recordCaptureIssues(false);

		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapDependency("dependency", overrideAutoWire);
			}
		};

		// Load
		SuppliedManagedObjectType type = this
				.loadSuppliedManagedObjectType(new Init() {
					@Override
					public void supply(SupplierSourceContext context) {
						MockTypeManagedObjectSource source = new MockTypeManagedObjectSource(
								Object.class);
						source.addDependency("dependency", Object.class, null);
						source.addAsManagedObject(context, wirer);
					}
				});

		// Validate overridden dependency auto-wire
		SuppliedManagedObjectDependencyType[] dependencies = type
				.getDependencyTypes();
		assertEquals("Incorrect number of dependencies", 1, dependencies.length);
		SuppliedManagedObjectDependencyType dependency = dependencies[0];
		assertEquals("Incorrect dependency name", "dependency",
				dependency.getDependencyName());
		assertEquals("Incorrect dependency type", Connection.class.getName(),
				dependency.getDependencyType());
		assertEquals("Incorrect dependency qualifier", "QUALIFIED",
				dependency.getTypeQualifier());
	}

	/**
	 * Ensure issue if flow not {@link AutoWire}.
	 */
	public void testUnwiredManagedObjectFlow() {

		final MockTypeManagedObjectSource source = new MockTypeManagedObjectSource(
				Object.class);

		// Record issue as unknown flow
		this.issues.recordCaptureIssues(false);
		this.issues.recordIssue("Flow 'flow' must be wired for ManagedObject "
				+ new AutoWire(Object.class).getQualifiedType());

		// Load
		SuppliedManagedObjectType type = this
				.loadSuppliedManagedObjectType(new Init() {
					@Override
					public void supply(SupplierSourceContext context) {
						source.addFlow("flow", Integer.class);
						source.addAsManagedObject(context, null);
					}
				});

		// Should not have flow
		assertEquals("Should not load the flow", 0, type.getFlowTypes().length);
	}

	/**
	 * Ensure issue if unknown flow {@link AutoWire}.
	 */
	public void testUnknownManagedObjectFlowAutoWire() {

		final MockTypeManagedObjectSource source = new MockTypeManagedObjectSource(
				Object.class);

		// Record obtaining managed object type
		this.issues.recordCaptureIssues(false);

		// Record issue as unknown flow
		this.issues
				.recordIssue("Wired flow 'flow' not specified on ManagedObjectType for ManagedObject "
						+ new AutoWire(Object.class).getQualifiedType());

		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapFlow("flow", "SECTION", "INPUT");
			}
		};

		// Load
		this.loadSuppliedManagedObjectType(new Init() {
			@Override
			public void supply(SupplierSourceContext context) {
				source.addAsManagedObject(context, wirer);
			}
		});
	}

	/**
	 * Ensure issue if {@link Team} not {@link AutoWire}.
	 */
	public void testUnwiredManagedObjectTeam() {

		final MockTypeManagedObjectSource source = new MockTypeManagedObjectSource(
				Object.class);

		// Record obtaining managed object type
		this.issues.recordCaptureIssues(false);

		// Record issue as unknown team
		this.issues.recordIssue("Team 'team' must be wired for ManagedObject "
				+ new AutoWire(Object.class).getQualifiedType());

		// Load
		SuppliedManagedObjectType type = this
				.loadSuppliedManagedObjectType(new Init() {
					@Override
					public void supply(SupplierSourceContext context) {
						source.addTeam("team");
						source.addAsManagedObject(context, null);
					}
				});

		// Should not have team
		assertEquals("Should not load the team", 0, type.getTeamTypes().length);
	}

	/**
	 * Ensure issue if unknown {@link Team} {@link AutoWire}.
	 */
	public void testUnknownManagedObjectTeamAutoWire() {

		final MockTypeManagedObjectSource source = new MockTypeManagedObjectSource(
				Object.class);

		// Record obtaining managed object type
		this.issues.recordCaptureIssues(false);

		// Record issue as unknown team
		this.issues
				.recordIssue("Wired team 'team' not specified on ManagedObjectType for ManagedObject "
						+ new AutoWire(Object.class).getQualifiedType());

		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapTeam("team", new AutoWire(Connection.class));
			}
		};

		// Load
		this.loadSuppliedManagedObjectType(new Init() {
			@Override
			public void supply(SupplierSourceContext context) {
				source.addAsManagedObject(context, wirer);
			}
		});
	}

	/**
	 * Ensure {@link Team} not appear on {@link SuppliedManagedObjectType} if
	 * specified.
	 */
	public void testManagedObjectTeamSpecified() {

		// Record obtaining managed object type
		this.issues.recordCaptureIssues(false);

		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapTeam("team", OnePersonTeamSource.class.getName());
			}
		};

		// Load
		SuppliedManagedObjectType type = this
				.loadSuppliedManagedObjectType(new Init() {
					@Override
					public void supply(SupplierSourceContext context) {
						MockTypeManagedObjectSource source = new MockTypeManagedObjectSource(
								Object.class);
						source.addTeam("team");
						source.addAsManagedObject(context, wirer);
					}
				});

		// Validate team not appears
		assertEquals("Should have no teams", 0, type.getTeamTypes().length);
	}

	/**
	 * Ensure able to load multiple {@link ManagedObject} instances and order is
	 * maintained in {@link SupplierType}.
	 */
	public void testMultipleManagedObjects() {

		// Record obtaining the managed object types
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);

		// Load the managed objects
		SupplierType type = this.loadSupplierType(true, new Init() {
			@Override
			public void supply(SupplierSourceContext context) {
				new MockTypeManagedObjectSource(Object.class)
						.addAsManagedObject(context, null);
				new MockTypeManagedObjectSource(Connection.class)
						.addAsManagedObject(context, null);
				addClassManagedObject(context, SimpleManagedObject.class, null);
				MockTypeManagedObjectSource source = new MockTypeManagedObjectSource(
						Object.class);
				context.addManagedObject(source, null, new AutoWire(
						"QUALIFIED", Object.class.getName()));
			}
		});

		// Validate the managed object types (and order)
		SuppliedManagedObjectType[] managedObjects = type
				.getSuppliedManagedObjectTypes();
		assertEquals("Incorrect number of managed objects", 4,
				managedObjects.length);
		assertEquals("Incorrect first managed object", new AutoWire(
				Object.class), managedObjects[0].getAutoWiring()[0]);
		assertEquals("Incorrect second managed object", new AutoWire(
				Connection.class), managedObjects[1].getAutoWiring()[0]);
		assertEquals("Incorrect third managed object", new AutoWire(
				SimpleManagedObject.class),
				managedObjects[2].getAutoWiring()[0]);
		assertEquals("Incorrect fourth managed object", new AutoWire(
				"QUALIFIED", Object.class.getName()),
				managedObjects[3].getAutoWiring()[0]);
	}

	/**
	 * Convenience method to load a {@link SupplierSource} with only one
	 * {@link SuppliedManagedObjectType}.
	 * 
	 * @param init
	 *            {@link Init}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 * @return Single {@link SuppliedManagedObjectType}.
	 */
	private SuppliedManagedObjectType loadSuppliedManagedObjectType(Init init,
			String... propertyNameValuePairs) {

		// Load the supplier type
		SupplierType type = this.loadSupplierType(true, init,
				propertyNameValuePairs);

		// Ensure only a single supplied managed object
		SuppliedManagedObjectType[] moTypes = type
				.getSuppliedManagedObjectTypes();
		assertEquals("Expecting only one supplied managed object type", 1,
				moTypes.length);

		// Return the single supplied managed object type
		return moTypes[0];
	}

	/**
	 * Ensure loads an empty {@link SupplierType}. In other words, a
	 * {@link SupplierType} with no {@link SuppliedManagedObjectType} instances.
	 * 
	 * @param init
	 *            {@link Init}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 */
	private void loadEmptySupplierType(Init init,
			String... propertyNameValuePairs) {

		// Load the supplier type
		SupplierType type = this.loadSupplierType(true, init,
				propertyNameValuePairs);

		// Ensure no supplied managed object types
		assertEquals("Supplier should not have managed objects", 0,
				type.getSuppliedManagedObjectTypes().length);
	}

	/**
	 * Loads the {@link SupplierType}.
	 * 
	 * @param isExpectedToLoad
	 *            Flag indicating if expecting to load the {@link SupplierType}.
	 * @param init
	 *            {@link Init}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 * @return Loaded {@link SupplierType}.
	 */
	private SupplierType loadSupplierType(boolean isExpectedToLoad, Init init,
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

		// Create the managed object loader and load the managed object type
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		SupplierLoader supplierLoader = compiler.getSupplierLoader();
		MockSupplierSource.init = init;
		SupplierType supplierType = supplierLoader.loadSupplierType(
				MockSupplierSource.class, propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the supplier type", supplierType);
		} else {
			assertNull("Should not load the supplier type", supplierType);
		}

		// Return the supplier type
		return supplierType;
	}

	/**
	 * Implement to initialise the {@link MockSupplierSource}.
	 */
	private static interface Init {

		/**
		 * Implemented to init the {@link SupplierSource}.
		 * 
		 * @param context
		 *            {@link SupplierSourceContext}.
		 */
		void supply(SupplierSourceContext context) throws Exception;
	}

	/**
	 * Convenience method to add a {@link ClassManagedObjectSource}.
	 * 
	 * @param context
	 *            {@link SupplierSourceContext}.
	 * @param clazz
	 *            Class for the {@link ClassManagedObjectSource}.
	 * @param wirer
	 *            {@link ManagedObjectSourceWirer}.
	 * @param autoWiring
	 *            {@link AutoWire} instances for the {@link AutoWireObject}. May
	 *            be empty as will default {@link AutoWire} from class.
	 * @return {@link AutoWireObject}.
	 */
	private static AutoWireObject addClassManagedObject(
			SupplierSourceContext context, Class<?> clazz,
			ManagedObjectSourceWirer wirer, AutoWire... autoWiring) {

		// Use class if no auto-wiring
		if (autoWiring.length == 0) {
			autoWiring = new AutoWire[] { new AutoWire(clazz) };
		}

		// Configure in the managed object
		AutoWireObject object = context.addManagedObject(
				new ClassManagedObjectSource(), wirer, autoWiring);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				clazz.getName());

		// Return the managed object
		return object;
	}

	/**
	 * Mock {@link SupplierSource}.
	 */
	@TestSource
	public static class MockSupplierSource implements SupplierSource {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * {@link Init} to init the {@link SupplierSource}.
		 */
		public static Init init = null;

		/**
		 * Resets the state for next test.
		 */
		public static void reset() {
			instantiateFailure = null;
			init = null;
		}

		/**
		 * Initiate with possible failure.
		 */
		public MockSupplierSource() {
			// Throw instantiate failure
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================== SupplierSource ===========================
		 */

		@Override
		public SupplierSourceSpecification getSpecification() {
			fail("Should not obtain specification");
			return null;
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Run the init if available
			if (init != null) {
				init.supply(context);
			}
		}
	}

}