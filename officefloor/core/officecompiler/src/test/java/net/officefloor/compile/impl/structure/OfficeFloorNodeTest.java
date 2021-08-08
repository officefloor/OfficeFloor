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

package net.officefloor.compile.impl.structure;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.compile.spi.managedobject.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectDependency;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectFlow;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectTeam;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.impl.office.OfficeModelOfficeSource;
import net.officefloor.plugin.managedobject.singleton.Singleton;

/**
 * Tests the {@link OfficeFloorNode}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorNodeTest extends AbstractStructureTestCase {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private static final String OFFICE_FLOOR_LOCATION = "OFFICE_FLOOR";

	/**
	 * Mapping of {@link Profiler} by {@link Office} name.
	 */
	private final Map<String, Profiler> profilers = new HashMap<String, Profiler>();

	/**
	 * {@link OfficeFloorNode} implementation.
	 */
	private final OfficeFloorNode node = new OfficeFloorNodeImpl(OfficeFloorSource.class.getName(), null,
			OFFICE_FLOOR_LOCATION, this.nodeContext, this.profilers);

	/**
	 * Ensure allow {@link OfficeFloorSource} to report issues via the
	 * {@link OfficeFloorDeployer}.
	 */
	public void testAddIssue() {

		// Record adding the issue
		this.issues.recordIssue(this.node.getNodeName(), this.node.getClass(), "TEST_ISSUE");

		// Add the issue
		this.replayMockObjects();
		this.node.addIssue("TEST_ISSUE");
		this.verifyMockObjects();
	}

	/**
	 * Ensure allow {@link OfficeFloorSource} to report issues via the
	 * {@link OfficeFloorDeployer}.
	 */
	public void testAddIssueWithCause() {

		final Exception failure = new Exception("cause");

		// Record adding the issue
		this.issues.recordIssue(this.node.getNodeName(), this.node.getClass(), "TEST_ISSUE", failure);

		// Add the issue
		this.replayMockObjects();
		this.node.addIssue("TEST_ISSUE", failure);
		this.verifyMockObjects();
	}

	/**
	 * Tests adding an {@link OfficeFloorTeam}.
	 */
	public void testAddOfficeFloorTeam() {
		// Add two different teams verifying details
		this.replayMockObjects();
		OfficeFloorTeam team = this.addTeam(this.node, "TEAM", null);
		assertNotNull("Must have team", team);
		assertEquals("Incorrect team name", "TEAM", team.getOfficeFloorTeamName());
		assertNotSame("Should obtain another managed object", team, this.addTeam(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the {@link OfficeFloorTeam} twice.
	 */
	public void testAddOfficeFloorTeamTwice() {

		// Record issue in adding the team twice
		this.issues.recordIssue("TEAM", TeamNodeImpl.class, "Team TEAM already added");

		// Add the team twice
		this.replayMockObjects();
		OfficeFloorTeam teamFirst = this.addTeam(this.node, "TEAM", null);
		OfficeFloorTeam teamSecond = this.addTeam(this.node, "TEAM", null);
		this.verifyMockObjects();

		// Should be the same team
		assertSame("Should be same team on adding twice", teamFirst, teamSecond);
	}

	/**
	 * Tests adding an {@link OfficeFloorManagedObjectSource}.
	 */
	public void testAddOfficeFloorManagedObjectSource() {
		// Add two different managed object sources verifying details
		this.replayMockObjects();
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO", null);
		assertNotNull("Must have managed object source", moSource);
		assertEquals("Incorrect managed object source name", "MO", moSource.getOfficeFloorManagedObjectSourceName());
		assertNotSame("Should obtain another managed object source", moSource,
				this.addManagedObjectSource(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the {@link OfficeFloorManagedObjectSource} twice.
	 */
	public void testAddOfficeFloorManagedObjectSourceTwice() {

		// Record issue in adding the managed object source twice
		this.issues.recordIssue("MO", ManagedObjectSourceNodeImpl.class, "Managed Object Source MO already added");

		// Add the managed object twice
		this.replayMockObjects();
		OfficeFloorManagedObjectSource moSourceFirst = this.addManagedObjectSource(this.node, "MO", null);
		OfficeFloorManagedObjectSource moSourceSecond = this.addManagedObjectSource(this.node, "MO", null);
		this.verifyMockObjects();

		// Should be the same managed object source
		assertSame("Should be same managed object source on adding twice", moSourceFirst, moSourceSecond);
	}

	/**
	 * Tests adding an {@link OfficeFloorManagedObjectSource} instance.
	 */
	public void testAddOfficeFloorManagedObjectSourceInstance() {
		// Add two different managed object sources verifying details
		this.replayMockObjects();
		OfficeFloorManagedObjectSource moSource = this.node.addManagedObjectSource("MO", new Singleton("TEST"));
		assertNotNull("Must have managed object source", moSource);
		assertEquals("Incorrect managed object source name", "MO", moSource.getOfficeFloorManagedObjectSourceName());
		assertNotSame("Should obtain another managed object source", moSource,
				this.node.addManagedObjectSource("ANOTHER", new Singleton("ANOTHER")));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the {@link OfficeFloorManagedObjectSource} instance
	 * twice.
	 */
	public void testAddOfficeFloorManagedObjectSourceInstanceTwice() {

		// Record issue in adding the managed object source twice
		this.issues.recordIssue("MO", ManagedObjectSourceNodeImpl.class, "Managed Object Source MO already added");

		// Add the managed object twice
		this.replayMockObjects();
		OfficeFloorManagedObjectSource moSourceFirst = this.node.addManagedObjectSource("MO", new Singleton("ONE"));
		OfficeFloorManagedObjectSource moSourceSecond = this.node.addManagedObjectSource("MO", new Singleton("TWO"));
		this.verifyMockObjects();

		// Should be the same managed object source
		assertSame("Should be same managed object source on adding twice", moSourceFirst, moSourceSecond);
	}

	/**
	 * Tests adding an {@link OfficeFloorInputManagedObject}.
	 */
	public void testAddOfficeFloorInputManagedObject() {
		// Add two different input managed objects verifying details
		this.replayMockObjects();
		OfficeFloorInputManagedObject inputMo = this.node.addInputManagedObject("INPUT", String.class.getName());
		assertNotNull("Must have input managed object", inputMo);
		assertEquals("Incorrect input managed object name", "INPUT", inputMo.getOfficeFloorInputManagedObjectName());
		assertNotSame("Should obtain another input managed object", inputMo,
				this.node.addInputManagedObject("ANOTHER", String.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if adding the {@link OfficeFloorInputManagedObject} twice.
	 */
	public void testAddOfficeFloorInputManagedObjectTwice() {

		// Record issue in adding twice
		this.issues.recordIssue("INPUT", InputManagedObjectNodeImpl.class, "Input Managed Object INPUT already added");

		// Add the input managed object twice
		this.replayMockObjects();
		OfficeFloorInputManagedObject moFirst = this.node.addInputManagedObject("INPUT", String.class.getName());
		OfficeFloorInputManagedObject moSecond = this.node.addInputManagedObject("INPUT", String.class.getName());
		this.verifyMockObjects();

		// Should be the same input managed object
		assertSame("Should be same input managed object on adding twice", moFirst, moSecond);
	}

	/**
	 * Tests adding an {@link OfficeFloorManagedObject}.
	 */
	public void testAddOfficeFloorManagedObject() {
		// Add two different managed objects verifying details
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO_SOURCE", null);
		this.replayMockObjects();
		OfficeFloorManagedObject mo = moSource.addOfficeFloorManagedObject("MO", ManagedObjectScope.THREAD);
		assertNotNull("Must have managed object", mo);
		assertEquals("Incorrect managed object name", "MO", mo.getOfficeFloorManagedObjectName());
		assertNotSame("Should obtain another managed object", mo,
				moSource.addOfficeFloorManagedObject("ANOTHER", ManagedObjectScope.THREAD));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the {@link OfficeFloorManagedObject} twice.
	 */
	public void testAddOfficeFloorManagedObjectTwice() {

		// Record issue in adding the managed object twice
		this.issues.recordIssue("MO", ManagedObjectNodeImpl.class, "Managed Object MO already added");

		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO_SOURCE", null);

		// Add the managed object twice
		this.replayMockObjects();
		OfficeFloorManagedObject moFirst = moSource.addOfficeFloorManagedObject("MO", ManagedObjectScope.FUNCTION);
		OfficeFloorManagedObject moSecond = moSource.addOfficeFloorManagedObject("MO", ManagedObjectScope.FUNCTION);
		this.verifyMockObjects();

		// Should be the same managed object
		assertSame("Should be same managed object on adding twice", moFirst, moSecond);
	}

	/**
	 * Ensure issue if adding the {@link OfficeFloorManagedObject} twice by
	 * different {@link OfficeFloorManagedObjectSource} instances.
	 */
	public void testAddOfficeFloorManagedObjectTwiceByDifferentSources() {

		// Record issue in adding the managed object twice
		this.issues.recordIssue("MO", ManagedObjectNodeImpl.class, "Managed Object MO already added");

		OfficeFloorManagedObjectSource moSourceOne = this.addManagedObjectSource(this.node, "MO_SOURCE_ONE", null);
		OfficeFloorManagedObjectSource moSourceTwo = this.addManagedObjectSource(this.node, "MO_SOURCE_TWO", null);

		// Add the managed object twice by different sources
		this.replayMockObjects();
		OfficeFloorManagedObject moFirst = moSourceOne.addOfficeFloorManagedObject("MO", ManagedObjectScope.FUNCTION);
		OfficeFloorManagedObject moSecond = moSourceTwo.addOfficeFloorManagedObject("MO", ManagedObjectScope.FUNCTION);
		this.verifyMockObjects();

		// Should be the same managed object
		assertSame("Should be same managed object on adding twice", moFirst, moSecond);
	}

	/**
	 * Tests adding an {@link OfficeFloorSupplier}.
	 */
	public void testAddOfficeFloorSupplier() {
		// Add two different supplier sources verifying details
		this.replayMockObjects();
		OfficeFloorSupplier supplier = this.addSupplier(this.node, "SUPPLIER", null);
		assertNotNull("Must have supplier", supplier);
		assertEquals("Incorrect supplier name", "SUPPLIER", supplier.getOfficeFloorSupplierName());
		assertNotSame("Should obtain another supplier", supplier, this.addSupplier(this.node, "ANOTTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the {@link OfficeFloorSupplier} twice.
	 */
	public void testAddOfficeFloorSupplierTwice() {

		// Record issue in adding the supplier twice
		this.issues.recordIssue("SUPPLIER", SupplierNodeImpl.class, "Supplier SUPPLIER already added");

		// Add the supplier twice
		this.replayMockObjects();
		OfficeFloorSupplier supplierFirst = this.addSupplier(this.node, "SUPPLIER", null);
		OfficeFloorSupplier supplierSecond = this.addSupplier(this.node, "SUPPLIER", null);
		this.verifyMockObjects();

		// Should be the same supplier
		assertSame("Should be same supplier on adding twice", supplierFirst, supplierSecond);
	}

	/**
	 * Tests adding a supplied {@link OfficeFloorManagedObjectSource}.
	 */
	public void testAddSuppliedOfficeFloorManagedObjectSource() {
		// Add two different supplied managed object sources verifying details
		OfficeFloorSupplier supplier = this.addSupplier(this.node, "SUPPLIER", null);
		this.replayMockObjects();
		OfficeFloorManagedObjectSource mos = supplier.getOfficeFloorManagedObjectSource("MO_SOURCE", null,
				Connection.class.getName());
		assertNotNull("Must have managed object source", mos);
		assertEquals("Incorrect managed object name", "MO_SOURCE", mos.getOfficeFloorManagedObjectSourceName());
		assertSame("Should obtain same managed object source", mos,
				supplier.getOfficeFloorManagedObjectSource("ANOTHER", null, Connection.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the supplied {@link OfficeFloorManagedObjectSource}
	 * twice.
	 */
	public void testAddSuppliedOfficeFloorManagedObjectSourceTwice() {

		// Managed object sources are re-used, so no issue in adding twice
		OfficeFloorSupplier supplier = this.addSupplier(this.node, "SUPPLIER", null);

		// Add the managed object twice
		this.replayMockObjects();
		OfficeFloorManagedObjectSource mosFirst = supplier.getOfficeFloorManagedObjectSource("MO_SOURCE", null,
				Connection.class.getName());
		OfficeFloorManagedObjectSource mosSecond = supplier.getOfficeFloorManagedObjectSource("MO_SOURCE", null,
				Connection.class.getName());
		this.verifyMockObjects();

		// Should be the same managed object source
		assertSame("Should be same managed object source on adding twice", mosFirst, mosSecond);
	}

	/**
	 * Ensure issue if adding the {@link OfficeFloorManagedObjectSource} twice by
	 * different {@link OfficeFloorSupplier} instances.
	 */
	public void testAddOfficeFloorManagedObjectSourceTwiceByDifferentSuppliers() {

		// Record issue in adding the managed object source twice
		this.issues.recordIssue("MO_SOURCE", ManagedObjectSourceNodeImpl.class,
				"Managed Object Source MO_SOURCE already added");

		OfficeFloorSupplier supplierOne = this.addSupplier(this.node, "SUPPLIER_ONE", null);
		OfficeFloorSupplier supplierTwo = this.addSupplier(this.node, "SUPPLIER_TWO", null);

		// Add the managed object twice by different sources
		this.replayMockObjects();
		OfficeFloorManagedObjectSource moSourceFirst = supplierOne.getOfficeFloorManagedObjectSource("MO_SOURCE", null,
				Connection.class.getName());
		OfficeFloorManagedObjectSource moSourceSecond = supplierTwo.getOfficeFloorManagedObjectSource("MO_SOURCE", null,
				String.class.getName());
		this.verifyMockObjects();

		// Should be the same managed object
		assertSame("Should be same managed object source on adding twice", moSourceFirst, moSourceSecond);
	}

	/**
	 * Tests adding a {@link DeployedOffice}.
	 */
	public void testDeployOffice() {
		// Add two different offices verifying details
		this.replayMockObjects();
		DeployedOffice office = this.addDeployedOffice(this.node, "OFFICE", null);
		assertNotNull("Must have office", office);
		assertEquals("Incorrect office name", "OFFICE", office.getDeployedOfficeName());
		assertNotSame("Should obtain another office", office, this.addDeployedOffice(this.node, "ANOTHER", null));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the {@link DeployedOffice} twice.
	 */
	public void testAddDeployedOfficeTwice() {

		// Record issue in adding the office twice
		this.issues.recordIssue("OFFICE", OfficeNodeImpl.class, "Office OFFICE already added");

		// Add the office twice
		this.replayMockObjects();
		DeployedOffice officeFirst = this.addDeployedOffice(this.node, "OFFICE", null);
		DeployedOffice officeSecond = this.addDeployedOffice(this.node, "OFFICE", null);
		this.verifyMockObjects();

		// Should be the same office
		assertSame("Should be same office on adding twice", officeFirst, officeSecond);
	}

	/**
	 * Tests adding a {@link DeployedOffice} instance.
	 */
	public void testDeployOfficeInstance() {
		// Add two different offices verifying details
		this.replayMockObjects();
		DeployedOffice office = this.node.addDeployedOffice("OFFICE", new OfficeModelOfficeSource(), "location");
		assertNotNull("Must have office", office);
		assertEquals("Incorrect office name", "OFFICE", office.getDeployedOfficeName());
		assertNotSame("Should obtain another office", office,
				this.node.addDeployedOffice("ANOTHER", new OfficeModelOfficeSource(), "location"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if adding the {@link DeployedOffice} instance twice.
	 */
	public void testAddDeployedOfficeInstanceTwice() {

		// Record issue in adding the office twice
		this.issues.recordIssue("OFFICE", OfficeNodeImpl.class, "Office OFFICE already added");

		// Add the office twice
		this.replayMockObjects();
		DeployedOffice officeFirst = this.node.addDeployedOffice("OFFICE", new OfficeModelOfficeSource(), "location");
		DeployedOffice officeSecond = this.node.addDeployedOffice("OFFICE", new OfficeModelOfficeSource(), "location");
		this.verifyMockObjects();

		// Should be the same office
		assertSame("Should be same office on adding twice", officeFirst, officeSecond);
	}

	/**
	 * Ensure can bind the {@link OfficeFloorManagedObjectSource} for the
	 * {@link OfficeFloorInputManagedObject}.
	 */
	public void testBoundManagedObjectSourceForInputManagedObject() {

		// Record issue if trying to bind twice
		this.issues.recordIssue("INPUT_MO", InputManagedObjectNodeImpl.class,
				"Managed Object Source already bound for Input Managed Object 'INPUT_MO'");

		this.replayMockObjects();

		// Bind the managed object source for input managed object
		OfficeFloorInputManagedObject inputMo = this.node.addInputManagedObject("INPUT_MO", String.class.getName());
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO", null);
		inputMo.setBoundOfficeFloorManagedObjectSource(moSource);
		InputManagedObjectNode inputMoNode = (InputManagedObjectNode) inputMo;
		assertEquals("Ensure correct bound managed object source", moSource,
				inputMoNode.getBoundManagedObjectSourceNode());

		// Ensure can only bind once
		inputMo.setBoundOfficeFloorManagedObjectSource(this.addManagedObjectSource(this.node, "ANOTHER", null));
		assertEquals("Should be only bound to first", moSource, inputMoNode.getBoundManagedObjectSourceNode());

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link ManagingOffice} to the {@link DeployedOffice}.
	 */
	public void testLinkManagingOfficeToDeployedOffice() {

		// Record already being linked
		this.issues.recordIssue("MO.MANAGING_OFFICE", ManagingOfficeNodeImpl.class,
				"Managing Office MANAGING_OFFICE linked more than once");

		this.replayMockObjects();

		// Link
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO", null);
		ManagingOffice managingOffice = moSource.getManagingOffice();
		DeployedOffice office = this.addDeployedOffice(this.node, "OFFICE", null);
		this.node.link(managingOffice, office);
		assertOfficeLink("managing office -> deployed office", managingOffice, office);

		// Ensure only can link once
		this.node.link(managingOffice, this.addDeployedOffice(this.node, "ANOTHER", null));
		assertOfficeLink("Can only link once", managingOffice, office);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeFloorManagedObjectSource} to an
	 * {@link OfficeFloorInputManagedObject}.
	 */
	public void testLinkOfficeFloorManagedObjectSourceToOfficeFloorInputManagedObject() {

		// Record already being linked
		this.issues.recordIssue("MO_SOURCE", ManagedObjectSourceNodeImpl.class,
				"Managed object source MO_SOURCE already linked to an input managed object");

		this.replayMockObjects();

		// Link
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO_SOURCE", null);
		OfficeFloorInputManagedObject inputMo = this.node.addInputManagedObject("INPUT", String.class.getName());
		this.node.link(moSource, inputMo);
		assertSourceInput("office floor managed object source -> office floor input managed object", moSource, inputMo);

		// Ensure can link only once
		this.node.link(moSource, this.node.addInputManagedObject("ANOTHER", String.class.getName()));
		assertSourceInput("Can only link once", moSource, inputMo);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeFloorManagedObjectSource}
	 * {@link ManagedObjectTeam} to the {@link OfficeFloorTeam}.
	 */
	public void testLinkManagedObjectTeamToOfficeFloorTeam() {

		// Record already being linked
		this.issues.recordIssue("MO.TEAM", ManagedObjectTeamNodeImpl.class,
				"Managed Object Source Team TEAM linked more than once");

		this.replayMockObjects();

		// Link
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO", null);
		OfficeFloorManagedObjectTeam team = moSource.getOfficeFloorManagedObjectTeam("TEAM");
		OfficeFloorTeam officeFloorTeam = this.addTeam(this.node, "OFFICE_FLOOR_TEAM", null);
		this.node.link(team, officeFloorTeam);
		assertTeamLink("managed object source team -> office floor team", team, officeFloorTeam);

		// Ensure only can link once
		this.node.link(team, this.addTeam(this.node, "ANOTHER", null));
		assertTeamLink("Can only link once", team, officeFloorTeam);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeFloorManagedObject}
	 * {@link ManagedObjectDependency} to the {@link OfficeFloorManagedObject}.
	 */
	public void testLinkManagedObjectDependencyToOfficeFloorManagedObject() {

		// Record already being linked
		this.issues.recordIssue("MO.DEPENDENCY", ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO_SOURCE", null);
		OfficeFloorManagedObject mo = moSource.addOfficeFloorManagedObject("MO", ManagedObjectScope.PROCESS);
		OfficeFloorManagedObjectDependency dependency = mo.getOfficeFloorManagedObjectDependency("DEPENDENCY");
		OfficeFloorManagedObjectSource moSourceTarget = this.addManagedObjectSource(this.node, "MO_SOURCE_TARGET",
				null);
		OfficeFloorManagedObject moTarget = moSourceTarget.addOfficeFloorManagedObject("MO_TARGET",
				ManagedObjectScope.PROCESS);
		this.node.link(dependency, moTarget);
		assertObjectLink("managed object dependency -> office floor managed object", dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, moSourceTarget.addOfficeFloorManagedObject("ANOTHER", ManagedObjectScope.PROCESS));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeFloorManagedObject}
	 * {@link ManagedObjectDependency} to the {@link OfficeFloorInputManagedObject}.
	 */
	public void testLinkManagedObjectDependencyToOfficeFloorInputManagedObject() {

		// Record already being linked
		this.issues.recordIssue("MO.DEPENDENCY", ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO_SOURCE", null);
		OfficeFloorManagedObject mo = moSource.addOfficeFloorManagedObject("MO", ManagedObjectScope.PROCESS);
		OfficeFloorManagedObjectDependency dependency = mo.getOfficeFloorManagedObjectDependency("DEPENDENCY");
		OfficeFloorInputManagedObject inputMoTarget = this.node.addInputManagedObject("INPUT_TARGET",
				String.class.getName());
		this.node.link(dependency, inputMoTarget);
		assertObjectLink("managed object dependency -> office floor input managed object", dependency, inputMoTarget);

		// Ensure only can link once
		this.node.link(dependency, this.node.addInputManagedObject("ANOTHER", String.class.getName()));
		assertObjectLink("Can only link once", dependency, inputMoTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link Input {@link ManagedObjectDependency} to the
	 * {@link OfficeFloorManagedObject}.
	 */
	public void testLinkInputManagedObjectDependencyToOfficeFloorManagedObject() {

		// Record already being linked
		this.issues.recordIssue("MO_SOURCE.DEPENDENCY", ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO_SOURCE", null);
		OfficeFloorManagedObjectDependency dependency = moSource
				.getInputOfficeFloorManagedObjectDependency("DEPENDENCY");
		OfficeFloorManagedObjectSource moSourceTarget = this.addManagedObjectSource(this.node, "MO_SOURCE_TARGET",
				null);
		OfficeFloorManagedObject moTarget = moSourceTarget.addOfficeFloorManagedObject("MO_TARGET",
				ManagedObjectScope.PROCESS);
		this.node.link(dependency, moTarget);
		assertObjectLink("input managed object dependency -> office floor managed object", dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, moSourceTarget.addOfficeFloorManagedObject("ANOTHER", ManagedObjectScope.PROCESS));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeFloorManagedObjectSource}
	 * {@link ManagedObjectFlow} to the {@link DeployedOfficeInput}.
	 */
	public void testLinkManagedObjectFlowToDeployedOfficeInput() {

		// Record already being linked
		this.issues.recordIssue("MO.FLOW", ManagedObjectFlowNodeImpl.class,
				"Managed Object Source Flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO", null);
		OfficeFloorManagedObjectFlow flow = moSource.getOfficeFloorManagedObjectFlow("FLOW");
		DeployedOffice office = this.addDeployedOffice(this.node, "OFFICE", null);
		DeployedOfficeInput input = office.getDeployedOfficeInput("SECTION", "INPUT");
		this.node.link(flow, input);
		assertFlowLink("managed object flow -> office input", flow, input);

		// Ensure only can link once
		this.node.link(flow, office.getDeployedOfficeInput("SECTION", "ANOTHER"));
		assertFlowLink("Can only link once", flow, input);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeTeam} to the {@link OfficeFloorTeam}.
	 */
	public void testLinkOfficeTeamToOfficeFloorTeam() {

		// Record already being linked
		this.issues.recordIssue("OFFICE.TEAM", OfficeTeamNodeImpl.class, "Office Team TEAM linked more than once");

		this.replayMockObjects();

		// Link
		DeployedOffice office = this.addDeployedOffice(this.node, "OFFICE", null);
		OfficeTeam team = office.getDeployedOfficeTeam("TEAM");
		OfficeFloorTeam officeFloorTeam = this.addTeam(this.node, "OFFICE_FLOOR_TEAM", null);
		this.node.link(team, officeFloorTeam);
		assertTeamLink("office team -> office floor team", team, officeFloorTeam);

		// Ensure only can link once
		this.node.link(team, this.addTeam(this.node, "ANOTHER", null));
		assertTeamLink("Can only link once", team, officeFloorTeam);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeObject} to the {@link OfficeFloorManagedObject}.
	 */
	public void testLinkOfficeObjectToOfficeFloorManagedObject() {

		// Record already being linked
		this.issues.recordIssue("OFFICE.OBJECT", OfficeObjectNodeImpl.class,
				"Office Object OBJECT linked more than once");

		this.replayMockObjects();

		// Link
		DeployedOffice office = this.addDeployedOffice(this.node, "OFFICE", null);
		OfficeObject object = office.getDeployedOfficeObject("OBJECT");
		OfficeFloorManagedObjectSource moSource = this.addManagedObjectSource(this.node, "MO_SOURCE_TARGET", null);
		OfficeFloorManagedObject mo = moSource.addOfficeFloorManagedObject("MO_TARGET", ManagedObjectScope.THREAD);
		this.node.link(object, mo);
		assertObjectLink("office required object -> office floor managed object", object, mo);

		// Ensure only can link once
		this.node.link(object, moSource.addOfficeFloorManagedObject("ANOTHER", ManagedObjectScope.THREAD));
		assertObjectLink("Can only link once", object, mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link OfficeObject} to the
	 * {@link OfficeFloorInputManagedObject}.
	 */
	public void testLinkOfficeObjectToOfficeFloorInputManagedObject() {

		// Record already being linked
		this.issues.recordIssue("OFFICE.OBJECT", OfficeObjectNodeImpl.class,
				"Office Object OBJECT linked more than once");

		this.replayMockObjects();

		// Link
		DeployedOffice office = this.addDeployedOffice(this.node, "OFFICE", null);
		OfficeObject object = office.getDeployedOfficeObject("OBJECT");
		OfficeFloorInputManagedObject inputMo = this.node.addInputManagedObject("INPUT_TARGET", String.class.getName());
		this.node.link(object, inputMo);
		assertObjectLink("office required object -> office floor input managed object", object, inputMo);

		// Ensure only can link once
		this.node.link(object, this.node.addInputManagedObject("ANOTHER", String.class.getName()));
		assertObjectLink("Can only link once", object, inputMo);

		this.verifyMockObjects();
	}

}
