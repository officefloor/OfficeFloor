package net.officefloor.nosql.objectify;

import static org.junit.Assert.assertNotEquals;

import java.util.List;
import java.util.function.BiConsumer;

import org.junit.runners.model.Statement;

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
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.nosql.objectify.mock.ObjectifyRule;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Tests {@link ObjectifySupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifySupplierSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		SupplierLoaderUtil.validateSpecification(ObjectifySupplierSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() throws Throwable {
		ObjectifyRule rule = new ObjectifyRule();
		Closure<SupplierTypeBuilder> type = new Closure<>();
		rule.apply(new Statement() {
			@Override
			public void evaluate() throws Throwable {
				SupplierTypeBuilder buider = SupplierLoaderUtil.createSupplierTypeBuilder();
				buider.addSuppliedManagedObjectSource(null, Objectify.class, null);
				buider.addThreadSynchroniser();
				type.value = buider;
			}
		}, null).evaluate();
		SupplierLoaderUtil.validateSupplierType(type.value, ObjectifySupplierSource.class);
	}

	/**
	 * Ensure validate run.
	 */
	public void testValidate() {
		Closure<Boolean> isRun = new Closure<>();
		this.doObjectifyTest(WriteReadSection.class, (ObjectifyRule rule, MockEntity result) -> {
			isRun.value = true;
		});
		assertTrue("Should run validate", isRun.value);
	}

	/**
	 * Ensure can write and then read back the entity.
	 */
	public void testWriteRead() {
		this.doObjectifyTest(WriteReadSection.class, (ObjectifyRule rule, MockEntity result) -> {
			MockEntity entity = rule.get(MockEntity.class, (loader) -> loader);
			assertNotNull("Should have id", entity.getId());
			assertEquals("Incorrect id", result.getId(), entity.getId());
			assertEquals("Incorrect string", "string", entity.getStringValue());
			assertEquals("Incorrect indexed string", "indexed string", entity.getIndexedStringValue());
			assertEquals("Incorrect integer", Integer.valueOf(1), entity.getIntegerValue());
			assertEquals("Incorrect indexed integer", Integer.valueOf(2), entity.getIndexedIntegerValue());
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
	public void testIndexedString() {
		this.doObjectifyTest(WriteReadSection.class, (ObjectifyRule rule, MockEntity result) -> {
			MockEntity entity = rule.get(MockEntity.class,
					(loader) -> loader.filter("indexedStringValue", "indexed string"));
			assertEquals("Incorrect entity", result.getId(), entity.getId());
		});
	}

	/**
	 * Ensure can read via indexed integer.
	 */
	public void testIndexedInteger() {
		this.doObjectifyTest(WriteReadSection.class, (ObjectifyRule rule, MockEntity result) -> {
			MockEntity entity = rule.get(MockEntity.class, (loader) -> loader.filter("indexedIntegerValue", 2));
			assertEquals("Incorrect entity", result.getId(), entity.getId());
		});
	}

	/**
	 * Ensure can work within transaction.
	 */
	public void testTransaction() {
		this.doObjectifyTest(TransactionSection.class, (ObjectifyRule rule, MockEntity result) -> {
			MockEntity entity = rule.get(MockEntity.class, (loader) -> loader);
			assertNotNull("Should have id", entity.getId());
			assertEquals("Incorrect id", result.getId(), entity.getId());
			assertEquals("Incorrect string", "string", entity.getStringValue());
			assertEquals("Incorrect indexed string", "indexed string", entity.getIndexedStringValue());
			assertEquals("Incorrect integer", Integer.valueOf(1), entity.getIntegerValue());
			assertEquals("Incorrect indexed integer", Integer.valueOf(2), entity.getIndexedIntegerValue());
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
	public void testAcrossMethods() {
		this.doObjectifyTest(AcrossMethodsSection.class, (ObjectifyRule rule, MockEntity result) -> {
			List<MockEntity> entities = rule.get(MockEntity.class, 2, (loader) -> loader.order("indexedIntegerValue"));
			assertEquals("Incorrect first entity", "one", entities.get(0).getStringValue());
			assertEquals("Incorrect second entry", "two", entities.get(1).getStringValue());
		});
	}

	public static class AcrossMethodsSection {

		@NextFunction("other")
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
	public void testAcrossTeams() {
		this.doObjectifyTest(AcrossTeamsSection.class, (ObjectifyRule rule, MockEntity result) -> {
			List<MockEntity> entities = rule.get(MockEntity.class, 2, (loader) -> loader.order("indexedIntegerValue"));
			assertEquals("Incorrect first entity", "one", entities.get(0).getStringValue());
			assertEquals("Incorrect second entry", "two", entities.get(1).getStringValue());
		});
	}

	public static class AcrossTeamsSection {

		@NextFunction("otherTeam")
		public Thread service(Objectify objectify) {
			objectify.save().entities(new MockEntity(null, "one", "first", 1, 2)).now();
			return Thread.currentThread();
		}

		public void otherTeam(@Parameter Thread originalThread, Objectify objectify, TeamMarker marker) {
			assertNotNull("Should have original thread", originalThread);
			assertNotEquals("Should be different thread", originalThread, Thread.currentThread());
			objectify.save().entities(new MockEntity(null, "two", "second", 4, 8)).now();
		}
	}

	/**
	 * Ensure can register {@link Entity} instances via
	 * {@link ObjectifyEntityLocatorServiceFactory}.
	 */
	public void testServiceRegisteredEntity() {
		this.doObjectifyTest(ServiceRegisteredEntitySection.class,
				(ObjectifyRule rule, ServiceRegisteredEntity entity) -> {
					ServiceRegisteredEntity retrieved = rule.get(ServiceRegisteredEntity.class, null);
					assertEquals("Incorrect retrieved", entity.getId(), retrieved.getId());
					assertEquals("Incorrect value", "TEST", retrieved.getTest());
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
	public void testLocatedEntity() {
		this.doObjectifyTest(LocatedEntitySection.class, (ObjectifyRule rule, LocatedEntity entity) -> {
			LocatedEntity retrieved = rule.get(LocatedEntity.class, null);
			assertEquals("Incorrect retrieved", entity.getId(), retrieved.getId());
			assertEquals("Incorrect value", "TEST", retrieved.getLocation());
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
	private <R> void doObjectifyTest(Class<?> sectionClass, BiConsumer<ObjectifyRule, R> validator,
			String... propertyConfiguredEntityTypes) {
		try {

			// Easy access to test
			ObjectifySupplierSourceTest test = this;

			// Configure rule
			ObjectifyRule rule = new ObjectifyRule();
			rule.apply(new Statement() {
				@Override
				public void evaluate() throws Throwable {

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
						OfficeSupplier objectify = office.addSupplier("OBJECTIFY",
								ObjectifySupplierSource.class.getName());
						if (propertyConfiguredEntityTypes.length == 0) {
							// Load the default entity for testing
							objectify.addProperty(ObjectifySupplierSource.PROPERTY_ENTITY_LOCATORS,
									MockEntity.class.getName());
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
					test.officeFloor = compiler.compileAndOpenOfficeFloor();

					// Undertake the logic
					CompileOfficeFloor.invokeProcess(test.officeFloor, "TEST.service", null);

					// Validate the result
					validator.accept(rule, returnResult.get());
				}
			}, null).evaluate();

		} catch (Throwable ex) {
			throw fail(ex);
		}
	}

	public static class TeamMarker {
	}

}