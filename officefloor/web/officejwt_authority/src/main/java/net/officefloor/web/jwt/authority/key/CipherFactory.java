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
