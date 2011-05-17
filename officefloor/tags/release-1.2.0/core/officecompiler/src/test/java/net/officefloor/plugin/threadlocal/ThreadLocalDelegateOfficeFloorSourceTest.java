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

package net.officefloor.plugin.threadlocal;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ThreadLocalDelegateOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalDelegateOfficeFloorSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Ensure appropriately binds to {@link Thread} and instance available to
	 * delegate against.
	 */
	public void testBindAndDelegate() throws Exception {

		// Ensure clean starting point for test
		ThreadLocalDelegateOfficeFloorSource.unbindDelegates();

		// Create the compiler
		final OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler();

		// Create the delegate
		MockOfficeFloorSource delegate = new MockOfficeFloorSource();

		// Test
		ThreadLocalDelegateOfficeFloorSource.bindDelegate(delegate, compiler);
		compiler.compile("location");
		assertTrue("Delegate should be source", delegate.isSourced);
	}

	/**
	 * Mock {@link OfficeFloorSource} for testing.
	 */
	private class MockOfficeFloorSource extends AbstractOfficeFloorSource {

		/**
		 * Flag indicating if sourced.
		 */
		public boolean isSourced = false;

		/*
		 * ==================== OfficeFloorSource =======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void specifyConfigurationProperties(
				RequiredProperties requiredProperties,
				OfficeFloorSourceContext context) throws Exception {
			// No further required properties
		}

		@Override
		public void sourceOfficeFloor(OfficeFloorDeployer deployer,
				OfficeFloorSourceContext context) throws Exception {
			this.isSourced = true;
		}
	}

}