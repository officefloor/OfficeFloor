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

import java.security.Key;

import javax.crypto.KeyGenerator;

/**
 * AES {@link SynchronousKeyFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class AesSynchronousKeyFactory implements SynchronousKeyFactory {

	@Override
	public Key createSynchronousKey() throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(256);
		return kgen.generateKey();
	}

}
