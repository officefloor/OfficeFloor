package net.officefloor.nosql.objectify.mock;

import com.googlecode.objectify.ObjectifyService;

import net.officefloor.frame.test.JUnitAgnosticAssert;
import net.officefloor.nosql.objectify.MockEntity;

/**
 * Tests the {@link ObjectifyRule}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractObjectifyTestCase {

	/**
	 * Obtains the {@link AbstractObjectifyJUnit}.
	 * 
	 * @return {@link AbstractObjectifyJUnit}.
	 */
	protected abstract AbstractObjectifyJUnit getObjectify();

	/**
	 * Ensure can store and get values.
	 */
	public void storeGet() throws Throwable {

		// Register the entity
		ObjectifyService.register(MockEntity.class);

		// Store entity
		MockEntity entity = new MockEntity(null, "TEST", "INDEXED", 1, 2);
		this.getObjectify().store(entity);

		// Ensure find by filter
		MockEntity foundByFilter = this.getObjectify()
				.get(MockEntity.class, 1, (load) -> load.filter("indexedStringValue", "INDEXED")).get(0);
		JUnitAgnosticAssert.assertEquals("TEST", foundByFilter.getStringValue(), "Should find entity by filter");

		// Ensure find by result
		MockEntity foundByResult = this.getObjectify().get(MockEntity.class, (load) -> load.id(entity.getId()));
		JUnitAgnosticAssert.assertEquals("TEST", foundByResult.getStringValue(), "Should find entity by result");

		// Ensure find by id
		MockEntity foundById = this.getObjectify().get(MockEntity.class, entity.getId());
		JUnitAgnosticAssert.assertEquals("TEST", foundById.getStringValue(), "Should find entity by id");

		// Ensure can find as first
		MockEntity foundByFirst = this.getObjectify().get(MockEntity.class);
		JUnitAgnosticAssert.assertEquals("TEST", foundByFirst.getStringValue(), "Should find entity by first");

		// Update the entity (allowing eventual consistency)
		entity.setStringValue("CHANGED");
		this.getObjectify().ofy().save().entities(entity).now();

		// Ensure can retrieve once consistent
		MockEntity consistentEntity = this.getObjectify().consistent(
				() -> this.getObjectify().get(MockEntity.class, entity.getId()),
				(checkEntity) -> "CHANGED".equals(checkEntity.getStringValue()));
		JUnitAgnosticAssert.assertEquals("CHANGED", consistentEntity.getStringValue(), "Should have consistent entity");
	}

}