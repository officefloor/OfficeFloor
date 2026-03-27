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

	// START SNIPPET: tutorial
	public void service(@HttpPathParameter("key") String key, ObjectResponse<Message> response,
			@Hello Cache<String, Message> helloCache, @World Cache<String, Message> worldCache) {
		Message helloText = helloCache.get(key);
		Message worldText = worldCache.get(key);
		response.send(new Message(helloText.getText() + " " + worldText.getText()));
	}
	// END SNIPPET: tutorial

}