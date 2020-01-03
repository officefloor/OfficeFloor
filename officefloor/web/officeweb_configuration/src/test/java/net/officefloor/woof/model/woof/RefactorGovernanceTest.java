package net.officefloor.woof.model.woof;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.PropertyModel;
import net.officefloor.woof.model.woof.WoofGovernanceModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionOutputModel;

/**
 * Tests refactoring the {@link WoofGovernanceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorGovernanceTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofGovernanceModel}.
	 */
	private WoofGovernanceModel governance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.governance = this.model.getWoofGovernances().get(0);
	}

	/**
	 * Ensure handle no change.
	 */
	public void testNoChange() {

		// Create the governance type
		final GovernanceType<?, ?> governanceType = this
				.createMock(GovernanceType.class);
		this.replayMockObjects();

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.a").setValue("value.a");
		properties.addProperty("name.b").setValue("value.b");

		// Refactor the governance with same details
		Change<WoofGovernanceModel> change = this.operations
				.refactorGovernance(this.governance, "GOVERNANCE",
						"net.example.ExampleGovernanceSource", properties,
						governanceType);

		// Validate change
		this.assertChange(change, null, "Refactor Governance", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure handle change to all details.
	 */
	public void testChange() {

		// Create the governance type
		final GovernanceType<?, ?> governanceType = this
				.createMock(GovernanceType.class);
		this.replayMockObjects();

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.1").setValue("value.a");
		properties.addProperty("name.b").setValue("value.2");

		// Refactor the governance with changed details
		Change<WoofGovernanceModel> change = this.operations
				.refactorGovernance(this.governance, "CHANGE",
						"net.example.ChangeGovernanceSource", properties,
						governanceType);

		// Validate change
		this.assertChange(change, null, "Refactor Governance", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure handle remove {@link PropertyModel}, {@link WoofSectionInputModel}
	 * and {@link WoofSectionOutputModel} instances.
	 */
	public void testRemoveDetails() {

		// Create the governance type
		final GovernanceType<?, ?> governanceType = this
				.createMock(GovernanceType.class);
		this.replayMockObjects();

		// Refactor the governance with removed details
		Change<WoofGovernanceModel> change = this.operations
				.refactorGovernance(this.governance, "REMOVE",
						"net.example.RemoveGovernanceSource", null,
						governanceType);

		// Validate change
		this.assertChange(change, null, "Refactor Governance", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure handle adding {@link PropertyModel}, {@link WoofSectionInputModel}
	 * and {@link WoofSectionOutputModel} instances.
	 */
	public void testAddDetails() {

		// Create the governance type
		final GovernanceType<?, ?> governanceType = this
				.createMock(GovernanceType.class);
		this.replayMockObjects();

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.a").setValue("value.a");
		properties.addProperty("name.b").setValue("value.b");
		properties.addProperty("name.c").setValue("value.c");
		properties.addProperty("name.d").setValue("value.d");

		// Refactor the governance with added details
		Change<WoofGovernanceModel> change = this.operations
				.refactorGovernance(this.governance, "ADD",
						"net.example.AddGovernanceSource", properties,
						governanceType);

		// Validate change
		this.assertChange(change, null, "Refactor Governance", true);

		// Verify
		this.verifyMockObjects();
	}

}