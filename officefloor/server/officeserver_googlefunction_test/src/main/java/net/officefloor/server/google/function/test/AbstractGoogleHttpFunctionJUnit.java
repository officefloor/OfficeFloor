package net.officefloor.server.google.function.test;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.google.function.wrap.HttpFunctionSectionSource;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpServerSocketManagedObjectSource;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.HttpServerLocationImpl;

/**
 * Abstract Google {@link HttpFunction} JUnit functionality.
 */
public class AbstractGoogleHttpFunctionJUnit<J extends AbstractGoogleHttpFunctionJUnit<J>> {

	/**
	 * {@link DeployedOfficeInput} section name.
	 */
	private static final String HANDLER_SECTION_NAME = "handle";

	/**
	 * {@link HttpFunction} {@link Class}.
	 */
	private final Class<?> httpFunctionClass;

	/**
	 * Port to run HTTP server.
	 */
	private int port = HttpServerLocationImpl.DEFAULT_HTTP_PORT;

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
	 * Specifies the port.
	 * 
	 * @param port Port.
	 * @return <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public J port(int port) {
		this.port = port;
		return (J) this;
	}

	/**
	 * Starts the HTTP server for the {@link HttpFunction}.
	 * 
	 * @throws Exception If fails to start the HTTP server.
	 */
	protected void openHttpServer() throws Exception {

		// Start the OfficeFloor
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.officeFloor((context) -> {

			// Obtain the OfficeFloor deployer
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();

			// Configure the HTTP managed object source
			OfficeFloorManagedObjectSource httpMos = deployer.addManagedObjectSource("HTTP",
					HttpServerSocketManagedObjectSource.class.getName());
			httpMos.addProperty(HttpServerLocation.PROPERTY_CLUSTER_HTTP_PORT, String.valueOf(this.port));

			// Configure input
			OfficeFloorInputManagedObject inputHttp = deployer.addInputManagedObject("HTTP",
					ServerHttpConnection.class.getName());
			deployer.link(httpMos, inputHttp);

			// Configure office
			DeployedOffice office = context.getDeployedOffice();
			deployer.link(httpMos.getManagingOffice(), office);

			// Configure handling request
			deployer.link(
					httpMos.getOfficeFloorManagedObjectFlow(
							HttpServerSocketManagedObjectSource.HANDLE_REQUEST_FLOW_NAME),
					office.getDeployedOfficeInput(HANDLER_SECTION_NAME, HttpFunctionSectionSource.INPUT_NAME));
		});
		compiler.office((context) -> {
			// Configure HTTP Function handling
			context.getOfficeArchitect().addOfficeSection(HANDLER_SECTION_NAME,
					new HttpFunctionSectionSource(this.httpFunctionClass), null);
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
	}

	/**
	 * Stops the HTTP server for the {@link HttpFunction}.
	 * 
	 * @throws Exception If fails to close the HTTP server.
	 */
	protected void close() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.close();
		}
	}
}
