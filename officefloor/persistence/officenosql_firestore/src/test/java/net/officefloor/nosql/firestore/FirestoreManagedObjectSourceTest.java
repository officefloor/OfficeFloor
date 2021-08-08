/*-
 * #%L
 * Firestore
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.nosql.firestore;

import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.cloud.firestore.Firestore;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.nosql.firestore.test.FirestoreExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link FirestoreManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreManagedObjectSourceTest {

	public static final @RegisterExtension FirestoreExtension firestore = new FirestoreExtension().waitForFirestore();

	/**
	 * Ensure correct specification.
	 */
	@UsesDockerTest
	public void specification() {
		ManagedObjectLoaderUtil.validateSpecification(FirestoreManagedObjectSource.class);
	}

	/**
	 * Ensure correct meta-data.
	 */
	@UsesDockerTest
	public void metaData() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(Firestore.class);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, FirestoreManagedObjectSource.class);
	}

	/**
	 * Ensure {@link FirestoreManagedObjectSource} working.
	 */
	@UsesDockerTest
	public void firestore() throws Throwable {

		// Compile
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Register Firestore
			office.addOfficeManagedObjectSource("FIRESTORE", FirestoreManagedObjectSource.class.getName())
					.addOfficeManagedObject("FIRESTORE", ManagedObjectScope.THREAD);

			// Register test logic
			context.addSection("TEST", TestSection.class);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Invoke functionality
			final String documentId = "1";
			CompileOfficeFloor.invokeProcess(officeFloor, "TEST.service", documentId);

			// Ensure able to obtain data
			TestSection.validate(firestore.getFirestore(), documentId);
		}
	}

}
