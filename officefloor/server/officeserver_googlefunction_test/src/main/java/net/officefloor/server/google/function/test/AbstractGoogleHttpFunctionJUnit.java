package net.officefloor.server.google.function.test;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.mock.MockHttpServer;

/**
 * Abstract Google {@link HttpFunction} JUnit functionality.
 */
public class AbstractGoogleHttpFunctionJUnit extends MockHttpServer {

	/**
	 * {@link DeployedOfficeInput} section name.
	 */
	private static final String HANDLER_SECTION_NAME = "handle";

	/**
	 * {@link HttpFunction} {@link Class}.
	 */
	private final Class<?> httpFunctionClass;

	/**
	 * {@link OfficeFloor} hosting the {@link HttpFunction}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * Instantiate.
	 * 
	 * @param httpFunctionClass {@link HttpFunction} {@link Class}.
	 */
	public AbstractGoogleHttpFunctionJUnit(Class<?> httpFunctionClass) {
		this.httpFunctionClass = httpFunctionClass;
	}

	/**
	 * Open the {@link MockHttpServer} for the {@link HttpFunction}.
	 * 
	 * @throws Exception If fails to start {@link MockHttpServer}.
	 */
	protected void openMockHttpServer() throws Exception {

		// Start the OfficeFloor
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.officeFloor((context) -> {

			// Configure server to service requests
			DeployedOfficeInput input = context.getDeployedOffice().getDeployedOfficeInput(HANDLER_SECTION_NAME,
					HttpFunctionSectionSource.INPUT_NAME);
			MockHttpServer.configureMockHttpServer(this, input);
		});
		compiler.office((context) -> {
			// Configure HTTP Function handling
			context.getOfficeArchitect().addOfficeSection(HANDLER_SECTION_NAME,
					new HttpFunctionSectionSource(this.httpFunctionClass), null);
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
	}

	/**
	 * Closes the {@link MockHttpServer}.
	 * 
	 * @throws Exception If fails to close the {@link MockHttpServer}.
	 */
	protected void close() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.close();
		}
	}

}
