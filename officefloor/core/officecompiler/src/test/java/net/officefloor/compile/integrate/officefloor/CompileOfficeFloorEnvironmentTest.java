/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.integrate.officefloor;

import net.officefloor.compile.impl.structure.OfficeFloorNodeImpl;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.impl.officefloor.OfficeFloorModelOfficeFloorSource;

/**
 * <p>
 * Tests compiling the {@link OfficeFloorModelOfficeFloorSource}.
 * <p>
 * Focus of this test is to ensure the {@link OfficeFloor} is flexible enough to
 * be deployed to various environments. It achieves this by tag replacement
 * based on the properties.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileOfficeFloorEnvironmentTest extends AbstractCompileTestCase {

	/**
	 * Ensure issue if missing tag.
	 */
	public void testMissingTag() {

		// Record issue if tag not provided as property
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Can not obtain ConfigurationItem at location 'office-floor' as missing property 'missing.tag'");

		// Missing properties
		this.compile(false);
	}

	/**
	 * Ensure only a single issue on missing tag repeated.
	 */
	public void testWarnOnceOnMissingTagRepeated() {

		// Record issue if tag not provided as property
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Can not obtain ConfigurationItem at location 'office-floor' as missing property 'repeated'");

		// Missing property
		this.compile(false);
	}

	/**
	 * Ensure issue for each missing tag.
	 */
	public void testMultipleMissingTags() {

		// Record issue for each missing unique tag
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Can not obtain ConfigurationItem at location 'office-floor' as missing property 'tag.one'");

		// Missing properties
		this.compile(false);
	}

	/**
	 * Ensure issue if only some tags provided.
	 */
	public void testPartialSetOfTagsProvided() {

		// Record loading with tags provided
		this.issues.recordIssue(OfficeFloorNode.OFFICE_FLOOR_NAME, OfficeFloorNodeImpl.class,
				"Can not obtain ConfigurationItem at location 'office-floor' as missing property 'not.provided'");

		// Missing properties
		this.compile(false, "provided", "tag available");
	}

	/**
	 * Ensure can compile if all tags provided.
	 */
	public void testTagsProvided() {

		// Record loading with tags provided
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");

		// Ensure can compile with tag values
		this.compile(true, "office.name", "OFFICE", "office.location", "office");
	}

}
