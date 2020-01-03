package net.officefloor.scalatest;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Provides access to {@link MockWoofServer} static methods.
 * 
 * @author Daniel Sagenschneider
 */
public class ScalaMockWoofServerStaticAccess {

	/**
	 * {@link ObjectMapper}.
	 */
	private static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.registerModule(new DefaultScalaModule());
	}

	/**
	 * Obtains {@link MockHttpRequestBuilder} for '/'.
	 * 
	 * @return {@link MockHttpRequestBuilder}.
	 */
	public MockHttpRequestBuilder mockRequest() {
		return MockWoofServer.mockRequest();
	}

	/**
	 * Obtains {@link MockHttpRequestBuilder}.
	 * 
	 * @param requestUri Request URI for {@link MockHttpRequestBuilder}.
	 * @return {@link MockHttpRequestBuilder}.
	 */
	public MockHttpRequestBuilder mockRequest(String requestUri) {
		return MockWoofServer.mockRequest(requestUri);
	}

	/**
	 * Obtains the {@link HttpMethod}.
	 * 
	 * @param methodName {@link HttpMethod} name.
	 * @return {@link HttpMethod}.
	 */
	public HttpMethod httpMethod(String methodName) {
		return HttpMethod.getHttpMethod(methodName);
	}

	/**
	 * Translate entity object to JSON.
	 * 
	 * @param entity Entity.
	 * @return JSON for entity.
	 * @throws IOException If fails to write object to JSON.
	 */
	public String jsonEntity(Object entity) throws IOException {
		return mapper.writeValueAsString(entity);
	}

}