/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.woof.model.woof;

import org.junit.Assert;

import net.officefloor.model.change.Change;
import net.officefloor.woof.template.WoofTemplateExtensionSourceContext;
import net.officefloor.woof.template.impl.AbstractWoofTemplateExtensionSource;

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
