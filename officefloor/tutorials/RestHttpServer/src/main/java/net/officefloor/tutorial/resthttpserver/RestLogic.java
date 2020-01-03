/*-
 * #%L
 * REST Tutorial
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

package net.officefloor.tutorial.resthttpserver;

import javax.persistence.EntityManager;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * REST logic.
 * 
 * @author Daniel Sagenschneider
 */
public class RestLogic {

	// START SNIPPET: post
	public void createVehicle(Vehicle vehicle, EntityManager entityManager) {
		if ((vehicle == null) || (vehicle.getVehicleType() == null)) {
			throw new HttpException(new HttpStatus(444, "Must have vehicleType"));
		}
		entityManager.persist(vehicle);
	}
	// END SNIPPET: post

	// START SNIPPET: get
	public void getVehicle(@HttpPathParameter("id") String vehicleId, EntityManager entityManager,
			ObjectResponse<Vehicle> responder) {
		responder.send(entityManager.find(Vehicle.class, Integer.parseInt(vehicleId)));
	}
	// END SNIPPET: get

}
