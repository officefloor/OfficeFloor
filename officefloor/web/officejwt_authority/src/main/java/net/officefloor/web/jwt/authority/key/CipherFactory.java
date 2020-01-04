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

import javax.crypto.Cipher;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Factory for {@link Cipher}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface CipherFactory {

	/**
	 * Allows for the {@link CipherFactory} to be configured.
	 * 
	 * @param context {@link SourceContext}.
	 */
	default void init(SourceContext context) {
		// No configuration by default
	}

	/**
	 * Allows overriding the init vector size.
	 * 
	 * @return Init vector size.
	 */
	default int getInitVectorSize() {
		return 16;
	}

	/**
	 * Creates a {@link Cipher}.
	 * 
	 * @return {@link Cipher}.
	 * @throws Exception If fails to create {@link Cipher}.
	 */
	Cipher createCipher() throws Exception;

}
