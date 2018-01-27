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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.compile.spi.managedobject.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSupplier;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.impl.classloader.ClassLoaderConfigurationContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.objects.WoofObjectsRepositoryImpl;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link WoofObjectsLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofObjectsLoaderTest extends OfficeFrameTestCase {

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
	 * Mock {@link WoofObjectsLoaderContext}.
	 */
	private final WoofObjectsLoaderContext loaderContext = this.createMock(WoofObjectsLoaderContext.class);

	/**
	 * Mock {@link OfficeArchitect}.
	 */
	private final OfficeArchitect office = this.createMock(OfficeArchitect.class);

	/**
	 * Ensure can load configuration to {@link OfficeArchitect} with objects.
	 */
	public void testLoading() throws Exception {

		// Initialise loading
		this.recordInitLoader("application.objects");

		// Record managed object source
		OfficeManagedObjectSource mosOne = this.recordManagedObjectSource("QUALIFIED:net.orm.Session",
				"net.example.ExampleManagedObjectSourceA", 10, "MO_ONE", "VALUE_ONE", "MO_TWO", "VALUE_TWO");

		// Record linking flow
		this.recordManagedObjectFlow(mosOne, "FLOW", "SECTION", "INPUT");

		// Record qualifying team
		this.recordManagedObjectTeam(mosOne, "TEAM", "QUALIFIER", "net.orm.Session");

		// Record managed object
		OfficeManagedObject moOne = this.recordManagedObject(mosOne, "QUALIFIED:net.orm.Session",
				ManagedObjectScope.THREAD, "QUALIFIED", "net.orm.Session", null, "net.orm.SessionLocal");

		// Record dependency
		this.recordManagedObjectDependency(moOne, "DEPENDENCY", "QUALIFIER", "net.example.Dependency");

		// Record first supplier
		this.recordSupplier("net.example.ExampleSupplierSourceA", "net.example.ExampleSupplierSourceA", "SUPPLIER_A",
				"VALUE_A", "SUPPLIER_B", "VALUE_B");

		// Record second managed object
		OfficeManagedObjectSource mosTwo = this.recordManagedObjectSource("QUALIFIER:net.example.Type",
				"net.example.ExampleManagedObjectSourceB", 0);
		this.recordManagedObject(mosTwo, "QUALIFIER:net.example.Type", ManagedObjectScope.THREAD, "QUALIFIER",
				"net.example.Type");

		// Record second supplier
		this.recordSupplier("net.example.ExampleSupplierSourceB", "net.example.ExampleSupplierSourceB");

		// Test
		this.replayMockObjects();
		this.loader.loadWoofObjectsConfiguration(this.loaderContext);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load {@link ClassManagedObjectSource} shortcut configuration.
	 */
	public void testClassShortcuts() throws Exception {

		// Record initialise loader
		this.recordInitLoader("class.objects.xml");

		// Record class A
		OfficeManagedObjectSource mosA = this.recordManagedObjectSource("net.example.ExampleClassA",
				ClassManagedObjectSource.class.getName(), 0, ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				"net.example.ExampleClassA");
		this.recordManagedObject(mosA, "net.example.ExampleClassA", ManagedObjectScope.THREAD);

		// Record class B
		OfficeManagedObjectSource mosB = this.recordManagedObjectSource("net.example.ExampleClassB",
				ClassManagedObjectSource.class.getName(), 0, ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				"net.example.ExampleClassB");
		this.recordManagedObject(mosB, "net.example.ExampleClassB", ManagedObjectScope.THREAD, "QUALIFIER",
				"net.example.ExampleClassB");

		// Record class C
		OfficeManagedObjectSource mosC = this.recordManagedObjectSource("net.example.ExampleClassC",
				ClassManagedObjectSource.class.getName(), 0, ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				"net.example.Type");
		this.recordManagedObject(mosC, "net.example.ExampleClassC", ManagedObjectScope.THREAD, null,
				"net.example.Type");

		// Record class D
		OfficeManagedObjectSource mosD = this.recordManagedObjectSource("net.example.ExampleClassD",
				ClassManagedObjectSource.class.getName(), 0, ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				"net.example.ExampleClassD");
		this.recordManagedObject(mosD, "net.example.ExampleClassD", ManagedObjectScope.THREAD, "QUALIFIER",
				"net.example.Type");

		// Record class E
		OfficeManagedObjectSource mosE = this.recordManagedObjectSource("net.example.ExampleClassE",
				ClassManagedObjectSource.class.getName(), 0, ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				"net.example.ExampleClassE");
		this.recordManagedObject(mosE, "net.example.ExampleClassE", ManagedObjectScope.THREAD, null,
				"net.example.Type");

		// Record object F
		OfficeManagedObjectSource mosF = this.recordManagedObjectSource("net.example.ExampleClassF",
				ClassManagedObjectSource.class.getName(), 10, ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				"net.example.ExampleClassF", "MO_ONE", "VALUE_ONE", "MO_TWO", "VALUE_TWO");
		this.recordManagedObjectFlow(mosF, "FLOW", "SECTION", "INPUT");
		this.recordManagedObjectTeam(mosF, "TEAM", "QUALIFIER", "net.example.Type");
		OfficeManagedObject moF = this.recordManagedObject(mosF, "net.example.ExampleClassF", ManagedObjectScope.THREAD,
				"QUALIFIED", "net.orm.Session", null, "net.orm.SessionLocal");
		this.recordManagedObjectDependency(moF, "DEPENDENCY", "QUALIFIER", "net.example.Dependency");

		// Record source (ignores class)
		OfficeManagedObjectSource mosG = this.recordManagedObjectSource("net.example.Type",
				"net.example.ExampleManagedObjectSource", 0);
		this.recordManagedObject(mosG, "net.example.Type", ManagedObjectScope.THREAD, null, "net.example.Type");

		// Test
		this.replayMockObjects();
		this.loader.loadWoofObjectsConfiguration(this.loaderContext);
		this.verifyMockObjects();
	}

	/**
	 * Ensure handles {@link ManagedObjectScope} values.
	 */
	public void testManagedObjectScopes() throws Exception {

		// Record initialise loader
		this.recordInitLoader("scoped.objects.xml");

		// Record first managed object
		OfficeManagedObjectSource processMos = this.recordManagedObjectSource(String.class.getName(),
				"net.example.ExampleManagedObjectSourceA", 0);
		this.recordManagedObject(processMos, String.class.getName(), ManagedObjectScope.PROCESS);

		// Record second managed object
		OfficeManagedObjectSource threadMos = this.recordManagedObjectSource(String.class.getName(),
				"net.example.ExampleManagedObjectSourceB", 0);
		this.recordManagedObject(threadMos, String.class.getName(), ManagedObjectScope.THREAD);

		// Record third managed object
		OfficeManagedObjectSource functionMos = this.recordManagedObjectSource(String.class.getName(),
				"net.example.ExampleManagedObjectSourceC", 0);
		this.recordManagedObject(functionMos, String.class.getName(), ManagedObjectScope.FUNCTION);

		// Record fourth managed object (using default scope as not specified)
		OfficeManagedObjectSource defaultMos = this.recordManagedObjectSource(String.class.getName(),
				"net.example.ExampleManagedObjectSourceD", 0);
		this.recordManagedObject(defaultMos, String.class.getName(), ManagedObjectScope.THREAD);

		// Fifth managed object should report issue
		this.office.addIssue("Invalid managed object scope 'invalid' for managed object java.lang.String");

		// Test
		this.replayMockObjects();
		this.loader.loadWoofObjectsConfiguration(this.loaderContext);
		this.verifyMockObjects();
	}

	/**
	 * Records initialising the {@link WoofObjectsLoader}.
	 * 
	 * @param fileName
	 *            File name for {@link ConfigurationItem}.
	 */
	private void recordInitLoader(String fileName) throws Exception {

		// Obtain the configuration
		String location = this.getFileLocation(this.getClass(), fileName);
		ConfigurationContext context = new ClassLoaderConfigurationContext(this.compiler.getClassLoader(), null);
		ConfigurationItem configuration = context.getConfigurationItem(location, null);
		assertNotNull("Can not find configuration '" + fileName + "'", configuration);
		this.recordReturn(this.loaderContext, this.loaderContext.getConfiguration(), configuration);

		// Obtain the application
		this.recordReturn(this.loaderContext, this.loaderContext.getOfficeArchitect(), this.office);
	}

	/**
	 * Records the {@link OfficeManagedObjectSource}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeManagedObjectSource}.
	 * @param managedObjectSourceClassName
	 *            {@link OfficeManagedObjectSource} {@link Class} name.
	 * @param timeout
	 *            Timeout.
	 * @param propertyNameValuePairs
	 *            {@link PropertyList} name/value pairs.
	 * @return Mock {@link OfficeManagedObjectSource}.
	 */
	private OfficeManagedObjectSource recordManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName, int timeout, String... propertyNameValuePairs) {
		final OfficeManagedObjectSource mos = this.createMock(OfficeManagedObjectSource.class);
		this.recordReturn(this.office,
				this.office.addOfficeManagedObjectSource(managedObjectSourceName, managedObjectSourceClassName), mos);
		mos.setTimeout(timeout);
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			mos.addProperty(propertyNameValuePairs[i], propertyNameValuePairs[i + 1]);
		}
		return mos;
	}

	/**
	 * Records the {@link ManagedObjectFlow}.
	 * 
	 * @param mos
	 *            Mock {@link OfficeManagedObjectSource}.
	 * @param flowName
	 *            Name of {@link ManagedObjectFlow}.
	 * @param sectionName
	 *            Name of {@link OfficeSection} handling the {@link Flow}.
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput} handling the
	 *            {@link Flow}.
	 */
	private void recordManagedObjectFlow(OfficeManagedObjectSource mos, String flowName, String sectionName,
			String inputName) {
		ManagedObjectFlow mosFlow = this.createMock(ManagedObjectFlow.class);
		this.recordReturn(mos, mos.getManagedObjectFlow(flowName), mosFlow);
		OfficeSection section = this.createMock(OfficeSection.class);
		this.recordReturn(this.office, this.office.getOfficeSection(sectionName), section);
		OfficeSectionInput sectionInput = this.createMock(OfficeSectionInput.class);
		this.recordReturn(section, section.getOfficeSectionInput(inputName), sectionInput);
		this.office.link(mosFlow, sectionInput);
	}

	/**
	 * Records the {@link ManagedObjectTeam}.
	 * 
	 * @param mos
	 *            Mock {@link OfficeManagedObjectSource}.
	 * @param teamName
	 *            Name of the {@link ManagedObjectTeam}.
	 * @param qualifier
	 *            Qualifier.
	 * @param type
	 *            Type.
	 */
	private void recordManagedObjectTeam(OfficeManagedObjectSource mos, String teamName, String qualifier,
			String type) {
		ManagedObjectTeam mosTeam = this.createMock(ManagedObjectTeam.class);
		this.recordReturn(mos, mos.getManagedObjectTeam(teamName), mosTeam);
		mosTeam.addTypeQualification(qualifier, type);
	}

	/**
	 * Records the {@link OfficeManagedObject}.
	 * 
	 * @param mos
	 *            Mock {@link OfficeManagedObjectSource}.
	 * @param managedObjectName
	 *            Name of the {@link OfficeManagedObject}.
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 * @param typeQualifierPairs
	 *            Type qualifier name/value pairs.
	 * @return Mock {@link OfficeManagedObject}.
	 */
	private OfficeManagedObject recordManagedObject(OfficeManagedObjectSource mos, String managedObjectName,
			ManagedObjectScope scope, String... typeQualifierPairs) {
		OfficeManagedObject mo = this.createMock(OfficeManagedObject.class);
		this.recordReturn(mos, mos.addOfficeManagedObject(managedObjectName, ManagedObjectScope.THREAD), mo);
		for (int i = 0; i < typeQualifierPairs.length; i += 2) {
			mo.addTypeQualification(typeQualifierPairs[i], typeQualifierPairs[i + 1]);
		}
		return mo;
	}

	/**
	 * Records the {@link ManagedObjectDependency}.
	 * 
	 * @param mo
	 *            Mock {@link OfficeManagedObject}.
	 * @param dependencyName
	 *            Name of the {@link ManagedObjectDependency}.
	 * @param qualifier
	 *            Qualifier.
	 * @param type
	 *            Type.
	 */
	private void recordManagedObjectDependency(OfficeManagedObject mo, String dependencyName, String qualifier,
			String type) {
		ManagedObjectDependency dependency = this.createMock(ManagedObjectDependency.class);
		this.recordReturn(mo, mo.getManagedObjectDependency(dependencyName), dependency);
		dependency.setOverrideQualifier(qualifier);
		dependency.setSpecificType(type);
	}

	/**
	 * Records the {@link OfficeSupplier}.
	 * 
	 * @param supplierName
	 *            Name of the {@link OfficeSupplier}.
	 * @param supplierSourceClassName
	 *            {@link SupplierSource} {@link Class} name.
	 * @param propertyNameValuePairs
	 *            {@link PropertyList} name/value pairs.
	 * @return Mock {@link OfficeSupplier}.
	 */
	private OfficeSupplier recordSupplier(String supplierName, String supplierSourceClassName,
			String... propertyNameValuePairs) {
		OfficeSupplier supplier = this.createMock(OfficeSupplier.class);
		this.recordReturn(this.office, this.office.addSupplier(supplierName, supplierSourceClassName), supplier);
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			supplier.addProperty(propertyNameValuePairs[i], propertyNameValuePairs[i + 1]);
		}
		return supplier;
	}

}