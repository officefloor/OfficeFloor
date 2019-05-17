/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.app.subscription.user;

import com.googlecode.objectify.Objectify;

import net.officefloor.app.subscription.jwt.JwtClaims;
import net.officefloor.app.subscription.store.User;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.server.http.HttpException;

/**
 * {@link ManagedObjectSource} to provide the logged in {@link User}.
 * 
 * @author Daniel Sagenschneider
 */
public class UserManagedObjectSource extends AbstractManagedObjectSource<UserManagedObjectSource.DEPENDENCIES, None> {

	public static enum DEPENDENCIES {
		JWT_CLAIMS, OBJECTIFY
	}

	/*
	 * ================== ManagedObjectSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<DEPENDENCIES, None> context) throws Exception {
		context.setObjectClass(User.class);
		context.setManagedObjectClass(UserManagedObject.class);
		context.addDependency(DEPENDENCIES.JWT_CLAIMS, JwtClaims.class);
		context.addDependency(DEPENDENCIES.OBJECTIFY, Objectify.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new UserManagedObject();
	}

	/**
	 * {@link User} {@link ManagedObject}.
	 */
	private static class UserManagedObject implements CoordinatingManagedObject<DEPENDENCIES> {

		private User user;

		@Override
		public void loadObjects(ObjectRegistry<DEPENDENCIES> registry) throws Throwable {

			// Obtain dependencies
			JwtClaims claims = (JwtClaims) registry.getObject(DEPENDENCIES.JWT_CLAIMS);
			Objectify objectify = (Objectify) registry.getObject(DEPENDENCIES.OBJECTIFY);

			// Obtain the user
			this.user = objectify.load().type(User.class).id(claims.getUserId()).now();
			if (this.user == null) {
				throw new HttpException(401, "Unknown user. Require login to create user.");
			}
		}

		@Override
		public Object getObject() throws Throwable {
			return this.user;
		}
	}

}