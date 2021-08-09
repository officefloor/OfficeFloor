/*-
 * #%L
 * JWT Authority
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
