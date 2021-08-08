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
