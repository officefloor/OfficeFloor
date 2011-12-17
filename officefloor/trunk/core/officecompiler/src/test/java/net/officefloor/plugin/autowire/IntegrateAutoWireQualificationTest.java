/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.autowire;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.Connection;
import java.sql.SQLException;

import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.work.clazz.Qualifier;

/**
 * Integration of qualified {@link AutoWire}.
 * 
 * @author Daniel Sagenschneider
 */
public class IntegrateAutoWireQualificationTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to integrate qualified {@link AutoWire}.
	 */
	public void testIntegrateQualifiedAutoWire() throws Exception {

		// Mocks
		final Connection a = this.createSynchronizedMock(Connection.class);
		final Connection b = this.createSynchronizedMock(Connection.class);
		final Connection c = this.createSynchronizedMock(Connection.class);

		// Record connections
		this.recordReturn(a, a.nativeSQL("a"), "A");
		this.recordReturn(b, b.nativeSQL("b"), "B");
		this.recordReturn(c, c.nativeSQL("c"), "C");

		// Test
		this.replayMockObjects();

		// Configure the office
		AutoWireApplication app = new AutoWireOfficeFloorSource();
		app.addObject(a, new AutoWire(MockA.class, Connection.class));
		app.addObject(b, new AutoWire(MockB.class, Connection.class));
		app.addObject(c, new AutoWire(Connection.class));
		app.addSection("TEST", ClassSectionSource.class.getName(),
				MockSection.class.getName());
		app.assignDefaultTeam(PassiveTeamSource.class.getName());

		// Ensure invoke task with appropriate dependencies
		AutoWireOfficeFloor officeFloor = app.openOfficeFloor();
		officeFloor.invokeTask("TEST.MockSection", "task", null);
		officeFloor.closeOfficeFloor();

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Mock A qualifier.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	public @interface MockA {
	}

	/**
	 * Mock B qualifier.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	public @interface MockB {
	}

	/**
	 * Mock section.
	 */
	public class MockSection {

		public void task(@MockA Connection a, @MockB Connection b, Connection c)
				throws SQLException {
			assertEquals("Qualified A", "A", a.nativeSQL("a"));
			assertEquals("Qualified B", "B", b.nativeSQL("b"));
			assertEquals("Unqualified C", "C", c.nativeSQL("c"));
		}
	}

}