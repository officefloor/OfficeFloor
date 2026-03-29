/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.profile;

import java.util.List;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Tests configuring a profile.
 * 
 * @author Daniel Sagenschneider
 */
public class ProfileTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure handle no profile.
	 */
	public void testNoProfile() {
		this.doProfilesTest();
	}

	/**
	 * Ensure able to add profile.
	 */
	public void testSingleProfile() throws Exception {
		this.getOfficeFloorBuilder().addProfile("profile");
		this.doProfilesTest("profile");
	}

	/**
	 * Ensure able to add multiple profiles and keep them unique.
	 */
	public void testDeduplicateMultipleProfiles() {
		for (String profile : new String[] { "one", "one", "two", "three", "four", "repeat", "repeat", "one" }) {
			this.getOfficeFloorBuilder().addProfile(profile);
		}
		this.doProfilesTest("one", "two", "three", "four", "repeat");
	}

	private void doProfilesTest(String... expectedProfiles) {
		try {
			// Compile
			ProfileManagedObjectSource.profiles = null;
			this.constructManagedObject("CHECK", ProfileManagedObjectSource.class, this.getOfficeName());
			this.constructOfficeFloor();
			List<String> activeProfiles = ProfileManagedObjectSource.profiles;

			// Ensure correct profiles
			assertEquals("Incorrect number of profiles " + activeProfiles.toString(), expectedProfiles.length,
					activeProfiles.size());
			for (int i = 0; i < expectedProfiles.length; i++) {
				String expected = expectedProfiles[i];
				String actual = activeProfiles.get(i);
				assertEquals("Incorrect profile " + i, expected, actual);
			}
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	@TestSource
	public static class ProfileManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		private static List<String> profiles;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(Object.class);
			profiles = context.getManagedObjectSourceContext().getProfiles();
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not require managed object");
			return null;
		}
	}

}
