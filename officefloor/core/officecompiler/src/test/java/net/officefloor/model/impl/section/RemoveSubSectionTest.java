package net.officefloor.model.impl.section;

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionModel;

/**
 * Tests removing the {@link SubSectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveSubSectionTest extends AbstractSectionChangesTestCase {

	/**
	 * Use specific setup file.
	 */
	public RemoveSubSectionTest() {
		super(true);
	}

	/**
	 * Ensure no change if {@link SubSectionModel} not in the
	 * {@link SectionModel}.
	 */
	public void testRemoveSubSectionNotInSection() {
		SubSectionModel subSection = new SubSectionModel("NOT_IN_SECTION",
				"net.example.ExampleSectionSource", "LOCATION");
		Change<SubSectionModel> change = this.operations
				.removeSubSection(subSection);
		this.assertChange(change, subSection,
				"Remove sub section NOT_IN_SECTION", false,
				"Sub section NOT_IN_SECTION not in section");
	}

	/**
	 * Ensure can remove {@link SubSectionModel} when other
	 * {@link SubSectionModel} instances.
	 */
	public void testRemoveSubSectionWhenOtherSubSections() {
		SubSectionModel subSection = this.model.getSubSections().get(1);
		Change<SubSectionModel> change = this.operations
				.removeSubSection(subSection);
		this.assertChange(change, subSection,
				"Remove sub section SUB_SECTION_B", true);
	}

	/**
	 * Ensure can remove {@link SubSectionModel} with {@link ConnectionModel}
	 * instances.
	 */
	public void testRemoveSubSectionWithConnections() {
		SubSectionModel subSection = this.model.getSubSections().get(0);
		Change<SubSectionModel> change = this.operations
				.removeSubSection(subSection);
		this.assertChange(change, subSection,
				"Remove sub section SUB_SECTION_A", true);
	}

}