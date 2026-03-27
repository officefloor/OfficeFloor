/*-
 * #%L
 * Firestore Persistence Testing
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

package net.officefloor.nosql.firestore.test;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.cloud.firestore.Firestore;

import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link FirestoreExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreExtensionTest extends AbstractFirestoreTestCase {

	public final @RegisterExtension @Order(1) FirestoreExtension firestore = new FirestoreExtension();

	public final @RegisterExtension @Order(1) FirestoreConnectExtension connect = new FirestoreConnectExtension();

	@UsesDockerTest
	public void firestore() throws Exception {
		Firestore firestore = this.firestore.getFirestore();
		this.doTest(firestore);
	}

	@UsesDockerTest
	public void connect() throws Exception {
		Firestore firestore = this.connect.getFirestore();
		this.doTest(firestore);
	}

}
