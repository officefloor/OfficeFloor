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
import net.officefloor.cabinet.admin.OfficeCabinetAdmin;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.Query;
import net.officefloor.cabinet.spi.Query.QueryField;
import net.officefloor.cabinet.spi.Range;
import net.officefloor.cabinet.spi.Range.Direction;

/**
 * Tests child {@link Document} instances via {@link HierarchicalDocument}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeCabinetHierarchyTest {

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
	@MStore(cabinets = @MCabinet(HierarchicalDocument.class))
	public void storeAndRetrieve() {
		OfficeCabinet<HierarchicalDocument> cabinet = this.testcase().cabinetManager
				.getOfficeCabinet(HierarchicalDocument.class);

		// Store document
		HierarchicalDocument document = this.testcase().newDocument(HierarchicalDocument.class, 0);
		assertNull(document.getKey(), "New document so should not have key");
		cabinet.store(document);
		String key = document.getKey();
		assertNotNull(key, "Should assign key to document");

		// Ensure with same cabinet that same instance
		HierarchicalDocument retrieved = cabinet.retrieveByKey(document.getKey()).get();
		assertSame(document, retrieved, "Should retrieve same instance");
		assertEquals(key, retrieved.getKey(), "Should not change the key");
	}

	/**
	 * Ensure can store and retrieve values.
	 */
	@Test
	@MStore(cabinetDomainType = HierarchicalDocumentCabinet.class)
	public void domain_storeAndRetrieve() throws Exception {
		HierarchicalDocumentCabinet cabinet = this.testcase()
				.createDomainSpecificCabinet(HierarchicalDocumentCabinet.class);

		// Store document
		HierarchicalDocument document = this.testcase().newDocument(HierarchicalDocument.class, 0);
		assertNull(document.getKey(), "New document so should not have key");
		cabinet.save(document);
		String key = document.getKey();
		assertNotNull(key, "Should assign key to document");

		// Ensure with same cabinet that same instance
		HierarchicalDocument retrieved = cabinet.findByKey(document.getKey()).get();
		assertSame(document, retrieved, "Should retrieve same instance");
		assertEquals(key, retrieved.getKey(), "Should not change the key");
	}

	/**
	 * Ensure can store and later retrieve values.
	 */
	@Test
	@MStore(cabinets = @MCabinet(HierarchicalDocument.class))
	public void storeAndLaterRetrieve() throws Exception {

		// Create the cabinet
		OfficeCabinet<HierarchicalDocument> cabinetTwo = this.testcase().cabinetManager
				.getOfficeCabinet(HierarchicalDocument.class);

		// Store document
		HierarchicalDocument document = this.testcase().setupDocument(HierarchicalDocument.class, 0);

		// Obtain document later (via another cabinet)
		HierarchicalDocument retrieved = cabinetTwo.retrieveByKey(document.getKey()).get();
		assertNotSame(document, retrieved, "Should retrieve different instance");

		// Ensure same data
		assertEquals(document.getKey(), retrieved.getKey(), "Should have same key");
		document.assertDocumentEquals(retrieved);
	}

	/**
	 * Ensure can store and later retrieve values.
	 */
	@Test
	@MStore(cabinetDomainType = HierarchicalDocumentCabinet.class)
	public void domain_storeAndLaterRetrieve() throws Exception {

		// Create the cabinet
		HierarchicalDocumentCabinet cabinetTwo = this.testcase()
				.createDomainSpecificCabinet(HierarchicalDocumentCabinet.class);

		// Store document
		HierarchicalDocument document = this.testcase().setupDocument(HierarchicalDocument.class, 0);

		// Obtain document later (via another cabinet)
		HierarchicalDocument retrieved = cabinetTwo.findByKey(document.getKey()).get();
		assertNotSame(document, retrieved, "Should retrieve different instance");

		// Ensure same data
		assertEquals(document.getKey(), retrieved.getKey(), "Should have same key");
		document.assertDocumentEquals(retrieved);
	}

	@Test
	@MStore(cabinets = @MCabinet(HierarchicalDocument.class))
	public void detectDirty() throws Exception {

		// Create the cabinet
		OfficeCabinet<HierarchicalDocument> cabinet = this.testcase().cabinetManager
				.getOfficeCabinet(HierarchicalDocument.class);

		// Setup document
		String key = this.testcase().setupDocument(HierarchicalDocument.class, 0).getKey();

		// Obtain the document
		HierarchicalDocument document = cabinet.retrieveByKey(key).get();

		// Change the child value
		final String CHANGE = "CHANGED";
		assertNotEquals(CHANGE, document.getChild().getStringObject(), "INVALID TEST: not changing value");
		document.getChild().setStringObject(CHANGE);

		// Close (causing save on being dirty)
		OfficeCabinetAdmin admin = this.testcase().getOfficeCabinetAdmin(cabinet);
		admin.close();

		// Ensure dirty change saved
		HierarchicalDocument updated = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(HierarchicalDocument.class).retrieveByKey(key).get();
		assertEquals(CHANGE, updated.getChild().getStringObject(), "Should update in store as dirty");
	}

	@Test
	@MStore(cabinetDomainType = HierarchicalDocumentCabinet.class)
	public void domain_detectDirty() throws Exception {

		// Create the cabinet
		HierarchicalDocumentCabinet cabinet = this.testcase()
				.createDomainSpecificCabinet(HierarchicalDocumentCabinet.class);

		// Setup document
		String key = this.testcase().setupDocument(HierarchicalDocument.class, 0).getKey();

		// Obtain the document
		HierarchicalDocument document = cabinet.findByKey(key).get();

		// Change the child value
		final String CHANGE = "CHANGED";
		assertNotEquals(CHANGE, document.getChild().getStringObject(), "INVALID TEST: not changing value");
		document.getChild().setStringObject(CHANGE);

		// Ensure dirty change saved
		HierarchicalDocument updated = this.testcase().createDomainSpecificCabinet(HierarchicalDocumentCabinet.class)
				.findByKey(key).get();
		assertEquals(CHANGE, updated.getChild().getStringObject(), "Should update in store as dirty");
	}

	@Test
	@MStore(cabinets = @MCabinet(value = HierarchicalDocument.class, indexes = @MIndex("testName")))
	public void query() throws Exception {

		// Create the cabinet
		OfficeCabinet<HierarchicalDocument> cabinet = this.testcase().cabinetManager
				.getOfficeCabinet(HierarchicalDocument.class);

		// Setup the document
		HierarchicalDocument setup = this.testcase().setupDocument(HierarchicalDocument.class, 0);

		// Obtain the document
		Iterator<HierarchicalDocument> documents = cabinet
				.retrieveByQuery(new Query(new QueryField("testName", setup.getTestName())), null);

		// Ensure obtain attribute
		assertTrue(documents.hasNext(), "Should find document");
		HierarchicalDocument document = documents.next();
		assertNotNull(document, "Should retrieve document");

		// No further documents
		assertFalse(documents.hasNext(), "Should only be one document");

		// Ensure correct document
		document.assertDocumentEquals(this.testcase().newDocument(HierarchicalDocument.class, 0));
	}

	@Test
	@MStore(cabinetDomainType = HierarchicalDocumentCabinet.class)
	public void domain_query() throws Exception {

		// Create the cabinet
		HierarchicalDocumentCabinet cabinet = this.testcase()
				.createDomainSpecificCabinet(HierarchicalDocumentCabinet.class);

		// Setup the document
		HierarchicalDocument setup = this.testcase().setupDocument(HierarchicalDocument.class, 0);

		// Obtain the document
		Iterator<HierarchicalDocument> documents = cabinet.findByTestName(setup.getTestName());

		// Ensure obtain attribute
		assertTrue(documents.hasNext(), "Should find document");
		HierarchicalDocument document = documents.next();
		assertNotNull(document, "Should retrieve document");

		// No further documents
		assertFalse(documents.hasNext(), "Should only be one document");

		// Ensure correct document
		document.assertDocumentEquals(this.testcase().newDocument(HierarchicalDocument.class, 0));
	}

	@Test
	@MStore(cabinets = @MCabinet(value = HierarchicalDocument.class, indexes = @MIndex("testName")))
	public void session() throws Exception {

		// Create the cabinet
		OfficeCabinet<HierarchicalDocument> cabinet = this.testcase().cabinetManager
				.getOfficeCabinet(HierarchicalDocument.class);

		// Setup the document
		HierarchicalDocument setup = this.testcase().setupDocument(HierarchicalDocument.class, 0);

		// Obtain by key
		HierarchicalDocument retrieved = cabinet.retrieveByKey(setup.getKey()).get();

		// Ensure obtains same instance to maintain state
		HierarchicalDocument sameByKey = cabinet.retrieveByKey(setup.getKey()).get();
		assertSame(retrieved, sameByKey, "Should retrieve same instance by key");

		// Ensure same instance by query
		Iterator<HierarchicalDocument> documents = cabinet
				.retrieveByQuery(new Query(new QueryField("testName", setup.getTestName())), null);
		HierarchicalDocument sameByQuery = documents.next();
		assertSame(retrieved, sameByQuery, "Should retrieve same instance by query");
	}

	@Test
	@MStore(cabinetDomainType = HierarchicalDocumentCabinet.class)
	public void domain_session() throws Exception {

		// Create the cabinet
		HierarchicalDocumentCabinet cabinet = this.testcase()
				.createDomainSpecificCabinet(HierarchicalDocumentCabinet.class);

		// Setup the document
		HierarchicalDocument setup = this.testcase().setupDocument(HierarchicalDocument.class, 0);

		// Obtain by key
		HierarchicalDocument retrieved = cabinet.findByKey(setup.getKey()).get();

		// Ensure obtains same instance to maintain state
		HierarchicalDocument sameByKey = cabinet.findByKey(setup.getKey()).get();
		assertSame(retrieved, sameByKey, "Should retrieve same instance by key");

		// Ensure same instance by query
		Iterator<HierarchicalDocument> documents = cabinet.findByTestName(setup.getTestName());
		HierarchicalDocument sameByQuery = documents.next();
		assertSame(retrieved, sameByQuery, "Should retrieve same instance by query");
	}

	/**
	 * Ensure able to retrieve next {@link DocumentBundle} instances of different
	 * sizes, count and repetitions.
	 */
	@ParameterizedTest(name = "{index}. Hierarchy Next - Bundle size {0}, Bundle count {1}, Repeated {2}")
	@CsvSource({ "1,1,0", "1,2,0", "1,10,0", "2,1,0", "10,1,0", "10,10,0", "1,1,10", "1,1,2", "1,10,10", "10,1,10",
			"10,10,10" })
	@MStore(cabinets = @MCabinet(value = HierarchicalDocument.class, indexes = @MIndex(value = "testName", sort = "intPrimitive")))
	public void retrieveNextBundles(int bundleSize, int bundleCount, int repeated) throws Exception {
		this.retrieveBundles(new RetrieveHierarchicalDocuments().bundleSize(bundleSize).bundleCount(bundleCount)
				.repeatCount(repeated));
	}

	/**
	 * Ensure able to retrieve next {@link DocumentBundle} instances by next
	 * document token of different sizes, count and repetitions.
	 */
	@ParameterizedTest(name = "{index}. Hierarchy Token - Bundle size {0}, Bundle count {1}, Repeated {2}")
	@CsvSource({ "1,1,0", "1,2,0", "1,10,0", "2,1,0", "10,1,0", "10,10,0", "1,1,10", "1,1,2", "1,10,10", "10,1,10",
			"10,10,10" })
	@MStore(cabinets = @MCabinet(value = HierarchicalDocument.class, indexes = @MIndex(value = "testName", sort = "offset")))
	public void retrieveNextBundlesByNextDocumentToken(int bundleSize, int bundleCount, int repeated)
			throws Exception {
		this.retrieveBundles(new RetrieveHierarchicalDocuments().getNextBundle((bundle, cabinet) -> {
			String token = bundle.getNextDocumentBundleToken();
			return token == null ? null
					: cabinet.retrieveByQuery(
							new Query(new QueryField("testName",
									AbstractOfficeCabinetHierarchyTest.this.testcase().testName)),
							new Range("offset", Direction.Ascending, bundleSize, token));
		}).bundleSize(bundleSize).bundleCount(bundleCount).repeatCount(repeated));
	}

	/**
	 * Manages retrieving the {@link HierarchicalDocument}.
	 */
	protected class RetrieveHierarchicalDocuments extends
			RetrieveBundle<HierarchicalDocument, OfficeCabinet<HierarchicalDocument>, RetrieveHierarchicalDocuments> {
		public RetrieveHierarchicalDocuments() {
			this.getFirstBundle((cabinet) -> cabinet.retrieveByQuery(
					new Query(new QueryField("testName", AbstractOfficeCabinetHierarchyTest.this.testcase().testName)),
					new Range("offset", Direction.Ascending, this.bundleSize)));
			this.getDocumentIndex((document) -> document.getOffset());
			this.getNextBundle((bundle, cabinet) -> bundle.nextDocumentBundle());
		}
	}

	/**
	 * Retrieves the {@link HierarchicalDocument}.
	 * 
	 * @param retrieveBundle {@link RetrieveHierarchicalDocuments}.
	 */
	private void retrieveBundles(RetrieveHierarchicalDocuments retrieveBundle) {

		// Create the cabinet
		OfficeCabinet<HierarchicalDocument> cabinet = this.testcase().cabinetManager
				.getOfficeCabinet(HierarchicalDocument.class);

		// Ensure no data
		DocumentBundle<HierarchicalDocument> bundle = retrieveBundle.getFirstBundle.apply(cabinet);
		assertNull(bundle, "Should be no data setup yet");

		// Set up documents
		final int size = retrieveBundle.bundleSize * retrieveBundle.bundleCount;
		for (int i = 0; i < size; i++) {
			HierarchicalDocument doc = this.testcase().newDocument(HierarchicalDocument.class, i + 1); // +1 offset
			cabinet.store(doc);
		}

		// Retrieve the bundles
		this.testcase().retrieveBundles(cabinet, retrieveBundle);
	}

}