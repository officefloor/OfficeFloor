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

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionObjectModel;

/**
 * Tests setting the {@link Object} as parameter.
 * 
 * @author Daniel Sagenschneider
 */
public class SetObjectAsParameterTest extends AbstractSectionChangesTestCase {

	/**
	 * {@link Object} {@link ManagedFunctionObjectModel}.
	 */
	private ManagedFunctionObjectModel object;

	/**
	 * Parameter {@link ManagedFunctionObjectModel}.
	 */
	private ManagedFunctionObjectModel parameter;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the object parameter function object
		ManagedFunctionModel managedFunction = this.model.getFunctionNamespaces().get(0).getManagedFunctions().get(0);
		this.object = managedFunction.getManagedFunctionObjects().get(0);
		this.parameter = managedFunction.getManagedFunctionObjects().get(1);
	}

	/**
	 * Ensures no change if {@link ManagedFunctionModel} is not on the
	 * {@link SectionModel}.
	 */
	public void testManagedFunctionNotInSection() {
		ManagedFunctionObjectModel functionObject = new ManagedFunctionObjectModel("NOT_IN_SECTION", null,
				String.class.getName(), false);
		Change<ManagedFunctionObjectModel> change = this.operations.setObjectAsParameter(true, functionObject);
		this.assertChange(change, functionObject, "Set managed function object NOT_IN_SECTION as a parameter", false,
				"Managed function object NOT_IN_SECTION not in section");
	}

	/**
	 * Ensures can set a {@link ManagedFunctionObjectModel} as a parameter.
	 */
	public void testSetToParameter() {
		Change<ManagedFunctionObjectModel> change = this.operations.setObjectAsParameter(true, this.object);
		this.assertChange(change, this.object, "Set managed function object OBJECT as a parameter", true);
	}

	/**
	 * Ensures can set a {@link ManagedFunctionObjectModel} as an object.
	 */
	public void testSetToObject() {
		Change<ManagedFunctionObjectModel> change = this.operations.setObjectAsParameter(false, this.parameter);
		this.assertChange(change, this.parameter, "Set managed function object PARAMETER as an object", true);
	}

	/**
	 * Ensures can set a {@link ManagedFunctionObjectModel} as a parameter with
	 * connected {@link ConnectionModel} instances.
	 */
	public void testSetToParameterWithConnections() {
		this.useTestSetupModel();
		Change<ManagedFunctionObjectModel> change = this.operations.setObjectAsParameter(true, this.object);
		this.assertChange(change, this.object, "Set managed function object OBJECT as a parameter", true);
	}

}