package net.officefloor.tutorial.resthttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import net.officefloor.jdbc.h2.test.H2Reset;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockObjectResponse;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the REST end points.
 * 
 * @author Daniel Sagenschneider
 */
public class RestHttpServerTest {

	@BeforeEach
	public void reset(H2Reset reset) {
		reset.reset();
	}

	// START SNIPPET: calling
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void postMissingData() throws Exception {

		// POST with missing data
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest("/vehicle").method(HttpMethod.POST).entity("{}"));
		response.assertResponse(444, "{\"error\":\"Must have vehicleType\"}");
	}

	@Test
	public void postEntry(EntityManager entityManager) throws Exception {

		// POST to create row and validate successful
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/vehicle").method(HttpMethod.POST)
				.header("content-type", "application/json").entity("{ \"vehicleType\": \"bike\", \"wheels\": 2 }"));
		response.assertResponse(204, "");

		// Ensure row created
		Vehicle vehicle = entityManager.createQuery("SELECT V FROM Vehicle V WHERE vehicleType = 'bike'", Vehicle.class)
				.getSingleResult();
		assertNotNull(vehicle, "Should have row created");
		assertEquals(2, vehicle.getWheels().intValue(), "Incorrect row");
	}

	@Test
	public void getEntry(EntityManager entityManager) throws Exception {

		// Create entry
		Vehicle vehicle = new Vehicle("car", 4);
		entityManager.persist(vehicle);
		JpaManagedObjectSource.commitTransaction(entityManager);

		// GET entry
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/vehicle/" + vehicle.getId()));
		assertEquals(200, response.getStatus().getStatusCode(), "Should be successful");
		response.assertHeader("content-type", "application/json");
		JsonNode entity = new ObjectMapper().readTree(response.getEntity(null));
		assertEquals(vehicle.getId().intValue(), entity.get("id").asInt(), "Incorrect id");
		assertEquals("car", entity.get("vehicleType").asText(), "Incorrect vehicle type");
		assertEquals(4, entity.get("wheels").asInt(), "Incorrect wheels");
	}
	// END SNIPPET: calling

	// START SNIPPET: pojo
	@Test
	public void createWithMissingData(EntityManager entityManager) {
		try {
			new RestLogic().createVehicle(new Vehicle(), entityManager);
			fail("Should not be successful");
		} catch (HttpException ex) {
			assertEquals(444, ex.getHttpStatus().getStatusCode(), "Incorrect status");
			assertEquals("Must have vehicleType", ex.getMessage(), "Incorrect reason");
		}
	}

	@Test
	public void createVehicle(EntityManager entityManager) {

		// Create the vehicle
		new RestLogic().createVehicle(new Vehicle("tricycle", 3), entityManager);

		// Ensure row created
		Vehicle vehicle = entityManager
				.createQuery("SELECT V FROM Vehicle V WHERE vehicleType = 'tricycle'", Vehicle.class).getSingleResult();
		assertNotNull(vehicle, "Should have row created");
		assertEquals(3, vehicle.getWheels().intValue(), "Incorrect row");
	}

	@Test
	public void getVehicle(EntityManager entityManager) {

		// Create entry
		Vehicle created = new Vehicle("unicycle", 1);
		entityManager.persist(created);

		// Obtain the vehicle
		MockObjectResponse<Vehicle> response = new MockObjectResponse<>();
		new RestLogic().getVehicle(String.valueOf(created.getId()), entityManager, response);
		Vehicle vehicle = response.getObject();
		assertEquals("unicycle", vehicle.getVehicleType(), "Incorrect row sent");
	}
	// END SNIPPET: pojo

}