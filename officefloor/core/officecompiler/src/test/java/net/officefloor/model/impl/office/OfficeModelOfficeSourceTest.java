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

package net.officefloor.model.impl.office;

import java.sql.Connection;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.test.office.OfficeLoaderUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.office.OfficeModel;

/**
 * Tests the {@link OfficeModelOfficeSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeModelOfficeSourceTest extends OfficeFrameTestCase {

	/**
	 * No specification properties required.
	 */
	public void testNoSpecification() {
		OfficeLoaderUtil.validateSpecification(OfficeModelOfficeSource.class);
	}

	/**
	 * Ensure can source an {@link OfficeModel}.
	 */
	public void testOffice() {

		// Create the expected office
		OfficeArchitect architect = OfficeLoaderUtil
				.createOfficeArchitect(OfficeModelOfficeSource.class.getName());
		architect.addOfficeObject("OBJECT", Connection.class.getName());
		architect.addOfficeTeam("TEAM");

		// Validate the office is as expected
		OfficeLoaderUtil.validateOffice(architect,
				OfficeModelOfficeSource.class, this.getClass(),
				"OfficeModelOfficeSourceTest.office.xml");
	}

}
