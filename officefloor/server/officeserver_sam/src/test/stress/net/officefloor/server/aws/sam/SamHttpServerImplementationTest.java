package net.officefloor.server.aws.sam;

import org.eclipse.jetty.server.Server;

import net.officefloor.server.http.AbstractHttpServerImplementationTest;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerLocation;

/**
 * Tests the {@link SamHttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class SamHttpServerImplementationTest extends AbstractHttpServerImplementationTest<Server> {

	/*
	 * =================== HttpServerImplementationTest =====================
	 */

	@Override
	protected Class<? extends HttpServerImplementation> getHttpServerImplementationClass() {
		return SamHttpServerImplementation.class;
	}

	@Override
	protected Server startRawHttpServer(HttpServerLocation serverLocation) throws Exception {
		// TODO implement AbstractHttpServerImplementationTest<Object>.stopRawHttpServer
		throw new UnsupportedOperationException(
				"TODO implement AbstractHttpServerImplementationTest<Object>.stopRawHttpServer");
	}

	@Override
	protected void stopRawHttpServer(Server momento) throws Exception {
		// TODO implement AbstractHttpServerImplementationTest<Object>.stopRawHttpServer
		throw new UnsupportedOperationException(
				"TODO implement AbstractHttpServerImplementationTest<Object>.stopRawHttpServer");
	}

	@Override
	protected HttpHeader[] getServerResponseHeaderValues() {
		// TODO implement
		// AbstractHttpServerImplementationTest<Object>.getServerResponseHeaderValues
		throw new UnsupportedOperationException(
				"TODO implement AbstractHttpServerImplementationTest<Object>.getServerResponseHeaderValues");
	}

	@Override
	protected String getServerNameSuffix() {
		// TODO implement
		// AbstractHttpServerImplementationTest<Object>.getServerNameSuffix
		throw new UnsupportedOperationException(
				"TODO implement AbstractHttpServerImplementationTest<Object>.getServerNameSuffix");
	}

}