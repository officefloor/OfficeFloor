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
package net.officefloor.frame.impl.execute.administration;

import java.util.List;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveAdministrationBuilder;
import net.officefloor.frame.test.ReflectiveAdministrationBuilder.ReflectiveDutyBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure extract the extension interface from the {@link ManagedObject}
 * instances.
 *
 * @author Daniel Sagenschneider
 */
public class ExtractExtensionInterfaceTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to extract extension interface from {@link ManagedObject}.
	 */
	public void testExtractExtensionInterface() {

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (metaData) -> {
			metaData.addManagedObjectExtensionInterface(ManagedObjectExtension.class, new ManagedObjectExtension(null));
		};

		// Construct the administrator
		TestAdministration admin = new TestAdministration();
		ReflectiveAdministrationBuilder administratorBuilder = this.constructAdministrator(admin);
		ReflectiveDutyBuilder duty = administratorBuilder.constructDuty("preTask");

	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {
		public void task(TestObject object) {
		}
	}

	/**
	 * Test {@link Administration}.
	 */
	public static class TestAdministration {
		public void preTask(List<ManagedObjectExtension> extensions) {

		}
	}

	/**
	 * Extension for the {@link ManagedObject}.
	 */
	public static class ManagedObjectExtension implements ExtensionInterfaceFactory<ManagedObjectExtension> {

		public ManagedObject managedObject;

		public ManagedObjectExtension(ManagedObject managedObject) {
			this.managedObject = managedObject;
		}

		@Override
		public ManagedObjectExtension createExtensionInterface(ManagedObject managedObject) {
			return new ManagedObjectExtension(managedObject);
		}
	}

}