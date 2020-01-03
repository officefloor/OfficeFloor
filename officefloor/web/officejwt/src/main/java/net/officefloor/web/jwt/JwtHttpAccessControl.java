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

package net.officefloor.web.jwt;

import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Extra JWT functions to {@link HttpAccessControl}.
 * 
 * @author Daniel Sagenschneider
 */
public interface JwtHttpAccessControl<C> extends HttpAccessControl {

	/**
	 * <p>
	 * Obtains the JWT claims.
	 * <p>
	 * While this is available, it does reduce the re-usability of the
	 * {@link HttpAccessControl} (and resulting infrastructure built on it).
	 * Ideally, this should not be used with {@link #inRole(String)} being
	 * preferred.
	 * <p>
	 * However, if direct access to the claim is required, it should just be
	 * depended on as a custom access control object. For example, a
	 * {@link ManagedObjectSource} can be created to depend on the claims object and
	 * data store. This {@link ManagedObjectSource} can retrieve the user entry for
	 * the JWT claims from the data store and make available for dependency
	 * injection. This provides a re-usable application centric
	 * {@link ManagedObjectSource} that does not depend on (possibly changing)
	 * OfficeFloor enforced "standard" {@link HttpSecurity} interfaces.
	 * 
	 * @return JWT claims.
	 */
	C getClaims();

}
