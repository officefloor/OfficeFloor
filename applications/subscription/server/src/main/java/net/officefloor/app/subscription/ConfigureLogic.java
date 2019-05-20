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
package net.officefloor.app.subscription;

import java.util.Arrays;
import java.util.Iterator;

import com.googlecode.objectify.Objectify;

import lombok.Value;
import net.officefloor.app.subscription.store.Administration;
import net.officefloor.app.subscription.store.User;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;

/**
 * Enables configuring the application.
 * 
 * @author Daniel Sagenschneider
 */
public class ConfigureLogic {

	public static final String ROLE_ADMIN = "admin";

	@Value
	@HttpObject
	public static class Configuration {
		private String paypalEnvironment;
		private String paypalClientId;
		private String paypalClientSecret;
	}

	@Value
	public static class Configured {
		private boolean successful;
	}

	public static void configure(User user, Configuration configuration, Objectify objectify,
			ObjectResponse<Configured> response) {

		// Obtain the administration
		Administration administration = null;
		Iterator<Administration> iterator = objectify.load().type(Administration.class).iterator();
		if (iterator.hasNext()) {
			administration = iterator.next();
		}
		iterator.forEachRemaining((extra) -> objectify.delete().entity(extra).now());

		// Determine if configuring for first time
		if (administration == null) {
			// No configuration, so admin

			// Obtain the user to flag as administrator
			User adminUser = objectify.load().type(User.class).id(user.getId()).now();
			adminUser.setRoles(new String[] { ROLE_ADMIN });

			// Create the administration
			administration = new Administration(configuration.getPaypalEnvironment(), configuration.getPaypalClientId(),
					configuration.getPaypalClientSecret());

			// Store the details
			objectify.save().entities(adminUser, administration).now();

		} else if (Arrays.asList(user.getRoles()).contains(ROLE_ADMIN)) {
			// Admin, so allow updating configuration
			administration.setPaypalEnvironment(configuration.getPaypalEnvironment());
			administration.setPaypalClientId(configuration.getPaypalClientId());
			administration.setPaypalClientSecret(configuration.getPaypalClientSecret());
			objectify.save().entity(administration).now();

		} else {
			// Disallowed to change
			throw new HttpException(HttpStatus.FORBIDDEN, "Must have '" + ROLE_ADMIN + "' role");
		}

		// Successfully configured
		response.send(new Configured(true));
	}

}