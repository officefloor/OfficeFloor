package net.officefloor.server.google.function.test;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.server.google.function.wrap.AbstractSetupGoogleHttpFunctionJUnit;
import net.officefloor.server.google.function.wrap.HttpFunctionSectionSource;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpServerSocketManagedObjectSource;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.HttpServerLocationImpl;

/**
 * Abstract Google {@link HttpFunction} JUnit functionality.
 */
public class AbstractGoogleHttpFunctionJUnit<J extends AbstractGoogleHttpFunctionJUnit<J>>
		extends AbstractSetupGoogleHttpFunctionJUnit<J> {

	/**
	 * Obtains the {@link OfficeFloorExtensionService} to provide
	 * {@link HttpServerSocketManagedObjectSource} to the {@link HttpFunction}.
	 * 
	 * @param httpPort  HTTP port.
	 * @param httpsPort HTTPS port.
	 * @return {@link OfficeFloorExtensionService}.
	 */
	public static OfficeFloorExtensionService getHttpServerSocketOfficeFloorExtensionService(int httpPort,
			int httpsPort) {
		return (deployer, context) -> {

			// Obtain office
			DeployedOffice office = deployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME);
			DeployedOfficeInput serviceInput = office.getDeployedOfficeInput(HttpFunctionSectionSource.SECTION_NAME,
					HttpFunctionSectionSource.INPUT_NAME);

			// Create the input
			OfficeFloorInputManagedObject input = deployer.addInputManagedObject("GOOGLE_FUNCTION_INPUT",
					ServerHttpConnection.class.getName());
			input.addTypeQualification(HttpFunctionSectionSource.CONNECTION_TYPE_QUALIFIER,
					ServerHttpConnection.class.getName());

			// Load both secure and non-secure handling
			for (boolean isSecure : new boolean[] { false, true }) {

				// Configure the HTTP managed object source
				String mosName = "GOOGLE_FUNCTION_" + (isSecure ? "HTTP" : "HTTPS");
				OfficeFloorManagedObjectSource mos = deployer.addManagedObjectSource(mosName,
						HttpServerSocketManagedObjectSource.class.getName());

				// Configure input
				deployer.link(mos, input);

				// Configure handling request
				deployer.link(mos.getManagingOffice(), office);
				deployer.link(mos.getOfficeFloorManagedObjectFlow(
						HttpServerSocketManagedObjectSource.HANDLE_REQUEST_FLOW_NAME), serviceInput);

				// Add secure specific details
				if (isSecure) {

					// Configure secure
					mos.addProperty(HttpServerLocation.PROPERTY_HTTPS_PORT, String.valueOf(httpsPort));
					mos.addProperty(HttpServerSocketManagedObjectSource.PROPERTY_SECURE, "true");
					deployer.link(
							mos.getOfficeFloorManagedObjectTeam(HttpServerSocketManagedObjectSource.SSL_TEAM_NAME),
							deployer.addTeam("GOOGLE_FUNCTION_HTTPS_TEAM", ExecutorCachedTeamSource.class.getName()));

				} else {
					// Configure insecure
					mos.addProperty(HttpServerLocation.PROPERTY_HTTP_PORT, String.valueOf(httpPort));
					input.setBoundOfficeFloorManagedObjectSource(mos);
				}
			}
		};
	}

	/**
	 * Port for HTTP socket.
	 */
	private int httpPort = HttpServerLocationImpl.DEFAULT_HTTP_PORT;

	/**
	 * Port for HTTPS socket.
	 */
	private int httpsPort = HttpServerLocationImpl.DEFAULT_HTTPS_PORT;

	/**
	 * Instantiate.
	 * 
	 * @param httpFunctionClass {@link HttpFunction} {@link Class}.
	 */
	public AbstractGoogleHttpFunctionJUnit(Class<?> httpFunctionClass) {
		super(httpFunctionClass);
	}

	/**
	 * Instantiate using default {@link OfficeFloor} {@link HttpFunction}.
	 */
	public AbstractGoogleHttpFunctionJUnit() {
	}

	/**
	 * Specifies the HTTP port.
	 * 
	 * @param port HTTP port.
	 * @return <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public J httpPort(int port) {
		this.httpPort = port;
		return (J) this;
	}

	/**
	 * Specifies the HTTPS port.
	 * 
	 * @param port HTTPS port.
	 * @return <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public J httpsPort(int port) {
		this.httpsPort = port;
		return (J) this;
	}

	/**
	 * Starts the HTTP server for the {@link HttpFunction}.
	 * 
	 * @throws Exception If fails to start the HTTP server.
	 */
	protected void openHttpServer() throws Exception {
		this.setupHttpFunction(getHttpServerSocketOfficeFloorExtensionService(this.httpPort, this.httpsPort));
	}

}
