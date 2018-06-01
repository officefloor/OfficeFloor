/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.web;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.build.HttpInput;
import net.officefloor.web.build.HttpPathFactory;

/**
 * Provides path details for the {@link HttpInput}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpInputPath {

	/**
	 * Indicates if the path matches the {@link HttpInput} path.
	 * 
	 * @param path
	 *            Path.
	 * @param endingPathParameterTerminatingCharacter
	 *            {@link Character} value for the {@link Character} that terminates
	 *            the ending path parameter. This is ignored if the last part of the
	 *            path is static (i.e. only applies for last parameter to know when
	 *            it terminates the path, e.g. <code>/path/{last}</code>). Should
	 *            the last parameter consume the remainder of the path, provide
	 *            <code>-1</code> to indicate no terminating {@link Character}.
	 * @return <code>true</code> if the path matches the {@link HttpInput} path.
	 */
	boolean isMatchPath(String path, int endingPathParameterTerminatingCharacter);

	/**
	 * Indicates if the path contains parameters (e.g. <code>/{param}</code>).
	 * 
	 * @return <code>true</code> if the path contains parameters.
	 */
	boolean isPathParameters();

	/**
	 * Creates the {@link HttpPathFactory}.
	 * 
	 * @param <T>
	 *            Value type.
	 * @param valuesType
	 *            Type to use for obtaining values to construct the path. Should the
	 *            path not contain parameters, it may be <code>null</code>.
	 * @return {@link HttpPathFactory}.
	 * @throws HttpException
	 *             If required path parameters are not available on the values type.
	 */
	<T> HttpPathFactory<T> createHttpPathFactory(Class<T> valuesType) throws HttpException;

}