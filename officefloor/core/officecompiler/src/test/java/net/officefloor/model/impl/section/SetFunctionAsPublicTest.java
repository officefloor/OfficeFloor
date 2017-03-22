/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.model.impl.section;

import net.officefloor.model.change.Change;
import net.officefloor.model.section.DeskModel;
import net.officefloor.model.section.FunctionModel;

/**
 * Tests setting the {@link FunctionModel} as public.
 * 
 * @author Daniel Sagenschneider
 */
public class SetFunctionAsPublicTest extends AbstractDeskChangesTestCase {

	/**
	 * Public {@link FunctionModel}.
	 */
	private FunctionModel publicFunction;

	/**
	 * Private {@link FunctionModel}.
	 */
	private FunctionModel privateFunction;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the public and private functions
		this.publicFunction = this.model.getFunctions().get(0);
		this.privateFunction = this.model.getFunctions().get(1);
	}

	/**
	 * Ensure no change if the {@link FunctionModel} not on the
	 * {@link DeskModel}.
	 */
	public void testFunctionNotOnDesk() {
		FunctionModel function = new FunctionModel("FUNCTION", false, "NAMESPACE", "MANAGED_FUNCTION", null);
		Change<FunctionModel> change = this.operations.setFunctionAsPublic(true, function);
		this.assertChange(change, function, "Set function FUNCTION public", false, "Function FUNCTION not on desk");
	}

	/**
	 * Ensures can set a {@link FunctionModel} to be public.
	 */
	public void testFunctionPublic() {
		Change<FunctionModel> change = this.operations.setFunctionAsPublic(true, this.privateFunction);
		this.assertChange(change, this.privateFunction, "Set function PRIVATE public", true);
	}

	/**
	 * Ensures can set a {@link FunctionModel} to be private.
	 */
	public void testSetFunctionPrivate() {
		Change<FunctionModel> change = this.operations.setFunctionAsPublic(false, this.publicFunction);
		this.assertChange(change, this.publicFunction, "Set function PUBLIC private", true);
	}

}