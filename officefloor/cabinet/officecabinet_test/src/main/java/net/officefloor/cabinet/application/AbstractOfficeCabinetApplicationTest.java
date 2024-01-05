package net.officefloor.cabinet.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.cabinet.AbstractOfficeCabinetTestCase;
import net.officefloor.cabinet.Cabinet;
import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.MCabinet;
import net.officefloor.cabinet.MStore;
import net.officefloor.cabinet.attributes.AttributeTypesDocument;
import net.officefloor.cabinet.source.CabinetOfficeExtensionService;
import net.officefloor.compile.test.officefloor.CompileOfficeExtension;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpObject;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Undertakes testing within an application.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeCabinetApplicationTest {

	/**
	 * Obtains the test case.
	 * 
	 * @return {@link AbstractOfficeCabinetTestCase}.
	 */
	protected abstract AbstractOfficeCabinetTestCase testcase();

	/**
	 * Ensure can store and retrieve values.
	 */
	@Test
	@MStore(cabinets = @MCabinet(AttributeTypesDocument.class))
	public void noCabinet() throws Exception {

		// Compile application
		CompileWoof compiler = new CompileWoof();
		compiler.web((web) -> web.link(false, "/", NoCabinetService.class));
		MockWoofServer server = compiler.open();

		// Ensure can obtain result
		MockWoofResponse response = server.send(MockWoofServer.mockRequest());
		response.assertJson(200, new DataObject("TEST"));
	}

	public static class NoCabinetService {
		public void service(ObjectResponse<DataObject> response) {
			response.send(new DataObject("TEST"));
		}
	}

	@HttpObject
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DataObject {
		private String test;
	}

	/**
	 * Ensure can store and retrieve values.
	 */
	@Test
	@MStore(cabinets = @MCabinet(MockEntity.class))
	public void cabinet() throws Exception {

		// Compile application
		CompileWoof compiler = new CompileWoof();
		compiler.web((web) -> {

			// Web servicing
			web.link(false, "POST", "/store", StoreService.class);
			web.link(false, "/retrieve/{key}", RetrieveService.class);
		});
		compiler.office(CompileOfficeExtension.of(new CabinetOfficeExtensionService()));
		MockWoofServer server = compiler.open();

		// Ensure can create entity
		final String TEST = "test";
		MockWoofResponse storeResponse = server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/store", new DataObject(TEST)));
		storeResponse.assertStatus(HttpStatus.OK);
		String key = storeResponse.getEntity(null);
		assertNotNull(key, "Should have key");
		assertTrue(key.trim().length() > 0, "Should have key value");

		// Ensure can retrieve entity
		MockWoofResponse retrieveResponse = server.send(MockWoofServer.mockRequest("/retrieve/" + key));
		retrieveResponse.assertJson(200, new DataObject(TEST));
	}

	public static class StoreService {
		public void service(DataObject request, MockDataStore dataStore, ServerHttpConnection connection)
				throws IOException {
			MockEntity entity = new MockEntity();
			entity.setTest(request.getTest());
			dataStore.store(entity);
			String key = entity.getKey();
			connection.getResponse().getEntityWriter().write(key);
		}
	}

	public static class RetrieveService {
		public void service(@HttpPathParameter("key") String key, MockDataStore dataStore,
				ObjectResponse<DataObject> response) {
			MockEntity entity = dataStore.retrieveByKey(key);
			String test = entity.getTest();
			response.send(new DataObject(test));
		}
	}

	@Cabinet
	public static interface MockDataStore {
		void store(MockEntity entity);

		MockEntity retrieveByKey(String key);
	}

	@Document
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class MockEntity {
		private @Key String key;
		private String test;
	}

}
