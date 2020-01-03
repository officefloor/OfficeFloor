package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.WoofTemplateModel;

/**
 * Tests changing the {@link WoofTemplateModel} URI.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeTemplateApplicationPathTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofTemplateModel}.
	 */
	private WoofTemplateModel template;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.template = this.model.getWoofTemplates().get(0);
	}

	/**
	 * Ensure able make no change to application path.
	 */
	public void testNotChangeApplicationPath() {

		// Test
		this.replayMockObjects();

		// Change with same details
		Change<WoofTemplateModel> change = this.operations.changeApplicationPath(this.template, "/template",
				this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, this.template, "Change Template Application Path", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can change to unique application path.
	 */
	public void testChangeApplicationPath() {

		// Test
		this.replayMockObjects();

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeApplicationPath(this.template, "/change",
				this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, this.template, "Change Template Application Path", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can not change to non-unique application path.
	 */
	public void testNonUniqueApplicationPath() {

		// Test
		this.replayMockObjects();

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeApplicationPath(this.template, "/templateLink",
				this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, this.template, "Change Template Application Path", false,
				"Application path '/templateLink' already configured for Template");

		// Verify
		this.verifyMockObjects();
	}

}