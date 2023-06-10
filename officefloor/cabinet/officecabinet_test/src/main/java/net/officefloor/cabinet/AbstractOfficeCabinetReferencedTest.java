package net.officefloor.cabinet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import net.officefloor.cabinet.spi.CabinetManager;
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
	public void notReferenced_storeAndRetrieve() throws Exception {

		// Create document
		ReferencingDocument referencing = this.testcase().newDocument(ReferencingDocument.class);
		assertNull(referencing.getKey(), "New referencing document so should not have key");

		// Store document
		CabinetManager manager = this.testcase().officeStore.createCabinetManager();
		OfficeCabinet<ReferencingDocument> referencingCabinet = manager.getOfficeCabinet(ReferencingDocument.class);
		referencingCabinet.store(referencing);

		// Ensure assign key on store
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

		// Create document
		ReferencingDocument referencing = this.testcase().newDocument(ReferencingDocument.class);
		ReferencedDocument referenced = this.testcase().newDocument(ReferencedDocument.class);
		referencing.getOneToOne().set(referenced);

		// Store document
		CabinetManager manager = this.testcase().officeStore.createCabinetManager();
		OfficeCabinet<ReferencingDocument> referencingCabinet = manager.getOfficeCabinet(ReferencingDocument.class);
		assertNull(referencing.getKey(), "New referencing document so should not have key");
		assertNull(referenced.getKey(), "New referenced document so should not have key");
		referencingCabinet.store(referencing);

		// Check the referencing document
		String referencingKey = referencing.getKey();
		assertNotNull(referencingKey, "Should assign key to referencing document");

		// Check the referenced document (is only linked to referencing)
		// Only on flush should the new document be setup (may be removed before flush)
		assertNull(referenced.getKey(), "Should not assign key to referenced document");

		// Ensure retrieve same referencing instance
		ReferencingDocument retrievedReferencing = referencingCabinet.retrieveByKey(referencingKey).get();
		assertSame(referencing, retrievedReferencing, "Should retrieve same referencing instance");
		assertEquals(referencingKey, retrievedReferencing.getKey(), "Should not change the referencing key");
		assertSame(referenced, retrievedReferencing.getOneToOne().get(), "Should retrieve same linked instance");
	}

	/**
	 * Ensure can store and later retrieve values.
	 */
	@Test
	@MStore(cabinets = @MCabinet(ReferencingDocument.class))
	public void notReferenced_storeAndLaterRetrieve() throws Exception {

		// Store document
		ReferencingDocument referencing = this.testcase().setupDocument(ReferencingDocument.class);

		// With another manager obtain the document
		ReferencingDocument retrieved = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(ReferencingDocument.class).retrieveByKey(referencing.getKey()).get();
		assertNotSame(referencing, retrieved, "Should be different instance");
		assertNotNull(retrieved.getKey(), "Should assign key to referencing document");
		assertEquals(referencing.getKey(), retrieved.getKey(), "Should not change the referencing key");
		assertNull(retrieved.getOneToOne().get(), "Should not have referenced document");
	}

	/**
	 * Ensure can store and later retrieve values.
	 */
	@Test
	@MStore(cabinets = { @MCabinet(ReferencingDocument.class), @MCabinet(ReferencedDocument.class) })
	public void storeAndLaterRetrieve() {

		// Store document
		ReferencedDocument referenced = this.testcase().newDocument(ReferencedDocument.class);
		ReferencingDocument referencing = this.testcase().setupDocument(ReferencingDocument.class,
				(doc, index) -> doc.getOneToOne().set(referenced));

		// Should assign key to referenced document
		String referencedKey = referenced.getKey();
		assertNotNull(referencedKey, "Should assign key to referenced document");

		// Retrieve document with another manager
		OfficeCabinet<ReferencingDocument> referencingCabinet = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(ReferencingDocument.class);
		String referencingKey = referencing.getKey();
		ReferencingDocument retrievedReferencing = referencingCabinet.retrieveByKey(referencingKey).get();
		assertNotSame(referencing, retrievedReferencing, "Should retrieve new referencing instance");
		assertEquals(referencingKey, retrievedReferencing.getKey(), "Should not change the referencing key");

		// Check the referenced document also accessible via retrieval
		OfficeCabinet<ReferencedDocument> referencedCabinet = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(ReferencedDocument.class);
		ReferencedDocument retrievedReferenced = referencedCabinet.retrieveByKey(referencedKey).get();
		assertEquals(referencedKey, retrievedReferenced.getKey(), "Should not change the referenced key");
	}

	/**
	 * Ensure can store and retrieve linked.
	 */
	@Test
	@MStore(cabinets = { @MCabinet(ReferencingDocument.class), @MCabinet(ReferencedDocument.class) })
	public void storeAndLaterRetrieveLinked() {

		// Store document
		ReferencedDocument referenced = this.testcase().newDocument(ReferencedDocument.class);
		ReferencingDocument referencing = this.testcase().setupDocument(ReferencingDocument.class,
				(doc, index) -> doc.getOneToOne().set(referenced));

		// Retrieve document with another manager
		OfficeCabinet<ReferencingDocument> referencingCabinet = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(ReferencingDocument.class);
		ReferencingDocument retrievedReferencing = referencingCabinet.retrieveByKey(referencing.getKey()).get();

		// Ensure can retrieve linked
		ReferencedDocument retrievedReferenced = retrievedReferencing.getOneToOne().get();
		assertNotNull(retrievedReferenced, "Should retrieve referenced");
		assertEquals(referenced.getKey(), retrievedReferenced.getKey(), "Incorrect referenced");
	}

	/**
	 * Ensure can changing referenced {@link Document} before get does not overwrite
	 * with get.
	 */
	@Test
	@MStore(cabinets = { @MCabinet(ReferencingDocument.class), @MCabinet(ReferencedDocument.class) })
	public void changeReferencedDocumentBeforeGet() throws Exception {

		// Store document
		ReferencedDocument referenced = this.testcase().newDocument(ReferencedDocument.class);
		ReferencingDocument referencing = this.testcase().setupDocument(ReferencingDocument.class,
				(doc, index) -> doc.getOneToOne().set(referenced));

		// Retrieve document with another manager
		CabinetManager manager = this.testcase().officeStore.createCabinetManager();
		OfficeCabinet<ReferencingDocument> referencingCabinet = manager.getOfficeCabinet(ReferencingDocument.class);
		ReferencingDocument retrievedReferencing = referencingCabinet.retrieveByKey(referencing.getKey()).get();

		// Change the referenced document
		ReferencedDocument changedReference = this.testcase().newDocument(ReferencedDocument.class);
		retrievedReferencing.getOneToOne().set(changedReference);

		// Ensure get same changed reference
		assertSame(changedReference, retrievedReferencing.getOneToOne().get(),
				"Should not trigger retrieve to override the set related document");
	}

	/**
	 * Ensure can clear referenced {@link Document} before get does not overwrite
	 * with get.
	 */
	@Test
	@MStore(cabinets = { @MCabinet(ReferencingDocument.class), @MCabinet(ReferencedDocument.class) })
	public void clearReferencedDocumentBeforeGet() throws Exception {

		// Store document
		ReferencedDocument referenced = this.testcase().newDocument(ReferencedDocument.class);
		ReferencingDocument referencing = this.testcase().setupDocument(ReferencingDocument.class,
				(doc, index) -> doc.getOneToOne().set(referenced));

		// Retrieve document with another manager
		CabinetManager manager = this.testcase().officeStore.createCabinetManager();
		OfficeCabinet<ReferencingDocument> referencingCabinet = manager.getOfficeCabinet(ReferencingDocument.class);
		ReferencingDocument retrievedReferencing = referencingCabinet.retrieveByKey(referencing.getKey()).get();

		// Clear the referenced document
		retrievedReferencing.getOneToOne().set(null);

		// Ensure get same changed reference
		assertNull(retrievedReferencing.getOneToOne().get(),
				"Should not trigger retrieve to override the set related document");
	}

	/**
	 * Ensure can change referenced {@link Document}.
	 */
	@Test
	@MStore(cabinets = { @MCabinet(ReferencingDocument.class), @MCabinet(ReferencedDocument.class) })
	public void changeReferencedDocument() throws Exception {

		// Store document
		ReferencedDocument referenced = this.testcase().newDocument(ReferencedDocument.class);
		ReferencingDocument referencing = this.testcase().setupDocument(ReferencingDocument.class,
				(doc, index) -> doc.getOneToOne().set(referenced));

		// Retrieve document with another manager
		CabinetManager manager = this.testcase().officeStore.createCabinetManager();
		OfficeCabinet<ReferencingDocument> referencingCabinet = manager.getOfficeCabinet(ReferencingDocument.class);
		ReferencingDocument retrievedReferencing = referencingCabinet.retrieveByKey(referencing.getKey()).get();

		// Change the referenced document
		ReferencedDocument changedReference = this.testcase().newDocument(ReferencedDocument.class);
		retrievedReferencing.getOneToOne().set(changedReference);

		// Save changes
		manager.flush();
		assertNotNull(changedReference.getKey(), "Should assign key on new referenced document store");

		// Retrieve document with another manager
		ReferencingDocument retrievedChangedReferencing = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(ReferencingDocument.class).retrieveByKey(referencing.getKey()).get();

		// Ensure changed linked
		ReferencedDocument retrievedChangedReferenced = retrievedChangedReferencing.getOneToOne().get();
		assertNotNull(retrievedChangedReferenced, "Should retrieve referenced");
		assertEquals(changedReference.getKey(), retrievedChangedReferenced.getKey(), "Incorrect referenced");
	}

	/**
	 * Ensure saves changes for the referenced {@link Document}.
	 */
	@Test
	@MStore(cabinets = { @MCabinet(ReferencingDocument.class), @MCabinet(ReferencedDocument.class) })
	public void dirtyReferencedDocument() throws Exception {

		// Create the documents
		final String INITIAL = "INITIAL";
		ReferencingDocument referencing = this.testcase().newDocument(ReferencingDocument.class);
		ReferencedDocument referenced = this.testcase().newDocument(ReferencedDocument.class);
		referenced.setChange(INITIAL);
		referencing.getOneToOne().set(referenced);

		// Store the document
		CabinetManager manager = this.testcase().officeStore.createCabinetManager();
		OfficeCabinet<ReferencingDocument> referencingCabinet = manager.getOfficeCabinet(ReferencingDocument.class);
		referencingCabinet.store(referencing);

		// Make referenced document dirty
		final String DIRTY = "DIRTY";
		assertEquals(INITIAL, referencing.getOneToOne().get().getChange(),
				"Should be initial value to allow making dirty");
		referenced.setChange(DIRTY);

		// Save dirty changes
		manager.flush();

		// Ensure dirty referenced document is saved
		ReferencingDocument retrievedChangedReferencing = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(ReferencingDocument.class).retrieveByKey(referencing.getKey()).get();
		assertEquals(DIRTY, retrievedChangedReferencing.getOneToOne().get().getChange(), "Should save dirty change");
	}

}