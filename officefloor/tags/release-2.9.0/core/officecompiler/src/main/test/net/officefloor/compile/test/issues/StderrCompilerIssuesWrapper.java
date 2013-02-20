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

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.internal.structure.Asset;

/**
 * Wrapper around a delegate {@link CompilerIssues} to report issues to
 * {@link System#err}.
 * 
 * @author Daniel Sagenschneider
 */
public class StderrCompilerIssuesWrapper implements CompilerIssues {

	/**
	 * Delegate {@link CompilerIssues}.
	 */
	private final CompilerIssues delegate;

	/**
	 * Initiate.
	 * 
	 * @param delegate
	 *            Delegate {@link CompilerIssues}.
	 */
	public StderrCompilerIssuesWrapper(CompilerIssues delegate) {
		this.delegate = delegate;
	}

	/*
	 * =================== CompilerIssues ==============================
	 */

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription) {

		// Report the issue
		this.reportIssue(locationType, location, assetType, assetName,
				issueDescription, null);

		// Delegate
		this.delegate.addIssue(locationType, location, assetType, assetName,
				issueDescription);
	}

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription,
			Throwable cause) {

		// Report the issue
		this.reportIssue(locationType, location, assetType, assetName,
				issueDescription, cause);

		// Delegate
		this.delegate.addIssue(locationType, location, assetType, assetName,
				issueDescription, cause);
	}

	/**
	 * Reports the issue to {@link System#err}.
	 * 
	 * @param locationType
	 *            {@link LocationType}.
	 * @param location
	 *            Location.
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of {@link Asset}.
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue. May be <code>null</code>.
	 */
	private void reportIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription,
			Throwable cause) {

		// Obtain the message to report
		StringBuilder msg = new StringBuilder();
		msg.append(locationType + ":" + location);
		if (assetType != null) {
			msg.append(" [" + assetType + ":" + assetName + "]");
		}
		msg.append("   " + issueDescription);
		if (cause != null) {
			StringWriter stackTrace = new StringWriter();
			cause.printStackTrace(new PrintWriter(stackTrace));
			msg.append("\n" + stackTrace.toString());
		}

		// Report the message
		System.err.println(msg.toString());
	}

}