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

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireSupplier;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.objects.AutoWireObjectsRepositoryImpl;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link AutoWireObjectsLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireObjectsLoaderTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler = OfficeFloorCompiler
			.newOfficeFloorCompiler(null);

	/**
	 * {@link AutoWireObjectsLoader} to test.
	 */
	private final AutoWireObjectsLoader loader = new AutoWireObjectsLoaderImpl(
			new AutoWireObjectsRepositoryImpl(new ModelRepositoryImpl()));

	/**
	 * Mock {@link AutoWireApplication}.
	 */
	private final AutoWireApplication app = this
			.createMock(AutoWireApplication.class);

	/**
	 * Ensure can load configuration to {@link AutoWireApplication}.
	 */
	public void testLoading() throws Exception {

		final ManagedObjectSourceWirerContext wireContext = this
				.createMock(ManagedObjectSourceWirerContext.class);

		// Record first managed object
		final AutoWireObject objectOne = this.createMock(AutoWireObject.class);
		this.recordReturn(this.app, this.app.addManagedObject(
				"net.example.ExampleManagedObjectSourceA", null, new AutoWire(
						"QUALIFIED", "net.orm.Session"), new AutoWire(
						"net.orm.SessionLocal")), objectOne,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {

						// Validate source and auto-wiring matches
						boolean isMatch = (expected[0].equals(actual[0]));
						AutoWire[] eAutoWiring = (AutoWire[]) expected[2];
						AutoWire[] aAutoWiring = (AutoWire[]) actual[2];
						isMatch = isMatch
								&& (eAutoWiring.length == aAutoWiring.length);
						if (isMatch) {
							for (int i = 0; i < eAutoWiring.length; i++) {
								isMatch = isMatch
										&& (eAutoWiring[i]
												.equals(aAutoWiring[i]));
							}
						}

						// Trigger the wiring
						ManagedObjectSourceWirer wirer = (ManagedObjectSourceWirer) actual[1];
						wirer.wire(wireContext);

						return isMatch;
					}
				});

		// Record wiring
		wireContext.mapFlow("FLOW", "SECTION", "INPUT");
		wireContext.mapTeam("TEAM", new AutoWire("QUALIFIER",
				"net.example.Type"));
		wireContext.mapDependency("DEPENDENCY", new AutoWire("QUALIFIER",
				"net.example.Dependency"));

		// Record remaining of object
		objectOne.setTimeout(10);
		objectOne.addProperty("MO_ONE", "VALUE_ONE");
		objectOne.loadProperties("example/object.properties");
		objectOne.addProperty("MO_TWO", "VALUE_TWO");

		// Record first supplier
		final AutoWireSupplier supplierOne = this
				.createMock(AutoWireSupplier.class);
		this.recordReturn(this.app,
				this.app.addSupplier("net.example.ExampleSupplierSourceA"),
				supplierOne);
		supplierOne.addProperty("SUPPLIER_A", "VALUE_A");
		supplierOne.loadProperties("example/supplier.properties");
		supplierOne.addProperty("SUPPLIER_B", "VALUE_B");

		// Record second managed object
		final AutoWireObject objectTwo = this.createMock(AutoWireObject.class);
		this.recordReturn(this.app, this.app.addManagedObject(
				"net.example.ExampleManagedObjectSourceB", null, new AutoWire(
						"QUALIFIER", "net.example.Type")), objectTwo);

		// Record second supplier
		final AutoWireSupplier supplierTwo = this
				.createMock(AutoWireSupplier.class);
		this.recordReturn(this.app,
				this.app.addSupplier("net.example.ExampleSupplierSourceB"),
				supplierTwo);

		// Test
		this.replayMockObjects();
		this.loader.loadAutoWireObjectsConfiguration(
				this.getConfiguration("application.objects"), this.app);
		this.verifyMockObjects();
	}

	/**
	 * Obtains the {@link ConfigurationItem}.
	 * 
	 * @param fileName
	 *            File name for {@link ConfigurationItem}.
	 * @return {@link ConfigurationItem}.
	 */
	private ConfigurationItem getConfiguration(String fileName)
			throws Exception {
		String location = this.getFileLocation(this.getClass(), fileName);
		ConfigurationContext context = new ClassLoaderConfigurationContext(
				this.compiler.getClassLoader());
		ConfigurationItem configuration = context
				.getConfigurationItem(location);
		assertNotNull("Can not find configuration '" + fileName + "'",
				configuration);
		return configuration;
	}

}