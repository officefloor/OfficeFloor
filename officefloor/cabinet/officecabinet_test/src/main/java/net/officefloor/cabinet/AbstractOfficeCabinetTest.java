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
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
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
	 * Obtains the {@link OfficeCabinetArchive} for the
	 * {@link AttributeTypesEntity}.
	 * 
	 * @return {@link OfficeCabinetArchive} for the {@link AttributeTypesDocument}.
	 * @throws Exception If fails to create {@link OfficeCabinet}.
	 */
	protected abstract OfficeCabinetArchive<AttributeTypesDocument> getAttributeTypesOfficeCabinetArchive()
			throws Exception;

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
	 * {@link OfficeCabinetArchive}.
	 */
	private OfficeCabinetArchive<AttributeTypesDocument> archive;

	/**
	 * Setup.
	 * 
	 * @throws Exception If fails to setup.
	 */
	@BeforeEach
	public void setup() throws Exception {
		this.archive = this.getAttributeTypesOfficeCabinetArchive();
	}

	/**
	 * Ensure can store and retrieve values.
	 */
	@Test
	public void storeAndRetrieve() throws Exception {
		OfficeCabinet<AttributeTypesDocument> cabinet = this.archive.createOfficeCabinet();
		AttributeTypesDocument document = new AttributeTypesDocument(0);
		cabinet.store(document);
		String key = document.getKey();
		assertNotNull(key, "Should assign key to document");
		AttributeTypesDocument retrieved = cabinet.retrieveByKey(document.getKey()).get();
		assertSame(document, retrieved, "Should retrieve same instance");
		assertEquals(key, retrieved.getKey(), "Should not change the key");
	}

	/**
	 * Ensure can store and later retrieve values.
	 */
	@Test
	public void storeAndLaterRetrieve() throws Exception {

		// Store document
		AttributeTypesDocument document = this.setupDocument(0);

		// Obtain document later
		OfficeCabinet<AttributeTypesDocument> cabinetTwo = this.archive.createOfficeCabinet();
		AttributeTypesDocument retrieved = cabinetTwo.retrieveByKey(document.getKey()).get();
		assertNotSame(document, retrieved, "Should retrieve different instance");

		// Ensure same data
		assertEquals(document.getKey(), retrieved.getKey(), "Should have same key");
		document.assertEquals(retrieved);
	}

	@Test
	public void detectDirty() throws Exception {

		// Setup document
		String key = this.setupDocument(0).getKey();

		// Obtain the document
		OfficeCabinet<AttributeTypesDocument> cabinet = this.archive.createOfficeCabinet();
		AttributeTypesDocument document = cabinet.retrieveByKey(key).get();

		// Change the value
		final int CHANGE = 1000;
		assertNotEquals(CHANGE, document.getIntPrimitive(), "INVALID TEST: not changing value");
		document.setIntPrimitive(CHANGE);

		// Close (causing save on being dirty)
		OfficeCabinetAdmin admin = this.getOfficeCabinetAdmin(cabinet);
		admin.close();

		// Ensure dirty change saved
		AttributeTypesDocument updated = this.archive.createOfficeCabinet().retrieveByKey(key).get();
		assertEquals(CHANGE, updated.getIntPrimitive(), "Should update in store as dirty");
	}

	/**
	 * Sets up the {@link AttributeTypesDocument} in {@link OfficeCabinet}.
	 * 
	 * @param offset Offset for state.
	 * @return Set up {@link AttributeTypesDocument}.
	 */
	private AttributeTypesDocument setupDocument(int offset) {
		OfficeCabinet<AttributeTypesDocument> cabinet = this.archive.createOfficeCabinet();
		AttributeTypesDocument document = new AttributeTypesDocument(offset);
		cabinet.store(document);
		return document;
	}

}