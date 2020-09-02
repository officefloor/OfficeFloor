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