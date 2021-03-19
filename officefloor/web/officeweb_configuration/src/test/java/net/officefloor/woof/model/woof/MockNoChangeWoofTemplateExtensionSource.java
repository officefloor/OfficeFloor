/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
