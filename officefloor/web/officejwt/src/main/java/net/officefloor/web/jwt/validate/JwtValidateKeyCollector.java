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
