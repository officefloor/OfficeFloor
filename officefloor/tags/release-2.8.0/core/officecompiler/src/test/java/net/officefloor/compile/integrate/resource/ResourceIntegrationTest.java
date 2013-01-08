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
package net.officefloor.compile.integrate.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Tests making the {@link ResourceSource} available to the {@link OfficeFrame}.
 * 
 * @author Daniel Sagenschneider
 */
public class ResourceIntegrationTest extends OfficeFrameTestCase {

	/**
	 * Resource.
	 */
	private static InputStream workResource;

	/**
	 * Ensure the {@link SourceContext} of {@link OfficeFrame} contains the
	 * {@link ClassLoader} and {@link ResourceSource} instances.
	 */
	public void testIntegrateSourceContext() throws Exception {

		final ResourceSource resourceSource = this
				.createMock(ResourceSource.class);
		final InputStream resource = new ByteArrayInputStream(new byte[0]);

		// Reset test
		workResource = null;

		// Record (multiple times as loading managed object type)
		for (int i = 0; i < 3; i++) {
			this.recordReturn(resourceSource,
					resourceSource.sourceResource("REQUIRED RESOURCE"),
					resource);
		}

		// Test
		this.replayMockObjects();

		// Configure OfficeFloor
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource(
				OfficeFloorCompiler.newOfficeFloorCompiler(null));
		source.addManagedObject(ClassLoaderManagedObjectSource.class.getName(),
				null, new AutoWire(ClassLoader.class));
		source.addManagedObject(ResourceManagedObjectSource.class.getName(),
				null, new AutoWire(InputStream.class));
		source.addSection("SECTION", ClassSectionSource.class.getName(),
				ResourceWork.class.getName());
		source.assignDefaultTeam(PassiveTeamSource.class.getName());

		// Configure for the OfficeFrame
		source.getOfficeFloorCompiler().addResources(resourceSource);

		// Start the OfficeFloor
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();

		// Invoke the work
		officeFloor.invokeTask("SECTION.WORK", "task", null);

		// Ensure correct resources
		assertSame("Incorrect resource", resource, workResource);

		// Close OfficeFloor
		officeFloor.closeOfficeFloor();

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * {@link Work}.
	 */
	public static class ResourceWork {
		public void task(ClassLoader classLoader, InputStream resource) {
			workResource = resource;
		}
	}

	/**
	 * {@link ManagedObjectSource} to obtain the {@link ClassLoader}.
	 */
	public static class ClassLoaderManagedObjectSource extends
			AbstractManagedObjectSource<None, None> implements ManagedObject {

		private ClassLoader classLoader;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// NO specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context)
				throws Exception {
			ManagedObjectSourceContext<None> mosContext = context
					.getManagedObjectSourceContext();
			context.setObjectClass(ClassLoader.class);
			this.classLoader = mosContext.getClassLoader();
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		@Override
		public Object getObject() throws Throwable {
			return this.classLoader;
		}
	}

	/**
	 * {@link ManagedObjectSource} to obtain the resource.
	 */
	public static class ResourceManagedObjectSource extends
			AbstractManagedObjectSource<None, None> implements ManagedObject {

		private InputStream resource;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// NO specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context)
				throws Exception {
			ManagedObjectSourceContext<None> mosContext = context
					.getManagedObjectSourceContext();
			context.setObjectClass(InputStream.class);
			this.resource = mosContext.getResource("REQUIRED RESOURCE");
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		@Override
		public Object getObject() throws Throwable {
			return this.resource;
		}
	}

}