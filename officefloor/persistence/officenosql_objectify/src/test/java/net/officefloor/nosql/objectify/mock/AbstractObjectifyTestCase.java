/*-
 * #%L
 * Objectify Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.nosql.objectify.mock;

import com.googlecode.objectify.ObjectifyService;

import net.officefloor.nosql.objectify.MockEntity;
import net.officefloor.test.JUnitAgnosticAssert;

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
