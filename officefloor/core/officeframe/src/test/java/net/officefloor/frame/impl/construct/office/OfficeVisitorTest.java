/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.construct.office;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.build.OfficeVisitor;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Tests the {@link OfficeVisitor}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class OfficeVisitorTest {

	/**
	 * {@link ConstructTestSupport}.
	 */
	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * Ensure provide {@link OfficeMetaData}.
	 */
	@Test
	public void officeVisitor() throws Exception {
		this.doOfficeTest(this.construct.getOfficeName());
	}

	/**
	 * Ensure visit multiple {@link Office} instances.
	 */
	@Test
	public void multipleOfficesVisited() throws Exception {

		// Add offices
		String[] officeNames = new String[] { this.construct.getOfficeName(), "Two", "Three", "Four" };
		for (int i = 1; i < officeNames.length; i++) {
			this.construct.getOfficeFloorBuilder().addOffice(officeNames[i]);
		}

		// Ensure visit each office
		this.doOfficeTest(officeNames);
	}

	private void doOfficeTest(String... expectedOfficeNames) throws Exception {

		List<OfficeMetaData> officeMetaDatas = new LinkedList<>();
		this.construct.getOfficeFloorBuilder().addOfficeVisitor((metaData) -> officeMetaDatas.add(metaData));

		// Construct Office
		try (OfficeFloor officeFloor = this.construct.constructOfficeFloor()) {
			officeFloor.openOfficeFloor();

			// Ensure correct number of offices
			assertEquals(expectedOfficeNames.length, officeFloor.getOfficeNames().length,
					"Incorrect number of offices");

			// Ensure the office visited
			assertEquals(expectedOfficeNames.length, officeMetaDatas.size(), "Incorrect number of offices visited");
			for (int i = 0; i < expectedOfficeNames.length; i++) {
				assertEquals(expectedOfficeNames[i], officeMetaDatas.get(i).getOfficeName(), "Incorrect office " + i);
			}
		}
	}

}
