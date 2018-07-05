/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.run;

import net.officefloor.compile.AbstractModelCompilerTestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.integrate.managedfunction.CompileFunctionTest.InputManagedObject;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.model.impl.officefloor.OfficeFloorModelOfficeFloorSource;

/**
 * Ensure able to run with an {@link InputManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractRunTestCase extends AbstractModelCompilerTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor = null;

	@Override
	protected void tearDown() throws Exception {

		// Ensure close the OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}

		// Complete tear down
		super.tearDown();
	}

	/**
	 * Compiles and opens the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 */
	protected OfficeFloor open() {

		// Ensure open
		assertNull("OfficeFloor already compiled", this.officeFloor);

		// Obtain the resource source
		ResourceSource resourceSource = this.getResourceSource();

		// Create the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setOfficeFloorSourceClass(OfficeFloorModelOfficeFloorSource.class);
		compiler.setOfficeFloorLocation("office-floor");
		compiler.addResources(resourceSource);

		// Compile the OfficeFloor
		this.officeFloor = compiler.compile("OfficeFloor");
		assertNotNull("Should compile the OfficeFloor", officeFloor);

		// Open the OfficeFloor
		try {
			this.officeFloor.openOfficeFloor();
		} catch (Exception ex) {
			throw fail(ex);
		}

		// Return the open OfficeFloor
		return this.officeFloor;
	}

}