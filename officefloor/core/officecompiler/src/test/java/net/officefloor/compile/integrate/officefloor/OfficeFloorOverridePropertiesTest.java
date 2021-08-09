/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.integrate.officefloor;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutive;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.pool.source.impl.AbstractManagedObjectPoolSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.impl.AbstractExecutiveSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Ensure able to override the {@link PropertyList} for various aspects of the
 * {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorOverridePropertiesTest extends AbstractCompileTestCase {

	/**
	 * Ensure can override {@link Property} for the {@link DeployedOffice}.
	 */
	public void testOverrideOfficeProperties() {

		// Enables override of properties
		this.enableOverrideProperties();

		// Record the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OVERRIDE_OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can override {@link Property} for the
	 * {@link OfficeFloorManagedObjectSource}.
	 */
	public void testOverrideManagedObjectSourceProperties() {

		// Enables override of properties
		this.enableOverrideProperties();

		// Record the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OVERRIDE_MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", CompileManagedObject.class.getName(), "additional",
				"another");
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can override the {@link ManagedObjectPool} properties.
	 */
	public void testOverrideManagedObjectPoolProperties() {

		// Create the managed object pool factory
		ManagedObjectPoolFactory poolFactory = this.createMock(ManagedObjectPoolFactory.class);
		TestManagedObjectPoolSource.managedObjectPoolFactory = poolFactory;

		// Enables override of properties
		this.enableOverrideProperties();

		// Record the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedObjectBuilder<?> moBuilder = this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.recordReturn(moBuilder, moBuilder.setManagedObjectPool(poolFactory), null);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can override {@link Property} for the {@link OfficeFloorTeam}.
	 */
	public void testOverrideTeamProperties() {

		// Enables override of properties
		this.enableOverrideProperties();

		// Record the OfficeFloor
		this.record_init();
		TeamBuilder<?> team = this.record_officeFloorBuilder_addTeam("OVERRIDE_TEAM", new TestTeamSource(), "value",
				"override", "additional", "another");
		team.setTeamSize(50);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can override {@link Property} for the {@link OfficeFloorExecutive}.
	 */
	public void testOverrideExecutiveProperties() {

		// Enables override of properties
		this.enableOverrideProperties();

		// Record the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_setExecutive(new TestExecutiveSource(), "value", "override", "additional",
				"another");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can override {@link Property} for the {@link OfficeFloorSupplier}.
	 */
	public void testOverrideSupplierSourceProperties() {

		// Enables override of properties
		this.enableOverrideProperties();

		// Record the OfficeFloor
		CompileSupplierSource.propertyValue = null;
		this.record_supplierSetup();
		this.record_init();

		// Compile the OfficeFloor
		this.compile(true);
		assertEquals("Should override property value", "SUPPLY_OVERRIDE", CompileSupplierSource.propertyValue);
	}

	public static class CompileManagedObject {
	}

	@TestSource
	public static class TestOfficeSource extends AbstractOfficeSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty("value");
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {
			assertEquals("Incorrect overridden value", "override", context.getProperty("value"));
			assertEquals("Incorrect additional value", "another", context.getProperty("additional"));
		}
	}

	@TestSource
	public static class TestTeamSource extends AbstractTeamSource implements Team {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty("value");
		}

		@Override
		public Team createTeam(TeamSourceContext context) throws Exception {
			return this; // required in sourcing (for type)
		}

		@Override
		public void startWorking() {
			fail("Should not be invoked");
		}

		@Override
		public void assignJob(Job job) {
			fail("Should not be invoked");
		}

		@Override
		public void stopWorking() {
			fail("Should not be invoked");
		}
	}

	@TestSource
	public static class TestManagedObjectPoolSource extends AbstractManagedObjectPoolSource {

		private static ManagedObjectPoolFactory managedObjectPoolFactory;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty("value");
		}

		@Override
		protected void loadMetaData(MetaDataContext context) throws Exception {
			assertEquals("Incorrect overridden value", "override",
					context.getManagedObjectPoolSourceContext().getProperty("value"));
			assertEquals("Incorrect additional value", "another",
					context.getManagedObjectPoolSourceContext().getProperty("additional"));
			context.setPooledObjectType(CompileManagedObject.class);
			context.setManagedObjectPoolFactory(managedObjectPoolFactory);
		}
	}

	@TestSource
	public static class TestExecutiveSource extends AbstractExecutiveSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty("value");
		}

		@Override
		public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
			assertEquals("Incorrect overridden value", "override", context.getProperty("value"));
			assertEquals("Incorrect additional value", "another", context.getProperty("additional"));
			return new DefaultExecutive();
		}
	}

	@TestSource
	public static class CompileSupplierSource extends AbstractSupplierSource {

		private static String propertyValue = null;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {
			propertyValue = context.getProperty("override");
		}

		@Override
		public void terminate() {
			// nothing to terminate
		}
	}

}
