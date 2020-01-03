package net.officefloor.web.json;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.HttpObjectParser;
import net.officefloor.web.build.HttpObjectParserFactory;
import net.officefloor.web.build.HttpObjectParserServiceFactory;
import net.officefloor.web.json.JacksonJsonTest.InputObject;

/**
 * Mock {@link HttpObjectParserServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpObjectParser implements HttpObjectParserServiceFactory, HttpObjectParserFactory {

	/*
	 * ===================== HttpObjectParserServiceFactory ===================
	 */

	@Override
	public HttpObjectParserFactory createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== HttpObjectParserFactory =======================
	 */

	@Override
	public String getContentType() {
		return "test/mock";
	}

	@Override
	public <T> HttpObjectParser<T> createHttpObjectParser(Class<T> objectClass) throws Exception {
		return new HttpObjectParser<T>() {

			@Override
			public String getContentType() {
				return MockHttpObjectParser.this.getContentType();
			}

			@Override
			public Class<T> getObjectType() {
				return objectClass;
			}

			@Override
			@SuppressWarnings("unchecked")
			public T parse(ServerHttpConnection connection) throws HttpException {
				return (T) new InputObject("INPUT");
			}
		};
	}

}