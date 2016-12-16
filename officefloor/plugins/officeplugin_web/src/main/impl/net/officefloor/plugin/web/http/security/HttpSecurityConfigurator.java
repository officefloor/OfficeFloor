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
package net.officefloor.plugin.web.http.security;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;

/**
 * Provides means to configure the {@link HttpSecurity}
 * {@link ManagedObjectSource}, {@link WorkSource} and {@link SectionSource}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityConfigurator {

	/**
	 * {@link HttpSecurityConfiguration} instances by their key.
	 */
	private static Map<String, HttpSecurityConfiguration<?, ?, ?, ?>> configurations = new HashMap<String, HttpSecurityConfiguration<?, ?, ?, ?>>();

	/**
	 * Index for the next {@link HttpSecuritySource} key.
	 */
	private static int nextKeyIndex = 1;

	/**
	 * Registers the {@link HttpSecurityConfiguration}.
	 * 
	 * @param <S>
	 *            Security type.
	 * @param <C>
	 *            Credentials type.
	 * @param <D>
	 *            Dependency keys type.
	 * @param <F>
	 *            {@link Flow} keys type.
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource}.
	 * @param httpSecurityType
	 *            {@link HttpSecurityType}.
	 * @return Key to retrieve the {@link HttpSecuritySource}.
	 */
	public static synchronized <S, C, D extends Enum<D>, F extends Enum<F>> String registerHttpSecuritySource(
			HttpSecuritySource<S, C, D, F> httpSecuritySource,
			HttpSecurityType<S, C, D, F> httpSecurityType) {

		// Obtain the key
		String key = String.valueOf(nextKeyIndex++);

		// Register the source
		configurations.put(key, new HttpSecurityConfiguration<S, C, D, F>(
				httpSecuritySource, httpSecurityType));

		// Return the key
		return key;
	}

	/**
	 * Obtains the registered {@link HttpSecurityConfiguration}.
	 * 
	 * @param key
	 *            Key identifying the registered
	 *            {@link HttpSecurityConfiguration}.
	 * @return Registered {@link HttpSecurityConfiguration}. May be
	 *         <code>null</code> if unknown key.
	 */
	public static synchronized HttpSecurityConfiguration<?, ?, ?, ?> getHttpSecuritySource(
			String key) {
		return configurations.get(key);
	}

	/**
	 * All access via static methods.
	 */
	private HttpSecurityConfigurator() {
	}

}