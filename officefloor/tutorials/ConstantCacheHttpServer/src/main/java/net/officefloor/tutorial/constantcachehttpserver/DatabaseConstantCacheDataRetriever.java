package net.officefloor.tutorial.constantcachehttpserver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.cache.constant.ConstantCacheDataRetriever;
import net.officefloor.plugin.clazz.Dependency;

/**
 * Database {@link ConstantCacheDataRetriever}.
 * 
 * @author Daniel Sagenschneider
 */
public class DatabaseConstantCacheDataRetriever implements ConstantCacheDataRetriever<String, Message> {

	private @Dependency DataSource dataSource;

	@Override
	public Map<String, Message> getData() throws Exception {
		try (Connection connection = this.dataSource.getConnection()) {
			ResultSet result = connection.prepareStatement("SELECT KEY, MESSAGE FROM REFERENCE_DATA").executeQuery();
			Map<String, Message> data = new HashMap<>();
			while (result.next()) {
				data.put(result.getString("KEY"), new Message(result.getString("MESSAGE")));
			}
			return data;
		}
	}

}
