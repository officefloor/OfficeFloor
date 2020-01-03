package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.WoofResourceModel;

/**
 * Tests refactoring the {@link WoofResourceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorResourceTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofResourceModel}.
	 */
	private WoofResourceModel resource;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.resource = this.model.getWoofResources().get(0);
	}

	/**
	 * Ensure can refactor.
	 */
	public void testRefactor() {

		/*
		 * Expecting the refactor method delegates to the change resource path
		 * method which will handle all refactoring. Therefore only providing
		 * simple test to ensure delegating.
		 */

		// Refactor template to change path
		Change<WoofResourceModel> change = this.operations.refactorResource(this.resource, "/resource.png");

		// Validate the change
		this.assertChange(change, this.resource, "Refactor Resource", true);
	}

}