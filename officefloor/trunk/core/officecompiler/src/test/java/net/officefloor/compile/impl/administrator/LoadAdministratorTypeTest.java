/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.administrator;

import java.util.Properties;

import javax.transaction.xa.XAResource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.administrator.DutyType;
import net.officefloor.compile.impl.issues.MockCompilerIssues;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.structure.AdministratorNodeImpl;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorDutyMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceContext;
import net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorSourceSpecification;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests loading the {@link AdministratorType}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadAdministratorTypeTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link AdministratorSourceMetaData}.
	 */
	private final AdministratorSourceMetaData<?, ?> metaData = this
			.createMock(AdministratorSourceMetaData.class);

	@Override
	protected void setUp() throws Exception {
		MockAdministratorSource.reset(this.metaData);
	}

	/**
	 * Ensure issue if fail to instantiate the {@link AdministratorSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException(
				"instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				Node.TYPE_NAME,
				AdministratorNodeImpl.class,
				"Failed to instantiate "
						+ MockAdministratorSource.class.getName()
						+ " by default constructor", failure);

		// Attempt to load
		MockAdministratorSource.instantiateFailure = failure;
		this.loadAdministratorType(false, null);
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues.recordIssue(Node.TYPE_NAME, AdministratorNodeImpl.class,
				"Missing property 'missing'");

		// Attempt to load
		this.loadAdministratorType(false, new Init() {
			@Override
			public void init(AdministratorSourceContext context) {
				context.getProperty("missing");
			}
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Record basic meta-data
		this.record_loadAdminType(DutyKey.ONE);

		// Attempt to load
		this.loadAdministratorType(true, new Init() {
			@Override
			public void init(AdministratorSourceContext context) {
				assertEquals("Ensure get defaulted property", "DEFAULT",
						context.getProperty("missing", "DEFAULT"));
				assertEquals("Ensure get property ONE", "1",
						context.getProperty("ONE"));
				assertEquals("Ensure get property TWO", "2",
						context.getProperty("TWO"));
				Properties properties = context.getProperties();
				assertEquals("Incorrect number of properties", 2,
						properties.size());
				assertEquals("Incorrect property ONE", "1",
						properties.get("ONE"));
				assertEquals("Incorrect property TWO", "2",
						properties.get("TWO"));
			}
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.issues.recordIssue(Node.TYPE_NAME, AdministratorNodeImpl.class,
				"Can not load class 'missing'");

		// Attempt to load
		this.loadAdministratorType(false, new Init() {
			@Override
			public void init(AdministratorSourceContext context) {
				context.loadClass("missing");
			}
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing class
		this.issues.recordIssue(Node.TYPE_NAME, AdministratorNodeImpl.class,
				"Can not obtain resource at location 'missing'");

		// Attempt to load
		this.loadAdministratorType(false, new Init() {
			@Override
			public void init(AdministratorSourceContext context) {
				context.getResource("missing");
			}
		});
	}

	/**
	 * Ensure issue if fails to init the {@link AdministratorSource}.
	 */
	public void testFailInitAdministratorSource() {

		final NullPointerException failure = new NullPointerException(
				"Fail init AdministratorSource");

		// Record failure to init the Administrator Source
		this.issues.recordIssue(Node.TYPE_NAME, AdministratorNodeImpl.class,
				"Failed to init", failure);

		// Attempt to load
		this.loadAdministratorType(false, new Init() {
			@Override
			public void init(AdministratorSourceContext context) {
				throw failure;
			}
		});
	}

	/**
	 * Ensure issue if <code>null</code> {@link AdministratorSourceMetaData}.
	 */
	public void testNullAdministratorSourceMetaData() {

		// Record null the Administrator Source meta-data
		this.issues.recordIssue(Node.TYPE_NAME, AdministratorNodeImpl.class,
				"Returned null AdministratorSourceMetaData");

		// Attempt to load
		this.loadAdministratorType(false, new Init() {
			@Override
			public void init(AdministratorSourceContext context) {
				MockAdministratorSource.metaData = null;
			}
		});
	}

	/**
	 * Ensure issue if fails to obtain the {@link AdministratorSourceMetaData}.
	 */
	public void testFailGetAdministratorSourceMetaData() {

		final Error failure = new Error("Obtain meta-data failure");

		// Record failure to obtain the meta-data
		this.issues.recordIssue(Node.TYPE_NAME, AdministratorNodeImpl.class,
				"Failed to get AdministratorSourceMetaData", failure);

		// Attempt to load
		MockAdministratorSource.metaDataFailure = failure;
		this.loadAdministratorType(false, null);
	}

	/**
	 * Ensure issue if no extension interface type from meta-data.
	 */
	public void testNoExtensionInterface() {

		// Record no object class
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(),
				null);
		this.issues.recordIssue(Node.TYPE_NAME, AdministratorNodeImpl.class,
				"No extension interface provided");

		// Attempt to load
		this.loadAdministratorType(false, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link AdministratorDutyMetaData}
	 * array.
	 */
	public void testNullDutyArray() {

		// Record null duty meta-data array
		this.record_extensionInterface();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(), null);
		this.issues.recordIssue(Node.TYPE_NAME, AdministratorNodeImpl.class,
				"Must have at least one duty");

		// Attempt to load
		this.loadAdministratorType(false, null);
	}

	/**
	 * Ensure issue if empty {@link AdministratorDutyMetaData} array.
	 */
	public void testEmptyDutyArray() {

		// Record null duty meta-data array
		this.record_extensionInterface();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[0]);
		this.issues.recordIssue(Node.TYPE_NAME, AdministratorNodeImpl.class,
				"Must have at least one duty");

		// Attempt to load
		this.loadAdministratorType(false, null);
	}

	/**
	 * Ensure issue if <code>null</code> {@link AdministratorDutyMetaData} in
	 * the returned array.
	 */
	public void testNullDutyMetaData() {

		// Record null duty meta-data entry in the array
		this.record_extensionInterface();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[] { null });
		this.issues.recordIssue(Node.TYPE_NAME, AdministratorNodeImpl.class,
				"Null meta data for duty 0");

		// Attempt to load
		this.loadAdministratorType(false, null);
	}

	/**
	 * Ensure issue if blank {@link AdministratorDutyMetaData} name.
	 */
	public void testBlankDutyName() {

		// Record null duty key
		this.record_loadAdminType((Enum<?>) null);
		this.issues.recordIssue(Node.TYPE_NAME, AdministratorNodeImpl.class,
				"No name for duty 0");

		// Attempt to load
		this.loadAdministratorType(false, null);
	}

	/**
	 * Ensure issue if duplicate {@link AdministratorDutyMetaData} name.
	 */
	public void testDuplicateName() {

		// Record null duty key
		final AdministratorDutyMetaData<?, ?> dutyOne = this
				.createMock(AdministratorDutyMetaData.class);
		final AdministratorDutyMetaData<?, ?> dutyTwo = this
				.createMock(AdministratorDutyMetaData.class);

		final String DUPLICATE_NAME = "duplicate";

		// Record missing type
		this.record_extensionInterface();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[] { dutyOne, dutyTwo });
		this.recordReturn(dutyOne, dutyOne.getDutyName(), DUPLICATE_NAME);
		this.recordReturn(dutyOne, dutyOne.getKey(), DutyKey.ONE);
		this.recordReturn(dutyTwo, dutyTwo.getDutyName(), DUPLICATE_NAME);
		this.issues.recordIssue(Node.TYPE_NAME, AdministratorNodeImpl.class,
				"Duplicate duty name 'duplicate'");

		// Attempt to load
		this.loadAdministratorType(false, null);
	}

	/**
	 * Ensure issue if invalid {@link AdministratorDutyMetaData} key. In other
	 * words, keys of different types.
	 */
	public void testInvalidDutyKey() {

		// Record invalid duty key
		this.record_loadAdminType(DutyKey.ONE, InvalidDutyKey.INVALID_DUTY_KEY);
		this.issues.recordIssue(Node.TYPE_NAME, AdministratorNodeImpl.class,
				"Key " + InvalidDutyKey.INVALID_DUTY_KEY
						+ " for duty 1 is invalid (type="
						+ InvalidDutyKey.class.getName() + ", required type="
						+ DutyKey.class.getName() + ")");

		// Attempt to load
		this.loadAdministratorType(false, null);
	}

	/**
	 * Ensures issue if first {@link Duty} has a key while a subsequent
	 * {@link Duty} does not.
	 */
	public void testMissingKey() {

		final AdministratorDutyMetaData<?, ?> dutyOne = this
				.createMock(AdministratorDutyMetaData.class);
		final AdministratorDutyMetaData<?, ?> dutyTwo = this
				.createMock(AdministratorDutyMetaData.class);

		// Record missing type
		this.record_extensionInterface();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[] { dutyOne, dutyTwo });
		this.recordReturn(dutyOne, dutyOne.getDutyName(), DutyKey.ONE.name());
		this.recordReturn(dutyOne, dutyOne.getKey(), DutyKey.ONE);
		this.recordReturn(dutyTwo, dutyTwo.getDutyName(), DutyKey.TWO.name());
		this.recordReturn(dutyTwo, dutyTwo.getKey(), null);
		this.issues.recordIssue(Node.TYPE_NAME, AdministratorNodeImpl.class,
				"Must have key for duty " + 1);

		// Attempt to load
		this.loadAdministratorType(false, null);
	}

	/**
	 * Ensures issue if first {@link Duty} does not have a key while a
	 * subsequent {@link Duty} does.
	 */
	public void testUnexpectedKey() {

		final AdministratorDutyMetaData<?, ?> dutyOne = this
				.createMock(AdministratorDutyMetaData.class);
		final AdministratorDutyMetaData<?, ?> dutyTwo = this
				.createMock(AdministratorDutyMetaData.class);

		// Record missing type
		this.record_extensionInterface();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(),
				new AdministratorDutyMetaData[] { dutyOne, dutyTwo });
		this.recordReturn(dutyOne, dutyOne.getDutyName(), DutyKey.ONE.name());
		this.recordReturn(dutyOne, dutyOne.getKey(), null);
		this.recordReturn(dutyTwo, dutyTwo.getDutyName(), DutyKey.TWO.name());
		this.recordReturn(dutyTwo, dutyTwo.getKey(), DutyKey.TWO);
		this.issues.recordIssue(Node.TYPE_NAME, AdministratorNodeImpl.class,
				"Should not have key for duty " + 1);

		// Attempt to load
		this.loadAdministratorType(false, null);
	}

	/**
	 * Validates the loaded {@link AdministratorType} with keys.
	 */
	public void testLoadAdministratorTypeWithKeys() {

		// Record loading the administrator type.
		// (Should be ordered when returned as a type)
		this.record_loadAdminType(DutyKey.TWO, DutyKey.ONE);

		// Load the administrator type
		AdministratorType<?, ?> adminType = this.loadAdministratorType(true,
				null);

		// Validate the administrator type
		assertEquals("Incorrect extension interface", XAResource.class,
				adminType.getExtensionInterface());
		DutyType<?, ?>[] dutyTypes = adminType.getDutyTypes();
		assertEquals("Incorrect number of duties", 2, dutyTypes.length);
		assertEquals("Incorrect name for first duty", DutyKey.ONE.name(),
				dutyTypes[0].getDutyName());
		assertEquals("Incorrect key for first duty", DutyKey.ONE,
				dutyTypes[0].getDutyKey());
		assertEquals("Incorrect name for second duty", DutyKey.TWO.name(),
				dutyTypes[1].getDutyName());
		assertEquals("Incorrect key for second duty", DutyKey.TWO,
				dutyTypes[1].getDutyKey());
	}

	/**
	 * Validates the loaded {@link AdministratorType} without keys.
	 */
	public void testLoadAdministratorTypeWithoutKeys() {

		// Record loading the administrator type
		// (No keys so take in order as per meta-data)
		this.record_loadAdminType(false, DutyKey.ONE, DutyKey.TWO);

		// Load the administrator type
		AdministratorType<?, ?> adminType = this.loadAdministratorType(true,
				null);

		// Validate the administrator type
		assertEquals("Incorrect extension interface", XAResource.class,
				adminType.getExtensionInterface());
		DutyType<?, ?>[] dutyTypes = adminType.getDutyTypes();
		assertEquals("Incorrect number of duties", 2, dutyTypes.length);
		assertEquals("Incorrect name for first duty", DutyKey.ONE.name(),
				dutyTypes[0].getDutyName());
		assertNull("Should not have key for first duty",
				dutyTypes[0].getDutyKey());
		assertEquals("Incorrect name for second duty", DutyKey.TWO.name(),
				dutyTypes[1].getDutyName());
		assertNull("Should not have key for second duty",
				dutyTypes[1].getDutyKey());
	}

	/**
	 * {@link Duty} keys.
	 */
	private enum DutyKey {
		ONE, TWO
	}

	/**
	 * Invalid {@link Duty} key.
	 */
	private enum InvalidDutyKey {
		INVALID_DUTY_KEY
	}

	/**
	 * Records obtaining the extension interface.
	 */
	private void record_extensionInterface() {
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(),
				XAResource.class);
	}

	/**
	 * Records loading the {@link AdministratorType}.
	 * 
	 * @param keys
	 *            Keys of the {@link AdministratorDutyMetaData}.
	 */
	private void record_loadAdminType(Enum<?>... keys) {
		this.record_loadAdminType(true, keys);
	}

	/**
	 * Records loading the {@link AdministratorType}.
	 * 
	 * @param isIncludeKeys
	 *            Flag whether names only (<code>false</code> to not include
	 *            keys).
	 * @param keys
	 *            Keys of the {@link AdministratorDutyMetaData}.
	 */
	private void record_loadAdminType(boolean isIncludeKeys, Enum<?>... keys) {

		// Create the listing of duty meta-data
		AdministratorDutyMetaData<?, ?>[] duties = new AdministratorDutyMetaData[keys.length];
		for (int i = 0; i < duties.length; i++) {
			duties[i] = this.createMock(AdministratorDutyMetaData.class);
		}

		// Record loading the administrator type
		this.record_extensionInterface();
		this.recordReturn(this.metaData,
				this.metaData.getAdministratorDutyMetaData(), duties);
		for (int i = 0; i < duties.length; i++) {
			AdministratorDutyMetaData<?, ?> duty = duties[i];
			Enum<?> key = keys[i];
			if (key == null) {
				// Provide blank name
				this.recordReturn(duty, duty.getDutyName(), null);
			} else {
				// Provide details of duty
				this.recordReturn(duty, duty.getDutyName(), key.name());
				this.recordReturn(duty, duty.getKey(), (isIncludeKeys ? key
						: null));
			}
		}
	}

	/**
	 * Loads the {@link AdministratorType}.
	 * 
	 * @param isExpectedToLoad
	 *            Flag indicating if expecting to load the {@link WorkType}.
	 * @param init
	 *            {@link Init}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 * @return Loaded {@link AdministratorType}.
	 */
	@SuppressWarnings("rawtypes")
	public AdministratorType<?, ?> loadAdministratorType(
			boolean isExpectedToLoad, Init init,
			String... propertyNameValuePairs) {

		// Replay mock objects
		this.replayMockObjects();

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Create the administrator loader and load the administrator type
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		AdministratorLoader adminLoader = compiler.getAdministratorLoader();
		MockAdministratorSource.init = init;
		AdministratorType adminType = adminLoader.loadAdministratorType(
				MockAdministratorSource.class, propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the administrator type", adminType);
		} else {
			assertNull("Should not load the administrator type", adminType);
		}

		// Return the administrator type
		return adminType;
	}

	/**
	 * Implement to initialise the {@link MockAdministratorSource}.
	 */
	private static interface Init {

		/**
		 * Implemented to init the {@link AdministratorSource}.
		 * 
		 * @param context
		 *            {@link AdministratorSourceContext}.
		 */
		void init(AdministratorSourceContext context);
	}

	/**
	 * Mock {@link AdministratorSource}.
	 */
	@TestSource
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class MockAdministratorSource implements
			AdministratorSource<Object, Indexed> {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * {@link Init} to init the {@link AdministratorSource}.
		 */
		public static Init init = null;

		/**
		 * Failure to obtain the {@link AdministratorMetaData}.
		 */
		public static Error metaDataFailure = null;

		/**
		 * {@link AdministratorSourceSpecification}.
		 */
		public static AdministratorSourceMetaData metaData;

		/**
		 * Resets the state for next test.
		 * 
		 * @param metaData
		 *            {@link AdministratorSourceMetaData}.
		 */
		public static void reset(AdministratorSourceMetaData<?, ?> metaData) {
			instantiateFailure = null;
			init = null;
			metaDataFailure = null;
			MockAdministratorSource.metaData = metaData;
		}

		/**
		 * Initiate with possible failure.
		 */
		public MockAdministratorSource() {
			// Throw instantiate failure
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================== AdministratorSource ===========================
		 */

		@Override
		public AdministratorSourceSpecification getSpecification() {
			fail("Should not obtain specification");
			return null;
		}

		@Override
		public void init(AdministratorSourceContext context) throws Exception {
			// Run the init if available
			if (init != null) {
				init.init(context);
			}
		}

		@Override
		public AdministratorSourceMetaData getMetaData() {

			// Throw meta-data failure
			if (metaDataFailure != null) {
				throw metaDataFailure;
			}

			// Return the meta-data
			return metaData;
		}

		@Override
		public Administrator createAdministrator() {
			fail("Should not create Administrator");
			return null;
		}
	}

}