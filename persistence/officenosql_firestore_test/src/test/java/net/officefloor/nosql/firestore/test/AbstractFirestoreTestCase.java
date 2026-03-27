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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;

/**
 * Tests the JUnit {@link Firestore} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractFirestoreTestCase {

	/**
	 * Undertakes a synchronous test.
	 * 
	 * @param firestore {@link Firestore} to test.
	 */
	public void doTest(Firestore firestore) throws Exception {

		// Create the item
		DocumentReference docRef = firestore.collection("test").document("1");
		Map<String, Object> data = new HashMap<>();
		data.put("first", "Daniel");
		data.put("last", "Sagenschneider");
		data.put("level", 5);
		docRef.set(data).get();

		// Retrieve the item
		DocumentSnapshot snapshot = firestore.collection("test").document("1").get().get();
		assertEquals("Daniel", snapshot.getString("first"), "Incorrect first name");
		assertEquals("Sagenschneider", snapshot.getString("last"), "Incorrect last name");
		assertEquals(5, snapshot.getLong("level"), "Incorrect level");

		// Obtain all the documents
		List<QueryDocumentSnapshot> documents = firestore.collection("test").get().get().getDocuments();
		assertEquals(1, documents.size(), "Incorrect number of documents");
		assertEquals("1", documents.get(0).getReference().getId());
	}

}
