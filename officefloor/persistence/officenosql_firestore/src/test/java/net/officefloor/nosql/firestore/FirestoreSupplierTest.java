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

import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.supplier.SupplierLoaderUtil;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.nosql.firestore.test.FirestoreExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link FirestoreSupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreSupplierTest {

	public final @RegisterExtension FirestoreExtension firestore = new FirestoreExtension().waitForFirestore();

	/**
	 * Validates the specification.
	 */
	@UsesDockerTest
	public void specification() {
		SupplierLoaderUtil.validateSpecification(FirestoreSupplierSource.class);
	}

	/**
	 * Ensure {@link Firestore} working.
	 */
	@UsesDockerTest
	public void firestore() throws Throwable {

		// Compile
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {

			// Register Firestore
			context.getOfficeArchitect().addSupplier("FIRESTORE", FirestoreSupplierSource.class.getName());

			// Register test logic
			context.addSection("TEST", TestSection.class);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Invoke functionality
			String documentId = "1";
			CompileOfficeFloor.invokeProcess(officeFloor, "TEST.service", documentId);

			// Validate logic
			TestSection.validate(firestore.getFirestore(), documentId);
		}
	}

}
