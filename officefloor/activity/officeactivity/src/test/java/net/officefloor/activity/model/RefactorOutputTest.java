package net.officefloor.activity.model;

import net.officefloor.model.change.Change;

/**
 * Tests refactoring the {@link ActivityOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorOutputTest extends AbstractActivityChangesTestCase {

	/**
	 * {@link ActivityOutputModel}.
	 */
	private ActivityOutputModel output;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.output = this.model.getActivityOutputs().get(1);
	}

	/**
	 * Ensure can refactor.
	 */
	public void testRefactor() {
		
		// Refactor template to change path
		Change<ActivityOutputModel> change = this.operations.refactorOutput(this.output, "CHANGE",
				String.class.getName());

		// Validate the change
		this.assertChange(change, this.output, "Refactor Output", true);
	}

	/**
	 * Ensure not able to refactor to an existing {@link ActivityOutputModel}.
	 */
	public void testOutputAlreadyExists() {

		// Change to a existing output
		Change<ActivityOutputModel> change = this.operations.refactorOutput(this.output, "EXISTS", null);

		// Validate the change
		this.assertChange(change, this.output, "Refactor Output", true);
	}

}