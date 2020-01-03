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

package net.officefloor.web.jwt.authority.jwks;

import java.security.Key;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Creates the JWKS <code>key</code> {@link JsonNode} from the {@link Key}.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwksKeyWriter<K extends Key> {

	/**
	 * Indicates if able to write the {@link Key}.
	 * 
	 * @param key {@link Key}.
	 * @return <code>true</code> if able to write the {@link Key}.
	 */
	boolean canWriteKey(Key key);

	/**
	 * Writes the {@link Key} as {@link ObjectNode}.
	 * 
	 * @param context {@link JwksKeyWriterContext}.
	 * @throws Exception If fails to write the {@link Key}.
	 */
	void writeKey(JwksKeyWriterContext<K> context) throws Exception;

}
