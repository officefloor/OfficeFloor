/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.construct.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.construct.MockConstruct;
import net.officefloor.frame.impl.construct.MockConstruct.OfficeMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawBoundManagedObjectInstanceMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.MockConstruct.RawOfficeMetaDataMockBuilder;
import net.officefloor.frame.impl.construct.asset.AssetManagerRegistry;
import net.officefloor.frame.impl.construct.escalation.EscalationFlowFactory;
import net.officefloor.frame.impl.construct.flow.FlowMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.impl.construct.office.RawOfficeMetaData;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectExtensionExtractorMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawAdministrationMetaDataFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawAdministrationMetaDataFactoryTest extends OfficeFrameTestCase {

	/**
	 * Name of the {@link Administration}.
	 */
	private static final String ADMINISTRATION_NAME = "ADMIN";

	/**
	 * {@link AdministrationConfiguration}.
	 */
	private AdministrationBuilderImpl<?, ?, ?> configuration = new AdministrationBuilderImpl<>(ADMINISTRATION_NAME,
			String.class, () -> null);

	/**
	 * Name of the {@link Office}.
	 */
	private static final String OFFICE_NAME = "OFFICE";

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaDataMockBuilder officeMetaData = MockConstruct.mockOfficeMetaData(OFFICE_NAME);

	/**
	 * {@link RawOfficeMetaData}.
	 */
	private final RawOfficeMetaDataMockBuilder rawOfficeMetaData = MockConstruct.mockRawOfficeMetaData(OFFICE_NAME);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this.createMock(OfficeFloorIssues.class);

	/**
	 * Ensures issue if not {@link Administration} name.
	 */
	public void testNoAdministrationName() {

		// Record
		this.issues.addIssue(AssetType.OFFICE, OFFICE_NAME, "Administration added without a name");

		// Construct
		this.replayMockObjects();
		this.constructRawAdministration(false, new AdministrationBuilderImpl<>(null, Object.class, () -> null));
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no extension type.
	 */
	public void testNoExtensionType() {

		// Record
		this.record_issue("Administration " + ADMINISTRATION_NAME + " did not provide extension type");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministration(false, new AdministrationBuilderImpl<>(ADMINISTRATION_NAME, null, () -> null));
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link AdministrationFactory}.
	 */
	public void testNoAdministrationFactory() {

		// Record
		this.record_issue("Administration " + ADMINISTRATION_NAME + " did not provide an AdministrationFactory");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministration(false,
				new AdministrationBuilderImpl<>(ADMINISTRATION_NAME, Object.class, null));
		this.verifyMockObjects();
	}

	/**
	 * Ensures any {@link Team} responsible.
	 */
	public void testAnyTeam() {

		// Record
		this.configuration.setResponsibleTeam(null);

		// Construct the administrators
		this.replayMockObjects();
		RawAdministrationMetaData admin = this.constructRawAdministration(true, this.configuration)[0];
		this.verifyMockObjects();

		// SHould be any team
		assertNull("Should be any team", admin.getAdministrationMetaData().getResponsibleTeam());
	}

	/**
	 * Ensures issue if unknown {@link Team}.
	 */
	public void testUnknownTeam() {

		// Record
		this.configuration.setResponsibleTeam("UNKNOWN");
		this.record_issue("Administration " + ADMINISTRATION_NAME + " team UNKNOWN can not be found");

		// Construct
		this.replayMockObjects();
		this.constructRawAdministration(false, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can construct with responsible {@link Team}.
	 */
	public void testResponsibleTeam() {

		// Record
		this.configuration.setResponsibleTeam("TEAM");
		TeamManagement team = this.rawOfficeMetaData.addTeam("TEAM");

		// Construct
		this.replayMockObjects();
		RawAdministrationMetaData admin = this.constructRawAdministration(true, this.configuration)[0];
		this.verifyMockObjects();

		// Ensure have responsible team
		assertSame("Incorrect responsible team", team, admin.getAdministrationMetaData().getResponsibleTeam());
	}

	/**
	 * Ensures issue if no {@link ManagedObject} name.
	 */
	public void testNoManagedObjectName() {

		// Record
		this.configuration.administerManagedObject(null);
		this.record_issue("Administration " + ADMINISTRATION_NAME + " specifying no name for managed object");

		// Construct
		this.replayMockObjects();
		this.constructRawAdministration(false, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link RawBoundManagedObjectMetaData}.
	 */
	public void testUnknownBoundManagedObject() {

		// Record
		this.configuration.administerManagedObject("UNKNOWN");
		this.record_issue("Managed Object UNKNOWN not available to Administration " + ADMINISTRATION_NAME);

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministration(false, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if {@link RawBoundManagedObjectMetaData} does not have
	 * extension interfaces.
	 */
	public void testBoundManagedObjectHasNoExtensionInterfaces() {

		// Record
		this.configuration.administerManagedObject("MO");
		this.rawOfficeMetaData.addScopeBoundManagedObject("MO");
		this.record_issue("Managed Object MO does not support extension type " + String.class.getName()
				+ " required by Administration " + ADMINISTRATION_NAME + " (ManagedObjectSource=MO)");

		// Construct the administrators
		this.replayMockObjects();
		this.constructRawAdministration(false, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if {@link RawBoundManagedObjectMetaData} does not support
	 * extension interface.
	 */
	public void testBoundManagedObjectNotSupportExtensionInterface() {

		// Record
		this.configuration.administerManagedObject("MO");
		this.rawOfficeMetaData.addScopeBoundManagedObject("MO").getRawBoundManagedObjectMetaData()
				.getRawManagedObjectBuilder().getMetaDataBuilder().addExtension(Integer.class, (mo) -> 1);
		this.record_issue("Managed Object MO does not support extension type " + String.class.getName()
				+ " required by Administration " + ADMINISTRATION_NAME + " (ManagedObjectSource=MO)");

		// Construct
		this.replayMockObjects();
		this.constructRawAdministration(false, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to construct {@link AdministrationMetaData}.
	 */
	public void testConstructAdministratorMetaData() throws Throwable {

		// Record
		this.configuration.administerManagedObject("MO");
		final String EXTENSION = "EXTENSION";
		RawBoundManagedObjectInstanceMetaDataMockBuilder<?, ?> boundManagedObject = this.rawOfficeMetaData
				.addScopeBoundManagedObject("MO");
		boundManagedObject.getRawBoundManagedObjectMetaData().getRawManagedObjectBuilder().getMetaDataBuilder()
				.addExtension(String.class, (mo) -> EXTENSION);

		// Test
		this.replayMockObjects();

		// Load managed object meta data
		boundManagedObject.getRawBoundManagedObjectMetaData().build();
		boundManagedObject.build().loadManagedObjectMetaData(AssetType.OFFICE, OFFICE_NAME,
				MockConstruct.mockAssetManagerRegistry(), 1, this.issues);

		// Construct the administrators
		RawAdministrationMetaData[] rawAdminMetaDatas = this.constructRawAdministration(true, this.configuration);
		assertEquals("Should just be the one administration", 1, rawAdminMetaDatas.length);
		RawAdministrationMetaData rawAdminMetaData = rawAdminMetaDatas[0];

		// Verify extension factory by extracting extension
		AdministrationMetaData<?, ?, ?> adminMetaData = rawAdminMetaData.getAdministrationMetaData();
		assertEquals("Incorrect number of administered managed objects", 1,
				adminMetaData.getManagedObjectExtensionExtractorMetaData().length);
		ManagedObjectExtensionExtractorMetaData<?> extensionMetaData = adminMetaData
				.getManagedObjectExtensionExtractorMetaData()[0];
		ManagedObjectMetaData<?> moMetaData = boundManagedObject.build().getManagedObjectMetaData();
		assertEquals("Incorrect extracted extension", EXTENSION,
				extensionMetaData.getManagedObjectExtensionExtractor().extractExtension(null, moMetaData));

		// Verify mocks
		this.verifyMockObjects();

		// Verify administration meta-data
		assertEquals("Incorrect name", "ADMIN", adminMetaData.getAdministrationName());
		assertNotNull("Must have admin factory", adminMetaData.getAdministrationFactory());
		assertNull("Allow any team", adminMetaData.getResponsibleTeam());
		assertEquals("Incorrect administered managed object",
				boundManagedObject.getRawBoundManagedObjectMetaData().build().getManagedObjectIndex(),
				extensionMetaData.getManagedObjectIndex());

		// Ensure also include administered managed object
		RawBoundManagedObjectMetaData[] administeredManagedObjects = rawAdminMetaData
				.getRawBoundManagedObjectMetaData();
		assertEquals("Incorrect number of administered managed objects", 1, administeredManagedObjects.length);
		assertSame("Incorrect administered managed object",
				boundManagedObject.getRawBoundManagedObjectMetaData().build(), administeredManagedObjects[0]);
	}

	/**
	 * Ensure issue if no {@link Governance} name for linking to
	 * {@link Administration}.
	 */
	public void testLinkNoGovernanceName() {

		// Record
		this.configuration.linkGovernance(0, null);
		this.record_issue("Governance linked without a name");

		// Construct
		this.replayMockObjects();
		this.constructRawAdministration(false, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link Governance} linked to {@link Administration}.
	 */
	public void testLinkUnkownGovernance() {

		// Record
		this.configuration.linkGovernance(0, "UNKNOWN");
		this.record_issue("Can not find governance UNKNOWN");

		// Construct
		this.replayMockObjects();
		this.constructRawAdministration(false, this.configuration);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link Governance}.
	 */
	public void testLinkGovernance() {

		// Record
		this.configuration.linkGovernance(0, "GOVERNANCE");
		this.officeMetaData.getProcessMetaData().getThreadMetaData().addGovernanceMetaData("NOT_MATCH");
		this.officeMetaData.getProcessMetaData().getThreadMetaData().addGovernanceMetaData("GOVERNANCE");

		// Construct the administration
		this.replayMockObjects();
		RawAdministrationMetaData[] adminMetaDatas = this.constructRawAdministration(true, this.configuration);
		AdministrationMetaData<?, ?, ?> adminMetaData = adminMetaDatas[0].getAdministrationMetaData();
		this.verifyMockObjects();

		// Verify the governance
		int threadIndex = adminMetaData.translateGovernanceIndexToThreadIndex(0);
		assertEquals("Incorrect thread governance index", 1, threadIndex);
	}

	/**
	 * Records an issue regarding the {@link Administration}.
	 * 
	 * @param issueDescription Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(AssetType.ADMINISTRATOR, ADMINISTRATION_NAME, issueDescription);
	}

	/**
	 * Constructs the {@link AdministrationMetaData}.
	 * 
	 * @param isCreate      Indicates if create the
	 *                      {@link RawAdministrationMetaData}.
	 * @param configuration {@link AdministrationConfiguration} instances.
	 * @return {@link AdministrationMetaData}.
	 */
	private RawAdministrationMetaData[] constructRawAdministration(boolean isCreate,
			AdministrationConfiguration<?, ?, ?>... configuration) {

		// Construct the meta-data
		RawAdministrationMetaData[] rawAdministrations = new RawAdministrationMetaDataFactory(
				this.officeMetaData.build(), new FlowMetaDataFactory(this.officeMetaData.build()),
				new EscalationFlowFactory(this.officeMetaData.build()), this.rawOfficeMetaData.build().getTeams())
						.constructRawAdministrationMetaData("Asset", "pre", configuration,
								this.rawOfficeMetaData.build().getOfficeScopeManagedObjects(), AssetType.OFFICE,
								OFFICE_NAME, new AssetManagerRegistry(null, null), 1, this.issues);

		// Ensure correct number created
		if (isCreate) {
			assertNotNull("Should create the raw administration", rawAdministrations);
			assertEquals("Incorrect number of created meta-data", configuration.length, rawAdministrations.length);
		} else {
			assertNull("Should not create raw administration meta-data", rawAdministrations);
		}

		// Return the raw administrations
		return rawAdministrations;
	}

}
