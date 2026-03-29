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
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Initiate with reason.
	 * 
	 * @param reason Reason.
	 */
	public OfficeFloorBuildException(String reason) {
		super(reason);
	}

	/**
	 * Initiate.
	 * 
	 * @param reason Reason.
	 * @param cause  Cause.
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
	 * @param officeFloorBuilder {@link OfficeFloorBuilder}.
	 * @return {@link OfficeFloor}.
	 * @throws OfficeFloorBuildException {@link OfficeFloorBuildException} if
	 *                                   failure to construct {@link OfficeFloor}.
	 * 
	 * @see OfficeFrame
	 */
	public static final OfficeFloor buildOfficeFloor(OfficeFloorBuilder officeFloorBuilder)
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
		public void addIssue(AssetType assetType, String assetName, String issueDescription) {
			// Indicate construction issue
			this.indicateIssue(assetType + " " + assetName + ": " + issueDescription);
		}

		@Override
		public void addIssue(AssetType assetType, String assetName, String issueDescription, Throwable cause) {

			// Obtain the stack trace of the cause
			StringWriter stackTrace = new StringWriter();
			cause.printStackTrace(new PrintWriter(stackTrace));

			// Indicate construction issue
			this.addIssue(assetType, assetName, issueDescription + "\n\nCaused by: " + stackTrace);
		}

		/**
		 * Indicates issue in construction.
		 * 
		 * @param issueDescription Description of the issue.
		 * @throws ConstructError Propagate to be caught in register office floor.
		 */
		private void indicateIssue(String issueDescription) throws ConstructError {

			// Create the construction exception
			OfficeFloorBuildException cause = new OfficeFloorBuildException(issueDescription);

			// Propagate to be caught in register office floor
			throw new ConstructError(cause);
		}

	}

	/**
	 * Construct {@link Error} to propagate issue.
	 */
	private static class ConstructError extends Error {

		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Initiate.
		 * 
		 * @param cause Cause.
		 */
		public ConstructError(Throwable cause) {
			super(cause);
		}
	}

}
