/*-
 * #%L
 * OfficeCompiler
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
