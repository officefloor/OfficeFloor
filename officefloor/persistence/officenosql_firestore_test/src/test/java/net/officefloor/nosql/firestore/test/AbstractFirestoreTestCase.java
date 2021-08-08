/*-
 * #%L
 * Firestore Persistence Testing
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
