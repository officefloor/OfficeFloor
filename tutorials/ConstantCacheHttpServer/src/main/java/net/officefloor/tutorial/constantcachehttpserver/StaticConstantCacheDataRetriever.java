package net.officefloor.tutorial.constantcachehttpserver;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.cache.constant.ConstantCacheDataRetriever;

/**
 * Static {@link ConstantCacheDataRetriever}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class StaticConstantCacheDataRetriever implements ConstantCacheDataRetriever<String, Message> {

	@Override
	public Map<String, Message> getData() throws Exception {
		Map<String, Message> data = new HashMap<>();
		data.put("1", new Message("Hello"));
		data.put("2", new Message("Hi"));
		return data;
	}
}
// END SNIPPET: tutorial
