package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.WoofResourceModel;

/**
 * Tests changing the {@link WoofResourceModel} resource path.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeResourcePathTest extends AbstractWoofChangesTestCase {

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
	 * Ensure able make no change to resource path.
	 */
	public void testNotChangeResourcePath() {

		// Change resource path to be same
		Change<WoofResourceModel> change = this.operations.changeResourcePath(this.resource, "/resource.html");

		// Validate the change
		this.assertChange(change, this.resource, "Change Resource Path", true);
	}

	/**
	 * Ensure can change to unique resource path.
	 */
	public void testChangeResourcePath() {

		// Change template to unique path
		Change<WoofResourceModel> change = this.operations.changeResourcePath(this.resource, "/resource.gif");

		// Validate the change
		this.assertChange(change, this.resource, "Change Resource Path", true);
	}

	/**
	 * Ensure can change to non-unique resource path.
	 */
	public void testNonUniqueResourcePath() {

		// Change template to non-unique path
		Change<WoofResourceModel> change = this.operations.changeResourcePath(this.resource, "/resource.png");

		// Validate the change
		this.assertChange(change, this.resource, "Change Resource Path", false,
				"Resource already exists for '/resource.png'");
	}

	/**
	 * Ensure no change if attempt to clear resource path.
	 */
	public void testClearResourcePath() {

		// Change to attempting to clear resource path
		Change<WoofResourceModel> change = this.operations.changeResourcePath(this.resource, null);

		// Validate the change
		this.assertChange(change, this.resource, "Change Resource Path", false, "Must provide resource path");
	}

}