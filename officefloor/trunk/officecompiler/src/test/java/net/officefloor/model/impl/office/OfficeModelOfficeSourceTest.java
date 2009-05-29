/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
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
				.createOfficeArchitect(OfficeModelOfficeSource.class);
		architect.addOfficeObject("OBJECT", Connection.class.getName());
		architect.addOfficeTeam("TEAM");

		// Validate the office is as expected
		OfficeLoaderUtil.validateOffice(architect,
				OfficeModelOfficeSource.class, this,
				"OfficeModelOfficeSourceTest.office.xml");
	}

}