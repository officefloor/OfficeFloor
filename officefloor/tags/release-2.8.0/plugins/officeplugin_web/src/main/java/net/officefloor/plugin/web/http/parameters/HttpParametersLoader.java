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
package net.officefloor.plugin.web.http.parameters;

import java.io.IOException;
import java.util.Map;

import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.value.loader.ObjectInstantiator;

/**
 * <p>
 * Loader that loads the {@link HttpRequest} parameters onto an Object -
 * following bean pattern of <code>void setXxx(String value)</code> methods.
 * <p>
 * Due to the complications of attempting to validate and translate values, the
 * {@link HttpParametersLoader} will only load {@link String} values. Validation
 * of the values can then be done on the values contained in the loaded Object.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpParametersLoader<T> {

	/**
	 * Initialises this {@link HttpParametersLoader}.
	 * 
	 * @param type
	 *            Type of object to be loaded (and may be an interface). The
	 *            type is interrogated for
	 *            <code>public void setXxx(String value)</code> methods for
	 *            loading corresponding parameters. The property name of each
	 *            method is the method name stripped of the leading
	 *            <code>set</code>.
	 * @param aliasMappings
	 *            Alias mappings so {@link HttpRequest} parameter names need not
	 *            match exactly the Object method property names.
	 * @param isCaseSensitive
	 *            Flag indicating if matching on parameter names is to be case
	 *            sensitive. Specifying <code>false</code> results in matching
	 *            names ignoring case - which makes for more tolerable loading.
	 * @param objectInstantiator
	 *            {@link ObjectInstantiator}.
	 * @throws Exception
	 *             If fails to initialise.
	 */
	void init(Class<T> type, Map<String, String> aliasMappings,
			boolean isCaseSensitive, ObjectInstantiator objectInstantiator)
			throws Exception;

	/**
	 * Loads the parameters of the {@link HttpRequest} to the Object.
	 * 
	 * @param httpRequest
	 *            {@link HttpRequest} to extract the parameters.
	 * @param object
	 *            Object to be loaded with the parameters.
	 * @throws IOException
	 *             If fails to read data from the {@link HttpRequest}.
	 * @throws HttpParametersException
	 *             If fails to load the {@link HttpRequest} parameters to the
	 *             Object.
	 */
	<O extends T> void loadParameters(HttpRequest httpRequest, O object)
			throws IOException, HttpParametersException;

}