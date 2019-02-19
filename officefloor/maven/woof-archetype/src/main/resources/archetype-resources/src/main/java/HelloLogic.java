package ${package};

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * Logic for hello world.
 */
public class HelloLogic {

	public static class Message {
		private String message;

		// Consider simplifying with Lombok
		private Message(String message) {
			this.message = message;
		}

		public String getMessage() {
			return this.message;
		}
	}

	public void hello(@HttpPathParameter("name") String name, ObjectResponse<Message> response) {
		response.send(new Message("Hello " + name));
	}
}
