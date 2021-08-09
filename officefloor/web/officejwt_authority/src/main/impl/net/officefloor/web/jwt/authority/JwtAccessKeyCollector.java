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

package net.officefloor.web.jwt.authority;

import net.officefloor.web.jwt.authority.repository.JwtAccessKey;
import net.officefloor.web.jwt.validate.JwtValidateKey;

/**
 * <p>
 * Collects {@link JwtAccessKey} instances for JWT validation.
 * <p>
 * It is expected that the {@link JwtAccessKey} instances (and their
 * corresponding {@link JwtValidateKey} instances) are rotated. This minimises
 * the impact of "leaked" keys (for whatever reason) from creating security
 * problems.
 * <p>
 * Furthermore, in a clustered environment, co-ordinating the creation of
 * {@link JwtAccessKey} instances can become complicated. It is, therefore,
 * possible to have multiple {@link JwtAccessKey} instances in play, with the
 * example following algorithm:
 * <ol>
 * <li>A collect of keys is triggered for a particular instance in the
 * cluster</li>
 * <li>The instance retrieves all {@link JwtAccessKey} instances from a central
 * store, and identifies a new {@link JwtAccessKey} is required.</li>
 * <li>The instance creates the {@link JwtAccessKey} and stores it in the
 * central store.</li>
 * <ul>
 * <li>Note: the active window for the {@link JwtAccessKey} should be in the
 * future. It should only be active after a time that all instances in the
 * cluster will have collected the new {@link JwtAccessKey} (and corresponding
 * {@link JwtValidateKey} instances).</li>
 * </ul>
 * </li>
 * <li>The instance then includes the {@link JwtAccessKey} in its encoding</li>
 * <li>Other instances in the cluster trigger a collect, and pull in the created
 * {@link JwtAccessKey} from the central store.</li>
 * <li>Should two instances in the cluster create a {@link JwtAccessKey}
 * simultaneously, then both {@link JwtAccessKey} instances can be arbitrarily
 * used. This is ok as all instances should load both corresponding
 * {@link JwtValidateKey} instances.
 * <ul>
 * <li>Note: this does come with the cost of extra computation on the consumers
 * to validate the JWT instances. However, this algorithm also works if the
 * cluster is co-ordinated to only create the one {@link JwtAccessKey} per time
 * period (reducing this computation).</li>
 * </ul>
 * </li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtAccessKeyCollector {

	/**
	 * Specifies the {@link JwtAccessKey} instances.
	 * 
	 * @param keys {@link JwtAccessKey} instances.
	 */
	void setKeys(JwtAccessKey[] keys);

}
