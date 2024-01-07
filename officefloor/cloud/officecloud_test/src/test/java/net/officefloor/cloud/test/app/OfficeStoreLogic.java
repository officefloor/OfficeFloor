package net.officefloor.cloud.test.app;

import java.io.IOException;

import net.officefloor.cabinet.spi.OfficeStore;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * Logic for using {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeStoreLogic {

	public void store(MockDocument document, MockRepository repository, ServerHttpConnection connection)
			throws IOException {
		repository.store(document);
		connection.getResponse().getEntityWriter().write(document.getKey());
	}

	public void retrieve(@HttpPathParameter("key") String key, MockRepository repository,
			ObjectResponse<MockDocument> response) {
		MockDocument document = repository.getMockDocumentByKey(key);
		response.send(document);
	}
}
