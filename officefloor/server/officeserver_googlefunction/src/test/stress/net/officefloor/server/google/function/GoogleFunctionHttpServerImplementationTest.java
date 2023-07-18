package net.officefloor.server.google.function;

import java.io.IOException;

import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.server.google.function.test.GoogleHttpFunctionExtension;
import net.officefloor.server.http.AbstractHttpServerImplementationTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.test.ExternalServerRunner;

/**
 * Google Function {@link AbstractHttpServerImplementationTestCase}.
 */
public class GoogleFunctionHttpServerImplementationTest extends AbstractHttpServerImplementationTestCase {

	private final @RegisterExtension GoogleHttpFunctionExtension httpFunction = new GoogleHttpFunctionExtension();

	/*
	 * ================= AbstractHttpServerImplementationTestCase =================
	 */

	@Override
	protected Class<? extends HttpServerImplementation> getHttpServerImplementationClass() {
		return GoogleFunctionHttpServerImplementation.class;
	}

	@Override
	protected AutoCloseable startHttpServer(OfficeFloorExtensionService officeFloorExtension,
			OfficeExtensionService officeExtension) throws Exception {

		// Start the server
		ExternalServerRunner.startExternalServer(officeFloorExtension, officeExtension, () -> {
			OfficeFloorHttpFunction.open();
		});

		// Provide means to stop server
		return () -> OfficeFloorHttpFunction.close();
	}

	@Override
	protected AutoCloseable startRawHttpServer(HttpServerLocation serverLocation) throws Exception {

		// Start the server
		ExternalServerRunner.startExternalServer(SimpleRequestTestHelper.getOfficeFloorExtension(),
				SimpleRequestTestHelper.getOfficeExtension(RawServicer.class), () -> {
					OfficeFloorHttpFunction.open();
				});

		// Provide means to stop server
		return () -> OfficeFloorHttpFunction.close();
	}

	public static class RawServicer {
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("hello world");
		}
	}

	@Override
	protected HttpHeader[] getServerResponseHeaderValues() {
		return new HttpHeader[] { newHttpHeader("content-type", "?"), newHttpHeader("content-length", "?") };
	}

	@Override
	protected String getServerNameSuffix() {
		return "GoogleFunction";
	}

	@Override
	protected boolean isHandleCancel() {
		return false;
	}

}
