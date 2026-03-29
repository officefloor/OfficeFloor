package net.officefloor.tutorial.resthttpserver;

import jakarta.persistence.EntityManager;
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