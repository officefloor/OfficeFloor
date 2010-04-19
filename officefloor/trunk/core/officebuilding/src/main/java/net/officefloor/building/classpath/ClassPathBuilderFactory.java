/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.building.classpath;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Factory for the creation of a {@link ClassPathBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassPathBuilderFactory {

	/**
	 * Local repository directory.
	 */
	private final File localRepositoryDirectory;

	/**
	 * Listing of the remote repository URLs.
	 */
	private List<String> remoteRepositoryUrls = new LinkedList<String>();

	/**
	 * Initiate.
	 * 
	 * @param localRepositoryDirectory
	 *            Local repository directory.
	 */
	public ClassPathBuilderFactory(File localRepositoryDirectory) {
		this.localRepositoryDirectory = localRepositoryDirectory;
	}

	/**
	 * Adds a remote repository URL.
	 * 
	 * @param remoteRepositoryUrl
	 *            Remote repository URL.
	 */
	public void addRemoteRepositoryUrl(String remoteRepositoryUrl) {
		this.remoteRepositoryUrls.add(remoteRepositoryUrl);
	}

	/**
	 * Creates a new {@link ClassPathBuilder}.
	 * 
	 * @return New {@link ClassPathBuilder}.
	 * @throws Exception
	 *             If fails to create a {@link ClassPathBuilder}.
	 */
	public ClassPathBuilder createClassPathBuilder() throws Exception {
		return new ClassPathBuilder(this.localRepositoryDirectory,
				this.remoteRepositoryUrls.toArray(new String[0]));
	}

}