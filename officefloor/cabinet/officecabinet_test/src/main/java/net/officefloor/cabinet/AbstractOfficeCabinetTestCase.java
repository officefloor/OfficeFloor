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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInfo;

import net.officefloor.cabinet.domain.DomainCabinetDocumentMetaData;
import net.officefloor.cabinet.domain.DomainCabinetFactory;
import net.officefloor.cabinet.domain.DomainCabinetManufacturer;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.Index.IndexField;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * Tests Office Cabinet.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeCabinetTestCase {

	/**
	 * {@link AbstractOfficeCabinetAttributesTest} tests.
	 */
	@Nested
	public class Attributes extends AbstractOfficeCabinetAttributesTest {

		@Override
		protected AbstractOfficeCabinetTestCase testcase() {
			return AbstractOfficeCabinetTestCase.this;
		}
	}

	/**
	 * {@link AbstractOfficeCabinetHierarchyTest} tests.
	 */
	@Nested
	public class Hierarchy extends AbstractOfficeCabinetHierarchyTest {

		@Override
		protected AbstractOfficeCabinetTestCase testcase() {
			return AbstractOfficeCabinetTestCase.this;
		}
	}

	/**
	 * {@link AbstractOfficeCabinetReferencedTest} tests.
	 */
	@Nested
	public class Referenced extends AbstractOfficeCabinetReferencedTest {

		@Override
		protected AbstractOfficeCabinetTestCase testcase() {
			return AbstractOfficeCabinetTestCase.this;
		}
	}

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

	protected String testName;

	protected OfficeStore officeStore;

	private static final int DEFAULT_DOCUMENT_INDEX = 0;

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
	protected <C> C createDomainSpecificCabinet(Class<C> cabinetType, CabinetManager cabinetManager) throws Exception {
		DomainCabinetManufacturer manufacturer = this.getDomainSpecificCabinetManufacturer();
		DomainCabinetFactory<C> factory = manufacturer.createDomainCabinetFactory(cabinetType);

		// Build the domain specific cabinet
		C domainCabinet = factory.createDomainSpecificCabinet(cabinetManager);
		return domainCabinet;
	}

	/**
	 * Creates the domain specific {@link OfficeCabinet} with new
	 * {@link CabinetManager}.
	 * 
	 * @param <C>         Interface providing domain specific {@link Method}
	 *                    instances.
	 * @param cabinetType Interface providing domain specific {@link Method}
	 *                    instances.
	 * @return Domain specific {@link OfficeCabinet}.
	 * @throws Exception If fails to create domain specific {@link OfficeCabinet}.
	 */
	protected <C> C createDomainSpecificCabinet(Class<C> cabinetType) throws Exception {
		return this.createDomainSpecificCabinet(cabinetType, this.officeStore.createCabinetManager());
	}

	/**
	 * Creates the {@link Document} with default offset.
	 * 
	 * @param documentType {@link Document} type.
	 * @return New {@link Document}.
	 */
	protected <D> D newDocument(Class<D> documentType) {
		return this.newDocument(documentType, DEFAULT_DOCUMENT_INDEX);
	}

	/**
	 * Creates the {@link Document}.
	 * 
	 * @param documentType {@link Document} type.
	 * @param offset       Offset for state.
	 * @return New {@link Document}.
	 */
	protected <D> D newDocument(Class<D> documentType, int offset) {
		try {
			return documentType.getConstructor(int.class, String.class).newInstance(offset, this.testName);
		} catch (Exception ex) {
			return fail("Failed new " + documentType.getSimpleName() + "(" + offset + ")", ex);
		}
	}

	/**
	 * Interface to setup a {@link Document}.
	 */
	@FunctionalInterface
	protected interface DocumentSetup<D> {

		/**
		 * Sets up a {@link Document}.
		 * 
		 * @param document {@link Document}.
		 * @param index    Index of the {@link Document}.
		 */
		void setupDocument(D document, int index);
	}

	/**
	 * Sets up the {@link Document} in {@link OfficeCabinet} with default index.
	 * 
	 * @param documentType {@link Document} type.
	 * @return Set up {@link AttributeTypesDocument}.
	 */
	protected <D> D setupDocument(Class<D> documentType) {
		return this.setupDocument(documentType, null);
	}

	/**
	 * Sets up single {@link Document} instance with default index.
	 * 
	 * @param <D>          Type of {@link Document}.
	 * @param documentType Type of {@link Document}.
	 * @param setup        Optional {@link DocumentSetup}.
	 * @return Setup {@link Document} instance.
	 */
	protected <D> D setupDocument(Class<D> documentType, DocumentSetup<D> setup) {
		return this.setupDocument(documentType, DEFAULT_DOCUMENT_INDEX, setup);
	}

	/**
	 * Sets up the {@link Document} in {@link OfficeCabinet}.
	 * 
	 * @param documentType {@link Document} type.
	 * @param offset       Offset for state.
	 * @param setup        {@link DocumentSetup}.
	 * @return Set up {@link AttributeTypesDocument}.
	 */
	protected <D> D setupDocument(Class<D> documentType, int offset, DocumentSetup<D> setup) {

		// Create the document
		D document = this.newDocument(documentType, offset);
		if (setup != null) {
			setup.setupDocument(document, offset);
		}

		// Store the document
		CabinetManager cabinetManager = this.officeStore.createCabinetManager();
		OfficeCabinet<D> cabinet = cabinetManager.getOfficeCabinet(documentType);
		cabinet.store(document);
		try {
			cabinetManager.flush();
		} catch (Exception ex) {
			return fail("Failed to setup " + documentType.getSimpleName() + "(" + offset + ")", ex);
		}
		return document;
	}

	/**
	 * Sets up multiple {@link Document} instances.
	 * 
	 * @param <D>               Type of {@link Document}.
	 * @param numberOfDocuments Number of {@link Document} instances to setup.
	 * @param documentType      Type of {@link Document}.
	 * @param setup             Optional {@link DocumentSetup}.
	 * @return {@link List} of setup {@link Document} instances.
	 */
	protected <D> List<D> setupDocuments(int numberOfDocuments, Class<D> documentType, DocumentSetup<D> setup) {
		CabinetManager cabinetManager = this.officeStore.createCabinetManager();
		OfficeCabinet<D> cabinet = cabinetManager.getOfficeCabinet(documentType);
		List<D> documents = new ArrayList<>(numberOfDocuments);
		for (int i = 0; i < numberOfDocuments; i++) {
			D doc = this.newDocument(documentType, i);
			if (setup != null) {
				setup.setupDocument(doc, i);
			}
			cabinet.store(doc);
			documents.add(doc);
		}
		try {
			cabinetManager.flush();
		} catch (Exception ex) {
			return fail("Failed to flush setup of " + documentType.getSimpleName(), ex);
		}
		return documents;
	}

	/**
	 * Meta-data on querying for {@link Document} instances.
	 *
	 * @param <D> {@link Document} type.
	 * @param <C> {@link OfficeCabinet} type.
	 * @param <R> {@link RetrieveBundle} type.
	 */
	public static class RetrieveBundle<D, C extends OfficeCabinet<D>, R extends RetrieveBundle<? extends D, C, R>> {
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

	/**
	 * Retrieves the {@link Document} instances.
	 * 
	 * @param <D>            {@link Document} type.
	 * @param <C>            {@link OfficeCabinet} type.
	 * @param cabinet        {@link OfficeCabinet}.
	 * @param retrieveBundle {@link RetrieveBundle}.
	 */
	public <D, C extends OfficeCabinet<D>> void retrieveBundles(C cabinet,
			RetrieveBundle<D, C, ? extends RetrieveBundle<? extends D, C, ?>> retrieveBundle) {
		int documentIndex = 0;
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