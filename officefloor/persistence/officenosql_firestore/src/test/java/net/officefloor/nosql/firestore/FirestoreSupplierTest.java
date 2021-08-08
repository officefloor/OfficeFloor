/*-
 * #%L
 * Firestore
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
