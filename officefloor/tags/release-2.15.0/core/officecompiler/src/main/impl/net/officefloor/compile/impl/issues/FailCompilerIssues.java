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
package net.officefloor.compile.impl.issues;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.Asset;

/**
 * {@link CompilerIssues} that fails on an issue.
 * 
 * @author Daniel Sagenschneider
 */
public class FailCompilerIssues extends Exception implements CompilerIssues {

	/**
	 * Compiles with exception of first issue.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param officeFloorLocation
	 *            {@link OfficeFloor} location.
	 * @return {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to compile.
	 */
	public static OfficeFloor compile(OfficeFloorCompiler compiler,
			String officeFloorLocation) throws Exception {

		// Override the compiler issues
		compiler.setCompilerIssues(new FailCompilerIssues(null, null, null,
				null));

		// Compile
		try {
			return compiler.compile(officeFloorLocation);
		} catch (CompileError propagate) {
			throw (Exception) propagate.getCause();
		}
	}

	/**
	 * Only initialise via compiling.
	 * 
	 * @param assetType
	 *            {@link AssetType}
	 * @param assetName
	 *            {@link Asset} name.
	 * @param issueDescription
	 *            Issue description.
	 * @param cause
	 *            Cause. May be <code>null</code>.
	 */
	private FailCompilerIssues(AssetType assetType, String assetName,
			String issueDescription, Throwable cause) {
		super(assetType + " " + assetName + ": " + issueDescription, cause);
	}

	/*
	 * ========================= CompilerIssues =======================
	 */

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription) {
		throw new CompileError(new FailCompilerIssues(assetType, assetName,
				issueDescription, null));
	}

	@Override
	public void addIssue(LocationType locationType, String location,
			AssetType assetType, String assetName, String issueDescription,
			Throwable cause) {

		// Ensure if issue due to existing issue
		if (cause instanceof CompileError) {
			throw (CompileError) cause;
		}

		// Propagate compiler error
		throw new CompileError(new FailCompilerIssues(assetType, assetName,
				issueDescription, cause));
	}

	/**
	 * Compile {@link Error} to propagate issue.
	 */
	private static class CompileError extends Error {

		/**
		 * Initiate.
		 * 
		 * @param cause
		 *            Cause.
		 */
		public CompileError(Throwable cause) {
			super(cause);
		}
	}

}