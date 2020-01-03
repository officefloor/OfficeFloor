package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;

/**
 * Tests refactoring the {@link WoofHttpContinuationModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorHttpContinuationTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofHttpContinuationModel}.
	 */
	private WoofHttpContinuationModel httpContinuation;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.httpContinuation = this.model.getWoofHttpContinuations().get(0);
	}

	/**
	 * Ensure handle no change.
	 */
	public void testNoChange() {

		// Refactor with same details
		Change<WoofHttpContinuationModel> change = this.operations.refactorHttpContinuation(this.httpContinuation,
				"/path", false);

		// Validate change
		this.assertChange(change, null, "Refactor HTTP Continuation", true);
	}

	/**
	 * Ensure handle change to all details.
	 */
	public void testChange() {

		// Refactor the section with same details
		Change<WoofHttpContinuationModel> change = this.operations.refactorHttpContinuation(this.httpContinuation,
				"/change", true);

		// Validate change
		this.assertChange(change, null, "Refactor HTTP Continuation", true);
	}

}