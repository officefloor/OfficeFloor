package net.officefloor.server.google.function;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link HttpFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorHttpFunction implements HttpFunction {

	/*
	 * ==================== HttpFunction ========================
	 */

	@Override
	public void service(HttpRequest request, HttpResponse response) throws Exception {

		// TODO use OfficeFloor for response
		response.getWriter().append("Hello via function");
	}

}