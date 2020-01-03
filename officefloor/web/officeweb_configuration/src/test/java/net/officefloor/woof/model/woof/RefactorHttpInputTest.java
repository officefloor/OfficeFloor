package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;

/**
 * Tests refactoring the {@link WoofHttpInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorHttpInputTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofHttpInputModel}.
	 */
	private WoofHttpInputModel httpInput;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.httpInput = this.model.getWoofHttpInputs().get(0);
	}

	/**
	 * Ensure handle no change.
	 */
	public void testNoChange() {

		// Refactor with same details
		Change<WoofHttpInputModel> change = this.operations.refactorHttpInput(this.httpInput, "/input", "POST", false);

		// Validate change
		this.assertChange(change, null, "Refactor HTTP Input", true);
	}

	/**
	 * Ensure handle change to all details.
	 */
	public void testChange() {

		// Refactor with changes
		Change<WoofHttpInputModel> change = this.operations.refactorHttpInput(this.httpInput, "/change", "PUT", true);

		// Validate change
		this.assertChange(change, null, "Refactor HTTP Input", true);
	}

}