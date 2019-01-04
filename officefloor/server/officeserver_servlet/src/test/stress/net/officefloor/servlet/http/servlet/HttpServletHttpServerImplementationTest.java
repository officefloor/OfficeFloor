package net.officefloor.servlet.http.servlet;

import org.eclipse.jetty.server.Server;

import net.officefloor.server.http.AbstractHttpServerImplementationTest;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.servlet.HttpServletHttpServerImplementation;

/**
 * Tests the {@link HttpServletHttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletHttpServerImplementationTest extends AbstractHttpServerImplementationTest<Server> {

	@Override
	protected Class<? extends HttpServerImplementation> getHttpServerImplementationClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Server startRawHttpServer(HttpServerLocation serverLocation) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void stopRawHttpServer(Server momento) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected HttpHeader[] getServerResponseHeaderValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getServerNameSuffix() {
		// TODO Auto-generated method stub
		return null;
	}

}