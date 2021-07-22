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
	 */
	protected abstract OfficeCabinet<AttributeTypesEntity> getAttributeTypesOfficeCabinet();

	/**
	 * Ensure can store and retrieve values.
	 */
	@Test
	public void storeAndRetrieve() {
		AttributeTypesEntity entity = new AttributeTypesEntity(true, (byte) 1, (short) 2, '3', 4, 5L, 6.0f, 7.0);
		OfficeCabinet<AttributeTypesEntity> cabinet = this.getAttributeTypesOfficeCabinet();
		cabinet.store(entity);
		AttributeTypesEntity retrieved = cabinet.retrieveById("1").get();
		assertNotSame(entity, retrieved, "Should retrieve different instance");
		entity.assertEquals(retrieved);
	}

}