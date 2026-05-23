package net.officefloor.tutorial.httpservlet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;

/**
 * Section {@link ClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class Increment {

	@Data
	@HttpObject
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Request {
		private String value;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Response {
		private String value;
	}

	public void increment(Request request, ObjectResponse<Response> response) {
		int value = Integer.parseInt(request.getValue());
		response.send(new Response(String.valueOf(value + 1)));
	}

}
