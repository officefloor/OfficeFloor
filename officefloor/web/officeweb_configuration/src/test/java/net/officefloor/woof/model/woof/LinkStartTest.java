package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofStartModel;
import net.officefloor.woof.model.woof.WoofStartToWoofSectionInputModel;

/**
 * Tests linking from the {@link WoofStartModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkStartTest extends AbstractWoofChangesTestCase {

	/**
	 * Index of start A.
	 */
	private static final int A = 0;

	/**
	 * Index of start B.
	 */
	private static final int B = 1;

	/**
	 * Ensure can link to {@link WoofProcedureModel}.
	 */
	public void testLinkToProcedure() {
		this.doLinkToProcedure(A);
	}

	/**
	 * Ensure link overrides other links for {@link WoofStartToWoofProcedureModel}.
	 */
	public void testLinkOverrideToProcedure() {
		this.doLinkToProcedure(B);
	}

	/**
	 * Undertakes linking to a {@link WoofProcedureModel}.
	 * 
	 * @param startIndex {@link WoofStartModel} index.
	 */
	private void doLinkToProcedure(int startIndex) {

		// Obtain the items to link
		WoofStartModel start = this.model.getWoofStarts().get(startIndex);
		WoofProcedureModel procedure = this.model.getWoofProcedures().get(B);

		// Link the start to procedure
		Change<WoofStartToWoofProcedureModel> change = this.operations.linkStartToProcedure(start, procedure);

		// Validate change
		this.assertChange(change, null, "Link Start to Procedure", true);
	}

	/**
	 * Ensure can remove the {@link WoofStartToWoofProcedureModel}.
	 */
	public void testRemoveToProcedure() {

		// Obtain the link to remove
		WoofStartToWoofProcedureModel link = this.model.getWoofStarts().get(B).getWoofProcedure();

		// Remove the link
		Change<WoofStartToWoofProcedureModel> change = this.operations.removeStartToProcedure(link);

		// Validate change
		this.assertChange(change, null, "Remove Start to Procedure", true);
	}

	/**
	 * Ensure can link to {@link WoofSectionInputModel}.
	 */
	public void testLinkToSectionInput() {
		this.doLinkToSectionInput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofStartToWoofSectionInputModel}.
	 */
	public void testLinkOverrideToSectionInput() {
		this.doLinkToSectionInput(B);
	}

	/**
	 * Undertakes linking to a {@link WoofSectionInputModel}.
	 * 
	 * @param startIndex {@link WoofStartModel} index.
	 */
	private void doLinkToSectionInput(int startIndex) {

		// Obtain the items to link
		WoofStartModel start = this.model.getWoofStarts().get(startIndex);
		WoofSectionInputModel sectionInput = this.model.getWoofSections().get(B).getInputs().get(0);

		// Link the start to section input
		Change<WoofStartToWoofSectionInputModel> change = this.operations.linkStartToSectionInput(start, sectionInput);

		// Validate change
		this.assertChange(change, null, "Link Start to Section Input", true);
	}

	/**
	 * Ensure can remove the {@link WoofStartToWoofSectionInputModel}.
	 */
	public void testRemoveToSectionInput() {

		// Obtain the link to remove
		WoofStartToWoofSectionInputModel link = this.model.getWoofStarts().get(B).getWoofSectionInput();

		// Remove the link
		Change<WoofStartToWoofSectionInputModel> change = this.operations.removeStartToSectionInput(link);

		// Validate change
		this.assertChange(change, null, "Remove Start to Section Input", true);
	}

}