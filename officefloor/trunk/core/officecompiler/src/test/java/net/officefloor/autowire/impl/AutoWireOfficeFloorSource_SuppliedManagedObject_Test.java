/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.autowire.impl;

import java.sql.Connection;

import org.junit.Ignore;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.autowire.impl.supplier.MockTypeManagedObjectSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceContext;
import net.officefloor.autowire.supplier.SuppliedManagedObject;
import net.officefloor.autowire.supplier.SuppliedManagedObjectFlowType;
import net.officefloor.autowire.supplier.SuppliedManagedObjectTeamType;
import net.officefloor.autowire.supplier.SuppliedManagedObjectType;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link SuppliedManagedObject} configuration of the
 * {@link AutoWireOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore("TODO addSupplier functionality")
public class AutoWireOfficeFloorSource_SuppliedManagedObject_Test extends
		AbstractAutoWireOfficeFloorSourceTestCase {

	/**
	 * Ensure can use {@link SuppliedManagedObjectType}.
	 */
	public void testSuppliedManagedObject() throws Exception {

		final AutoWire autoWire = new AutoWire(Connection.class);
		final ClassManagedObjectSource mos = new ClassManagedObjectSource();

		// Add the supplier
		this.addSupplier(new SupplierInit() {
			@Override
			public void supply(SupplierSourceContext context) throws Exception {
				AutoWireObject object = context.addManagedObject(mos, null,
						autoWire);
				object.addProperty(
						ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
						MockRawObject.class.getName());
			}
		});

		// Record
		this.recordTeam();
		this.recordOffice(autoWire);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				autoWire, mos, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject mo = this
				.recordManagedObject(source, autoWire);
		this.recordOfficeObject(mo, autoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can selectively use a {@link SuppliedManagedObjectType}.
	 */
	public void testSelectivelyUseSuppliedManagedObjects() throws Exception {

		final MockTypeManagedObjectSource one = new MockTypeManagedObjectSource(
				String.class);
		final MockTypeManagedObjectSource two = new MockTypeManagedObjectSource(
				Connection.class);
		final MockTypeManagedObjectSource three = new MockTypeManagedObjectSource(
				Integer.class);

		// Add the supplier
		this.addSupplier(new SupplierInit() {
			@Override
			public void supply(SupplierSourceContext context) throws Exception {
				one.addAsManagedObject(context, null);
				two.addAsManagedObject(context, null);
				three.addAsManagedObject(context, null);
			}
		});

		// Record (only two is loaded)
		this.recordTeam();
		this.recordOffice(two.getAutoWire()); // selectively only use two
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				two.getAutoWire(), two, 0, 0);
		OfficeFloorManagedObject mo = this.recordManagedObject(source,
				two.getAutoWire());
		this.recordOfficeObject(mo, two.getAutoWire());

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can link {@link SuppliedManagedObjectFlowType}.
	 */
	public void testLinkSuppliedManagedObjectFlow() throws Exception {

		final MockTypeManagedObjectSource source = new MockTypeManagedObjectSource(
				String.class);
		source.addFlow("flow", Integer.class);

		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapFlow("flow", "SECTION", "INPUT");
			}
		};

		// Add the supplier
		this.addSupplier(new SupplierInit() {
			@Override
			public void supply(SupplierSourceContext context) throws Exception {
				source.addAsManagedObject(context, wirer);
			}
		});

		// Record
		this.recordTeam();
		// TODO supplied managed object type
		this.registerOfficeInput("SECTION", "INPUT");
		this.recordOffice();
		OfficeFloorManagedObjectSource mos = this.recordManagedObjectSource(
				source.getAutoWire(), source, 0, 0);
		this.recordInputManagedObject(mos, source.getAutoWire());
		this.recordManagedObjectFlow(mos, "flow", "SECTION", "INPUT");

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can link {@link SuppliedManagedObjectTeamType} to
	 * {@link OfficeFloorTeam}.
	 */
	public void testLink_SuppliedManagedObjectTeam_To_OfficeFloorTeam()
			throws Exception {
		this.doLinkSuppliedManagedObjectTeamTest(null, null);
	}

	/**
	 * Ensure can link qualified {@link SuppliedManagedObjectTeamType} to
	 * qualified {@link OfficeFloorTeam}.
	 */
	public void testLink_QualifiedSuppliedManagedObjectTeam_To_QualifiedOfficeFloorTeam()
			throws Exception {
		this.doLinkSuppliedManagedObjectTeamTest("QUALIFIED", "QUALIFIED");
	}

	/**
	 * Ensure can link qualified {@link SuppliedManagedObjectTeamType} to
	 * {@link OfficeFloorTeam}.
	 */
	public void testLink_QualifiedSuppliedManagedObjectTeam_To_OfficeFloorTeam()
			throws Exception {
		this.doLinkSuppliedManagedObjectTeamTest("QUALIFIED", null);
	}

	/**
	 * Undertakes testing to link {@link ManagedObjectTeam} to
	 * {@link OfficeFloorTeam}.
	 * 
	 * @param managedObjectTeamQualifier
	 *            Qualifier for the {@link ManagedObjectTeam}.
	 * @param teamQualifier
	 *            Qualifier for the {@link OfficeFloorTeam}.
	 */
	private void doLinkSuppliedManagedObjectTeamTest(
			final String managedObjectTeamQualifier, String teamQualifier)
			throws Exception {

		final AutoWire managedObjectTeamAutoWire = new AutoWire(
				managedObjectTeamQualifier, Connection.class.getName());
		final AutoWire officeFloorTeamAutoWire = new AutoWire(teamQualifier,
				Connection.class.getName());

		final MockTypeManagedObjectSource source = new MockTypeManagedObjectSource(
				String.class);
		source.addTeam("team");

		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapTeam("team", managedObjectTeamAutoWire);
			}
		};

		// Add the supplier
		this.addSupplier(new SupplierInit() {
			@Override
			public void supply(SupplierSourceContext context) throws Exception {
				source.addAsManagedObject(context, wirer);
			}
		});

		// Provide team
		this.source.assignTeam("TEAM", officeFloorTeamAutoWire);

		// Record
		this.recordTeam();
		// TODO supplied managed object type
		this.recordOffice();
		this.recordTeam(new String[] {}, officeFloorTeamAutoWire);
		OfficeFloorManagedObjectSource mos = this.recordManagedObjectSource(
				source.getAutoWire(), source, 0, 0);
		this.recordInputManagedObject(mos, source.getAutoWire());
		this.recordManagedObjectTeam(mos, "team", officeFloorTeamAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure issue if no {@link OfficeFloorTeam} for
	 * {@link SuppliedManagedObjectTeamType}.
	 */
	public void testNoTeamForSuppliedManagedObjectTeam() throws Exception {

		final MockTypeManagedObjectSource source = new MockTypeManagedObjectSource(
				String.class);
		source.addTeam("team");

		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapTeam("team", new AutoWire(Connection.class));
			}
		};

		// Add the supplier
		this.addSupplier(new SupplierInit() {
			@Override
			public void supply(SupplierSourceContext context) throws Exception {
				source.addAsManagedObject(context, wirer);
			}
		});

		// No OfficeFloor Team

		// Record
		this.recordTeam();
		// TODO supplied managed object type
		this.recordOffice(source.getAutoWire());
		OfficeFloorManagedObjectSource mos = this.recordManagedObjectSource(
				source.getAutoWire(), source, 0, 0);
		this.recordInputManagedObject(mos, source.getAutoWire());

		// Record issue as no corresponding OfficeFloor Team
		this.deployer
				.addIssue(
						"No OfficeFloorTeam available for ManagedObjectTeam 'team' (qualifier=null, type="
								+ Connection.class.getName() + ")",
						AssetType.MANAGED_OBJECT, source.getAutoWire()
								.getQualifiedType());

		// Test
		this.doSourceOfficeFloorTest();
	}

}