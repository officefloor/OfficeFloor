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
package net.officefloor.compile.integrate.officefloor;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
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
		this.issues.addIssue(LocationType.OFFICE_FLOOR, "office-floor",
				AssetType.OFFICE_FLOOR, "OfficeFloor",
				"Property 'missing.tag' must be specified");
		this.record_officeFloorBuilder_addOffice("OFFICE");

		// Missing properties only an issue
		this.compile(true);
	}

	/**
	 * Ensure only a single issue on missing tag repeated.
	 */
	public void testWarnOnceOnMissingTagRepeated() {

		// Record issue if tag not provided as property
		this.issues.addIssue(LocationType.OFFICE_FLOOR, "office-floor",
				AssetType.OFFICE_FLOOR, "OfficeFloor",
				"Property 'repeated' must be specified");
		this.record_officeFloorBuilder_addOffice("OFFICE");

		// Missing properties only an issue
		this.compile(true);
	}

	/**
	 * Ensure issue for each missing tag.
	 */
	public void testMultipleMissingTags() {

		// Record issue for each missing unique tag
		this.issues.addIssue(LocationType.OFFICE_FLOOR, "office-floor",
				AssetType.OFFICE_FLOOR, "OfficeFloor",
				"Property 'tag.one' must be specified");
		this.issues.addIssue(LocationType.OFFICE_FLOOR, "office-floor",
				AssetType.OFFICE_FLOOR, "OfficeFloor",
				"Property 'tag.two' must be specified");
		this.record_officeFloorBuilder_addOffice("OFFICE");

		// Missing properties only an issue
		this.compile(true);
	}

	/**
	 * Ensure issue if only some tags provided.
	 */
	public void testPartialSetOfTagsProvided() {

		// Record loading with tags provided
		this.issues.addIssue(LocationType.OFFICE_FLOOR, "office-floor",
				AssetType.OFFICE_FLOOR, "OfficeFloor",
				"Property 'not.provided' must be specified");
		this.record_officeFloorBuilder_addOffice("OFFICE");

		// Missing properties only an issue
		this.compile(true, "provided", "tag available");
	}

	/**
	 * Ensure can compile if all tags provided.
	 */
	public void testTagsProvided() {

		// Record loading with tags provided
		this.record_officeFloorBuilder_addOffice("OFFICE");

		// Ensure can compile with tag values
		this
				.compile(true, "office.name", "OFFICE", "office.location",
						"office");
	}

}