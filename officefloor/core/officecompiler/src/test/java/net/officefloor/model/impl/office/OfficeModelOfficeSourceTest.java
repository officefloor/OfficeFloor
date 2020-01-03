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