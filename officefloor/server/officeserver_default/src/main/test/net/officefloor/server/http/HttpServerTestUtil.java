/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.server.http;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.test.officefloor.CompileOfficeFloorContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.conversation.HttpEntity;
import net.officefloor.server.http.conversation.impl.HttpEntityImpl;
import net.officefloor.server.http.conversation.impl.HttpRequestImpl;
import net.officefloor.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.server.http.source.HttpServerSocketManagedObjectSource;
import net.officefloor.server.http.source.HttpsServerSocketManagedObjectSource;
import net.officefloor.server.impl.AbstractServerSocketManagedObjectSource;
import net.officefloor.server.ssl.OfficeFloorDefaultSslContextSource;
import net.officefloor.server.stream.impl.ServerInputStreamImpl;

/**
 * Utility class aiding in testing HTTP functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServerTestUtil {

	/**
	 * Starting port number.
	 */
	private static int portStart = 12643;

	/**
	 * Obtains the next port number for testing.
	 * 
	 * @return Next port number for testing.
	 */
	public static int getAvailablePort() {
		int port = portStart;
		portStart++; // increment port for next test
		return port;
	}

	/**
	 * Convenience method to configure {@link CompileOfficeFloorContext} with a
	 * test HTTP server.
	 * 
	 * @param context
	 *            {@link CompileOfficeFloorContext}.
	 * @param httpPort
	 *            Port to listen for HTTP requests.
	 * @param httpsPort
	 *            Port to listen for HTTPS requests.
	 * @param sectionName
	 *            Name of the {@link OfficeSection} servicing the requests.
	 * @param sectionInputName
	 *            Name of the {@link OfficeSectionInput} on the
	 *            {@link OfficeSection} servicing the requests.
	 * @return {@link OfficeFloorInputManagedObject}.
	 */
	public static OfficeFloorInputManagedObject configureTestHttpServer(CompileOfficeFloorContext context, int port,
			String sectionName, String sectionInputName) {
		return HttpServerSocketManagedObjectSource.configure(context.getOfficeFloorDeployer(), port,
				context.getDeployedOffice(), sectionName, sectionInputName);
	}

	/**
	 * Convenience method to configure {@link CompileOfficeFloorContext} with a
	 * test HTTP server.
	 * 
	 * @param context
	 *            {@link CompileOfficeFloorContext}.
	 * @param httpPort
	 *            Port to listen for HTTP requests.
	 * @param httpsPort
	 *            Port to listen for HTTPS requests.
	 * @param sectionName
	 *            Name of the {@link OfficeSection} servicing the requests.
	 * @param sectionInputName
	 *            Name of the {@link OfficeSectionInput} on the
	 *            {@link OfficeSection} servicing the requests.
	 * @return {@link OfficeFloorInputManagedObject}.
	 */
	public static OfficeFloorInputManagedObject configureTestHttpServer(CompileOfficeFloorContext context, int port,
			int httpsPort, String sectionName, String sectionInputName) {
		return HttpsServerSocketManagedObjectSource.configure(context.getOfficeFloorDeployer(), port, httpsPort,
				createTestServerSslContext(),
				context.getDeployedOffice().getDeployedOfficeInput(sectionName, sectionInputName));
	}


	/**
	 * Creates the {@link SSLContext}.
	 * 
	 * @return {@link SSLContext}.
	 */
	public static SSLContext createTestServerSslContext() {
		try {
			return OfficeFloorDefaultSslContextSource.createServerSslContext(null);
		} catch (Exception ex) {
			// Should always create, otherwise fail test
			throw OfficeFrameTestCase.fail(ex);
		}
	}

	/**
	 * Creates a {@link net.officefloor.server.http.HttpRequest} for testing.
	 * 
	 * @param method
	 *            HTTP method (GET, POST).
	 * @param requestUri
	 *            Request URI.
	 * @param entity
	 *            Contents of the
	 *            {@link net.officefloor.server.http.HttpRequest} entity.
	 * @param headerNameValues
	 *            {@link HttpHeader} name values.
	 * @return {@link net.officefloor.server.http.HttpRequest}.
	 * @throws Exception
	 *             If fails to create the
	 *             {@link net.officefloor.server.http.HttpRequest} .
	 */
	public static net.officefloor.server.http.HttpRequest createHttpRequest(String method, String requestUri,
			String entity, String... headerNameValues) throws Exception {

		// Obtain the entity data
		final Charset charset = AbstractServerSocketManagedObjectSource.getCharset(null);
		byte[] entityData = (entity == null ? new byte[0] : entity.getBytes(charset));

		// Create the headers
		List<HttpHeader> headers = new LinkedList<HttpHeader>();
		if (entity != null) {
			// Include content type and content length if entity
			headers.add(new HttpHeaderImpl("content-type", "text/plain; charset=" + charset.name()));
			headers.add(new HttpHeaderImpl("content-length", String.valueOf(entityData.length)));
		}
		for (int i = 0; i < headerNameValues.length; i += 2) {
			String name = headerNameValues[i];
			String value = headerNameValues[i + 1];
			headers.add(new HttpHeaderImpl(name, value));
		}

		// Create the entity input stream
		ServerInputStreamImpl inputStream = new ServerInputStreamImpl(new Object());
		inputStream.inputData(entityData, 0, (entityData.length - 1), false);
		HttpEntity httpEntity = new HttpEntityImpl(inputStream);

		// Return the HTTP request
		return new HttpRequestImpl(method, requestUri, "HTTP/1.1", headers, httpEntity);
	}

	/**
	 * All access via static methods.
	 */
	private HttpServerTestUtil() {
	}

}