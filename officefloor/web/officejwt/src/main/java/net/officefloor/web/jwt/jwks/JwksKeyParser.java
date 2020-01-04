/*-
 * #%L
 * JWT Security
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

package net.officefloor.web.jwt.jwks;

import java.security.Key;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Parses out the JWKS {@link Key} from the content.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwksKeyParser {

	/**
	 * Parses the {@link Key} from the JWK <code>key</code> {@link JsonNode}.
	 * 
	 * @param context {@link JwksKeyParserContext}.
	 * @return {@link Key} or <code>null</code> if not able to parse {@link Key}
	 *         (indicating for another {@link JwksKeyParser} to attempt to obtain
	 *         the {@link Key}).
	 * @throws Exception If failure parsing the {@link Key}.
	 */
	Key parseKey(JwksKeyParserContext context) throws Exception;

}
