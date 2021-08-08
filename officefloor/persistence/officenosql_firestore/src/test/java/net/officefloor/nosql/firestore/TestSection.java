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
