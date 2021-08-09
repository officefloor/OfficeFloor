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

package net.officefloor.web.jwt.validate;

import java.util.concurrent.TimeUnit;

/**
 * Collects {@link JwtValidateKey} instances for JWT validation.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtValidateKeyCollector {

	/**
	 * Obtains the current {@link JwtValidateKey} instances.
	 * 
	 * @return Current {@link JwtValidateKey} instances.
	 */
	JwtValidateKey[] getCurrentKeys();

	/**
	 * Specifies the {@link JwtValidateKey} instances.
	 * 
	 * @param keys {@link JwtValidateKey} instances.
	 */
	void setKeys(JwtValidateKey... keys);

	/**
	 * Indicates failure in retrieving the {@link JwtValidateKey} instances.
	 * 
	 * @param cause           Cause of the failure.
	 * @param timeToNextCheck Allows overriding the default poll refresh interval.
	 *                        This typically allows retrying earlier than the
	 *                        default refresh period.
	 * @param unit            {@link TimeUnit} for the next time to check.
	 */
	void setFailure(Throwable cause, long timeToNextCheck, TimeUnit unit);

}
