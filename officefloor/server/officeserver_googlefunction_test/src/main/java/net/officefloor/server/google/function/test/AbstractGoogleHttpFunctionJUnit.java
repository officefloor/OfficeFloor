package net.officefloor.server.google.function.test;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.frame.api.manage.OfficeFloor;
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
		extends AbstractSetupGoogleHttpFunctionJUnit {

	/**
	 * Port to run HTTP server.
	 */
	private int port = HttpServerLocationImpl.DEFAULT_HTTP_PORT;

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
		this.setupHttpFunction((deployer, context) -> {

			// Configure the HTTP managed object source
			OfficeFloorManagedObjectSource httpMos = deployer.addManagedObjectSource("HTTP",
					HttpServerSocketManagedObjectSource.class.getName());
			httpMos.addProperty(HttpServerLocation.PROPERTY_CLUSTER_HTTP_PORT, String.valueOf(this.port));

			// Configure input
			OfficeFloorInputManagedObject inputHttp = deployer.addInputManagedObject("HTTP",
					ServerHttpConnection.class.getName());
			deployer.link(httpMos, inputHttp);

			// Configure office
			DeployedOffice office = deployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME);
			deployer.link(httpMos.getManagingOffice(), office);

			// Configure handling request
			deployer.link(
					httpMos.getOfficeFloorManagedObjectFlow(
							HttpServerSocketManagedObjectSource.HANDLE_REQUEST_FLOW_NAME),
					office.getDeployedOfficeInput(HttpFunctionSectionSource.SECTION_NAME,
							HttpFunctionSectionSource.INPUT_NAME));
		});
	}

}
