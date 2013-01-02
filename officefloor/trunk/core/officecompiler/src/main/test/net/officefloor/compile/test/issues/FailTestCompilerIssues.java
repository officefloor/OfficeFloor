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
package net.officefloor.compile.test.issues;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;

/**
 * {@link CompilerIssues} that invokes {@link TestCase#fail()} for issues
 * raised.
 * 
 * @author Daniel Sagenschneider
 */
public class FailTestCompilerIssues implements CompilerIssues {

	/*
	 * =================== CompilerIssues =================================
	 */

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription) {
		// Fail because of the issue
		TestCase.fail(issueDescription + " [" + locationType + ":" + location
				+ ", " + assetType + ":" + assetName + "]");
	}

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription,
			Throwable cause) {

		// Obtain the stack trace
		StringWriter stackTrace = new StringWriter();
		cause.printStackTrace(new PrintWriter(stackTrace));

		// Fail because of the issue
		TestCase.fail(issueDescription + " [" + locationType + ":" + location
				+ ", " + assetType + ":" + assetName + "]\n"
				+ stackTrace.toString());
	}

}