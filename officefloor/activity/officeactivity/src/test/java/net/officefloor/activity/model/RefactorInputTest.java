package net.officefloor.activity.model;

import net.officefloor.model.change.Change;

/**
 * Tests refactoring the {@link ActivityInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorInputTest extends AbstractActivityChangesTestCase {

	/**
	 * {@link ActivityInputModel}.
	 */
	private ActivityInputModel input;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.input = this.model.getActivityInputs().get(1);
	}

	/**
	 * Ensure handle no change.
	 */
	public void testNoChange() {

		// Refactor with same details
		Change<ActivityInputModel> change = this.operations.refactorInput(this.input, "INPUT", String.class.getName());

		// Validate change
		this.assertChange(change, null, "Refactor Input", true);
	}

	/**
	 * Ensure handle change to all details.
	 */
	public void testChange() {

		// Refactor with changes
		Change<ActivityInputModel> change = this.operations.refactorInput(this.input, "CHANGE", null);

		// Validate change
		this.assertChange(change, null, "Refactor Input", true);
	}

	/**
	 * Ensure keeps unique {@link ActivityInputModel} name.
	 */
	public void testInputAlreadyExists() {

		// Refactor to existing name
		Change<ActivityInputModel> change = this.operations.refactorInput(this.input, "EXISTS", null);

		// Validate change
		this.assertChange(change, null, "Refactor Input", true);
	}

}