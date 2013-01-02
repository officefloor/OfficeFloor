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
package net.officefloor.building.command.parameters;

import java.util.Properties;

import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.command.RemoteRepositoryUrlsOfficeFloorCommandParameter;

/**
 * {@link OfficeFloorCommandParameter} for the remote repository URLs.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoteRepositoryUrlsOfficeFloorCommandParameterImpl extends
		AbstractMultiplePathsOfficeFloorCommandParameter implements
		RemoteRepositoryUrlsOfficeFloorCommandParameter {

	/**
	 * Default remote repository URLs.
	 */
	public static final String[] DEFAULT_REMOTE_REPOSITORY_URLS = new String[] {
			"http://www.officefloor.net/maven2",
			"http://repo1.maven.org/maven2/" };

	/**
	 * Separate URLs with a comma.
	 */
	private static final String URL_SEPARATOR = ",";

	/**
	 * Obtains the remote repository URLs from the environment
	 * {@link Properties}.
	 * 
	 * @param environment
	 *            Environment {@link Properties}.
	 * @return Remote repository URLs.
	 */
	public static String[] getRemoteRepositoryUrls(Properties environment) {
		return transformValueToPaths(environment
				.getProperty(PARAMETER_REMOTE_REPOSITORY_URLS), URL_SEPARATOR);
	}

	/**
	 * Transforms the remote repository URLs to a parameter value.
	 * 
	 * @param remoteRepositoryUrls
	 *            Remote repository URLs.
	 * @return Parameter value for the remote repository URLs.
	 */
	public static String transformForParameterValue(
			String[] remoteRepositoryUrls) {
		return transformPathsToValue(remoteRepositoryUrls, URL_SEPARATOR);
	}

	/**
	 * Initiate.
	 */
	public RemoteRepositoryUrlsOfficeFloorCommandParameterImpl() {
		super(PARAMETER_REMOTE_REPOSITORY_URLS, "rr", URL_SEPARATOR,
				"Remote repository URL to retrieve Artifacts");
	}

	/*
	 * ======== RemoteRepositoryUrlsOfficeFloorCommandParameter ======
	 */

	@Override
	public String[] getRemoteRepositoryUrls() {

		// Obtain URLs
		String[] urls = this.getPaths();
		if (urls.length == 0) {
			// Default URLs
			urls = DEFAULT_REMOTE_REPOSITORY_URLS;
		}

		// Return the URLs
		return urls;
	}

}