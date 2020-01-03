package net.officefloor.model.impl.section;

import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;

/**
 * Tests setting the {@link SubSectionInputModel} private/public.
 * 
 * @author Daniel Sagenschneider
 */
public class SetSubSectionInputPublicTest extends
		AbstractSectionChangesTestCase {

	/**
	 * Private {@link SubSectionInputModel}.
	 */
	private SubSectionInputModel inputPrivate;

	/**
	 * Public {@link SubSectionInputModel}.
	 */
	private SubSectionInputModel inputPublic;

	/**
	 * Public {@link SubSectionInputModel} with public name.
	 */
	private SubSectionInputModel inputPublicWithName;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.impl.AbstractOperationsTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the setup inputs
		SubSectionModel subSection = this.model.getSubSections().get(0);
		this.inputPrivate = subSection.getSubSectionInputs().get(0);
		this.inputPublic = subSection.getSubSectionInputs().get(1);
		this.inputPublicWithName = subSection.getSubSectionInputs().get(2);
	}

	/**
	 * Ensure no change if the {@link SubSectionModel} not in the
	 * {@link SectionModel}.
	 */
	public void testSubSectionInputNotInSection() {
		SubSectionInputModel input = new SubSectionInputModel("NOT_IN_SECTION",
				Object.class.getName(), false, null);
		Change<SubSectionInputModel> change = this.operations
				.setSubSectionInputPublic(true, null, input);
		this.assertChange(change, input,
				"Set sub section input NOT_IN_SECTION public", false,
				"Sub section input NOT_IN_SECTION not in section");
	}

	/**
	 * Ensures can set a {@link SubSectionInputModel} to be public.
	 */
	public void testSetSubSectionInputPublic() {
		Change<SubSectionInputModel> changeA = this.operations
				.setSubSectionInputPublic(true, "PUBLIC_NAME",
						this.inputPrivate);
		Change<SubSectionInputModel> changeB = this.operations
				.setSubSectionInputPublic(true, null, this.inputPublicWithName);
		this.assertChanges(changeA, changeB);
	}

	/**
	 * Ensures can set a {@link SubSectionInputModel} to be private.
	 */
	public void testSetSubSectionInputPrivate() {
		Change<SubSectionInputModel> changeA = this.operations
				.setSubSectionInputPublic(false, null, this.inputPublic);
		Change<SubSectionInputModel> changeB = this.operations
				.setSubSectionInputPublic(false, "IGNORED",
						this.inputPublicWithName);
		this.assertChanges(changeA, changeB);
	}
}