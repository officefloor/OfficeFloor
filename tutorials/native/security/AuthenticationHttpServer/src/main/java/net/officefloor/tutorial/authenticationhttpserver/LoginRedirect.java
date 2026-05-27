package net.officefloor.tutorial.authenticationhttpserver;

import java.io.IOException;

import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;

public class LoginRedirect {

	public void loginRedirect(ServerHttpConnection connection) throws IOException {
		connection.getResponse().setStatus(HttpStatus.SEE_OTHER);
		connection.getResponse().getHeaders().addHeader("location", "/login");
	}

}
