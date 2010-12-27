/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.OfficeSourceSpecification;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ThreadLocalDelegateOfficeSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalDelegateOfficeSourceTest extends OfficeFrameTestCase {

	/**
	 * Delegate {@link OfficeSource}.
	 */
	private final OfficeSource delegate = this.createMock(OfficeSource.class);

	/**
	 * Ensure appropriately binds to {@link Thread} and instance available to
	 * delegate against.
	 */
	public void testBindAndDelegate() throws Exception {

		// Ensure clean starting point for test
		ThreadLocalDelegateOfficeSource.unbindDelegates();

		final OfficeFloorDeployer deployer = this
				.createMock(OfficeFloorDeployer.class);
		final DeployedOffice office = this.createMock(DeployedOffice.class);
		final OfficeArchitect architect = this
				.createMock(OfficeArchitect.class);
		final OfficeSourceContext context = this
				.createMock(OfficeSourceContext.class);

		// Record binding
		this.recordReturn(deployer, deployer.addDeployedOffice("office",
				ThreadLocalDelegateOfficeSource.class.getName(), ""), office);
		office.addProperty(
				ThreadLocalDelegateOfficeSource.PROPERTY_INSTANCE_IDENTIFIER,
				"0");

		// Record delegation
		this
				.recordReturn(
						context,
						context
								.getProperty(ThreadLocalDelegateOfficeSource.PROPERTY_INSTANCE_IDENTIFIER),
						"0");
		this.delegate.sourceOffice(architect, context);

		// Test
		this.replayMockObjects();
		DeployedOffice returnedOffice = ThreadLocalDelegateOfficeSource
				.bindDelegate("office", this.delegate, deployer);
		assertSame("Incorrect returned office", office, returnedOffice);
		ThreadLocalDelegateOfficeSource source = new ThreadLocalDelegateOfficeSource();
		OfficeSourceSpecification specification = source.getSpecification();
		assertEquals("Should be no properties", 0, specification
				.getProperties().length);
		source.sourceOffice(architect, context);
		this.verifyMockObjects();
	}

}