/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.compile.impl.issues;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;

/**
 * {@link CompilerIssues} to write issues to {@link System#err}.
 * 
 * @author Daniel Sagenschneider
 */
public class StderrCompilerIssues implements CompilerIssues {

	/*
	 * ================= CompilerIssues ==================================
	 */

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription) {
		this.addIssue(locationType, location, assetType, assetName,
				issueDescription, null);
	}

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription,
			Throwable cause) {

		// Obtain the stack trace
		String stackTrace = "";
		if (cause != null) {
			StringWriter buffer = new StringWriter();
			cause.printStackTrace(new PrintWriter(buffer));
			stackTrace = "\n" + buffer.toString();
		}

		// Obtain the asset details
		String assetDetails = "";
		if (assetType != null) {
			assetDetails = ", " + assetType + "=" + assetName;
		}

		// Output details of issue
		System.err.println("ERROR: " + issueDescription + " [" + locationType
				+ "=" + location + assetDetails + "]" + stackTrace);
	}

}