package net.officefloor.web;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.AbstractWebArchitectTest.RegisteredObject;
import net.officefloor.web.build.HttpObjectParser;
import net.officefloor.web.build.HttpObjectParserFactory;
import net.officefloor.web.build.HttpObjectParserServiceFactory;

/**
 * Mock {@link HttpObjectParserServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpObjectParserServiceFactory<O>
		implements HttpObjectParserServiceFactory, HttpObjectParserFactory, HttpObjectParser<O> {

	/*
	 * ==================== HttpObjectParserServiceFactory ==================
	 */

	@Override
	public HttpObjectParserFactory createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================= HttpObjectParserFactory ======================
	 */

	@Override
	public String getContentType() {
		return "registered/object";
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> HttpObjectParser<T> createHttpObjectParser(Class<T> objectClass) throws Exception {
		if (!RegisteredObject.class.equals(objectClass)) {
			return null; // not handled
		}
		return (HttpObjectParser<T>) this;
	}

	/*
	 * ======================= HttpObjectParserFactory ======================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public Class<O> getObjectType() {
		return (Class<O>) RegisteredObject.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public O parse(ServerHttpConnection connection) throws HttpException {
		try {
			Reader content = new InputStreamReader(connection.getClientRequest().getEntity());
			StringWriter buffer = new StringWriter();
			for (int character = content.read(); character != -1; character = content.read()) {
				buffer.write(character);
			}
			return (O) new RegisteredObject(buffer.toString());
		} catch (IOException ex) {
			throw new HttpException(ex);
		}
	}

}