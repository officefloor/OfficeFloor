/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.run;

import net.officefloor.compile.AbstractModelCompilerTestCase;
import net.officefloor.compile.integrate.managedfunction.CompileFunctionTest.InputManagedObject;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Ensure able to run with an {@link InputManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractRunTestCase extends AbstractModelCompilerTestCase {

	/**
	 * {@link OpenOfficeFloorTestSupport}.
	 */
	public final OpenOfficeFloorTestSupport openOfficeFloorTestSupport = new OpenOfficeFloorTestSupport(
			this.modelTestSupport);

	@Override
	protected void tearDown() throws Exception {

		// Ensure close the OfficeFloor
		this.openOfficeFloorTestSupport.afterEach(null);

		// Complete tear down
		super.tearDown();
	}

	/**
	 * Compiles and opens the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 */
	protected OfficeFloor open() {
		return this.openOfficeFloorTestSupport.open();
	}

}
