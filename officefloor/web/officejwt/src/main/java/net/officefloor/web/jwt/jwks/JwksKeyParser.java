/*-
 * #%L
 * JWT Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
