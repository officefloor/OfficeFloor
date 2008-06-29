/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.administratorsource;

import java.util.Properties;

import net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.DutyFlowModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.PropertyModel;

/**
 * Tests the {@link AdministratorSourceLoader}.
 * 
 * @author Daniel
 */
public class AdministratorSourceLoaderTest extends OfficeFrameTestCase {

	/**
	 * Ensures loads the {@link AdministratorModel} from the
	 * {@link AdministratorSourceMetaData}.
	 */
	public void testLoadAdministratorSource() throws Throwable {

		// Create the loader
		AdministratorSourceLoader loader = new AdministratorSourceLoader();

		// Provide the properties
		Properties properties = new Properties();
		properties.setProperty("property name", "property value");

		// Load the administrator model
		AdministratorModel model = loader.loadAdministratorSource("test",
				new TestAdministratorSource(), properties, this.getClass()
						.getClassLoader());

		// ----------------------------------------------------------
		// Validate the Administrator Model
		// ----------------------------------------------------------
		assertProperties(new AdministratorModel("test",
				TestAdministratorSource.class.getName(), MockDutyKeys.class
						.getName(), null, null, null, null), model, "getId",
				"getSource", "getDutyKeyClass");

		// Expect no team
		assertNull("Should be no team assigned", model.getTeam());

		// Expect no managed objects
		assertEquals("Should be no managed objects", 0, model
				.getManagedObjects().size());

		// Validate the properties
		assertList(new String[] { "getName", "getValue" }, model
				.getProperties(), new PropertyModel("property name",
				"property value"));

		// Validate the duties
		assertList(new String[] { "getKey" }, model.getDuties(), new DutyModel(
				MockDutyKeys.KEY_ONE.name(), null, null, null, null),
				new DutyModel(MockDutyKeys.KEY_TWO.name(), null, null, null,
						null), new DutyModel(MockDutyKeys.KEY_THREE.name(),
						null, null, null, null));

		// Validate the flow items
		DutyModel duty = getItem(model.getDuties(), "getKey",
				MockDutyKeys.KEY_ONE.name());
		assertList(new String[] { "getKey" }, duty.getFlows(),
				new DutyFlowModel(MockDutyFlowKeys.FLOW_ONE.name(), null),
				new DutyFlowModel(MockDutyFlowKeys.FLOW_TWO.name(), null));
	}
}
