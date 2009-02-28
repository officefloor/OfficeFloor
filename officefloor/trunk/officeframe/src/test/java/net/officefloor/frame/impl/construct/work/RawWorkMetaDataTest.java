/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.construct.work;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.administrator.RawBoundAdministratorMetaData;
import net.officefloor.frame.impl.construct.administrator.RawBoundAdministratorMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.construct.office.RawOfficeMetaData;
import net.officefloor.frame.impl.execute.administrator.AdministratorIndexImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedWorkAdministratorConfiguration;
import net.officefloor.frame.internal.configuration.LinkedWorkManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link RawWorkMetaDataImpl}.
 * 
 * @author Daniel
 */
public class RawWorkMetaDataTest<W extends Work> extends OfficeFrameTestCase {

	/**
	 * Name of the {@link Work}.
	 */
	private static final String WORK_NAME = "WORK";

	/**
	 * {@link WorkConfiguration}.
	 */
	@SuppressWarnings("unchecked")
	private final WorkConfiguration<W> configuration = this
			.createMock(WorkConfiguration.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this
			.createMock(OfficeFloorIssues.class);

	/**
	 * {@link RawOfficeMetaData}.
	 */
	private final RawOfficeMetaData rawOfficeMetaData = this
			.createMock(RawOfficeMetaData.class);

	/**
	 * {@link RawBoundManagedObjectMetaDataFactory}.
	 */
	private final RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory = this
			.createMock(RawBoundManagedObjectMetaDataFactory.class);

	/**
	 * {@link RawBoundAdministratorMetaDataFactory}.
	 */
	private final RawBoundAdministratorMetaDataFactory rawBoundAdministratorFactory = this
			.createMock(RawBoundAdministratorMetaDataFactory.class);

	/**
	 * {@link WorkFactory}.
	 */
	@SuppressWarnings("unchecked")
	private final WorkFactory<W> workFactory = this
			.createMock(WorkFactory.class);

	/**
	 * {@link Office} scoped {@link RawBoundManagedObjectMetaData} instances by
	 * their scoped names.
	 */
	private final Map<String, RawBoundManagedObjectMetaData<?>> officeScopeManagedObjects = new HashMap<String, RawBoundManagedObjectMetaData<?>>();

	/**
	 * {@link Office} scoped {@link RawBoundAdministratorMetaData} instances by
	 * their scoped names.
	 */
	private final Map<String, RawBoundAdministratorMetaData<?, ?>> officeScopeAdministrators = new HashMap<String, RawBoundAdministratorMetaData<?, ?>>();

	/**
	 * Ensure issue if no {@link Work} name.
	 */
	public void testNoWorkName() {

		// Record no work name
		this.recordReturn(this.configuration, this.configuration.getWorkName(),
				null);
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData
				.getOfficeName(), "OFFICE");
		this.issues.addIssue(AssetType.OFFICE, "OFFICE",
				"Work added to office without name");

		// Attempt to construct work
		this.replayMockObjects();
		this.constructRawWorkMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link WorkFactory}.
	 */
	public void testNoWorkFactory() {

		// Record no work factory
		this.recordReturn(this.configuration, this.configuration.getWorkName(),
				WORK_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getWorkFactory(), null);
		this.record_workIssue("WorkFactory not provided");

		// Attempt to construct work
		this.replayMockObjects();
		this.constructRawWorkMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct {@link Work} without {@link ManagedObject}
	 * instances or {@link Administrator} instances.
	 */
	public void testNoManagedObjectsOrAdministrators() {

		// Record no managed objects or administrators
		this.record_workNameFactory();
		this.record_workBoundManagedObjects();
		this.record_workLinkedManagedObjects();
		this.record_workBoundAdministrators();
		this.record_workLinkedAdministrators();

		// Fully construct work
		this.replayMockObjects();
		RawWorkMetaData<W> metaData = this.fullyConstructRawWorkMetaData();
		WorkMetaData<W> workMetaData = metaData.getWorkMetaData(this.issues);
		this.verifyMockObjects();

		// Ensure no managed objects
		assertEquals("Should not have managed object indexes", 0, workMetaData
				.getManagedObjectIndexes().length);
		assertEquals("Should not have managed object meta-data", 0,
				workMetaData.getManagedObjectMetaData().length);

		// Ensure no administrators
		assertEquals("Should not have administrator indexes", 0, workMetaData
				.getAdministratorIndexes().length);
		assertEquals("Should not have administrator meta-data", 0, workMetaData
				.getAdministratorMetaData().length);
	}

	/**
	 * Ensure can construct {@link Work} with a single {@link Work} bound
	 * {@link ManagedObject}.
	 */
	public void testSingleWorkBoundManagedObject() {

		final ManagedObjectIndex index = new ManagedObjectIndexImpl(
				ManagedObjectScope.WORK, 0);
		final RawBoundManagedObjectMetaData<?> rawMoMetaData = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final ManagedObjectMetaData<?> moMetaData = this
				.createMock(ManagedObjectMetaData.class);

		// Record single work bound managed object
		this.record_workNameFactory();
		this.record_workBoundManagedObjects("MO", rawMoMetaData);
		this.record_workLinkedManagedObjects();
		this.record_workBoundAdministrators();
		this.record_workLinkedAdministrators();
		this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectIndex(),
				index);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getDependencyKeys(),
				new Enum[0]);
		this.recordReturn(rawMoMetaData, rawMoMetaData
				.getManagedObjectMetaData(this.issues), moMetaData);

		// Construct work with task managed object
		this.replayMockObjects();
		RawWorkMetaData<W> metaData = this.constructRawWorkMetaData(true);
		metaData.constructRawWorkManagedObjectMetaData("MO", this.issues);
		WorkMetaData<W> workMetaData = metaData.getWorkMetaData(this.issues);
		this.verifyMockObjects();

		// Verify work meta-data
		ManagedObjectIndex[] moIndexes = workMetaData.getManagedObjectIndexes();
		assertEquals("Should have a single managed object index", 1,
				moIndexes.length);
		assertEquals("Incorrect managed object index", index, moIndexes[0]);
		ManagedObjectMetaData<?>[] workMoMetaData = workMetaData
				.getManagedObjectMetaData();
		assertEquals("Should have a work bound managed object meta-data", 1,
				workMoMetaData.length);
		assertEquals("Incorrect managed object meta-data", moMetaData,
				workMoMetaData[0]);
	}

	/**
	 * Ensure can construct {@link Work} with a single {@link Work} linked
	 * {@link ManagedObject}.
	 */
	public void testSingleWorkLinkedManagedObject() {

		final ManagedObjectIndex index = new ManagedObjectIndexImpl(
				ManagedObjectScope.THREAD, 0);
		final RawBoundManagedObjectMetaData<?> rawMoMetaData = this
				.createMock(RawBoundManagedObjectMetaData.class);

		// Record work linked managed object
		this.record_workNameFactory();
		this.record_workBoundManagedObjects();
		this.record_workLinkedManagedObjects("MO", "bound", rawMoMetaData);
		this.record_workBoundAdministrators();
		this.record_workLinkedAdministrators();
		this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectIndex(),
				index);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getDependencyKeys(),
				new Enum[0]);

		// Construct work with task managed object
		this.replayMockObjects();
		RawWorkMetaData<W> metaData = this.constructRawWorkMetaData(true);
		metaData.constructRawWorkManagedObjectMetaData("MO", this.issues);
		WorkMetaData<W> workMetaData = metaData.getWorkMetaData(this.issues);
		this.verifyMockObjects();

		// Verify work meta-data
		ManagedObjectIndex[] moIndexes = workMetaData.getManagedObjectIndexes();
		assertEquals("Should have a single managed object index", 1,
				moIndexes.length);
		assertEquals("Incorrect managed object index", index, moIndexes[0]);
		ManagedObjectMetaData<?>[] workMoMetaData = workMetaData
				.getManagedObjectMetaData();
		assertEquals("Should not have managed object meta-data as linked", 0,
				workMoMetaData.length);
	}

	/**
	 * Ensure can construct {@link Work} with a {@link ManagedObject} having a
	 * dependency.
	 */
	@SuppressWarnings("unchecked")
	public void testManagedObjectWithDependency() {

		// Managed object used directly by the task
		final ManagedObjectIndex indexOne = new ManagedObjectIndexImpl(
				ManagedObjectScope.THREAD, 0);
		final RawBoundManagedObjectMetaData<DependencyKey> rawMoMetaDataOne = this
				.createMock(RawBoundManagedObjectMetaData.class);

		// Managed object that is dependency
		final ManagedObjectIndex indexTwo = new ManagedObjectIndexImpl(
				ManagedObjectScope.PROCESS, 0);
		final RawBoundManagedObjectMetaData<?> rawMoMetaDataTwo = this
				.createMock(RawBoundManagedObjectMetaData.class);

		// Record work linked managed object
		this.record_workNameFactory();
		this.record_workBoundManagedObjects();
		this.record_workLinkedManagedObjects("ONE", "one", rawMoMetaDataOne,
				"TWO", "two", rawMoMetaDataTwo);
		this.record_workBoundAdministrators();
		this.record_workLinkedAdministrators();
		this.recordReturn(rawMoMetaDataOne, rawMoMetaDataOne
				.getManagedObjectIndex(), indexOne);
		this.recordReturn(rawMoMetaDataOne, rawMoMetaDataOne
				.getDependencyKeys(), new Enum[] { DependencyKey.MO });
		this.recordReturn(rawMoMetaDataOne, rawMoMetaDataOne
				.getDependency(DependencyKey.MO), rawMoMetaDataTwo);
		this.recordReturn(rawMoMetaDataTwo, rawMoMetaDataTwo
				.getManagedObjectIndex(), indexTwo);
		this.recordReturn(rawMoMetaDataTwo, rawMoMetaDataTwo
				.getDependencyKeys(), new Enum[0]);

		// Construct work with task managed object
		this.replayMockObjects();
		RawWorkMetaData<W> metaData = this.constructRawWorkMetaData(true);
		metaData.constructRawWorkManagedObjectMetaData("ONE", this.issues);
		WorkMetaData<W> workMetaData = metaData.getWorkMetaData(this.issues);
		this.verifyMockObjects();

		// Verify work meta-data
		ManagedObjectIndex[] moIndexes = workMetaData.getManagedObjectIndexes();
		assertEquals(
				"Should have two managed object indexes (task used direct and its dependency)",
				2, moIndexes.length);
		assertEquals("Incorrect task used managed object index", indexOne,
				moIndexes[0]);
		assertEquals("Incorrect dependency managed object index", indexTwo,
				moIndexes[1]);
		ManagedObjectMetaData<?>[] workMoMetaData = workMetaData
				.getManagedObjectMetaData();
		assertEquals("Should not have managed object meta-data as all linked",
				0, workMoMetaData.length);
	}

	/**
	 * Dependency keys of the {@link ManagedObject}.
	 */
	private static enum DependencyKey {
		MO
	}

	/**
	 * Ensure can construct {@link Work} with a single {@link Work} bound
	 * {@link Administrator}.
	 */
	public void testSingleWorkBoundAdministrator() {

		final AdministratorIndex index = new AdministratorIndexImpl(
				AdministratorScope.WORK, 0);
		final RawBoundAdministratorMetaData<?, ?> rawAdminMetaData = this
				.createMock(RawBoundAdministratorMetaData.class);
		final AdministratorMetaData<?, ?> adminMetaData = this
				.createMock(AdministratorMetaData.class);

		// Record single work bound administrator
		this.record_workNameFactory();
		this.record_workBoundManagedObjects();
		this.record_workLinkedManagedObjects();
		this.record_workBoundAdministrators("ADMIN", rawAdminMetaData);
		this.record_workLinkedAdministrators();
		this.recordReturn(rawAdminMetaData, rawAdminMetaData
				.getAdministratorIndex(), index);
		this.recordReturn(rawAdminMetaData, rawAdminMetaData
				.getAdministratorMetaData(), adminMetaData);

		// Construct work with task managed object
		this.replayMockObjects();
		RawWorkMetaData<W> metaData = this.constructRawWorkMetaData(true);
		metaData.constructRawWorkAdministratorMetaData("ADMIN", this.issues);
		WorkMetaData<W> workMetaData = metaData.getWorkMetaData(this.issues);
		this.verifyMockObjects();

		// Verify work meta-data
		AdministratorIndex[] adminIndexes = workMetaData
				.getAdministratorIndexes();
		assertEquals("Should have a single administrator index", 1,
				adminIndexes.length);
		assertEquals("Incorrect administrator index", index, adminIndexes[0]);
		AdministratorMetaData<?, ?>[] workAdminMetaData = workMetaData
				.getAdministratorMetaData();
		assertEquals("Should have a work bound administrator meta-data", 1,
				workAdminMetaData.length);
		assertEquals("Incorrect administrator meta-data", adminMetaData,
				workAdminMetaData[0]);
	}

	/**
	 * Ensure can construct {@link Work} with a single {@link Work} linked
	 * {@link Administrator}.
	 */
	public void testSingleWorkLinkedAdministrator() {

		final AdministratorIndex index = new AdministratorIndexImpl(
				AdministratorScope.PROCESS, 0);
		final RawBoundAdministratorMetaData<?, ?> rawAdminMetaData = this
				.createMock(RawBoundAdministratorMetaData.class);

		// Record work linked administrator
		this.record_workNameFactory();
		this.record_workBoundManagedObjects();
		this.record_workLinkedManagedObjects();
		this.record_workBoundAdministrators();
		this
				.record_workLinkedAdministrators("ADMIN", "bound",
						rawAdminMetaData);
		this.recordReturn(rawAdminMetaData, rawAdminMetaData
				.getAdministratorIndex(), index);

		// Construct work with task administrator
		this.replayMockObjects();
		RawWorkMetaData<W> metaData = this.constructRawWorkMetaData(true);
		metaData.constructRawWorkAdministratorMetaData("ADMIN", this.issues);
		WorkMetaData<W> workMetaData = metaData.getWorkMetaData(this.issues);
		this.verifyMockObjects();

		// Verify work meta-data
		AdministratorIndex[] adminIndexes = workMetaData
				.getAdministratorIndexes();
		assertEquals("Should have a single administrator index", 1,
				adminIndexes.length);
		assertEquals("Incorrect administrator index", index, adminIndexes[0]);
		AdministratorMetaData<?, ?>[] workAdminMetaData = workMetaData
				.getAdministratorMetaData();
		assertEquals("Should not have administrator meta-data as linked", 0,
				workAdminMetaData.length);
	}

	/**
	 * Records obtaining the {@link Work} name and {@link WorkFactory}.
	 */
	private void record_workNameFactory() {
		this.recordReturn(this.configuration, this.configuration.getWorkName(),
				WORK_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getWorkFactory(), this.workFactory);
	}

	/**
	 * Records bounding {@link ManagedObject} instances to the {@link Work}.
	 * 
	 * @param nameMoPairs
	 *            Name and {@link RawBoundManagedObjectMetaData} pairs.
	 */
	private void record_workBoundManagedObjects(Object... nameMoPairs) {

		// Obtain the listing of work bound names and managed objects
		int moCount = nameMoPairs.length / 2;
		String[] boundMoNames = new String[moCount];
		RawBoundManagedObjectMetaData<?>[] workBoundMo = new RawBoundManagedObjectMetaData<?>[moCount];
		for (int i = 0; i < nameMoPairs.length; i += 2) {
			int loadIndex = i / 2;
			boundMoNames[loadIndex] = (String) nameMoPairs[i];
			workBoundMo[loadIndex] = (RawBoundManagedObjectMetaData<?>) nameMoPairs[i + 1];
		}

		// Record bounding managed objects to work
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData
				.getOfficeScopeManagedObject(), this.officeScopeManagedObjects);
		ManagedObjectConfiguration<?>[] moConfiguration = new ManagedObjectConfiguration[moCount];
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectConfiguration(), moConfiguration);
		if (moConfiguration.length > 0) {
			Map<String, RawManagedObjectMetaData<?, ?>> officeRegisteredManagedObjects = new HashMap<String, RawManagedObjectMetaData<?, ?>>(
					0);
			this
					.recordReturn(this.rawOfficeMetaData,
							this.rawOfficeMetaData.getManagedObjectMetaData(),
							officeRegisteredManagedObjects);
			this.recordReturn(this.rawBoundManagedObjectFactory,
					this.rawBoundManagedObjectFactory
							.constructBoundManagedObjectMetaData(
									moConfiguration, this.issues,
									ManagedObjectScope.WORK, AssetType.WORK,
									WORK_NAME, officeRegisteredManagedObjects,
									this.officeScopeManagedObjects),
					workBoundMo);
			for (int i = 0; i < moCount; i++) {
				this.recordReturn(workBoundMo[i], workBoundMo[i]
						.getBoundManagedObjectName(), boundMoNames[i]);
			}
		}
	}

	/**
	 * Records linking {@link ManagedObject} instances to the {@link Work}.
	 * 
	 * @param workNameBoundNameMoListing
	 *            {@link ManagedObject} {@link Work} name, bound name and the
	 *            {@link RawBoundManagedObjectMetaData} within the
	 *            {@link Office} scope.
	 */
	private void record_workLinkedManagedObjects(
			Object... workNameBoundNameMoListing) {

		// Obtain the listing of work and bound names and managed objects
		int moCount = workNameBoundNameMoListing.length / 3;
		String[] workMoNames = new String[moCount];
		String[] boundMoNames = new String[moCount];
		RawBoundManagedObjectMetaData<?>[] workBoundMo = new RawBoundManagedObjectMetaData[moCount];
		for (int i = 0; i < workNameBoundNameMoListing.length; i += 3) {
			int loadIndex = i / 3;
			workMoNames[loadIndex] = (String) workNameBoundNameMoListing[i];
			boundMoNames[loadIndex] = (String) workNameBoundNameMoListing[i + 1];
			workBoundMo[loadIndex] = (RawBoundManagedObjectMetaData<?>) workNameBoundNameMoListing[i + 2];
		}

		// Record linking the managed objects
		LinkedWorkManagedObjectConfiguration[] moConfigurations = new LinkedWorkManagedObjectConfiguration[moCount];
		for (int i = 0; i < moCount; i++) {
			moConfigurations[i] = this
					.createMock(LinkedWorkManagedObjectConfiguration.class);
		}
		this.recordReturn(this.configuration, this.configuration
				.getLinkedManagedObjectConfiguration(), moConfigurations);
		if (moConfigurations.length > 0) {
			for (int i = 0; i < moCount; i++) {
				this.recordReturn(moConfigurations[i], moConfigurations[i]
						.getWorkManagedObjectName(), workMoNames[i]);
				this.recordReturn(moConfigurations[i], moConfigurations[i]
						.getBoundManagedObjectName(), boundMoNames[i]);

				// Ensure the managed object available in office scope
				this.officeScopeManagedObjects.put(boundMoNames[i],
						workBoundMo[i]);
			}
		}
	}

	/**
	 * Records bounding {@link Administrator} instances to the {@link Work}.
	 * 
	 * @param nameAdminPairs
	 *            Name and {@link RawBoundAdministratorMetaData} pairs.
	 */
	private void record_workBoundAdministrators(Object... nameAdminPairs) {

		// Obtain the listing of work bound names and administrators
		int adminCount = nameAdminPairs.length / 2;
		String[] boundAdminNames = new String[adminCount];
		RawBoundAdministratorMetaData<?, ?>[] workBoundAdmins = new RawBoundAdministratorMetaData[adminCount];
		for (int i = 0; i < nameAdminPairs.length; i += 2) {
			int loadIndex = i / 2;
			boundAdminNames[loadIndex] = (String) nameAdminPairs[i];
			workBoundAdmins[loadIndex] = (RawBoundAdministratorMetaData<?, ?>) nameAdminPairs[i + 1];
		}

		// Record bounding administrators to work
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData
				.getOfficeScopeAdministrator(), this.officeScopeAdministrators);
		AdministratorSourceConfiguration<?, ?>[] adminConfiguration = new AdministratorSourceConfiguration[adminCount];
		this.recordReturn(this.configuration, this.configuration
				.getAdministratorConfiguration(), adminConfiguration);
		if (adminConfiguration.length > 0) {
			Map<String, Team> officeTeams = new HashMap<String, Team>(0);
			this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData
					.getTeams(), officeTeams);
			this.recordReturn(this.rawBoundAdministratorFactory,
					this.rawBoundAdministratorFactory
							.constructRawBoundAdministratorMetaData(
									adminConfiguration, this.issues,
									AdministratorScope.WORK, AssetType.WORK,
									WORK_NAME, officeTeams, null),
					workBoundAdmins, new AbstractMatcher() {
						@Override
						public boolean matches(Object[] expected,
								Object[] actual) {
							// Ensure first arguments are as expected
							for (int i = 0; i < 6; i++) {
								assertEquals("Incorrect argument " + i,
										expected[i], actual[i]);
							}
							assertNotNull("Must have scope managed objects",
									actual[6]);
							return true; // matches if here
						}
					});
			for (int i = 0; i < adminCount; i++) {
				this.recordReturn(workBoundAdmins[i], workBoundAdmins[i]
						.getAdministratorName(), boundAdminNames[i]);
			}
		}
	}

	/**
	 * Records linking {@link ManagedObject} instances of the {@link Work}.
	 * 
	 * @param workNameBoundNameAdminListing
	 *            {@link ManagedObject} {@link Work} name, bound name and the
	 *            {@link RawBoundAdministratorMetaData} within the
	 *            {@link Office} scope.
	 */
	private void record_workLinkedAdministrators(
			Object... workNameBoundNameAdminListing) {

		// Obtain the listing of work and bound names and administrators
		int adminCount = workNameBoundNameAdminListing.length / 3;
		String[] workAdminNames = new String[adminCount];
		String[] boundAdminNames = new String[adminCount];
		RawBoundAdministratorMetaData<?, ?>[] workBoundAdmin = new RawBoundAdministratorMetaData[adminCount];
		for (int i = 0; i < workNameBoundNameAdminListing.length; i += 3) {
			int loadIndex = i / 3;
			workAdminNames[loadIndex] = (String) workNameBoundNameAdminListing[i];
			boundAdminNames[loadIndex] = (String) workNameBoundNameAdminListing[i + 1];
			workBoundAdmin[loadIndex] = (RawBoundAdministratorMetaData<?, ?>) workNameBoundNameAdminListing[i + 2];
		}

		// Record linking the administrators
		LinkedWorkAdministratorConfiguration[] adminConfigurations = new LinkedWorkAdministratorConfiguration[adminCount];
		for (int i = 0; i < adminCount; i++) {
			adminConfigurations[i] = this
					.createMock(LinkedWorkAdministratorConfiguration.class);
		}
		this.recordReturn(this.configuration, this.configuration
				.getLinkedAdministratorConfiguration(), adminConfigurations);
		if (adminConfigurations.length > 0) {
			for (int i = 0; i < adminCount; i++) {
				this.recordReturn(adminConfigurations[i],
						adminConfigurations[i].getWorkAdministratorName(),
						workAdminNames[i]);
				this.recordReturn(adminConfigurations[i],
						adminConfigurations[i].getBoundAdministratorName(),
						boundAdminNames[i]);

				// Ensure the administrator available in office scope
				this.officeScopeAdministrators.put(boundAdminNames[i],
						workBoundAdmin[i]);
			}
		}
	}

	/**
	 * Records a {@link Work} construction issue.
	 * 
	 * @param issueDescription
	 *            Issue description.
	 */
	private void record_workIssue(String issueDescription) {
		this.issues.addIssue(AssetType.WORK, WORK_NAME, issueDescription);
	}

	/**
	 * Construct the {@link RawWorkMetaData}.
	 * 
	 * @param isExpectConstruct
	 *            Is expect to construct.
	 * @return {@link RawWorkMetaData}.
	 */
	private RawWorkMetaData<W> constructRawWorkMetaData(
			boolean isExpectConstruct) {

		// Construct the raw work meta-data
		RawWorkMetaData<W> metaData = RawWorkMetaDataImpl.getFactory()
				.constructRawWorkMetaData(this.configuration, this.issues,
						this.rawOfficeMetaData,
						this.rawBoundManagedObjectFactory,
						this.rawBoundAdministratorFactory);
		if (isExpectConstruct) {
			assertNotNull("Expect to construct meta-data", metaData);
		} else {
			assertNull("Not expected to construct meta-data", metaData);
		}

		// Return the meta-data
		return metaData;
	}

	/**
	 * Fully constructs the {@link RawWorkMetaData} including making the
	 * {@link WorkMetaData} available. Will always expect to construct the
	 * {@link RawWorkMetaData}.
	 * 
	 * @return {@link RawWorkMetaData}.
	 */
	private RawWorkMetaData<W> fullyConstructRawWorkMetaData() {

		// Construct the raw work meta-data
		RawWorkMetaData<W> metaData = this.constructRawWorkMetaData(true);

		// Obtain the work meta-data
		WorkMetaData<W> workMetaData = metaData.getWorkMetaData(this.issues);
		assertNotNull("Should obtain work meta-data", workMetaData);
		WorkMetaData<W> anotherWorkMetaData = metaData
				.getWorkMetaData(this.issues);
		assertEquals("Work meta-data should be same on each getWorkMetaData",
				workMetaData, anotherWorkMetaData);

		// Return the meta-data
		return metaData;
	}
}
