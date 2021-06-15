package net.officefloor.cache.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.cache.Cache;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.test.ThreadedTestSupport;
import net.officefloor.plugin.clazz.Qualified;

/**
 * Test the {@link ConstantCacheManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class ConstantCacheManagedObjectSourceTest {

	public static final AtomicInteger keyGenerator = new AtomicInteger();

	private final ThreadedTestSupport threading = new ThreadedTestSupport();

	/**
	 * Ensure correct specification.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void specification() {
		ManagedObjectLoaderUtil.validateSpecification(ConstantCacheManagedObjectSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void type() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(Cache.class);
		type.setInput(true);
		type.addFunctionDependency("RETRIEVER", ConstantCacheDataRetriever.class, null);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, ConstantCacheManagedObjectSource.class);
	}

	/**
	 * Ensure correct qualified dependency.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void qualified() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(Cache.class);
		type.setInput(true);
		type.addFunctionDependency("RETRIEVER", ConstantCacheDataRetriever.class, "QUALIFIED");
		ManagedObjectLoaderUtil.validateManagedObjectType(type, ConstantCacheManagedObjectSource.class,
				ConstantCacheManagedObjectSource.DATA_RETRIEVER_QUALIFIER, "QUALIFIED");
	}

	/**
	 * Ensure can get cached value and refreshed values.
	 */
	@Test
	public void cacheRefresh() throws Throwable {

		// Initiate
		RefreshConstantCacheDataRetriever.threading = this.threading;
		RefreshConstantCacheDataRetriever.failure = null;
		ValidateSection.value = null;
		keyGenerator.set(0);

		// Source
		ConstantCacheManagedObjectSource<Integer, String> cache = new ConstantCacheManagedObjectSource<>();

		// Compile
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.officeFloor((officeFloor) -> {
			officeFloor.getOfficeFloorDeployer().addTeam("TEAM", OnePersonTeamSource.class.getName())
					.addTypeQualification(null, ConstantCacheDataRetriever.class.getName());
		});
		compiler.office((office) -> {

			// Register the cache
			OfficeManagedObjectSource mos = office.getOfficeArchitect().addOfficeManagedObjectSource("CACHE", cache);
			mos.addProperty(ConstantCacheManagedObjectSource.POLL_INTERVAL, "1");
			mos.addOfficeManagedObject("CACHE", ManagedObjectScope.THREAD);

			// Register dependencies
			office.addManagedObject("RETRIEVER", RefreshConstantCacheDataRetriever.class, ManagedObjectScope.THREAD);

			// Service
			office.addSection("SECTION", ValidateSection.class);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Ensure obtain from cache and reload
			final int MAX_ITERATIONS = 10;
			do {

				// Increment for new cache value
				int key = keyGenerator.incrementAndGet();

				// Wait for cache to be updated
				this.threading.waitForTrue(() -> {
					String value = cache.getCache().get(key);
					return String.valueOf(key).equals(value);
				});

				// Service from cache
				ValidateSection.value = null;
				CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", null);
				String value = ValidateSection.value;
				assertEquals(String.valueOf(key), value, "Incorrect cached value");

			} while (keyGenerator.get() < MAX_ITERATIONS);

			// Ensure handle failure
			int exceptionKey = keyGenerator.get() + 1;
			Exception exception = new Exception("TEST");
			RefreshConstantCacheDataRetriever.failure = exception;

			// Wait for exception handling (by incrementing key)
			this.threading.waitForTrue(() -> {
				String value = cache.getCache().get(exceptionKey);
				return String.valueOf(exceptionKey).equals(value);
			});
		}
	}

	/**
	 * Mock {@link ConstantCacheDataRetriever} for refresh {@link Cache} testing.
	 */
	public static class RefreshConstantCacheDataRetriever implements ConstantCacheDataRetriever<Integer, String> {

		public static ThreadedTestSupport threading;

		public static volatile Exception failure = null;

		@Override
		public Map<Integer, String> getData() throws Exception {

			// Determine if failure
			Exception exception = failure;
			if (exception != null) {

				// Increment key to progress
				keyGenerator.incrementAndGet();

				// Only throw exception once
				failure = null;
				throw exception;
			}

			// Obtain the key
			int key = keyGenerator.get();

			// Provide next set of cached data
			Map<Integer, String> data = new HashMap<>();
			data.put(key, String.valueOf(key));
			return data;
		}
	}

	/**
	 * Validate section logic.
	 */
	public static class ValidateSection {

		public static String value = null;

		public void service(Cache<Integer, String> cache) {
			int key = keyGenerator.get();
			for (int i = 0; i < key; i++) {
				assertNull(cache.get(i), "Should be new set of cached values");
			}
			value = cache.get(key);
		}
	}

	/**
	 * Ensure can load multiple {@link Cache} instances.
	 */
	@Test
	public void multipleCaches() throws Throwable {

		// Compile
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((office) -> {

			// Register caches and retrievers
			for (String qualifier : new String[] { "ONE", "TWO" }) {

				// Register the cache
				OfficeManagedObjectSource mos = office.getOfficeArchitect().addOfficeManagedObjectSource(
						"CACHE_" + qualifier, ConstantCacheManagedObjectSource.class.getName());
				mos.addProperty(ConstantCacheManagedObjectSource.DATA_RETRIEVER_QUALIFIER, qualifier);
				mos.addOfficeManagedObject("CACHE_" + qualifier, ManagedObjectScope.THREAD)
						.addTypeQualification(qualifier, Cache.class.getName());

				// Register retriever of data (specific to cache)
				Class<?> retrieverClass = "ONE".equals(qualifier) ? OneConstantCacheDataRetriever.class
						: TwoConstantCacheDataRetriever.class;
				office.addManagedObject("RETRIEVER_" + qualifier, retrieverClass, ManagedObjectScope.THREAD)
						.addTypeQualification(qualifier, ConstantCacheDataRetriever.class.getName());
			}

			// Service
			office.addSection("SECTION", MultipleCacheSection.class);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Service from cache
			MultipleCacheSection.valueOne = null;
			MultipleCacheSection.valueTwo = null;
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", null);
			assertEquals("A", MultipleCacheSection.valueOne, "Incorrect cached one value");
			assertEquals("B", MultipleCacheSection.valueTwo, "Incorrect cached two value");
		}
	}

	/**
	 * Qualified {@link ConstantCacheDataRetriever}.
	 */
	public static class OneConstantCacheDataRetriever implements ConstantCacheDataRetriever<Integer, String> {

		@Override
		public Map<Integer, String> getData() throws Exception {
			Map<Integer, String> data = new HashMap<>();
			data.put(1, "A");
			return data;
		}
	}

	/**
	 * Qualified {@link ConstantCacheDataRetriever}.
	 */
	public static class TwoConstantCacheDataRetriever implements ConstantCacheDataRetriever<Integer, String> {

		@Override
		public Map<Integer, String> getData() throws Exception {
			Map<Integer, String> data = new HashMap<>();
			data.put(2, "B");
			return data;
		}
	}

	/**
	 * Use multiple {@link Cache} section logic.
	 */
	public static class MultipleCacheSection {

		public static String valueOne = null;

		public static String valueTwo = null;

		public void service(@Qualified("ONE") Cache<Integer, String> cacheOne,
				@Qualified("TWO") Cache<Integer, String> cacheTwo) {
			valueOne = cacheOne.get(1);
			valueTwo = cacheTwo.get(2);
		}
	}

}