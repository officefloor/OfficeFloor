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

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import net.officefloor.cabinet.admin.OfficeCabinetAdmin;
import net.officefloor.cabinet.domain.DomainCabinetDocumentMetaData;
import net.officefloor.cabinet.domain.DomainCabinetFactory;
import net.officefloor.cabinet.domain.DomainCabinetManufacturer;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.Index.IndexField;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeStore;
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
	 * Obtains the {@link OfficeStore}.
	 * 
	 * @return {@link OfficeStore}.
	 */
	protected abstract OfficeStore getOfficeStore();

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

	public static @interface MIndex {
		String sort() default "";

		String[] value();
	}

	public static @interface MCabinet {
		Class<?> value();

		MIndex[] indexes() default {};
	}

	@Retention(RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface MStore {
		Class<?> cabinetDomainType() default Object.class;

		MCabinet[] cabinets() default {};
	}

	private String testName;

	private OfficeStore officeStore;

	private CabinetManager cabinetManager;

	@BeforeEach
	public void setupOfficeStore(TestInfo info) throws Exception {

		// Capture test name
		this.testName = info.getDisplayName();

		// Obtain the test method
		Method testMethod = info.getTestMethod().get();
		MStore storeInfo = testMethod.getAnnotation(MStore.class);
		assertNotNull(storeInfo, "Must have test method annotated with " + MStore.class.getSimpleName());

		// Create the store
		OfficeStore store = this.getOfficeStore();

		// Determine if use domain specific
		Class<?> cabinetDomainType = storeInfo.cabinetDomainType();
		MCabinet[] cabinetInfos = storeInfo.cabinets();
		if ((cabinetDomainType != null) && (!Object.class.equals(cabinetDomainType))) {

			// Create store from manufacturer
			DomainCabinetManufacturer manufacturer = this.getDomainSpecificCabinetManufacturer();
			DomainCabinetFactory<?> factory = manufacturer.createDomainCabinetFactory(cabinetDomainType);

			// Use domain specific meta-data
			for (DomainCabinetDocumentMetaData cabinetMetaData : factory.getMetaData()) {
				Class<?> documentType = cabinetMetaData.getDocumentType();
				Index[] indexes = cabinetMetaData.getIndexes();
				store.setupOfficeCabinet(documentType, indexes);
			}

		} else if (cabinetInfos.length > 0) {

			// Use meta-data configuration
			for (MCabinet cabinetInfo : storeInfo.cabinets()) {

				// Create the indexes
				MIndex[] indexInfos = cabinetInfo.indexes();
				Index[] indexes = Arrays.stream(indexInfos).map((indexInfo) -> {
					String sortField = indexInfo.sort();
					String[] fieldNames = indexInfo.value();
					IndexField[] indexFields = Arrays.stream(fieldNames).map((fieldName) -> new IndexField(fieldName))
							.toArray(IndexField[]::new);
					return new Index((sortField == null) || (sortField.trim().length() == 0) ? null : sortField,
							indexFields);
				}).toArray(Index[]::new);

				// Set up the cabinet
				Class<?> documentType = cabinetInfo.value();
				store.setupOfficeCabinet(documentType, indexes);
			}

		} else {
			fail("Must provide either domain specific cabinet or cabinet meta-data for test");
		}

		// Capture store for use in tests
		this.officeStore = store;

		// Create manager
		this.cabinetManager = store.createCabinetManager();
	}

	/**
	 * Creates the domain specific {@link OfficeCabinet}.
	 * 
	 * @param <C>            Interface providing domain specific {@link Method}
	 *                       instances.
	 * @param cabinetType    Interface providing domain specific {@link Method}
	 *                       instances.
	 * @param cabinetManager {@link CabinetManager}.
	 * @return Domain specific {@link OfficeCabinet}.
	 * @throws Exception If fails to create domain specific {@link OfficeCabinet}.
	 */
	private <C> C createDomainSpecificCabinet(Class<C> cabinetType, CabinetManager cabinetManager) throws Exception {
		DomainCabinetManufacturer manufacturer = this.getDomainSpecificCabinetManufacturer();
		DomainCabinetFactory<C> factory = manufacturer.createDomainCabinetFactory(cabinetType);

		// Build the domain specific cabinet
		C domainCabinet = factory.createDomainSpecificCabinet(cabinetManager);
		return domainCabinet;
	}

	/**
	 * Creates the domain specific {@link OfficeCabinet} with default
	 * {@link CabinetManager}.
	 * 
	 * @param <C>         Interface providing domain specific {@link Method}
	 *                    instances.
	 * @param cabinetType Interface providing domain specific {@link Method}
	 *                    instances.
	 * @return Domain specific {@link OfficeCabinet}.
	 * @throws Exception If fails to create domain specific {@link OfficeCabinet}.
	 */
	private <C> C createDomainSpecificCabinet(Class<C> cabinetType) throws Exception {
		return this.createDomainSpecificCabinet(cabinetType, this.cabinetManager);
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
	private <D> D setupDocument(Class<D> documentType, int offset) {
		CabinetManager cabinetManager = this.officeStore.createCabinetManager();
		OfficeCabinet<D> cabinet = cabinetManager.getOfficeCabinet(documentType);
		D document = this.newDocument(documentType, offset);
		cabinet.store(document);
		return document;
	}

	/*
	 * ========================= Tests ===============================
	 */

	/**
	 * Ensure can store and retrieve values.
	 */
	@Test
	@MStore(cabinets = @MCabinet(AttributeTypesDocument.class))
	public void attributeTypes_storeAndRetrieve() {
		OfficeCabinet<AttributeTypesDocument> cabinet = this.cabinetManager
				.getOfficeCabinet(AttributeTypesDocument.class);

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
	@MStore(cabinetDomainType = AttributeTypesDocumentCabinet.class)
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
	@MStore(cabinets = @MCabinet(AttributeTypesDocument.class))
	public void attributeTypes_storeAndLaterRetrieve() throws Exception {

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinetTwo = this.cabinetManager
				.getOfficeCabinet(AttributeTypesDocument.class);

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
	@MStore(cabinetDomainType = AttributeTypesDocumentCabinet.class)
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
	@MStore(cabinets = @MCabinet(AttributeTypesDocument.class))
	public void attributeTypes_detectDirty() throws Exception {

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinet = this.cabinetManager
				.getOfficeCabinet(AttributeTypesDocument.class);

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
		AttributeTypesDocument updated = this.officeStore.createCabinetManager()
				.getOfficeCabinet(AttributeTypesDocument.class).retrieveByKey(key).get();
		assertEquals(CHANGE, updated.getIntPrimitive(), "Should update in store as dirty");
	}

	@Test
	@MStore(cabinetDomainType = AttributeTypesDocumentCabinet.class)
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

		// Ensure dirty change saved
		AttributeTypesDocument updated = this.createDomainSpecificCabinet(AttributeTypesDocumentCabinet.class)
				.findByKey(key).get();
		assertEquals(CHANGE, updated.getIntPrimitive(), "Should update in store as dirty");
	}

	@Test
	@MStore(cabinets = @MCabinet(value = AttributeTypesDocument.class, indexes = @MIndex("testName")))
	public void attributeTypes_query() throws Exception {

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinet = this.cabinetManager
				.getOfficeCabinet(AttributeTypesDocument.class);

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
	@MStore(cabinetDomainType = AttributeTypesDocumentCabinet.class)
	public void domain_attributeTypes_query() throws Exception {

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
	@MStore(cabinets = @MCabinet(value = AttributeTypesDocument.class, indexes = @MIndex("testName")))
	public void attributeTypes_session() throws Exception {

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinet = this.cabinetManager
				.getOfficeCabinet(AttributeTypesDocument.class);

		// Setup the document
		AttributeTypesDocument setup = this.setupDocument(AttributeTypesDocument.class, 0);

		// Obtain by key
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
	public void domain_attributeTypes_session() throws Exception {

		// Create the cabinet
		AttributeTypesDocumentCabinet cabinet = this.createDomainSpecificCabinet(AttributeTypesDocumentCabinet.class);

		// Setup the document
		AttributeTypesDocument setup = this.setupDocument(AttributeTypesDocument.class, 0);

		// Obtain by key
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
	public void attributeTypes_sortedAscending() throws Exception {

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinet = this.cabinetManager
				.getOfficeCabinet(AttributeTypesDocument.class);

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
	@MStore(cabinets = @MCabinet(value = AttributeTypesDocument.class, indexes = @MIndex(value = "testName", sort = "intPrimitive")))
	public void attributeTypes_sortedDescending() throws Exception {

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinet = this.cabinetManager
				.getOfficeCabinet(AttributeTypesDocument.class);

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

	/**
	 * Ensure able to retrieve {@link DocumentBundle}.
	 */
	@Test
	@MStore(cabinets = @MCabinet(value = AttributeTypesDocument.class, indexes = @MIndex(value = "testName", sort = "intPrimitive")))
	public void attributeTypes_retrieveFirstBundle() throws Exception {

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinet = this.cabinetManager
				.getOfficeCabinet(AttributeTypesDocument.class);

		// Set up documents
		final int size = 10;
		for (int i = 0; i < size; i++) {
			AttributeTypesDocument doc = this.newDocument(AttributeTypesDocument.class, i);
			doc.setIntPrimitive(i + 1); // one based index
			cabinet.store(doc);
		}

		// Retrieve the document bundles
		DocumentBundle<AttributeTypesDocument> bundle = cabinet.retrieveByQuery(
				new Query(new QueryField("testName", this.testName)),
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
	public void attributeTypes_retrieveNextBundles(int bundleSize, int bundleCount, int repeated) throws Exception {
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
	public void attributeTypes_retrieveNextBundlesByNextDocumentToken(int bundleSize, int bundleCount, int repeated)
			throws Exception {
		this.retrieveBundles(new RetrieveAttributeTypesDocuments().getNextBundle((bundle, cabinet) -> {
			String token = bundle.getNextDocumentBundleToken();
			return token == null ? null
					: cabinet.retrieveByQuery(
							new Query(new QueryField("testName", AbstractOfficeCabinetTest.this.testName)),
							new Range("intPrimitive", Direction.Ascending, bundleSize, token));
		}).bundleSize(bundleSize).bundleCount(bundleCount).repeatCount(repeated));
	}

	/**
	 * Ensure can store and retrieve values.
	 */
	@Test
	@MStore(cabinets = @MCabinet(HierarchicalDocument.class))
	public void hierarchy_storeAndRetrieve() {
		OfficeCabinet<HierarchicalDocument> cabinet = this.cabinetManager.getOfficeCabinet(HierarchicalDocument.class);

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
	@MStore(cabinetDomainType = HierarchicalDocumentCabinet.class)
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
	@MStore(cabinets = @MCabinet(HierarchicalDocument.class))
	public void hierarchy_storeAndLaterRetrieve() throws Exception {

		// Create the cabinet
		OfficeCabinet<HierarchicalDocument> cabinetTwo = this.cabinetManager
				.getOfficeCabinet(HierarchicalDocument.class);

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
	@MStore(cabinetDomainType = HierarchicalDocumentCabinet.class)
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
	@MStore(cabinets = @MCabinet(HierarchicalDocument.class))
	public void hierarchy_detectDirty() throws Exception {

		// Create the cabinet
		OfficeCabinet<HierarchicalDocument> cabinet = this.cabinetManager.getOfficeCabinet(HierarchicalDocument.class);

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
		HierarchicalDocument updated = this.officeStore.createCabinetManager()
				.getOfficeCabinet(HierarchicalDocument.class).retrieveByKey(key).get();
		assertEquals(CHANGE, updated.getChild().getStringObject(), "Should update in store as dirty");
	}

	@Test
	@MStore(cabinetDomainType = HierarchicalDocumentCabinet.class)
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

		// Ensure dirty change saved
		HierarchicalDocument updated = this.createDomainSpecificCabinet(HierarchicalDocumentCabinet.class)
				.findByKey(key).get();
		assertEquals(CHANGE, updated.getChild().getStringObject(), "Should update in store as dirty");
	}

	@Test
	@MStore(cabinets = @MCabinet(value = HierarchicalDocument.class, indexes = @MIndex("testName")))
	public void hierarchy_query() throws Exception {

		// Create the cabinet
		OfficeCabinet<HierarchicalDocument> cabinet = this.cabinetManager.getOfficeCabinet(HierarchicalDocument.class);

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
	@MStore(cabinetDomainType = HierarchicalDocumentCabinet.class)
	public void domain_hierarchy_query() throws Exception {

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

	@Test
	@MStore(cabinets = @MCabinet(value = HierarchicalDocument.class, indexes = @MIndex("testName")))
	public void hierarchy_session() throws Exception {

		// Create the cabinet
		OfficeCabinet<HierarchicalDocument> cabinet = this.cabinetManager.getOfficeCabinet(HierarchicalDocument.class);

		// Setup the document
		HierarchicalDocument setup = this.setupDocument(HierarchicalDocument.class, 0);

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
	public void domain_hierarchy_session() throws Exception {

		// Create the cabinet
		HierarchicalDocumentCabinet cabinet = this.createDomainSpecificCabinet(HierarchicalDocumentCabinet.class);

		// Setup the document
		HierarchicalDocument setup = this.setupDocument(HierarchicalDocument.class, 0);

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
	public void hierarchy_retrieveNextBundles(int bundleSize, int bundleCount, int repeated) throws Exception {
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
	public void hierarchy_retrieveNextBundlesByNextDocumentToken(int bundleSize, int bundleCount, int repeated)
			throws Exception {
		this.retrieveBundles(new RetrieveHierarchicalDocuments().getNextBundle((bundle, cabinet) -> {
			String token = bundle.getNextDocumentBundleToken();
			return token == null ? null
					: cabinet.retrieveByQuery(
							new Query(new QueryField("testName", AbstractOfficeCabinetTest.this.testName)),
							new Range("offset", Direction.Ascending, bundleSize, token));
		}).bundleSize(bundleSize).bundleCount(bundleCount).repeatCount(repeated));
	}

	/**
	 * Ensure can store and retrieve values.
	 */
	@Test
	@MStore(cabinets = @MCabinet(ReferencingDocument.class))
	public void referencing_notReferenced_storeAndRetrieve() {
		OfficeCabinet<ReferencingDocument> referencingCabinet = this.cabinetManager
				.getOfficeCabinet(ReferencingDocument.class);

		// Create document
		ReferencingDocument referencing = this.newDocument(ReferencingDocument.class, 0);

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
	public void referencing_storeAndRetrieve() {
		OfficeCabinet<ReferencingDocument> referencingCabinet = this.cabinetManager
				.getOfficeCabinet(ReferencingDocument.class);
		OfficeCabinet<ReferencedDocument> referencedCabinet = this.cabinetManager
				.getOfficeCabinet(ReferencedDocument.class);

		// Create document
		ReferencingDocument referencing = this.newDocument(ReferencingDocument.class, 0);
		ReferencedDocument referenced = this.newDocument(ReferencedDocument.class, 0);
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

	/*
	 * ========================= Helper Methods ===================================
	 */

	protected class RetrieveAttributeTypesDocuments extends
			RetrieveBundle<AttributeTypesDocument, OfficeCabinet<AttributeTypesDocument>, RetrieveAttributeTypesDocuments> {
		public RetrieveAttributeTypesDocuments() {
			this.getFirstBundle((cabinet) -> cabinet.retrieveByQuery(
					new Query(new QueryField("testName", AbstractOfficeCabinetTest.this.testName)),
					new Range("intPrimitive", Direction.Ascending, this.bundleSize)));
			this.getDocumentIndex((document) -> document.getIntPrimitive());
			this.getNextBundle((bundle, cabinet) -> bundle.nextDocumentBundle());
		}
	}

	private <D> void retrieveBundles(RetrieveAttributeTypesDocuments retrieveBundle) {

		// Create the cabinet
		OfficeCabinet<AttributeTypesDocument> cabinet = this.cabinetManager
				.getOfficeCabinet(AttributeTypesDocument.class);

		// Ensure no data
		DocumentBundle<AttributeTypesDocument> bundle = retrieveBundle.getFirstBundle.apply(cabinet);
		assertNull(bundle, "Should be no data setup yet");

		// Set up documents
		final int size = retrieveBundle.bundleSize * retrieveBundle.bundleCount;
		for (int i = 0; i < size; i++) {
			AttributeTypesDocument doc = this.newDocument(AttributeTypesDocument.class, i);
			doc.setIntPrimitive(i + 1); // one based index
			cabinet.store(doc);
		}

		// Retrieve the bundles
		this.retrieveBundles(cabinet, retrieveBundle);
	}

	protected class RetrieveHierarchicalDocuments extends
			RetrieveBundle<HierarchicalDocument, OfficeCabinet<HierarchicalDocument>, RetrieveHierarchicalDocuments> {
		public RetrieveHierarchicalDocuments() {
			this.getFirstBundle((cabinet) -> cabinet.retrieveByQuery(
					new Query(new QueryField("testName", AbstractOfficeCabinetTest.this.testName)),
					new Range("offset", Direction.Ascending, this.bundleSize)));
			this.getDocumentIndex((document) -> document.getOffset());
			this.getNextBundle((bundle, cabinet) -> bundle.nextDocumentBundle());
		}
	}

	private <D> void retrieveBundles(RetrieveHierarchicalDocuments retrieveBundle) {

		// Create the cabinet
		OfficeCabinet<HierarchicalDocument> cabinet = this.cabinetManager.getOfficeCabinet(HierarchicalDocument.class);

		// Ensure no data
		DocumentBundle<HierarchicalDocument> bundle = retrieveBundle.getFirstBundle.apply(cabinet);
		assertNull(bundle, "Should be no data setup yet");

		// Set up documents
		final int size = retrieveBundle.bundleSize * retrieveBundle.bundleCount;
		for (int i = 0; i < size; i++) {
			HierarchicalDocument doc = this.newDocument(HierarchicalDocument.class, i + 1); // +1 offset
			cabinet.store(doc);
		}

		// Retrieve the bundles
		this.retrieveBundles(cabinet, retrieveBundle);
	}

	protected static class RetrieveBundle<D, C extends OfficeCabinet<D>, R extends RetrieveBundle<? extends D, C, R>> {
		protected int bundleSize = 1;
		protected int bundleCount = 1;
		protected Function<C, DocumentBundle<D>> getFirstBundle;
		protected int repeatCount = 0;
		protected Function<D, Integer> getDocumentIndex;
		protected BiFunction<DocumentBundle<D>, C, DocumentBundle<D>> getNextBundle;

		@SuppressWarnings("unchecked")
		public R bundleSize(int expectedBundleSize) {
			this.bundleSize = expectedBundleSize;
			return (R) this;
		}

		@SuppressWarnings("unchecked")
		public R bundleCount(int expectedBundleCount) {
			this.bundleCount = expectedBundleCount;
			return (R) this;
		}

		@SuppressWarnings("unchecked")
		public R getFirstBundle(Function<C, DocumentBundle<D>> getFirstBundle) {
			this.getFirstBundle = getFirstBundle;
			return (R) this;
		}

		@SuppressWarnings("unchecked")
		public R repeatCount(int repeatCount) {
			this.repeatCount = repeatCount;
			return (R) this;
		}

		@SuppressWarnings("unchecked")
		public R getDocumentIndex(Function<D, Integer> getDocumentIndex) {
			this.getDocumentIndex = getDocumentIndex;
			return (R) this;
		}

		@SuppressWarnings("unchecked")
		public R getNextBundle(BiFunction<DocumentBundle<D>, C, DocumentBundle<D>> getNextBundle) {
			this.getNextBundle = getNextBundle;
			return (R) this;
		}
	}

	private <D, C extends OfficeCabinet<D>> void retrieveBundles(C cabinet,
			RetrieveBundle<D, C, ? extends RetrieveBundle<? extends D, C, ?>> retrieveBundle) {
		int documentIndex = 1;
		int bundleIndex = 0;
		DocumentBundle<D> bundle = retrieveBundle.getFirstBundle.apply(cabinet);
		do {
			int startingDocumentIndex = documentIndex;

			// Ensure no extra bundles
			assertTrue(bundleIndex < retrieveBundle.bundleCount,
					"Too many bundles - expected: " + retrieveBundle.bundleCount + " but was: " + (bundleIndex + 1));

			// Retrieve all the documents
			List<D> bundleDocuments = new ArrayList<>(retrieveBundle.bundleSize);
			while (bundle.hasNext()) {
				D document = bundle.next();
				bundleDocuments.add(document);
			}
			String bundleIndexesText = String.join(",",
					bundleDocuments.stream()
							.map((document) -> String.valueOf(retrieveBundle.getDocumentIndex.apply(document)))
							.toArray(String[]::new));
			assertEquals(retrieveBundle.bundleSize, bundleDocuments.size(),
					"Incorrect number of documents for bundle " + bundleIndex + " (" + bundleIndexesText + ")");

			// Ensure correct documents in bundle
			int bundleDocumentCount = 0;
			Iterator<D> documentIterator = bundleDocuments.iterator();
			while (documentIterator.hasNext()) {
				D document = documentIterator.next();
				bundleDocumentCount++;
				assertEquals(Integer.valueOf(documentIndex++), retrieveBundle.getDocumentIndex.apply(document),
						"Incorrect document in bundle " + bundleIndex);
			}

			// Ensure able to repeat obtaining the documents from bundle
			for (int repeat = 0; repeat < retrieveBundle.repeatCount; repeat++) {
				Iterator<D> iterator = bundle.iterator();
				int repeatDocumentIndex = startingDocumentIndex;
				bundleDocumentCount = 0;
				while (iterator.hasNext()) {
					D document = iterator.next();
					bundleDocumentCount++;
					assertEquals(Integer.valueOf(repeatDocumentIndex++),
							retrieveBundle.getDocumentIndex.apply(document),
							"Incorrect document in bundle " + bundleIndex + " for repeat " + repeat);
				}
				assertEquals(retrieveBundle.bundleSize, bundleDocumentCount,
						"Incorrect number of documents for bundle " + bundleIndex + " for repeat " + repeat);
			}

			bundleIndex++;
		} while ((bundle = retrieveBundle.getNextBundle.apply(bundle, cabinet)) != null);
		assertEquals(retrieveBundle.bundleCount, bundleIndex, "Incorrect number of bundles");
	}

}