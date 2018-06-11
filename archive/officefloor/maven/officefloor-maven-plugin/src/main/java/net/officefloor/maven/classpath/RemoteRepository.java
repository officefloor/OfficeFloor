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
package net.officefloor.maven.classpath;

/**
 * Remote repository for the {@link ClassPathFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoteRepository {

	/**
	 * Id of the remote repository.
	 */
	private final String id;

	/**
	 * Type of remote repository.
	 */
	private final String type;

	/**
	 * URL of the remote repository.
	 */
	private final String url;

	/**
	 * Initiate.
	 * 
	 * @param id
	 *            Id of the remote repository.
	 * @param type
	 *            Type of the remote repository.
	 * @param url
	 *            URL of the remote repository.
	 */
	public RemoteRepository(String id, String type, String url) {
		this.id = (id == null ? "repo" : id);
		this.type = (type == null ? "default" : type);
		this.url = url;
	}

	/**
	 * Initiate.
	 * 
	 * @param url
	 *            URL to the remote repository.
	 */
	public RemoteRepository(String url) {
		this(null, null, url);
	}

	/**
	 * Obtains the Id of the remote repository.
	 * 
	 * @return Id of the remote repository.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Obtains the type of the remote repository.
	 * 
	 * @return Type of the remote repository.
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Obtains the URL of the remote repository.
	 * 
	 * @return URL of the remote repository.
	 */
	public String getUrl() {
		return this.url;
	}

}