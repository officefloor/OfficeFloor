/*-
 * #%L
 * OfficeFloor Filing Cabinet Test
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

package net.officefloor.cabinet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import net.officefloor.cabinet.admin.OfficeCabinetAdmin;
import net.officefloor.cabinet.domain.CabinetSession;
import net.officefloor.cabinet.domain.DomainCabinetDocumentMetaData;
import net.officefloor.cabinet.domain.DomainCabinetFactory;
import net.officefloor.cabinet.domain.DomainCabinetManufacturer;
import net.officefloor.cabinet.domain.impl.CabinetSessionImpl;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.Index.IndexField;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;
import net.officefloor.cabinet.spi.Query;
import net.officefloor.cabinet.spi.Query.QueryField;
import net.officefloor.cabinet.spi.Range;
import net.officefloor.cabinet.spi.Range.Direction;

/**
 * Tests Office Cabinet.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeCabinetTest {

	/**
	 * Obtains the {@link OfficeCabinetArchive} for the {@link Document} type.
	 * 
	 * @param documentType {@link Document} type.
	 * @param indexes      {@link Index} instances for the
	 *                     {@link OfficeCabinetArchive}.
	 * @return {@link OfficeCabinetArchive} for the {@link Document} type.
	 * @throws Exception If fails to create {@link OfficeCabinet}.
	 */
	protected abstract <D> OfficeCabinetArchive<D> getOfficeCabinetArchive(Class<D> documentType, Index... indexes)
			throws Exception;

	/**
	 * Obtains the {@link DomainCabinetManufacturer}.
	 * 
	 * @return {@link DomainCabinetManufacturer}.
	 */
	protected abstract DomainCabinetManufacturer getDomainSpecificCabinetManufacturer();

	/**
	 * Obtains the {@link OfficeCabinetAdmin} for the {@link OfficeCabinet}.
	 * 
	 * @param cabinet {@link OfficeCabinet}.
	 * @return OfficeCabinetAdmin} for the {@link OfficeCabinet}.
	 * @throws Exception If fails to obtain the {@link OfficeCabinetAdmin}.
	 */
	protected OfficeCabinetAdmin getOfficeCabinetAdmin(Object cabinet) throws Exception {
		return (OfficeCabinetAdmin) cabinet;
	}

	/**
	 * {@link OfficeCabinetArchive} by their {@link OfficeCabinet} type and
	 * {@code Document} type.
	 */
	private final Map<Class<?>, OfficeCabinetArchive<?>> cachedArchives = new HashMap<>();

	@SuppressWarnings("unchecked")
	private <D> OfficeCabinetArchive<D> getArchive(Class<D> documentType, Index... indexes) {
		OfficeCabinetArchive<D> archive = (OfficeCabinetArchive<D>) this.cachedArchives.get(documentType);
		if (archive == null) {
			try {
				archive = this.getOfficeCabinetArchive(documentType, indexes);
			} catch (Exception ex) {
				return fail("Failed to create " + OfficeCabinetArchive.class.getSimpleName() + " for document "
						+ documentType.getName(), ex);
			}
			this.cachedArchives.put(documentType, archive);
		}
		return archive;
	}

	private <D> OfficeCabinet<D> createCabinet(Class<D> documentType, Index... indexes) {
		return this.getArchive(documentType, indexes).createOfficeCabinet();
	}

	private <C> C createDomainSpecificCabinet(Class<C> cabinetType) throws Exception {
		DomainCabinetManufacturer manufacturer = this.getDomainSpecificCabinetManufacturer();
		DomainCabinetFactory<C> factory = manufacturer.createDomainCabinetFactory(cabinetType);

		// Build the archives
		Map<Class<?>, OfficeCabinetArchive<?>> archives = new HashMap<>();
		for (DomainCabinetDocumentMetaData metaData : factory.getMetaData()) {
			Class<?> documentType = metaData.getDocumentType();
			OfficeCabinetArchive<?> archive = this.getArchive(documentType, metaData.getIndexes());
			archives.put(documentType, archive);
		}

		// Build the session
		CabinetSession session = new CabinetSessionImpl(archives);

		// Build the domain specific cabinet
		C domainCabinet = factory.createDomainSpecificCabinet(session);
		return domainCabinet;
	}

	/**
	 * Creates the {@link Document} in {@link OfficeCabinet},
	 * 
	 * @param documentType {@link Document} type.
	 * @param offset       Offset for state.
	 * @return New {@link Document}.
	 */
	private <D> D newDocument(Class<D> documentType, int offset) {
		try {
			return documentType.getConstructor(int.class, String.class).newInstance(offset, this.testName);
		} catch (Exception ex) {
			return fail("Failed new " + documentType.getSimpleName() + "(" + offset + ")", ex);
		}
	}

	/**
	 * Sets up the {@link Document} in {@link OfficeCabinet}.
	 * 
	 * @param documentType {@link Document} type.
	 * @param offset       Offset for state.
	 * @return Set up {@link AttributeTypesDocument}.
	 */
	@SuppressWarnings("unchecked")
	private <D> D setupDocument(Class<D> documentType, int offset) {
		OfficeCabinetArchive<D> archive = (OfficeCabinetArchive<D>) this.cachedArchives.get(documentType);
		assertNotNull(archive, "No " + OfficeCabinetArchive.class.getSimpleName() + " set up for document type "
				+ documentType.getName());
		OfficeCabinet<D> cabinet = archive.createOfficeCabinet();
		D document = this.newDocument(documentType, offset);
		cabinet.store(document);
		return document;
	}

	private String testName;

	@BeforeEach
	public void setupTestName(TestInfo info) {
		this.testName = info.getDisplayName();
	}

	/*
	 * ========================= Tests ===============================
	 */

	@AfterEach
	public void closeAllArchives() throws Exception {
		for (OfficeCabinetArchive<?> archive : this.cachedArchives.values()) {
			archive.close();
		}
	}

	/**
	 * Ensure can store and retrieve values.
	 */
	@Test
	public void attributeTypes_storeAndRetrieve() {
		OfficeCabinet<AttributeTypesDocument> cabinet = this.createCabinet(AttributeTypesDocument.class);

		// Store document
		AttributeTypesDocument document = this.newDocument(AttributeTypesDocument.class, 0);
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
	public void domain_attributeTypes_storeAndRetrieve() throws Exception {
		AttributeTypesDocumentCabinet cabinet = this.createDomainSpecificCabinet(AttributeTypesDocumentCabinet.class);

		// Store document
		AttributeTypesDocument document = this.newDocument(AttributeTypesDocument.class, 0);
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
	public void attributeTypes_storeAndLaterRetrieve() throws Exception {

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinetTwo = this.createCabinet(AttributeTypesDocument.class);

		// Store document
		AttributeTypesDocument document = this.setupDocument(AttributeTypesDocument.class, 0);

		// Obtain document later (via another cabinet)
		AttributeTypesDocument retrieved = cabinetTwo.retrieveByKey(document.getKey()).get();
		assertNotSame(document, retrieved, "Should retrieve different instance");

		// Ensure same data
		assertEquals(document.getKey(), retrieved.getKey(), "Should have same key");
		document.assertDocumentEquals(retrieved, "");
	}

	/**
	 * Ensure can store and later retrieve values.
	 */
	@Test
	public void domain_attributeTypes_storeAndLaterRetrieve() throws Exception {

		// Create the cabinet
		AttributeTypesDocumentCabinet cabinetTwo = this
				.createDomainSpecificCabinet(AttributeTypesDocumentCabinet.class);

		// Store document
		AttributeTypesDocument document = this.setupDocument(AttributeTypesDocument.class, 0);

		// Obtain document later (via another cabinet)
		AttributeTypesDocument retrieved = cabinetTwo.findByKey(document.getKey()).get();
		assertNotSame(document, retrieved, "Should retrieve different instance");

		// Ensure same data
		assertEquals(document.getKey(), retrieved.getKey(), "Should have same key");
		document.assertDocumentEquals(retrieved, "");
	}

	@Test
	public void attributeTypes_detectDirty() throws Exception {

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinet = this.createCabinet(AttributeTypesDocument.class);

		// Setup document
		String key = this.setupDocument(AttributeTypesDocument.class, 0).getKey();

		// Obtain the document
		AttributeTypesDocument document = cabinet.retrieveByKey(key).get();

		// Change the value
		final int CHANGE = 1000;
		assertNotEquals(CHANGE, document.getIntPrimitive(), "INVALID TEST: not changing value");
		document.setIntPrimitive(CHANGE);

		// Close (causing save on being dirty)
		OfficeCabinetAdmin admin = this.getOfficeCabinetAdmin(cabinet);
		admin.close();

		// Ensure dirty change saved
		AttributeTypesDocument updated = this.createCabinet(AttributeTypesDocument.class).retrieveByKey(key).get();
		assertEquals(CHANGE, updated.getIntPrimitive(), "Should update in store as dirty");
	}

	@Test
	public void domain_attributeTypes_detectDirty() throws Exception {

		// Create the cabinet
		AttributeTypesDocumentCabinet cabinet = this.createDomainSpecificCabinet(AttributeTypesDocumentCabinet.class);

		// Setup document
		String key = this.setupDocument(AttributeTypesDocument.class, 0).getKey();

		// Obtain the document
		AttributeTypesDocument document = cabinet.findByKey(key).get();

		// Change the value
		final int CHANGE = 1000;
		assertNotEquals(CHANGE, document.getIntPrimitive(), "INVALID TEST: not changing value");
		document.setIntPrimitive(CHANGE);

		// Close (causing save on being dirty)
		OfficeCabinetAdmin admin = this.getOfficeCabinetAdmin(cabinet);
		admin.close();

		// Ensure dirty change saved
		AttributeTypesDocument updated = this.createDomainSpecificCabinet(AttributeTypesDocumentCabinet.class)
				.findByKey(key).get();
		assertEquals(CHANGE, updated.getIntPrimitive(), "Should update in store as dirty");
	}

	@Test
	public void attributeTypes_index() throws Exception {

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinet = this.createCabinet(AttributeTypesDocument.class,
				new Index(new IndexField("testName")));

		// Setup the document
		AttributeTypesDocument setup = this.setupDocument(AttributeTypesDocument.class, 0);

		// Obtain the document
		Iterator<AttributeTypesDocument> documents = cabinet
				.retrieveByQuery(new Query(new QueryField("testName", setup.getTestName())), null);

		// Ensure obtain attribute
		assertTrue(documents.hasNext(), "Should find document");
		AttributeTypesDocument document = documents.next();
		assertNotNull(document, "Should retrieve document");

		// No further documents
		assertFalse(documents.hasNext(), "Should only be one document");

		// Ensure correct document
		document.assertDocumentEquals(this.newDocument(AttributeTypesDocument.class, 0), "Incorrect document");
	}

	@Test
	public void domain_attributeTypes_index() throws Exception {

		// Create the cabinet
		AttributeTypesDocumentCabinet cabinet = this.createDomainSpecificCabinet(AttributeTypesDocumentCabinet.class);

		// Setup the document
		AttributeTypesDocument setup = this.setupDocument(AttributeTypesDocument.class, 0);

		// Obtain the document
		Iterator<AttributeTypesDocument> documents = cabinet.findByTestName(setup.getTestName());

		// Ensure obtain attribute
		assertTrue(documents.hasNext(), "Should find document");
		AttributeTypesDocument document = documents.next();
		assertNotNull(document, "Should retrieve document");

		// No further documents
		assertFalse(documents.hasNext(), "Should only be one document");

		// Ensure correct document
		document.assertDocumentEquals(this.newDocument(AttributeTypesDocument.class, 0), "Incorrect document");
	}

	@Test
	public void attributeTypes_sortedAscending() throws Exception {

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinet = this.createCabinet(AttributeTypesDocument.class,
				new Index("intPrimitive", new IndexField("testName")));

		// Set up documents
		final int size = 10;
		for (int i = 0; i < size; i++) {
			AttributeTypesDocument doc = this.newDocument(AttributeTypesDocument.class, i);
			doc.setIntPrimitive((size - 1) - i); // zero based index
			cabinet.store(doc);
		}

		// Obtain sorted documents
		Iterator<AttributeTypesDocument> documents = cabinet.retrieveByQuery(
				new Query(new QueryField("testName", this.testName)),
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
	public void attributeTypes_sortedDescending() throws Exception {

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinet = this.createCabinet(AttributeTypesDocument.class,
				new Index("intPrimitive", new IndexField("testName")));

		// Set up documents
		final int size = 10;
		for (int i = 0; i < size; i++) {
			AttributeTypesDocument doc = this.newDocument(AttributeTypesDocument.class, i);
			doc.setIntPrimitive(i + 1); // one based index
			cabinet.store(doc);
		}

		// Obtain sorted documents
		Iterator<AttributeTypesDocument> documents = cabinet.retrieveByQuery(
				new Query(new QueryField("testName", this.testName)),
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

	@Test
	public void attributeTypes_bundle() throws Exception {

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinet = this.createCabinet(AttributeTypesDocument.class,
				new Index("intPrimitive", new IndexField("testName")));

		// Set up documents
		final int bundleSize = 10;
		final int bundleCount = 3;
		final int size = bundleSize * bundleCount;
		for (int i = 0; i < size; i++) {
			AttributeTypesDocument doc = this.newDocument(AttributeTypesDocument.class, i);
			doc.setIntPrimitive((size - 1) - i); // zero based index
			cabinet.store(doc);
		}

		// Obtain sorted documents
		DocumentBundle<AttributeTypesDocument> documents = cabinet.retrieveByQuery(
				new Query(new QueryField("testName", this.testName)),
				new Range("intPrimitive", Direction.Ascending, bundleSize));

		// Ensure all document bundles are correct
		for (int bundleIndex = 0; bundleIndex < bundleCount; bundleIndex++) {

			// Ensure have bundle
			assertNotNull(documents, "Should have document bundle " + bundleIndex);

			// Ensure all documents for bundle
			for (int documentIndex = 0; documentIndex < bundleSize; documentIndex++) {

				// Should have document
				assertTrue(documents.hasNext(),
						"Should have document (" + documentIndex + ", bundle " + bundleIndex + ")");
				AttributeTypesDocument doc = documents.next();

				// Ensure order
				int expectedIndex = (bundleIndex * bundleSize) + documentIndex;
				assertEquals(expectedIndex, doc.getIntPrimitive(), "Documents out of order for bundle " + bundleIndex);
			}

			// Ensure no further documents
			assertFalse(documents.hasNext(), "Should be no further documents for bundle " + bundleIndex);

			// Obtain the next bundle
			documents = documents.nextDocumentBundle();
		}

		// Ensure no further bundles
		assertNull(documents, "Should be no further bundles");
	}

	@Test
	public void attributeTypes_bundle_token() throws Exception {

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinet = this.createCabinet(AttributeTypesDocument.class,
				new Index("intPrimitive", new IndexField("testName")));

		// Set up documents
		final int bundleSize = 10;
		final int bundleCount = 3;
		final int size = bundleSize * bundleCount;
		for (int i = 0; i < size; i++) {
			AttributeTypesDocument doc = this.newDocument(AttributeTypesDocument.class, i);
			doc.setIntPrimitive((size - 1) - i); // zero based index
			cabinet.store(doc);
		}

		// Obtain sorted documents
		Query query = new Query(new QueryField("testName", this.testName));
		DocumentBundle<AttributeTypesDocument> documents = cabinet.retrieveByQuery(query,
				new Range("intPrimitive", Direction.Ascending, bundleSize));

		// Ensure all document bundles are correct
		for (int bundleIndex = 0; bundleIndex < bundleCount; bundleIndex++) {

			// Ensure have bundle
			assertNotNull(documents, "Should have document bundle " + bundleIndex);

			// Ensure all documents for bundle
			for (int documentIndex = 0; documentIndex < bundleSize; documentIndex++) {

				// Should have document
				assertTrue(documents.hasNext(),
						"Should have document (" + documentIndex + ", bundle " + bundleIndex + ")");
				AttributeTypesDocument doc = documents.next();

				// Ensure order
				int expectedIndex = (bundleIndex * bundleSize) + documentIndex;
				assertEquals(expectedIndex, doc.getIntPrimitive(), "Documents out of order");
			}

			// Ensure no further documents
			assertFalse(documents.hasNext(), "Should be no further documents for bundle " + bundleIndex);

			// Obtain the next bundle via token
			String nextDocumentBundleToken = documents.getNextDocumentBundleToken();
			if (bundleIndex >= (bundleCount - 1)) {
				assertNull(nextDocumentBundleToken, "Should be no further document bundles");
				documents = null;
			} else {
				assertNotNull(nextDocumentBundleToken, "Should have next document bundle token");
				documents = cabinet.retrieveByQuery(query,
						new Range("intPrimitive", Direction.Ascending, bundleSize, nextDocumentBundleToken));
			}
		}

		// Ensure no further bundles
		assertNull(documents, "Should be no further bundles");
	}

	@Test
	public void hierarchy_index() throws Exception {

		// Create the cabinet
		OfficeCabinet<HierarchicalDocument> cabinet = this.createCabinet(HierarchicalDocument.class,
				new Index(new IndexField("testName")));

		// Setup the document
		HierarchicalDocument setup = this.setupDocument(HierarchicalDocument.class, 0);

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
		document.assertDocumentEquals(this.newDocument(HierarchicalDocument.class, 0));
	}

	@Test
	public void domain_hierarchy_index() throws Exception {

		// Create the cabinet
		HierarchicalDocumentCabinet cabinet = this.createDomainSpecificCabinet(HierarchicalDocumentCabinet.class);

		// Setup the document
		HierarchicalDocument setup = this.setupDocument(HierarchicalDocument.class, 0);

		// Obtain the document
		Iterator<HierarchicalDocument> documents = cabinet.findByTestName(setup.getTestName());

		// Ensure obtain attribute
		assertTrue(documents.hasNext(), "Should find document");
		HierarchicalDocument document = documents.next();
		assertNotNull(document, "Should retrieve document");

		// No further documents
		assertFalse(documents.hasNext(), "Should only be one document");

		// Ensure correct document
		document.assertDocumentEquals(this.newDocument(HierarchicalDocument.class, 0));
	}

	/**
	 * Ensure can store and retrieve values.
	 */
	@Test
	public void hierarchy_storeAndRetrieve() {
		OfficeCabinet<HierarchicalDocument> cabinet = this.createCabinet(HierarchicalDocument.class);

		// Store document
		HierarchicalDocument document = this.newDocument(HierarchicalDocument.class, 0);
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
	public void domain_hierarchy_storeAndRetrieve() throws Exception {
		HierarchicalDocumentCabinet cabinet = this.createDomainSpecificCabinet(HierarchicalDocumentCabinet.class);

		// Store document
		HierarchicalDocument document = this.newDocument(HierarchicalDocument.class, 0);
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
	public void hierarchy_storeAndLaterRetrieve() throws Exception {

		// Create the cabinet
		OfficeCabinet<HierarchicalDocument> cabinetTwo = this.createCabinet(HierarchicalDocument.class);

		// Store document
		HierarchicalDocument document = this.setupDocument(HierarchicalDocument.class, 0);

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
	public void domain_hierarchy_storeAndLaterRetrieve() throws Exception {

		// Create the cabinet
		HierarchicalDocumentCabinet cabinetTwo = this.createDomainSpecificCabinet(HierarchicalDocumentCabinet.class);

		// Store document
		HierarchicalDocument document = this.setupDocument(HierarchicalDocument.class, 0);

		// Obtain document later (via another cabinet)
		HierarchicalDocument retrieved = cabinetTwo.findByKey(document.getKey()).get();
		assertNotSame(document, retrieved, "Should retrieve different instance");

		// Ensure same data
		assertEquals(document.getKey(), retrieved.getKey(), "Should have same key");
		document.assertDocumentEquals(retrieved);
	}

	@Test
	public void hierarchy_detectDirty() throws Exception {

		// Create the cabinet
		OfficeCabinet<HierarchicalDocument> cabinet = this.createCabinet(HierarchicalDocument.class);

		// Setup document
		String key = this.setupDocument(HierarchicalDocument.class, 0).getKey();

		// Obtain the document
		HierarchicalDocument document = cabinet.retrieveByKey(key).get();

		// Change the child value
		final String CHANGE = "CHANGED";
		assertNotEquals(CHANGE, document.getChild().getStringObject(), "INVALID TEST: not changing value");
		document.getChild().setStringObject(CHANGE);

		// Close (causing save on being dirty)
		OfficeCabinetAdmin admin = this.getOfficeCabinetAdmin(cabinet);
		admin.close();

		// Ensure dirty change saved
		HierarchicalDocument updated = this.createCabinet(HierarchicalDocument.class).retrieveByKey(key).get();
		assertEquals(CHANGE, updated.getChild().getStringObject(), "Should update in store as dirty");
	}

	@Test
	public void domain_hierarchy_detectDirty() throws Exception {

		// Create the cabinet
		HierarchicalDocumentCabinet cabinet = this.createDomainSpecificCabinet(HierarchicalDocumentCabinet.class);

		// Setup document
		String key = this.setupDocument(HierarchicalDocument.class, 0).getKey();

		// Obtain the document
		HierarchicalDocument document = cabinet.findByKey(key).get();

		// Change the child value
		final String CHANGE = "CHANGED";
		assertNotEquals(CHANGE, document.getChild().getStringObject(), "INVALID TEST: not changing value");
		document.getChild().setStringObject(CHANGE);

		// Close (causing save on being dirty)
		OfficeCabinetAdmin admin = this.getOfficeCabinetAdmin(cabinet);
		admin.close();

		// Ensure dirty change saved
		HierarchicalDocument updated = this.createDomainSpecificCabinet(HierarchicalDocumentCabinet.class)
				.findByKey(key).get();
		assertEquals(CHANGE, updated.getChild().getStringObject(), "Should update in store as dirty");
	}

}