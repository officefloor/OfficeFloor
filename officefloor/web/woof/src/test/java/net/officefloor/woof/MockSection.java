package net.officefloor.woof;

import java.io.IOException;
import java.io.Serializable;

import net.officefloor.frame.api.team.Team;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.HttpSessionStateful;
import net.officefloor.web.ObjectResponse;

/**
 * Mock logic for the section.
 * 
 * @author Daniel Sagenschneider
 */
public class MockSection {

	@HttpSessionStateful
	public static class State implements Serializable {
		private static final long serialVersionUID = 1L;

		private String previous = "-1";
	}

	/**
	 * Services.
	 * 
	 * @param parameter  Parameter.
	 * @param state      {@link State}.
	 * @param connection {@link ServerHttpConnection}.
	 * @param object     {@link MockObject} injected from configuration.
	 */
	public void service(@HttpQueryParameter("param") String parameter, State state, ServerHttpConnection connection,
			MockObject object) throws IOException {
		connection.getResponse().getEntityWriter()
				.write("param=" + parameter + ", previous=" + state.previous + ", object=" + object.getMessage());
		state.previous = parameter;
	}

	/**
	 * Provides testing of objects.
	 * 
	 * @param object     {@link MockObject} injected from configuration.
	 * @param httpObject {@link MockHttpObject}.
	 * @param response   Sends the {@link MockObject} as a response.
	 */
	public void objects(MockObject object, MockHttpObject httpObject, ObjectResponse<MockHttpObject> response) {
		response.send(new MockHttpObject(httpObject.getMessage() + "-" + object.getMessage()));
	}

	/**
	 * Initial service method for testing {@link Team}
	 * 
	 * @param flows {@link TeamFlows}.
	 */
	@Next("teamsDifferent")
	public String teams() {
		return Thread.currentThread().getName();
	}

	/**
	 * Ensure different {@link Team}.
	 */
	public void teamsDifferent(@Parameter String threadName, MockObject object, ObjectResponse<String> response) {
		boolean isSameThread = Thread.currentThread().getName().equals(threadName);
		response.send(isSameThread ? "SAME THREAD" : "DIFFERENT THREAD");
	}

}