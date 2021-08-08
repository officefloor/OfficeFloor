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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

import net.officefloor.plugin.clazz.NonFunctionMethod;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Test logic for {@link Firestore}.
 * 
 * @author Daniel Sagenschneider
 */
public class TestSection {

	/**
	 * Service logic.
	 * 
	 * @param firestore  {@link Firestore}.
	 * @param documentId Document id.
	 */
	public void service(Firestore firestore, @Parameter String documentId) throws Exception {

		// Add entity
		DocumentReference docRef = firestore.collection(TestEntity.class.getSimpleName()).document(documentId);
		docRef.create(new TestEntity("Daniel", "Sagenschneider", 5)).get();
	}

	/**
	 * Validates appropriately serviced.
	 * 
	 * @param firestore  {@link Firestore}.
	 * @param documentId Document id.
	 */
	@NonFunctionMethod
	public static void validate(Firestore firestore, String documentId) throws Exception {

		TestEntity entity = firestore.collection(TestEntity.class.getSimpleName()).document(documentId).get().get()
				.toObject(TestEntity.class);
		assertEquals("Daniel", entity.getFirstName(), "Incorrect first name");
		assertEquals("Sagenschneider", entity.getLastName(), "Incorrect last name");
		assertEquals(5, entity.getLevel(), "Incorrect level");
	}

}
