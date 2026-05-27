package net.officefloor.tutorial.authenticationhttpserver;

import java.io.IOException;

import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;

public class LoginRedirect {

	public void loginRedirect(ServerHttpConnection connection, HttpServerLocation serverLocation) throws IOException {
		String url = serverLocation.createClientUrl(true, "/login");
		connection.getResponse().setStatus(HttpStatus.SEE_OTHER);
		connection.getResponse().getHeaders().addHeader("location", url);
	}

}
