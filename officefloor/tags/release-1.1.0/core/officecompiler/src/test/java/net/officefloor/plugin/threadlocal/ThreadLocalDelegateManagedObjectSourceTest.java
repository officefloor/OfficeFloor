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

import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ThreadLocalDelegateManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ThreadLocalDelegateManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Delegate {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource delegate = this
			.createMock(ManagedObjectSource.class);

	/**
	 * Ensure appropriately binds to {@link Thread} and instance available to
	 * delegate against.
	 */
	public void testBindAndDelegate() throws Exception {

		// Ensure clean starting point for test
		ThreadLocalDelegateManagedObjectSource.unbindDelegates();

		final OfficeFloorDeployer deployer = this
				.createMock(OfficeFloorDeployer.class);
		final OfficeFloorManagedObjectSource managedObjectSource = this
				.createMock(OfficeFloorManagedObjectSource.class);
		final ManagedObjectSourceContext<?> context = this
				.createMock(ManagedObjectSourceContext.class);
		final ManagedObjectSourceMetaData<?, ?> metaData = this
				.createMock(ManagedObjectSourceMetaData.class);
		final ManagedObjectExecuteContext<?> executeContext = this
				.createMock(ManagedObjectExecuteContext.class);
		final ManagedObjectUser user = this.createMock(ManagedObjectUser.class);

		// Record binding
		this.recordReturn(deployer, deployer.addManagedObjectSource("mos",
				ThreadLocalDelegateManagedObjectSource.class.getName()),
				managedObjectSource);
		managedObjectSource.addProperty(
				ThreadLocalDelegateOfficeSource.PROPERTY_INSTANCE_IDENTIFIER,
				"0");

		// Record delegation
		this.recordReturn(
				context,
				context.getProperty(ThreadLocalDelegateOfficeSource.PROPERTY_INSTANCE_IDENTIFIER),
				"0");
		this.delegate.init(context);
		this.recordReturn(this.delegate, this.delegate.getMetaData(), metaData);
		this.delegate.start(executeContext);
		this.delegate.sourceManagedObject(user);
		this.delegate.stop();

		// Test
		this.replayMockObjects();
		OfficeFloorManagedObjectSource returnedMos = ThreadLocalDelegateManagedObjectSource
				.bindDelegate("mos", this.delegate, deployer);
		assertSame("Incorrect returned managed object source",
				managedObjectSource, returnedMos);
		ThreadLocalDelegateManagedObjectSource source = new ThreadLocalDelegateManagedObjectSource();
		ManagedObjectSourceSpecification specification = source
				.getSpecification();
		assertEquals("Should be no properties", 0,
				specification.getProperties().length);
		source.init(context);
		ManagedObjectSourceMetaData<?, ?> returnedMetaData = source
				.getMetaData();
		assertSame("Incorrect meta-data", metaData, returnedMetaData);
		source.start(executeContext);
		source.sourceManagedObject(user);
		source.stop();
		this.verifyMockObjects();
	}

}