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

import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

/**
 * Tests Office Cabinet.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeCabinetTest {

	/**
	 * Obtains the {@link OfficeCabinet} for the {@link AttributeTypesEntity}.
	 * 
	 * @return {@link OfficeCabinet} for the {@link AttributeTypesEntity}.
	 * @throws Exception If fails to create {@link OfficeCabinet}.
	 */
	protected abstract OfficeCabinet<AttributeTypesEntity> getAttributeTypesOfficeCabinet() throws Exception;

	/**
	 * Ensure can store and retrieve values.
	 */
	@Test
	public void storeAndRetrieve() throws Exception {
		AttributeTypesEntity entity = new AttributeTypesEntity(true, (byte) 1, (short) 2, '3', 4, 5L, 6.0f, 7.0);
		OfficeCabinet<AttributeTypesEntity> cabinet = this.getAttributeTypesOfficeCabinet();
		cabinet.store(entity);
		AttributeTypesEntity retrieved = cabinet.retrieveByKey(entity.getId()).get();
		assertNotSame(entity, retrieved, "Should retrieve different instance");
		entity.assertEquals(retrieved);
	}

}
