package net.officefloor.tutorial.vertxsqlclienthttpserver;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

public class VertxSqlClientLogic {

	// START SNIPPET: sqlclient
	public Future<Message> retrieveData(@HttpPathParameter("id") String id, Pool pool) {
		return pool.withConnection((connection) -> connection.preparedQuery("SELECT CONTENT FROM MESSAGE WHERE ID = $1")
				.execute(Tuple.of(Integer.parseInt(id))).map((rowSet) -> rowSet.iterator().next().getString(0))
				.map((content) -> new Message(content)));
	}
	// END SNIPPET: sqlclient

	// START SNIPPET: send
	public void send(@Parameter Message message, ObjectResponse<Message> response) {
		response.send(message);
	}
	// END SNIPPET: send
}