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
package net.officefloor.plugin.objects;

import org.easymock.AbstractMatcher;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireSupplier;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.objects.WoofObjectsRepositoryImpl;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link WoofObjectsLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireObjectsLoaderTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

	/**
	 * {@link WoofObjectsLoader} to test.
	 */
	private final WoofObjectsLoader loader = new WoofObjectsLoaderImpl(
			new WoofObjectsRepositoryImpl(new ModelRepositoryImpl()));

	/**
	 * Mock {@link AutoWireApplication}.
	 */
	private final AutoWireApplication app = this.createMock(AutoWireApplication.class);

	/**
	 * Mock {@link WoofObjectsLoaderContext}.
	 */
	private final WoofObjectsLoaderContext loaderContext = this.createMock(WoofObjectsLoaderContext.class);

	/**
	 * {@link ManagedObjectSourceWirerContext}.
	 */
	private final ManagedObjectSourceWirerContext wireContext = this.createMock(ManagedObjectSourceWirerContext.class);

	/**
	 * {@link AbstractMatcher} for matching the addition of the
	 * {@link ManagedObjectSource}.
	 */
	private final AbstractMatcher addManagedObjectMatcher = new AbstractMatcher() {
		@Override
		public boolean matches(Object[] expected, Object[] actual) {

			// Validate source and auto-wiring matches
			boolean isMatch = (expected[0].equals(actual[0]));
			AutoWire[] eAutoWiring = (AutoWire[]) expected[2];
			AutoWire[] aAutoWiring = (AutoWire[]) actual[2];
			isMatch = isMatch && (eAutoWiring.length == aAutoWiring.length);
			if (isMatch) {
				for (int i = 0; i < eAutoWiring.length; i++) {
					isMatch = isMatch && (eAutoWiring[i].equals(aAutoWiring[i]));
				}
			}

			// Trigger the wiring (if match)
			if (isMatch) {
				ManagedObjectSourceWirer wirer = (ManagedObjectSourceWirer) actual[1];
				wirer.wire(AutoWireObjectsLoaderTest.this.wireContext);
			}

			return isMatch;
		}
	};

	/**
	 * Ensure can load configuration to {@link AutoWireApplication}.
	 */
	public void testLoading() throws Exception {

		// Initialise loading
		this.recordInitLoader("application.objects");

		// Record first managed object
		final AutoWireObject objectOne = this.createMock(AutoWireObject.class);
		this.recordReturn(this.app,
				this.app.addManagedObject("net.example.ExampleManagedObjectSourceA", null,
						new AutoWire("QUALIFIED", "net.orm.Session"), new AutoWire("net.orm.SessionLocal")),
				objectOne, this.addManagedObjectMatcher);

		// Record wiring
		this.wireContext.setManagedObjectScope(ManagedObjectScope.THREAD);
		this.wireContext.mapFlow("FLOW", "SECTION", "INPUT");
		this.wireContext.mapTeam("TEAM", new AutoWire("QUALIFIER", "net.example.Type"));
		this.wireContext.mapDependency("DEPENDENCY", new AutoWire("QUALIFIER", "net.example.Dependency"));

		// Record remaining of object
		objectOne.setTimeout(10);
		objectOne.addProperty("MO_ONE", "VALUE_ONE");
		objectOne.loadProperties("example/object.properties");
		objectOne.addProperty("MO_TWO", "VALUE_TWO");

		// Record first supplier
		final AutoWireSupplier supplierOne = this.createMock(AutoWireSupplier.class);
		this.recordReturn(this.app, this.app.addSupplier("net.example.ExampleSupplierSourceA"), supplierOne);
		supplierOne.addProperty("SUPPLIER_A", "VALUE_A");
		supplierOne.loadProperties("example/supplier.properties");
		supplierOne.addProperty("SUPPLIER_B", "VALUE_B");

		// Record second managed object
		final AutoWireObject objectTwo = this.createMock(AutoWireObject.class);
		this.recordReturn(this.app, this.app.addManagedObject("net.example.ExampleManagedObjectSourceB", null,
				new AutoWire("QUALIFIER", "net.example.Type")), objectTwo);

		// Record second supplier
		final AutoWireSupplier supplierTwo = this.createMock(AutoWireSupplier.class);
		this.recordReturn(this.app, this.app.addSupplier("net.example.ExampleSupplierSourceB"), supplierTwo);

		// Test
		this.replayMockObjects();
		this.loader.loadAutoWireObjectsConfiguration(this.loaderContext);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load {@link ClassManagedObjectSource} shortcut configuration.
	 */
	public void testClassShortcuts() throws Exception {

		// Record initialise loader
		this.recordInitLoader("class.objects.xml");

		// Record class A
		final AutoWireObject objectA = this.createMock(AutoWireObject.class);
		this.recordReturn(this.app, this.app.addManagedObject(ClassManagedObjectSource.class.getName(), null,
				new AutoWire("net.example.ExampleClassA")), objectA, this.addManagedObjectMatcher);
		objectA.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, "net.example.ExampleClassA");

		// Record class B
		final AutoWireObject objectB = this.createMock(AutoWireObject.class);
		this.recordReturn(this.app, this.app.addManagedObject(ClassManagedObjectSource.class.getName(), null,
				new AutoWire("QUALIFIER", "net.example.ExampleClassB")), objectB);
		objectB.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, "net.example.ExampleClassB");

		// Record class C
		final AutoWireObject objectC = this.createMock(AutoWireObject.class);
		this.recordReturn(this.app, this.app.addManagedObject(ClassManagedObjectSource.class.getName(), null,
				new AutoWire("net.example.Type")), objectC);
		objectC.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, "net.example.ExampleClassC");

		// Record class D
		final AutoWireObject objectD = this.createMock(AutoWireObject.class);
		this.recordReturn(this.app, this.app.addManagedObject(ClassManagedObjectSource.class.getName(), null,
				new AutoWire("QUALIFIER", "net.example.Type")), objectD);
		objectD.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, "net.example.ExampleClassD");

		// Record class E
		final AutoWireObject objectE = this.createMock(AutoWireObject.class);
		this.recordReturn(this.app, this.app.addManagedObject(ClassManagedObjectSource.class.getName(), null,
				new AutoWire("net.example.Type")), objectE);
		objectE.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, "net.example.ExampleClassE");

		// Record object F
		final AutoWireObject objectF = this.createMock(AutoWireObject.class);
		this.recordReturn(this.app, this.app.addManagedObject(ClassManagedObjectSource.class.getName(), null,
				new AutoWire("QUALIFIED", "net.orm.Session"), new AutoWire("net.orm.SessionLocal")), objectF);
		this.wireContext.mapFlow("FLOW", "SECTION", "INPUT");
		this.wireContext.mapTeam("TEAM", new AutoWire("QUALIFIER", "net.example.Type"));
		this.wireContext.mapDependency("DEPENDENCY", new AutoWire("QUALIFIER", "net.example.Dependency"));
		objectF.setTimeout(10);
		objectF.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, "net.example.ExampleClassF");
		objectF.addProperty("MO_ONE", "VALUE_ONE");
		objectF.loadProperties("example/object.properties");
		objectF.addProperty("MO_TWO", "VALUE_TWO");

		// Record source (ignores class)
		final AutoWireObject objectSource = this.createMock(AutoWireObject.class);
		this.recordReturn(this.app, this.app.addManagedObject("net.example.ExampleManagedObjectSource", null,
				new AutoWire("net.example.Type")), objectSource);

		// Test
		this.replayMockObjects();
		this.loader.loadAutoWireObjectsConfiguration(this.loaderContext);
		this.verifyMockObjects();
	}

	/**
	 * Ensure handles {@link ManagedObjectScope} values.
	 */
	public void testManagedObjectScopes() throws Exception {

		// Record initialise loader
		this.recordInitLoader("scoped.objects.xml");

		// Record first managed object
		final AutoWireObject objectA = this.createMock(AutoWireObject.class);
		this.recordReturn(this.app,
				this.app.addManagedObject("net.example.ExampleManagedObjectSourceA", null, new AutoWire(String.class)),
				objectA, this.addManagedObjectMatcher);
		this.wireContext.setManagedObjectScope(ManagedObjectScope.PROCESS);

		// Record second managed object
		final AutoWireObject objectB = this.createMock(AutoWireObject.class);
		this.recordReturn(this.app,
				this.app.addManagedObject("net.example.ExampleManagedObjectSourceB", null, new AutoWire(String.class)),
				objectB, this.addManagedObjectMatcher);
		this.wireContext.setManagedObjectScope(ManagedObjectScope.THREAD);

		// Record third managed object
		final AutoWireObject objectC = this.createMock(AutoWireObject.class);
		this.recordReturn(this.app,
				this.app.addManagedObject("net.example.ExampleManagedObjectSourceC", null, new AutoWire(String.class)),
				objectC, this.addManagedObjectMatcher);
		this.wireContext.setManagedObjectScope(ManagedObjectScope.WORK);

		// Record fourth managed object (using default scope as not specified)
		final AutoWireObject objectD = this.createMock(AutoWireObject.class);
		this.recordReturn(this.app,
				this.app.addManagedObject("net.example.ExampleManagedObjectSourceD", null, new AutoWire(String.class)),
				objectD, this.addManagedObjectMatcher);

		// Fifth managed object should report issue
		this.loaderContext.addIssue("Invalid managed object scope 'invalid' for managed object java.lang.String");

		// Test
		this.replayMockObjects();
		this.loader.loadAutoWireObjectsConfiguration(this.loaderContext);
		this.verifyMockObjects();
	}

	/**
	 * Records initialising the {@link WoofObjectsLoader}.
	 * 
	 * @param fileName
	 *            File name for {@link ConfigurationItem}.r
	 */
	private void recordInitLoader(String fileName) throws Exception {

		// Obtain the configuration
		String location = this.getFileLocation(this.getClass(), fileName);
		ConfigurationContext context = new ClassLoaderConfigurationContext(this.compiler.getClassLoader());
		ConfigurationItem configuration = context.getConfigurationItem(location);
		assertNotNull("Can not find configuration '" + fileName + "'", configuration);
		this.recordReturn(this.loaderContext, this.loaderContext.getConfiguration(), configuration);

		// Obtain the application
		this.recordReturn(this.loaderContext, this.loaderContext.getOfficeArchitect(), this.app);
	}

}