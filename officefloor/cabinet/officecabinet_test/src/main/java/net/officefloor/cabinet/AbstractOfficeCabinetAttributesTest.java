package net.officefloor.cabinet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import net.officefloor.cabinet.AbstractOfficeCabinetTestCase.RetrieveBundle;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.Query;
import net.officefloor.cabinet.spi.Query.QueryField;
import net.officefloor.cabinet.spi.Range;
import net.officefloor.cabinet.spi.Range.Direction;

/**
 * Tests storing attributes via the {@link AttributeTypesDocument}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeCabinetAttributesTest {

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
	@MStore(cabinets = @MCabinet(AttributeTypesDocument.class))
	public void storeAndRetrieve() {
		OfficeCabinet<AttributeTypesDocument> cabinet = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(AttributeTypesDocument.class);

		// Store document
		AttributeTypesDocument document = this.testcase().newDocument(AttributeTypesDocument.class, 0);
		assertNull(document.getKey(), "New document so should not have key");
		cabinet.store(document);
		String key = document.getKey();
		assertNotNull(key, "Should assign key to document");

		// Ensure with same cabinet that same instance
		AttributeTypesDocument retrieved = cabinet.retrieveByKey(document.getKey()).get();
		assertSame(document, retrieved, "Should retrieve same instance");
		assertEquals(key, retrieved.getKey(), "Should not change the key");
	}

	/**
	 * Ensure can store and retrieve values.
	 */
	@Test
	@MStore(cabinetDomainType = AttributeTypesDocumentCabinet.class)
	public void domain_storeAndRetrieve() throws Exception {
		AttributeTypesDocumentCabinet cabinet = this.testcase()
				.createDomainSpecificCabinet(AttributeTypesDocumentCabinet.class);

		// Store document
		AttributeTypesDocument document = this.testcase().newDocument(AttributeTypesDocument.class, 0);
		assertNull(document.getKey(), "New document so should not have key");
		cabinet.save(document);
		String key = document.getKey();
		assertNotNull(key, "Should assign key to document");

		// Ensure with same cabinet that same instance
		AttributeTypesDocument retrieved = cabinet.findByKey(document.getKey()).get();
		assertSame(document, retrieved, "Should retrieve same instance");
		assertEquals(key, retrieved.getKey(), "Should not change the key");
	}

	/**
	 * Ensure can store and later retrieve values.
	 */
	@Test
	@MStore(cabinets = @MCabinet(AttributeTypesDocument.class))
	public void storeAndLaterRetrieve() throws Exception {

		// Store document
		AttributeTypesDocument document = this.testcase().setupDocument(AttributeTypesDocument.class, 0);

		// Obtain document later (via another cabinet)
		OfficeCabinet<AttributeTypesDocument> cabinet = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(AttributeTypesDocument.class);
		AttributeTypesDocument retrieved = cabinet.retrieveByKey(document.getKey()).get();
		assertNotSame(document, retrieved, "Should retrieve different instance");

		// Ensure same data
		assertEquals(document.getKey(), retrieved.getKey(), "Should have same key");
		document.assertDocumentEquals(retrieved, "");
	}

	/**
	 * Ensure can store and later retrieve values.
	 */
	@Test
	@MStore(cabinetDomainType = AttributeTypesDocumentCabinet.class)
	public void domain_storeAndLaterRetrieve() throws Exception {

		// Store document
		AttributeTypesDocument document = this.testcase().setupDocument(AttributeTypesDocument.class, 0);

		// Obtain document later (via another cabinet)
		AttributeTypesDocumentCabinet cabinet = this.testcase()
				.createDomainSpecificCabinet(AttributeTypesDocumentCabinet.class);
		AttributeTypesDocument retrieved = cabinet.findByKey(document.getKey()).get();
		assertNotSame(document, retrieved, "Should retrieve different instance");

		// Ensure same data
		assertEquals(document.getKey(), retrieved.getKey(), "Should have same key");
		document.assertDocumentEquals(retrieved, "");
	}

	@Test
	@MStore(cabinets = @MCabinet(AttributeTypesDocument.class))
	public void detectDirty() throws Exception {

		// Setup document
		String key = this.testcase().setupDocument(AttributeTypesDocument.class, 0).getKey();

		// Obtain the document
		CabinetManager manager = this.testcase().officeStore.createCabinetManager();
		OfficeCabinet<AttributeTypesDocument> cabinet = manager.getOfficeCabinet(AttributeTypesDocument.class);
		AttributeTypesDocument document = cabinet.retrieveByKey(key).get();

		// Change the value
		final int CHANGE = 1000;
		assertNotEquals(CHANGE, document.getIntPrimitive(), "INVALID TEST: not changing value");
		document.setIntPrimitive(CHANGE);

		// Save
		manager.flush();

		// Ensure dirty change saved
		AttributeTypesDocument updated = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(AttributeTypesDocument.class).retrieveByKey(key).get();
		assertEquals(CHANGE, updated.getIntPrimitive(), "Should update in store as dirty");
	}

	@Test
	@MStore(cabinetDomainType = AttributeTypesDocumentCabinet.class)
	public void domain_detectDirty() throws Exception {

		// Setup document
		String key = this.testcase().setupDocument(AttributeTypesDocument.class, 0).getKey();

		// Obtain the document
		CabinetManager manager = this.testcase().officeStore.createCabinetManager();
		AttributeTypesDocumentCabinet cabinet = this.testcase()
				.createDomainSpecificCabinet(AttributeTypesDocumentCabinet.class, manager);
		AttributeTypesDocument document = cabinet.findByKey(key).get();

		// Change the value
		final int CHANGE = 1000;
		assertNotEquals(CHANGE, document.getIntPrimitive(), "INVALID TEST: not changing value");
		document.setIntPrimitive(CHANGE);
		manager.flush();

		// Ensure dirty change saved
		AttributeTypesDocument updated = this.testcase()
				.createDomainSpecificCabinet(AttributeTypesDocumentCabinet.class).findByKey(key).get();
		assertEquals(CHANGE, updated.getIntPrimitive(), "Should update in store as dirty");
	}

	@Test
	@MStore(cabinets = @MCabinet(value = AttributeTypesDocument.class, indexes = @MIndex("testName")))
	public void query() throws Exception {

		// Setup the document
		AttributeTypesDocument setup = this.testcase().setupDocument(AttributeTypesDocument.class, 0);

		// Obtain the document
		OfficeCabinet<AttributeTypesDocument> cabinet = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(AttributeTypesDocument.class);
		Iterator<AttributeTypesDocument> documents = cabinet
				.retrieveByQuery(new Query(new QueryField("testName", setup.getTestName())), null);

		// Ensure obtain attribute
		assertTrue(documents.hasNext(), "Should find document");
		AttributeTypesDocument document = documents.next();
		assertNotNull(document, "Should retrieve document");

		// No further documents
		assertFalse(documents.hasNext(), "Should only be one document");

		// Ensure correct document
		document.assertDocumentEquals(this.testcase().newDocument(AttributeTypesDocument.class, 0),
				"Incorrect document");
	}

	@Test
	@MStore(cabinetDomainType = AttributeTypesDocumentCabinet.class)
	public void domain_query() throws Exception {

		// Setup the document
		AttributeTypesDocument setup = this.testcase().setupDocument(AttributeTypesDocument.class, 0);

		// Obtain the document
		AttributeTypesDocumentCabinet cabinet = this.testcase()
				.createDomainSpecificCabinet(AttributeTypesDocumentCabinet.class);
		Iterator<AttributeTypesDocument> documents = cabinet.findByTestName(setup.getTestName());

		// Ensure obtain attribute
		assertTrue(documents.hasNext(), "Should find document");
		AttributeTypesDocument document = documents.next();
		assertNotNull(document, "Should retrieve document");

		// No further documents
		assertFalse(documents.hasNext(), "Should only be one document");

		// Ensure correct document
		document.assertDocumentEquals(this.testcase().newDocument(AttributeTypesDocument.class, 0),
				"Incorrect document");
	}

	@Test
	@MStore(cabinets = @MCabinet(value = AttributeTypesDocument.class, indexes = @MIndex("testName")))
	public void session() throws Exception {

		// Setup the document
		AttributeTypesDocument setup = this.testcase().setupDocument(AttributeTypesDocument.class, 0);

		// Obtain by key
		OfficeCabinet<AttributeTypesDocument> cabinet = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(AttributeTypesDocument.class);
		AttributeTypesDocument retrieved = cabinet.retrieveByKey(setup.getKey()).get();

		// Ensure obtains same instance to maintain state
		AttributeTypesDocument sameByKey = cabinet.retrieveByKey(setup.getKey()).get();
		assertSame(retrieved, sameByKey, "Should retrieve same instance by key");

		// Ensure same instance by query
		Iterator<AttributeTypesDocument> documents = cabinet
				.retrieveByQuery(new Query(new QueryField("testName", setup.getTestName())), null);
		AttributeTypesDocument sameByQuery = documents.next();
		assertSame(retrieved, sameByQuery, "Should retrieve same instance by query");
	}

	@Test
	@MStore(cabinetDomainType = AttributeTypesDocumentCabinet.class)
	public void domain_session() throws Exception {

		// Setup the document
		AttributeTypesDocument setup = this.testcase().setupDocument(AttributeTypesDocument.class, 0);

		// Obtain by key
		AttributeTypesDocumentCabinet cabinet = this.testcase()
				.createDomainSpecificCabinet(AttributeTypesDocumentCabinet.class);
		AttributeTypesDocument retrieved = cabinet.findByKey(setup.getKey()).get();

		// Ensure obtains same instance to maintain state
		AttributeTypesDocument sameByKey = cabinet.findByKey(setup.getKey()).get();
		assertSame(retrieved, sameByKey, "Should retrieve same instance by key");

		// Ensure same instance by query
		Iterator<AttributeTypesDocument> documents = cabinet.findByTestName(setup.getTestName());
		AttributeTypesDocument sameByQuery = documents.next();
		assertSame(retrieved, sameByQuery, "Should retrieve same instance by query");
	}

	@Test
	@MStore(cabinets = @MCabinet(value = AttributeTypesDocument.class, indexes = @MIndex(value = "testName", sort = "intPrimitive")))
	public void sortedAscending() throws Exception {

		// Set up documents
		final int size = 10;
		this.testcase().setupDocuments(10, AttributeTypesDocument.class,
				(doc, index) -> doc.setIntPrimitive((size - 1) - index));

		// Obtain sorted documents
		OfficeCabinet<AttributeTypesDocument> cabinet = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(AttributeTypesDocument.class);
		Iterator<AttributeTypesDocument> documents = cabinet.retrieveByQuery(
				new Query(new QueryField("testName", this.testcase().testName)),
				new Range("intPrimitive", Direction.Ascending, size));

		// Ensure documents are sorted ascending
		for (int i = 0; i < size; i++) {

			// Should have document
			assertTrue(documents.hasNext(), "Should have document (" + i + ")");
			AttributeTypesDocument doc = documents.next();

			// Ensure order
			assertEquals(i, doc.getIntPrimitive(), "Documents out of order");
		}
		assertFalse(documents.hasNext(), "Should be no further documents");
	}

	@Test
	@MStore(cabinets = @MCabinet(value = AttributeTypesDocument.class, indexes = @MIndex(value = "testName", sort = "intPrimitive")))
	public void sortedDescending() throws Exception {

		// Set up documents
		final int size = 10;
		this.testcase().setupDocuments(size, AttributeTypesDocument.class,
				(doc, index) -> doc.setIntPrimitive(index + 1));

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinet = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(AttributeTypesDocument.class);

		// Obtain sorted documents
		Iterator<AttributeTypesDocument> documents = cabinet.retrieveByQuery(
				new Query(new QueryField("testName", this.testcase().testName)),
				new Range("intPrimitive", Direction.Descending, size));

		// Ensure documents are sorted descending
		for (int i = size; i > 0; i--) {

			// Should have document
			assertTrue(documents.hasNext(), "Should have document (" + i + ")");
			AttributeTypesDocument doc = documents.next();

			// Ensure order
			assertEquals(i, doc.getIntPrimitive(), "Documents out of order");
		}
		assertFalse(documents.hasNext(), "Should be no further documents");
	}

	/**
	 * Ensure able to retrieve {@link DocumentBundle}.
	 */
	@Test
	@MStore(cabinets = @MCabinet(value = AttributeTypesDocument.class, indexes = @MIndex(value = "testName", sort = "intPrimitive")))
	public void retrieveFirstBundle() throws Exception {

		// Set up documents
		final int size = 10;
		this.testcase().setupDocuments(size, AttributeTypesDocument.class,
				(doc, index) -> doc.setIntPrimitive(index + 1));

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinet = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(AttributeTypesDocument.class);

		// Retrieve the document bundles
		DocumentBundle<AttributeTypesDocument> bundle = cabinet.retrieveByQuery(
				new Query(new QueryField("testName", this.testcase().testName)),
				new Range("intPrimitive", Direction.Ascending, 1));
		assertTrue(bundle.hasNext(), "Should find document");
		AttributeTypesDocument document = bundle.next();
		assertEquals(1, document.getIntPrimitive(), "Incorrect value");
		assertFalse(bundle.hasNext(), "Should be no further documents");
	}

	/**
	 * Ensure able to retrieve next {@link DocumentBundle} instances of different
	 * sizes, count and repetitions.
	 */
	@ParameterizedTest(name = "{index}. Attributes Next - Bundle size {0}, Bundle count {1}, Repeated {2}")
	@CsvSource({ "1,1,0", "1,2,0", "1,10,0", "2,1,0", "10,1,0", "10,10,0", "1,1,10", "1,1,2", "1,10,10", "10,1,10",
			"10,10,10" })
	@MStore(cabinets = @MCabinet(value = AttributeTypesDocument.class, indexes = @MIndex(value = "testName", sort = "intPrimitive")))
	public void retrieveNextBundles(int bundleSize, int bundleCount, int repeated) throws Exception {
		this.retrieveBundles(new RetrieveAttributeTypesDocuments().bundleSize(bundleSize).bundleCount(bundleCount)
				.repeatCount(repeated));
	}

	/**
	 * Ensure able to retrieve next {@link DocumentBundle} instances by next
	 * document token of different sizes, count and repetitions.
	 */
	@ParameterizedTest(name = "{index}. Attributes Token - Bundle size {0}, Bundle count {1}, Repeated {2}")
	@CsvSource({ "1,1,0", "1,2,0", "1,10,0", "2,1,0", "10,1,0", "10,10,0", "1,1,10", "1,1,2", "1,10,10", "10,1,10",
			"10,10,10" })
	@MStore(cabinets = @MCabinet(value = AttributeTypesDocument.class, indexes = @MIndex({ "intPrimitive",
			"testName" })))
	public void retrieveNextBundlesByNextDocumentToken(int bundleSize, int bundleCount, int repeated) throws Exception {
		this.retrieveBundles(new RetrieveAttributeTypesDocuments().getNextBundle((bundle, cabinet) -> {
			String token = bundle.getNextDocumentBundleToken();
			return token == null ? null
					: cabinet.retrieveByQuery(
							new Query(new QueryField("testName",
									AbstractOfficeCabinetAttributesTest.this.testcase().testName)),
							new Range("intPrimitive", Direction.Ascending, bundleSize, token));
		}).bundleSize(bundleSize).bundleCount(bundleCount).repeatCount(repeated));
	}

	/**
	 * Manages retrieving the {@link AttributeTypesDocument}.
	 */
	protected class RetrieveAttributeTypesDocuments extends
			RetrieveBundle<AttributeTypesDocument, OfficeCabinet<AttributeTypesDocument>, RetrieveAttributeTypesDocuments> {
		public RetrieveAttributeTypesDocuments() {
			this.getFirstBundle((cabinet) -> cabinet.retrieveByQuery(
					new Query(new QueryField("testName", AbstractOfficeCabinetAttributesTest.this.testcase().testName)),
					new Range("intPrimitive", Direction.Ascending, this.bundleSize)));
			this.getDocumentIndex((document) -> document.getIntPrimitive());
			this.getNextBundle((bundle, cabinet) -> bundle.nextDocumentBundle());
		}
	}

	/**
	 * Retrieves the {@link AttributeTypesDocument}.
	 * 
	 * @param retrieveBundle {@link RetrieveAttributeTypesDocuments}.
	 */
	protected void retrieveBundles(RetrieveAttributeTypesDocuments retrieveBundle) {

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinet = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(AttributeTypesDocument.class);

		// Ensure no data
		DocumentBundle<AttributeTypesDocument> bundle = retrieveBundle.getFirstBundle.apply(cabinet);
		assertNull(bundle, "Should be no data setup yet");

		// Set up documents
		final int size = retrieveBundle.bundleSize * retrieveBundle.bundleCount;
		this.testcase().setupDocuments(size, AttributeTypesDocument.class, (doc, index) -> doc.setIntPrimitive(index));

		// Retrieve the bundles
		this.testcase().retrieveBundles(cabinet, retrieveBundle);
	}

}