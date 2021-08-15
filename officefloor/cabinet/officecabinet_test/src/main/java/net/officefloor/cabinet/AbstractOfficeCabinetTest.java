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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

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
	 * Ensure can store and retrieve values.
	 */
	@Test
	public void storeAndRetrieve() throws Exception {
		OfficeCabinet<AttributeTypesDocument> cabinet = this.getAttributeTypesOfficeCabinetArchive()
				.createOfficeCabinet();
		AttributeTypesDocument document = new AttributeTypesDocument(0);
		cabinet.store(document);
		assertNotNull(document.getKey(), "Should assign key to document");
		AttributeTypesDocument retrieved = cabinet.retrieveByKey(document.getKey()).get();
		assertSame(document, retrieved, "Should retrieve same instance");
	}

	/**
	 * Ensure can store and later retrieve values.
	 */
	@Test
	public void storeAndLaterRetrieve() throws Exception {
		OfficeCabinetArchive<AttributeTypesDocument> archive = this.getAttributeTypesOfficeCabinetArchive();

		// Store document
		OfficeCabinet<AttributeTypesDocument> cabinetOne = archive.createOfficeCabinet();
		AttributeTypesDocument document = new AttributeTypesDocument(0);
		cabinetOne.store(document);

		// Obtain document later
		OfficeCabinet<AttributeTypesDocument> cabinetTwo = archive.createOfficeCabinet();
		AttributeTypesDocument retrieved = cabinetTwo.retrieveByKey(document.getKey()).get();
		assertNotSame(document, retrieved, "Should retrieve different instance");
		
		// Ensure same data
		document.assertEquals(retrieved);
	}

}