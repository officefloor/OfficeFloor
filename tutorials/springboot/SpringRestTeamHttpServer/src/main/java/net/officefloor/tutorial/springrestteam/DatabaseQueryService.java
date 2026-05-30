package net.officefloor.tutorial.springrestteam;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

// START SNIPPET: tutorial
public class DatabaseQueryService {

	public void query(@Parameter String socketThread, JdbcTemplate jdbcTemplate,
					  ObjectResponse<ThreadDemoResponse> response) {
		String databaseThread = Thread.currentThread().getName();
		int tableCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.tables", Integer.class);
		response.send(new ThreadDemoResponse(socketThread, databaseThread, tableCount));
	}
}
// END SNIPPET: tutorial
