package net.officefloor.tutorial.constantcachehttpserver;

import net.officefloor.cache.Cache;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * Constant {@link Cache} logic.
 * 
 * @author Daniel Sagenschneider
 */
public class ConstantCacheLogic {

	public void service(@HttpPathParameter("key") String key, @Hello Cache<String, Message> hello,
			@World Cache<String, Message> world, ObjectResponse<Message> response) {
		Message helloText = hello.get(key);
		Message worldText = world.get(key);
		response.send(new Message(helloText.getText() + " " + worldText.getText()));
	}

}