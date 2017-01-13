/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.autowire.impl;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.frame.api.function.Work;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Integration test for the triggering of a start-up flow.
 * 
 * @author Daniel Sagenschneider
 */
public class IntegrateAutoWireStartupFlowTest extends OfficeFrameTestCase {

	/**
	 * Ensure can trigger the start-up flow.
	 */
	public void testStartupFlow() throws Exception {

		final MockObject object = this.createMock(MockObject.class);

		// Record start-up trigger
		object.doStartupTask();

		// Create the office floor
		AutoWireApplication app = new AutoWireOfficeFloorSource();
		AutoWireSection section = app.addSection("TEST",
				ClassSectionSource.class.getName(), MockWork.class.getName());
		app.addStartupFlow(section, "task");
		app.addObject(object, new AutoWire(MockObject.class));
		app.assignDefaultTeam(PassiveTeamSource.class.getName());

		// Open OfficeFloor which should trigger start-up
		this.replayMockObjects();
		app.openOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Mock {@link Work} for validating start-up triggered.
	 */
	public static class MockWork {
		public void task(MockObject object) {
			object.doStartupTask();
		}
	}

	/**
	 * Mock object for validating start-up triggered.
	 */
	public static interface MockObject {
		void doStartupTask();
	}

}