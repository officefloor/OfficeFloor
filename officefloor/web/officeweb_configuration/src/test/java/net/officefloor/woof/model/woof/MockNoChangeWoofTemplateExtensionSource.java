package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.WoofTemplateExtension;
import net.officefloor.woof.template.WoofTemplateExtensionSourceContext;
import net.officefloor.woof.template.impl.AbstractWoofTemplateExtensionSource;

import org.junit.Assert;

/**
 * Mock {@link WoofTemplateExtension} that does not cause a {@link Change}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockNoChangeWoofTemplateExtensionSource extends
		AbstractWoofTemplateExtensionSource {

	/*
	 * ================== WoofTemplateExtensionSource ===============
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		Assert.fail("Specification should not be required for changes");
	}

	@Override
	public void extendTemplate(WoofTemplateExtensionSourceContext context)
			throws Exception {
		Assert.fail("Should not be extending template for changes");
	}

}