package ${package};

import lombok.Value;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

public class HelloLogic {

	public void hello(
			@HttpPathParameter("name") String name, 
			ObjectResponse<Message> response) {
		response.send(new Message("Hello " + name));
	}

	@Value
	public static class Message {
		private String message;
	}
}
