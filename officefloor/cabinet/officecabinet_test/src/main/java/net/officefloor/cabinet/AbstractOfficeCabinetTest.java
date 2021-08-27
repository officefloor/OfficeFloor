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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import net.officefloor.cabinet.admin.OfficeCabinetAdmin;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;

/**
 * Tests Office Cabinet.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeCabinetTest {

	/**
	 * Obtains the {@link OfficeCabinetArchive} for the {@link Document} type.
	 * 
	 * @return {@link OfficeCabinetArchive} for the {@link Document} type.
	 * @throws Exception If fails to create {@link OfficeCabinet}.
	 */
	protected abstract <D> OfficeCabinetArchive<D> getOfficeCabinetArchive(Class<D> documentType) throws Exception;

	/**
	 * Obtains the {@link OfficeCabinetAdmin} for the {@link OfficeCabinet}.
	 * 
	 * @param cabinet {@link OfficeCabinet}.
	 * @return OfficeCabinetAdmin} for the {@link OfficeCabinet}.
	 * @throws Exception If fails to obtain the {@link OfficeCabinetAdmin}.
	 */
	protected OfficeCabinetAdmin getOfficeCabinetAdmin(OfficeCabinet<?> cabinet) throws Exception {
		return (OfficeCabinetAdmin) cabinet;
	}

	/**
	 * {@link OfficeCabinetArchive} by their {@link Document} type.
	 */
	private Map<Class<?>, OfficeCabinetArchive<?>> cachedArchives = new HashMap<>();

	@SuppressWarnings("unchecked")
	private <D> OfficeCabinetArchive<D> getArchive(Class<D> documentType) {
		OfficeCabinetArchive<D> archive = (OfficeCabinetArchive<D>) this.cachedArchives.get(documentType);
		if (archive == null) {
			try {
				archive = this.getOfficeCabinetArchive(documentType);
			} catch (Exception ex) {
				return fail("Failed to create " + OfficeCabinetArchive.class.getSimpleName() + " for document "
						+ documentType.getName(), ex);
			}
			this.cachedArchives.put(documentType, archive);
		}
		return archive;
	}

	private <D> OfficeCabinet<D> createCabinet(Class<D> documentType) {
		return this.getArchive(documentType).createOfficeCabinet();
	}

	/**
	 * Sets up the {@link AttributeTypesDocument} in {@link OfficeCabinet}.
	 * 
	 * @param offset Offset for state.
	 * @return Set up {@link AttributeTypesDocument}.
	 */
	private <D> D setupDocument(Class<D> documentType, int offset) {
		OfficeCabinet<D> cabinet = this.createCabinet(documentType);
		D document;
		try {
			document = documentType.getConstructor(int.class).newInstance(offset);
		} catch (Exception ex) {
			return fail("Failed new " + documentType.getSimpleName() + "(" + offset + ")", ex);
		}
		cabinet.store(document);
		return document;
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
		AttributeTypesDocument document = new AttributeTypesDocument(0);
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
	 * Ensure can store and later retrieve values.
	 */
	@Test
	public void attributeTypes_storeAndLaterRetrieve() throws Exception {

		// Store document
		AttributeTypesDocument document = this.setupDocument(AttributeTypesDocument.class, 0);

		// Obtain document later (via another cabinet)
		OfficeCabinet<AttributeTypesDocument> cabinetTwo = this.createCabinet(AttributeTypesDocument.class);
		AttributeTypesDocument retrieved = cabinetTwo.retrieveByKey(document.getKey()).get();
		assertNotSame(document, retrieved, "Should retrieve different instance");

		// Ensure same data
		assertEquals(document.getKey(), retrieved.getKey(), "Should have same key");
		document.assertDocumentEquals(retrieved);
	}

	@Test
	public void attributeTypes_detectDirty() throws Exception {

		// Setup document
		String key = this.setupDocument(AttributeTypesDocument.class, 0).getKey();

		// Obtain the document
		OfficeCabinet<AttributeTypesDocument> cabinet = this.createCabinet(AttributeTypesDocument.class);
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

	/**
	 * Ensure can store and retrieve values.
	 */
	@Test
	public void hierarchy_storeAndRetrieve() {
		OfficeCabinet<HierarchicalDocument> cabinet = this.createCabinet(HierarchicalDocument.class);

		// Store document
		HierarchicalDocument document = new HierarchicalDocument(0);
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
	 * Ensure can store and later retrieve values.
	 */
	@Test
	public void hierarchy_storeAndLaterRetrieve() throws Exception {

		// Store document
		HierarchicalDocument document = this.setupDocument(HierarchicalDocument.class, 0);

		// Obtain document later (via another cabinet)
		OfficeCabinet<HierarchicalDocument> cabinetTwo = this.createCabinet(HierarchicalDocument.class);
		HierarchicalDocument retrieved = cabinetTwo.retrieveByKey(document.getKey()).get();
		assertNotSame(document, retrieved, "Should retrieve different instance");

		// Ensure same data
		assertEquals(document.getKey(), retrieved.getKey(), "Should have same key");
		document.assertDocumentEquals(retrieved);
	}

	@Test
	public void hierarchy_detectDirty() throws Exception {

		// Setup document
		String key = this.setupDocument(HierarchicalDocument.class, 0).getKey();

		// Obtain the document
		OfficeCabinet<HierarchicalDocument> cabinet = this.createCabinet(HierarchicalDocument.class);
		HierarchicalDocument document = cabinet.retrieveByKey(key).get();

		// Change the child value
		final String CHANGE = "CHANGED";
		assertNotEquals(CHANGE, document.getChild().getName(), "INVALID TEST: not changing value");
		document.getChild().setName(CHANGE);

		// Close (causing save on being dirty)
		OfficeCabinetAdmin admin = this.getOfficeCabinetAdmin(cabinet);
		admin.close();

		// Ensure dirty change saved
		HierarchicalDocument updated = this.createCabinet(HierarchicalDocument.class).retrieveByKey(key).get();
		assertEquals(CHANGE, updated.getChild().getName(), "Should update in store as dirty");
	}

}