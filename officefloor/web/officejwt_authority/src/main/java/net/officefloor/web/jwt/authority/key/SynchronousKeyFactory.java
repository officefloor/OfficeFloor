/*-
 * #%L
 * JWT Authority
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.jwt.authority.key;

import java.security.Key;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Factory for synchronous {@link Key}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface SynchronousKeyFactory {

	/**
	 * Allows for the {@link SynchronousKeyFactory} to be configured.
	 * 
	 * @param context {@link SourceContext}.
	 */
	default void init(SourceContext context) {
		// No configuration by default
	}

	/**
	 * Creates a synchronous {@link Key}.
	 * 
	 * @return Synchronous {@link Key}.
	 * @throws Exception If fails to create synchronous {@link Key}.
	 */
	Key createSynchronousKey() throws Exception;

}
