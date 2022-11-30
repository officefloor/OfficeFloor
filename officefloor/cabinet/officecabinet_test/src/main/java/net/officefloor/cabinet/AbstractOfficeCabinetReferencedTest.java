package net.officefloor.cabinet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * Tests referenced {@link Document} via {@link ReferencingDocument} and
 * {@link ReferencedDocument}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeCabinetReferencedTest {

	/**
	 * Obtains the test case.
	 * 
	 * @return {@link AbstractOfficeCabinetTestCase}.
	 */
	protected abstract AbstractOfficeCabinetTestCase testcase();

	/**
	 * Ensure can store and retrieve values.
	 */
	@Test
	@MStore(cabinets = @MCabinet(ReferencingDocument.class))
	public void notReferenced_storeAndRetrieve() {
		OfficeCabinet<ReferencingDocument> referencingCabinet = this.testcase().cabinetManager
				.getOfficeCabinet(ReferencingDocument.class);

		// Create document
		ReferencingDocument referencing = this.testcase().newDocument(ReferencingDocument.class, 0);

		// Store document
		assertNull(referencing.getKey(), "New referencing document so should not have key");
		referencingCabinet.store(referencing);
		String referencingKey = referencing.getKey();
		assertNotNull(referencingKey, "Should assign key to referencing document");
		assertNull(referencing.getOneToOne().get(), "Should not have referenced document");

		// Ensure with same cabinet that same instance
		ReferencingDocument retrievedReferencing = referencingCabinet.retrieveByKey(referencing.getKey()).get();
		assertSame(referencing, retrievedReferencing, "Should retrieve same referencing instance");
		assertEquals(referencingKey, retrievedReferencing.getKey(), "Should not change the referencing key");
		assertNull(retrievedReferencing.getOneToOne().get(), "Should not have referenced document");
	}

	/**
	 * Ensure can store and retrieve values.
	 */
	@Test
	@MStore(cabinets = { @MCabinet(ReferencingDocument.class), @MCabinet(ReferencedDocument.class) })
	public void storeAndRetrieve() {
		OfficeCabinet<ReferencingDocument> referencingCabinet = this.testcase().cabinetManager
				.getOfficeCabinet(ReferencingDocument.class);
		OfficeCabinet<ReferencedDocument> referencedCabinet = this.testcase().cabinetManager
				.getOfficeCabinet(ReferencedDocument.class);

		// Create document
		ReferencingDocument referencing = this.testcase().newDocument(ReferencingDocument.class, 0);
		ReferencedDocument referenced = this.testcase().newDocument(ReferencedDocument.class, 0);
		referencing.getOneToOne().set(referenced);

		// Store document
		assertNull(referencing.getKey(), "New referencing document so should not have key");
		assertNull(referenced.getKey(), "New referenced document so should not have key");
		referencingCabinet.store(referencing);

		// Check the referencing document
		String referencingKey = referencing.getKey();
		assertNotNull(referencingKey, "Should assign key to referencing document");
		ReferencingDocument retrievedReferencing = referencingCabinet.retrieveByKey(referencing.getKey()).get();
		assertSame(referencing, retrievedReferencing, "Should retrieve same referencing instance");
		assertEquals(referencingKey, retrievedReferencing.getKey(), "Should not change the referencing key");

		// Check the referenced document
		String referencedKey = referenced.getKey();
		assertNotNull(referencedKey, "Should assign key to referenced document");
		ReferencedDocument retrievedReferenced = referencedCabinet.retrieveByKey(referenced.getKey()).get();
		assertSame(referenced, retrievedReferenced, "Should retrieve same referenced instance");
		assertEquals(referencedKey, retrievedReferenced.getKey(), "Should not change the referenced key");

		// Ensure retrieved linked
		assertSame(retrievedReferenced, retrievedReferencing.getOneToOne().get(), "Should retrieve linked");
	}

}