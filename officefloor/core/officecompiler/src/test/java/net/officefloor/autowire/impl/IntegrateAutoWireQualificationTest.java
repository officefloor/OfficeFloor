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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.Connection;
import java.sql.SQLException;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.Qualifier;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Integration of qualified {@link AutoWire}.
 * 
 * @author Daniel Sagenschneider
 */
public class IntegrateAutoWireQualificationTest extends OfficeFrameTestCase {

	/**
	 * Potential failure.
	 */
	private static Throwable failure = null;

	/**
	 * Ensure able to integrate qualified {@link AutoWire}.
	 */
	public void testIntegrateQualifiedAutoWire() throws Throwable {

		// Clear failure to reset to run test
		failure = null;

		// Mocks
		final Connection a = this.createSynchronizedMock(Connection.class);
		final Connection b = this.createSynchronizedMock(Connection.class);
		// c is unqualified (i.e. d)
		final Connection d = this.createSynchronizedMock(Connection.class);

		// Record connections
		this.recordReturn(a, a.nativeSQL("a"), "A");
		this.recordReturn(b, b.nativeSQL("b"), "B");
		this.recordReturn(d, d.nativeSQL("c"), "C");
		this.recordReturn(d, d.nativeSQL("d"), "D");

		// Test
		this.replayMockObjects();

		// Configure the office
		AutoWireApplication app = new AutoWireOfficeFloorSource();
		app.addObject(a, new AutoWire(MockA.class, Connection.class));
		app.addObject(b, new AutoWire(MockB.class, Connection.class));
		app.addObject(d, new AutoWire(Connection.class));
		app.addSection("TEST", ClassSectionSource.class.getName(),
				MockSection.class.getName());
		app.assignDefaultTeam(PassiveTeamSource.class.getName());

		// Ensure invoke task with appropriate dependencies
		AutoWireOfficeFloor officeFloor = app.openOfficeFloor();
		officeFloor.invokeTask("TEST.WORK", "task", null);
		officeFloor.closeOfficeFloor();

		// Verify
		this.verifyMockObjects();

		// Propagate if failure
		if (failure != null) {
			throw failure;
		}
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
	 * Mock C qualifier.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	public @interface MockC {
	}

	/**
	 * Mock section.
	 */
	public static class MockSection {

		public void task(@MockA Connection a, @MockB Connection b,
				@MockC Connection c, Connection d) throws SQLException {
			assertEquals("Qualified A", "A", a.nativeSQL("a"));
			assertEquals("Qualified B", "B", b.nativeSQL("b"));
			assertEquals("Default unqualified C", "C", c.nativeSQL("c"));
			assertEquals("Unqualified D", "D", d.nativeSQL("d"));
			assertSame("Default qualified to be unqualified", c, d);
		}

		public void handleFailure(@Parameter Throwable ex) {
			IntegrateAutoWireQualificationTest.failure = ex;
		}
	}

}