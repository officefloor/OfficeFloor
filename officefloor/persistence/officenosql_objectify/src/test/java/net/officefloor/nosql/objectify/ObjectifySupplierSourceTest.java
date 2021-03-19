/*-
 * #%L
 * Objectify Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.nosql.objectify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.annotation.Entity;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSupplier;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.supplier.SupplierLoaderUtil;
import net.officefloor.compile.test.supplier.SupplierTypeBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.nosql.objectify.mock.ObjectifyExtension;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.test.UsesGCloudTest;

/**
 * Tests {@link ObjectifySupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesGCloudTest
public class ObjectifySupplierSourceTest {

	/**
	 * {@link ObjectifyExtension}.
	 */
	@RegisterExtension
	public final ObjectifyExtension extension = new ObjectifyExtension();

	/**
	 * Validate specification.
	 */
	@Test
	public void specification() {
		SupplierLoaderUtil.validateSpecification(ObjectifySupplierSource.class);
	}

	/**
	 * Validate type.
	 */
	@Test
	public void type() throws Throwable {
		SupplierTypeBuilder type = SupplierLoaderUtil.createSupplierTypeBuilder();
		type.addSuppliedManagedObjectSource(null, Objectify.class, null);
		type.addThreadSynchroniser();
		SupplierLoaderUtil.validateInitialSupplierType(type, ObjectifySupplierSource.class);
	}

	/**
	 * Ensure validate run.
	 */
	@Test
	public void validate() {
		Closure<Boolean> isRun = new Closure<>();
		this.doObjectifyTest(WriteReadSection.class, (ObjectifyExtension extension, MockEntity result) -> {
			isRun.value = true;
		});
		assertTrue(isRun.value, "Should run validate");
	}

	/**
	 * Ensure can write and then read back the entity.
	 */
	@Test
	public void writeRead() {
		this.doObjectifyTest(WriteReadSection.class, (ObjectifyExtension extension, MockEntity result) -> {
			MockEntity entity = extension.get(MockEntity.class);
			assertNotNull(entity.getId(), "Should have id");
			assertEquals(result.getId(), entity.getId(), "Incorrect id");
			assertEquals("string", entity.getStringValue(), "Incorrect string");
			assertEquals("indexed string", entity.getIndexedStringValue(), "Incorrect indexed string");
			assertEquals(Integer.valueOf(1), entity.getIntegerValue(), "Incorrect integer");
			assertEquals(Integer.valueOf(2), entity.getIndexedIntegerValue(), "Incorrect indexed integer");
		});
	}

	public static class WriteReadSection {
		public void service(Objectify objectify, ThreadSafeClosure<MockEntity> closure) {
			MockEntity entity = new MockEntity(null, "string", "indexed string", 1, 2);
			objectify.save().entities(entity).now();
			closure.set(entity);
		}
	}

	/**
	 * Ensure can read via indexed string.
	 */
	@Test
	public void indexedString() {
		this.doObjectifyTest(WriteReadSection.class, (ObjectifyExtension extension, MockEntity result) -> {
			MockEntity entity = extension
					.get(MockEntity.class, 1, (loader) -> loader.filter("indexedStringValue", "indexed string")).get(0);
			assertEquals(result.getId(), entity.getId(), "Incorrect entity");
		});
	}

	/**
	 * Ensure can read via indexed integer.
	 */
	@Test
	public void testIndexedInteger() {
		this.doObjectifyTest(WriteReadSection.class, (ObjectifyExtension extension, MockEntity result) -> {
			MockEntity entity = extension.get(MockEntity.class, 1, (loader) -> loader.filter("indexedIntegerValue", 2))
					.get(0);
			assertEquals(result.getId(), entity.getId(), "Incorrect entity");
		});
	}

	/**
	 * Ensure can work within transaction.
	 */
	@Test
	public void transaction() {
		this.doObjectifyTest(TransactionSection.class, (ObjectifyExtension extension, MockEntity result) -> {
			MockEntity entity = extension.get(MockEntity.class);
			assertNotNull(entity.getId(), "Should have id");
			assertEquals(result.getId(), entity.getId(), "Incorrect id");
			assertEquals("string", entity.getStringValue(), "Incorrect string");
			assertEquals("indexed string", entity.getIndexedStringValue(), "Incorrect indexed string");
			assertEquals(Integer.valueOf(1), entity.getIntegerValue(), "Incorrect integer");
			assertEquals(Integer.valueOf(2), entity.getIndexedIntegerValue(), "Incorrect indexed integer");
		});
	}

	public static class TransactionSection {
		public void service(Objectify objectify, ThreadSafeClosure<MockEntity> closure) {
			objectify.transact(() -> {
				MockEntity entity = new MockEntity(null, "string", "indexed string", 1, 2);
				objectify.save().entities(entity).now();
				closure.set(entity);
			});
		}
	}

	/**
	 * Ensure can work across methods.
	 */
	@Test
	public void acrossMethods() {
		this.doObjectifyTest(AcrossMethodsSection.class, (ObjectifyExtension extension, MockEntity result) -> {
			List<MockEntity> entities = extension.get(MockEntity.class, 2,
					(loader) -> loader.order("indexedIntegerValue"));
			assertEquals("one", entities.get(0).getStringValue(), "Incorrect first entity");
			assertEquals("two", entities.get(1).getStringValue(), "Incorrect second entry");
		});
	}

	public static class AcrossMethodsSection {

		@Next("other")
		public void service(Objectify objectify) {
			objectify.save().entities(new MockEntity(null, "one", "first", 1, 2)).now();
		}

		public void other(Objectify objectify) {
			objectify.save().entities(new MockEntity(null, "two", "second", 4, 8)).now();
		}
	}

	/**
	 * Ensure can work across {@link Team} instances.
	 */
	@Test
	public void acrossTeams() {
		this.doObjectifyTest(AcrossTeamsSection.class, (ObjectifyExtension extension, MockEntity result) -> {
			List<MockEntity> entities = extension.get(MockEntity.class, 2,
					(loader) -> loader.order("indexedIntegerValue"));
			assertEquals("one", entities.get(0).getStringValue(), "Incorrect first entity");
			assertEquals("two", entities.get(1).getStringValue(), "Incorrect second entry");
		});
	}

	public static class AcrossTeamsSection {

		@Next("otherTeam")
		public Thread service(Objectify objectify) {
			objectify.save().entities(new MockEntity(null, "one", "first", 1, 2)).now();
			return Thread.currentThread();
		}

		public void otherTeam(@Parameter Thread originalThread, Objectify objectify, TeamMarker marker) {
			assertNotNull(originalThread, "Should have original thread");
			assertNotEquals(originalThread, Thread.currentThread(), "Should be different thread");
			objectify.save().entities(new MockEntity(null, "two", "second", 4, 8)).now();
		}
	}

	/**
	 * Ensure can register {@link Entity} instances via
	 * {@link ObjectifyEntityLocatorServiceFactory}.
	 */
	@Test
	public void serviceRegisteredEntity() {
		this.doObjectifyTest(ServiceRegisteredEntitySection.class,
				(ObjectifyExtension extension, ServiceRegisteredEntity entity) -> {
					ServiceRegisteredEntity retrieved = extension.get(ServiceRegisteredEntity.class);
					assertEquals(entity.getId(), retrieved.getId(), "Incorrect retrieved");
					assertEquals("TEST", retrieved.getTest(), "Incorrect value");
				});
	}

	public static class ServiceRegisteredEntitySection {
		public void service(Objectify objectify, ThreadSafeClosure<ServiceRegisteredEntity> closure) {
			ServiceRegisteredEntity entity = new ServiceRegisteredEntity(null, "TEST");
			objectify.save().entities(entity).now();
			closure.set(entity);
		}
	}

	/**
	 * Ensure can use {@link ObjectifyEntityLocator} configured in properties.
	 */
	@Test
	public void locatedEntity() {
		this.doObjectifyTest(LocatedEntitySection.class, (ObjectifyExtension extension, LocatedEntity entity) -> {
			LocatedEntity retrieved = extension.get(LocatedEntity.class);
			assertEquals(entity.getId(), retrieved.getId(), "Incorrect retrieved");
			assertEquals("TEST", retrieved.getLocation(), "Incorrect value");
		}, ObjectifySupplierSource.PROPERTY_ENTITY_LOCATORS,
				MockEntity.class.getName() + " , " + MockObjectifyEntityLocator.class.getName() + " ");
	}

	public static class LocatedEntitySection {
		public void service(Objectify objectify, ThreadSafeClosure<LocatedEntity> closure) {
			LocatedEntity entity = new LocatedEntity(null, "TEST");
			objectify.save().entities(entity).now();
			closure.set(entity);
		}
	}

	/**
	 * Undertakes testing with {@link Objectify}.
	 * 
	 * @param sectionClass                  Section {@link Class}.
	 * @param validator                     Validates the result from logic.
	 * @param propertyConfiguredEntityTypes Additional optional {@link Entity}
	 *                                      types.
	 */
	private <R> void doObjectifyTest(Class<?> sectionClass, BiConsumer<ObjectifyExtension, R> validator,
			String... propertyConfiguredEntityTypes) {
		try {

			// Capture the return value
			ThreadSafeClosure<R> returnResult = new ThreadSafeClosure<>();

			// Undertake logic
			CompileOfficeFloor compiler = new CompileOfficeFloor();
			compiler.officeFloor((context) -> {
				context.getOfficeFloorDeployer().addTeam("TEAM", ExecutorCachedTeamSource.class.getName())
						.addTypeQualification(null, TeamMarker.class.getName());
			});
			compiler.office((context) -> {
				OfficeArchitect office = context.getOfficeArchitect();

				// Auto-wire the teams
				office.enableAutoWireTeams();

				// Add Objectify
				OfficeSupplier objectify = office.addSupplier("OBJECTIFY", ObjectifySupplierSource.class.getName());
				if (propertyConfiguredEntityTypes.length == 0) {
					// Load the default entity for testing
					objectify.addProperty(ObjectifySupplierSource.PROPERTY_ENTITY_LOCATORS, MockEntity.class.getName());
				} else {
					// Load the test properties
					for (int i = 0; i < propertyConfiguredEntityTypes.length; i += 2) {
						String name = propertyConfiguredEntityTypes[i];
						String value = propertyConfiguredEntityTypes[i + 1];
						objectify.addProperty(name, value);
					}
				}

				// Add capture of return value
				office.addOfficeManagedObjectSource("RETURN", new Singleton(returnResult))
						.addOfficeManagedObject("RETURN", ManagedObjectScope.THREAD);

				// Add the team marker
				context.addManagedObject("MARKER", TeamMarker.class, ManagedObjectScope.THREAD);

				// Add section
				context.addSection("TEST", sectionClass);
			});
			try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

				// Undertake the logic
				CompileOfficeFloor.invokeProcess(officeFloor, "TEST.service", null);

				// Validate the result
				validator.accept(this.extension, returnResult.get());
			}

		} catch (Throwable ex) {
			fail(ex);
		}
	}

	public static class TeamMarker {
	}

}
