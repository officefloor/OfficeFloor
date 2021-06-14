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
		type.addFunctionDependency("RETRIEVER", ConstantCacheDataRetriever.class);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, ConstantCacheManagedObjectSource.class);
	}

	/**
	 * Ensure can get cached value.
	 */
	@Test
	public void cachedValue() throws Throwable {

		// Reset
		reset();
		MockConstantCacheDataRetriever.threading = this.threading;
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
			office.addManagedObject("RETRIEVER", MockConstantCacheDataRetriever.class, ManagedObjectScope.THREAD);

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
				reset();
				CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", null);
				String value = ValidateSection.value;
				assertEquals(String.valueOf(key), value, "Incorrect cached value");

			} while (keyGenerator.get() < MAX_ITERATIONS);

			// Ensure handle failure
			int exceptionKey = keyGenerator.get() + 1;
			Exception exception = new Exception("TEST");
			MockConstantCacheDataRetriever.failure = exception;

			// Wait for exception handling (by incrementing key)
			this.threading.waitForTrue(() -> {
				String value = cache.getCache().get(exceptionKey);
				return String.valueOf(exceptionKey).equals(value);
			});
		}
	}

	/**
	 * Reset.
	 */
	private static void reset() {
		MockConstantCacheDataRetriever.failure = null;
		ValidateSection.value = null;
	}

	/**
	 * Mock {@link ConstantCacheDataRetriever}.
	 */
	public static class MockConstantCacheDataRetriever implements ConstantCacheDataRetriever<Integer, String> {

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

}