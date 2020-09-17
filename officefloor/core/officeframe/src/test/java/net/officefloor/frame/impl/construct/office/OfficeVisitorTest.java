/*-
 * #%L
 * OfficeFrame
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
