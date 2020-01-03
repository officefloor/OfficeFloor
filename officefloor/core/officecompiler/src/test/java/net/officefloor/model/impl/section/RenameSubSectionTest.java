package net.officefloor.model.impl.section;

import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionModel;

/**
 * Tests renaming the {@link SubSectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RenameSubSectionTest extends AbstractSectionChangesTestCase {

	/**
	 * Ensures handles {@link SubSectionModel} not being in the
	 * {@link SectionModel}.
	 */
	public void testRenameSubSectionNotInSection() {
		SubSectionModel subSection = new SubSectionModel("NOT_IN_SECTION",
				null, null);
		Change<SubSectionModel> change = this.operations.renameSubSection(
				subSection, "NEW_NAME");
		this.assertChange(change, subSection,
				"Rename sub section NOT_IN_SECTION to NEW_NAME", false,
				"Sub section NOT_IN_SECTION not in section");
	}

	/**
	 * Ensure can rename the {@link SubSectionModel}.
	 */
	public void testRenameSubSection() {
		SubSectionModel subSection = this.model.getSubSections().get(0);
		Change<SubSectionModel> change = this.operations.renameSubSection(
				subSection, "NEW_NAME");
		this.assertChange(change, subSection,
				"Rename sub section OLD_NAME to NEW_NAME", true);
	}

	/**
	 * Ensures on renaming the {@link SubSectionModel} that order is maintained.
	 */
	public void testRenameSubSectionCausingSubSectionOrderChange() {
		this.useTestSetupModel();
		SubSectionModel subSection = this.model.getSubSections().get(0);
		Change<SubSectionModel> change = this.operations.renameSubSection(
				subSection, "SUB_SECTION_C");
		this.assertChange(change, subSection,
				"Rename sub section SUB_SECTION_A to SUB_SECTION_C", true);
	}

}