/*-
 * #%L
 * OfficeFloor Filing Cabinet Test
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
