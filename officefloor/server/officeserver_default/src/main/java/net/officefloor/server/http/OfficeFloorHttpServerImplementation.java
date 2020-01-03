package net.officefloor.server.http;

import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;

/**
 * {@link OfficeFloor} {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorHttpServerImplementation implements HttpServerImplementation {

	@Override
	public void configureHttpServer(HttpServerImplementationContext context) throws Exception {

		// Obtain the deployer
		OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();

		// Configure the input
		OfficeFloorInputManagedObject input = deployer.addInputManagedObject("HTTP",
				ServerHttpConnection.class.getName());

		// Obtain the handling
		DeployedOfficeInput serviceInput = context.getInternalServiceInput();
		DeployedOffice office = serviceInput.getDeployedOffice();

		// Obtain the HTTP server location
		HttpServerLocation serverLocation = context.getHttpServerLocation();

		// Obtain Server HTTP header
		HttpHeaderValue serverHttpHeaderValue = HttpServer.getServerHttpHeaderValue(context, null);

		// Obtain the Date HTTP header
		DateHttpHeaderClock dateHttpHeaderClock = context.getDateHttpHeaderClock();

		// Obtain whether to include the escalation stack trace
		boolean isIncludeEscalationStackTrace = context.isIncludeEscalationStackTrace();

		// Configure the non-secure HTTP
		OfficeFloorManagedObjectSource http = deployer.addManagedObjectSource("HTTP",
				new HttpServerSocketManagedObjectSource(serverLocation, serverHttpHeaderValue, dateHttpHeaderClock,
						isIncludeEscalationStackTrace));
		deployer.link(http.getManagingOffice(), office);
		deployer.link(
				http.getOfficeFloorManagedObjectFlow(HttpServerSocketManagedObjectSource.HANDLE_REQUEST_FLOW_NAME),
				serviceInput);
		deployer.link(http, input);

		// Configure the secure HTTP
		int httpsPort = serverLocation.getClusterHttpsPort();
		if (httpsPort > 0) {
			OfficeFloorManagedObjectSource https = deployer.addManagedObjectSource("HTTPS",
					new HttpServerSocketManagedObjectSource(serverLocation, serverHttpHeaderValue, dateHttpHeaderClock,
							isIncludeEscalationStackTrace, context.getSslContext()));
			deployer.link(https.getManagingOffice(), office);
			deployer.link(
					https.getOfficeFloorManagedObjectFlow(HttpServerSocketManagedObjectSource.HANDLE_REQUEST_FLOW_NAME),
					serviceInput);
			deployer.link(https, input);

			// Specify default bound name
			input.setBoundOfficeFloorManagedObjectSource(http);

			// Specify the SSL team
			OfficeFloorTeam sslTeam = deployer.addTeam(HttpServerSocketManagedObjectSource.SSL_TEAM_NAME,
					ExecutorCachedTeamSource.class.getName());
			deployer.link(https.getOfficeFloorManagedObjectTeam(HttpServerSocketManagedObjectSource.SSL_TEAM_NAME),
					sslTeam);
		}
	}

}