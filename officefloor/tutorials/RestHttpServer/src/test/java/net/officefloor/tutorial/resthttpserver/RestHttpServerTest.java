package net.officefloor.tutorial.resthttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.jdbc.test.DataSourceRule;
import net.officefloor.jpa.hibernate.HibernateJpaManagedObjectSource;
import net.officefloor.jpa.test.EntityManagerRule;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockObjectResponse;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the REST end points.
 * 
 * @author Daniel Sagenschneider
 */
public class RestHttpServerTest {

	// START SNIPPET: calling
	private final DataSourceRule dataSource = new DataSourceRule("datasource.properties");

	private final EntityManagerRule entityManager = new EntityManagerRule("entitymanager.properties",
			new HibernateJpaManagedObjectSource(), dataSource);

	private final MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public final RuleChain ordered = RuleChain.outerRule(this.dataSource).around(this.entityManager)
			.around(this.server);

	@Before
	public void cleanDatabase() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			new Setup().setup(connection);
			connection.createStatement().executeUpdate("TRUNCATE TABLE VEHICLE");
		}
	}

	@Test
	public void postMissingData() throws Exception {

		// POST with missing data
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest("/vehicle").method(HttpMethod.POST).entity("{}"));
		response.assertResponse(444, "{\"error\":\"Must have vehicleType\"}");
	}

	@Test
	public void postEntry() throws Exception {

		// POST to create row and validate successful
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/vehicle").method(HttpMethod.POST)
				.header("content-type", "application/json").entity("{ \"vehicleType\": \"bike\", \"wheels\": 2 }"));
		response.assertResponse(204, "");

		// Ensure row created
		Vehicle vehicle = this.entityManager.getEntityManager()
				.createQuery("SELECT V FROM Vehicle V WHERE vehicleType = 'bike'", Vehicle.class).getSingleResult();
		assertNotNull("Should have row created", vehicle);
		assertEquals("Incorrect row", 2, vehicle.getWheels().intValue());
	}

	@Test
	public void getEntry() throws Exception {

		// Create entry
		Vehicle vehicle = new Vehicle("car", 4);
		this.entityManager.getEntityManager().persist(vehicle);
		this.entityManager.getEntityManager().getTransaction().commit();

		// GET entry
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/vehicle/" + vehicle.getId()));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		response.assertHeader("content-type", "application/json");
		JsonNode entity = new ObjectMapper().readTree(response.getEntity(null));
		assertEquals("Incorrect id", vehicle.getId().intValue(), entity.get("id").asInt());
		assertEquals("Incorrect vehicle type", "car", entity.get("vehicleType").asText());
		assertEquals("Incorrect wheels", 4, entity.get("wheels").asInt());
	}
	// END SNIPPET: calling

	// START SNIPPET: pojo
	@Test
	public void createWithMissingData() {
		try {
			new RestLogic().createVehicle(new Vehicle(), this.entityManager.getEntityManager());
			fail("Should not be successful");
		} catch (HttpException ex) {
			assertEquals("Incorrect status", 444, ex.getHttpStatus().getStatusCode());
			assertEquals("Incorrect reason", "Must have vehicleType", ex.getMessage());
		}
	}

	@Test
	public void createVehicle() {

		// Create the vehicle
		new RestLogic().createVehicle(new Vehicle("tricycle", 3), this.entityManager.getEntityManager());

		// Ensure row created
		Vehicle vehicle = this.entityManager.getEntityManager()
				.createQuery("SELECT V FROM Vehicle V WHERE vehicleType = 'tricycle'", Vehicle.class).getSingleResult();
		assertNotNull("Should have row created", vehicle);
		assertEquals("Incorrect row", 3, vehicle.getWheels().intValue());
	}

	@Test
	public void getVehicle() {

		// Create entry
		Vehicle created = new Vehicle("unicycle", 1);
		this.entityManager.getEntityManager().persist(created);

		// Obtain the vehicle
		MockObjectResponse<Vehicle> response = new MockObjectResponse<>();
		new RestLogic().getVehicle(String.valueOf(created.getId()), this.entityManager.getEntityManager(), response);
		Vehicle vehicle = response.getObject();
		assertEquals("Incorrect row sent", "unicycle", vehicle.getVehicleType());
	}
	// END SNIPPET: pojo

}