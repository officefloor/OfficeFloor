/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.frame.api.build;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Indicates failure to build a {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorBuildException extends Exception {

	/**
	 * Initiate with reason.
	 * 
	 * @param reason
	 *            Reason.
	 */
	public OfficeFloorBuildException(String reason) {
		super(reason);
	}

	/**
	 * Initiate.
	 * 
	 * @param reason
	 *            Reason.
	 * @param cause
	 *            Cause.
	 */
	public OfficeFloorBuildException(String reason, Throwable cause) {
		super(reason, cause);
	}

	/**
	 * <p>
	 * Provides the necessary functionality to propagate the
	 * {@link OfficeFloorBuildException} on the first issue in constructing the
	 * {@link OfficeFloor}.
	 * <p>
	 * This is a convenience method for
	 * {@link OfficeFloorBuilder#buildOfficeFloor()}.
	 * 
	 * @param officeFloorBuilder
	 *            {@link OfficeFloorBuilder}.
	 * @return {@link OfficeFloor}.
	 * @throws OfficeFloorBuildException
	 *             {@link OfficeFloorBuildException} if failure to construct
	 *             {@link OfficeFloor}.
	 * 
	 * @see OfficeFrame
	 */
	public static final OfficeFloor buildOfficeFloor(
			OfficeFloorBuilder officeFloorBuilder)
			throws OfficeFloorBuildException {
		try {
			// Attempt to build and return the Office Floor
			return officeFloorBuilder.buildOfficeFloor(new FailOnFirstIssue());

		} catch (ConstructError issue) {
			// Propagate the issue
			throw (OfficeFloorBuildException) issue.getCause();
		}
	}

	/**
	 * {@link OfficeFloorIssues} that fails on the first issue.
	 */
	private static class FailOnFirstIssue implements OfficeFloorIssues {

		@Override
		public void addIssue(AssetType assetType, String assetName,
				String issueDescription) {
			// Indicate construction issue
			this.indicateIssue(assetType + " " + assetName + ": "
					+ issueDescription);
		}

		@Override
		public void addIssue(AssetType assetType, String assetName,
				String issueDescription, Throwable cause) {

			// Obtain the stack trace of the cause
			StringWriter stackTrace = new StringWriter();
			cause.printStackTrace(new PrintWriter(stackTrace));

			// Indicate construction issue
			this.addIssue(assetType, assetName, issueDescription
					+ "\n\nCaused by: " + stackTrace);
		}

		/**
		 * Indicates issue in construction.
		 * 
		 * @param issueDescription
		 *            Description of the issue.
		 * @throws ConstructError
		 *             Propagate to be caught in register office floor.
		 */
		private void indicateIssue(String issueDescription)
				throws ConstructError {

			// Create the construction exception
			OfficeFloorBuildException cause = new OfficeFloorBuildException(
					issueDescription);

			// Propagate to be caught in register office floor
			throw new ConstructError(cause);
		}

	}

	/**
	 * Construct {@link Error} to propagate issue.
	 */
	private static class ConstructError extends Error {

		/**
		 * Initiate.
		 * 
		 * @param cause
		 *            Cause.
		 */
		public ConstructError(Throwable cause) {
			super(cause);
		}
	}

}
