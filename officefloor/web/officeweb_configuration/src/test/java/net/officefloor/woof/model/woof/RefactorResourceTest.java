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

import net.officefloor.model.change.Change;

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
